package com.delorme.inreachapp.service;

import com.delorme.inreachapp.service.bluetooth.BluetoothConnectThread;
import com.delorme.inreachapp.service.bluetooth.BluetoothUtils;
import com.delorme.inreachapp.service.gps.InReachGPSDelegate;
import com.delorme.inreachcore.InReachManager;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * A service for communications with an inReach. It requires the following
 * permissions in the AndroidManifest.xml file.
 * 
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 * 
 * @author Eric Semle
 * @since inReachApp (07 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
public class InReachService extends Service
{
    /**
     * Called by the system when the service is first created.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onCreate()
    {
        super.onCreate();

        startManager();
    }
  
    /**
     * Return the communication channel to the service.
     * 
     * @param[in] intent The Intent that was used to bind to this service.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public IBinder onBind(Intent intent) 
    {
        return m_binder;
    }
    
    /**
     * Called by the system every time a client explicitly starts the 
     * service by calling startService(Intent), providing the arguments it 
     * supplied and a unique integer token representing the start request. Do 
     * not call this method directly. 
     * 
     * @param[in] intent The Intent supplied to startService(Intent).
     * @param[in] flags Additional data about this start request.
     * @param[in] startId A unique integer representing this specific request to start. 
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        
        startManager();
                
        return START_STICKY;
    }
    
    /**
     *  Called by the system to notify that the device is low on 
     * memory.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
    
        if (m_manager != null
            && !m_manager.isSendingMessage() 
            && !m_manager.isTracking()
            && !m_manager.isInEmergency())
        {
            stopManager();
        }
    }
    
    /**
     *  Called by the system to notify a Service that it is no longer 
     * used and is being removed.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        
        // cleanup the manager
        stopManager();
    }
    
    /**
     * Registers a messenger to receive messages. The message types
     * for received messages are in InReachEvents.
     * 
     * @param messenger A reference to a message handler for receiving
     * InReach Events
     * @throw IllegalArgumentException if the messenger is null.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public synchronized void registerMessenger(Messenger messenger) throws
        IllegalArgumentException
    {
        if (messenger == null)
            throw new IllegalArgumentException("The messenger cannot be null.");
       
        try
        {
            final Message msg = Message.obtain(null,
                InReachEventHandler.MSG_REGISTER_MESSENGER);
            msg.replyTo = messenger;
        
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to register message", ex);
        }
    }
    
    /**
     * Unregisters a messenger from receiving messages.
     * 
     * @param messenger A reference to a message handler that is currently
     * receiving messages.
     * @throw IllegalArgumentException if the messenger is null.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public synchronized void unregisterMessenger(Messenger messenger)
    {
        if (messenger == null)
            throw new IllegalArgumentException("The messenger cannot be null.");
        
        try
        {
            final Message msg = Message.obtain(null,
                InReachEventHandler.MSG_UNREGISTER_MESSENGER);
            msg.replyTo = messenger;
        
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to unregister messenger", ex);
        }
    }
    
    /**
     * Returns true if the service is currently trying to make
     * a connection to an inReach. Otherwise this will return false.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public synchronized boolean isConnecting()
    {
        return (m_btConnectThread != null);
    }
    
    /**
     * Returns true if service is currently connected to an inReach. Otherwise
     * this will return false.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public synchronized boolean isConnected()
    {
        return (m_manager != null && m_manager.isConnected());
    }
    
    /**
     * Returns a reference to the InReachManager. This can return null
     * if the manager hasn't been started.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public synchronized InReachManager getManager()
    {
        return m_manager;
    }
    
    /**
     * Initializes dependencies and then starts the InReachManager.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    private synchronized void startManager()
    {
        if (m_manager != null)
        {
            return;
        }
        
        // We'll start by configuring the GPS Delegate.
        // The delegate is for backwards compatibility with
        // inReach 1.0.
        m_gpsDelegate = new InReachGPSDelegate(this);
        
        final InReachEventObserver observer = new InReachEventObserver(m_messenger);
        
        m_manager = InReachManager.getInstance();
        m_manager.startManager(observer, m_gpsDelegate, true, true);
        
        m_btEventHandler = new BluetoothEventHandler(this);
        

        connect();
    }
    
    /**
     * Stops the manager and all dependencies
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    private synchronized void stopManager()
    {
        if (m_manager == null)
        {
            return;
        }
        
        if (m_btEventHandler != null)
        {
            m_btEventHandler.close();
            m_btEventHandler = null;
        }
        
        disconnect();
        
        if (m_gpsDelegate != null)
        {
            m_gpsDelegate.close();
            m_gpsDelegate = null;
        }
        
        m_manager.stopManager();
        m_manager = null;
    }
    
    /**
     * Start connecting attempt to an inReach. It will not start if
     * the BluetoothAdapter is off or there are no bonded inReach
     * devices.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    synchronized void connect() throws IllegalStateException
    {
        // disconnect before attempting to make a connection
        disconnect();
        
        if (m_manager == null)
            throw new IllegalStateException("The manager must be started be"
                + "before the connect should be invoked.");
        
        // Only attempt to connect when the Bluetooth Adapter is on
        // and there is a bonded inReach.
        if (BluetoothUtils.isBluetoothAdapterOn() &&
            BluetoothUtils.findBondedInReachDevice() != null)
        {
            m_btConnectThread = new BluetoothConnectThread(m_messenger);
            m_btConnectThread.start();
        }
    }
    
    
    /**
     * Disconnects the InReachManager and stops any connecting attempts.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    synchronized void disconnect()
    {
        if (m_btConnectThread != null)
        {
            try
            {
                m_btConnectThread.stopConnecting();
                m_btConnectThread.join();
            }
            catch (InterruptedException ex)
            {
                Log.d(DEBUG_TAG, "Error closing Bluetooth Connect Thread", ex);
            }
            m_btConnectThread = null;
        }

        if (m_manager != null)
        {
            m_manager.closeBluetoothSocket();
        }
    }
    
    /**
     * Passes a connected bluetooth socket to the InReachManager.
     * 
     * @param socket A connected BluetoothSocket
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    synchronized void setBluetoothSocket(BluetoothSocket socket)
    {
        if (m_manager == null)
            return;
    
        m_manager.setBluetoothSocket(socket);
    }
    
    
    /**
     *  Class used by Activities to bind to the service.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public class InReachBinder extends Binder
    {        
        //! Returns the service associated with this binder.
        public InReachService getService() 
        { 
            return InReachService.this; 
        }
    }

    //! Service Binder. 
    private final IBinder m_binder = new InReachBinder();

    //! Reference to the InReachManager singleton
    private InReachManager m_manager = null;
    
    //! Implementation of the GPS delegate
    private InReachGPSDelegate m_gpsDelegate = null;
    
    //! Helper thread for connecting to an inReach
    private BluetoothConnectThread m_btConnectThread = null;
    
    //! A Bluetooth event handler
    private BluetoothEventHandler m_btEventHandler = null;
    
    //! A messenger for InReach Observer events
    private final Messenger m_messenger = new Messenger(new InReachEventHandler());
    
    //! The tag for debug logcat output
    private static final String DEBUG_TAG = "InReachService";  
}
