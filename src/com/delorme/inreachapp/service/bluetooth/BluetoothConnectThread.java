package com.delorme.inreachapp.service.bluetooth;

import com.delorme.inreachapp.service.InReachEvents;

import android.bluetooth.BluetoothSocket;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * A thread for connecting to an inReach. It requires the following
 * permissions.
 * 
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 *  
 * @author Eric Semle
 * @since inReachApp (07 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
public class BluetoothConnectThread extends Thread
{
    /**
     * Constructor
     * 
     * @param messenger A message handler that is used to pass the connected
     * socket. This should not be null.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public BluetoothConnectThread(Messenger messenger) throws IllegalArgumentException
    {
        setName(DEBUG_TAG);
        
        if (messenger == null)
            throw new IllegalArgumentException("The messenger cannot be null.");
        
        m_messenger = messenger;
    }
    
    /**
     * Call this to begin stopping the connect thread. It is
     * recommended that a join is used after this method is
     * invoked.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void stopConnecting()
    {
        // kill the loop
        m_runThread = false;
        
        // interrupt the thread if it is sleeping
        interrupt();
    }
    
    /**
     * The entry point for the Bluetooth Connect Thread
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void run()
    {
        // Prepare looper is required to get the Bluetooth Adapter from a thread.
        Looper.prepare();
        
        signalBluetoothConnecting();
        
        // set defaults
        m_sleepPeriod = THREAD_SLEEP_SHORT;
        m_runThread = true;
        
        // save the time that the connect first started.
        final long startTime = System.currentTimeMillis();
        
        do
        {
            // This is a very simple example of connecting to an inReach. The
            // one issue with this example is that invoking socket.connect()
            // blocks the thread for an undetermined amount of time. Stopping
            // the thread could cause the UI to lock until the socket returns.
            // A better example would be to keep the socket as a member variable
            // and to close the socket when the thread is stopped.
            final BluetoothSocket socket = BluetoothUtils.connectToBondedInReach();
        
            // The socket returned, but the thread is waiting to stop.
            if (!m_runThread)
            {
                signalBluetoothCancelled();
                
                // always close the socket. not closing the socket could
                // cause weird behavior in Android!
                if (socket != null)
                {
                    try
                    {
                        socket.close();
                    }
                    catch (Exception ex)
                    { 
                        // fail silently 
                    }
                }
                return;
            }
            
            if (socket != null)
            {
                // send a message to let the service know a connection
                // has been made with an inReach
                signalBluetoothConnect(socket);

                return;
            }
            
            // If connect has failed for longer then X milliseconds, the
            // sleep period between connects is incremented from the short
            // to the long period. This is to help the prevent a Android
            // Bluetooth memory leak that causes the connection to fail until
            // reboot or crash device. This usually occurs on the 513 failed connect.
            // Google: "out of wsock blocks" or look for Issue 8676
            // http://code.google.com/p/android/issues/detail?id=8676
            if (m_sleepPeriod != THREAD_SLEEP_LONG &&
                System.currentTimeMillis() - startTime > EXTEND_SLEEP_TIMEOUT)
            {
                m_sleepPeriod = THREAD_SLEEP_LONG;
            }
            
            try
            {
                // This is to pause between connection attempts. Never "hammer"
                // on connects. It will cause issues.
                Thread.sleep(m_sleepPeriod);
            }
            catch (InterruptedException ex)
            {
                // the thread was interrupted.
                signalBluetoothCancelled();
                
                return;
            }
        } while (m_runThread);
        
        // indication that the connection process was cancelled
        signalBluetoothCancelled();
    }
    
    /**
     * Sends a message that signals the connection process has begun
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    private void signalBluetoothConnecting()
    {
        try
        {
            final Message msg = Message.obtain(null,
                InReachEvents.EVENT_BLUETOOTH_CONNECTING);
            
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.e(DEBUG_TAG, "Failed to send Bluetooth Connecting Message", ex);
        }
    }
    
    /**
     * Sends a message that signals the connection process was cancelled
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    private void signalBluetoothCancelled()
    {
        try
        {
            final Message msg = Message.obtain(null,
                InReachEvents.EVENT_BLUETOOTH_CANCELLED);
            
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.e(DEBUG_TAG, "Failed to send Bluetooth Cancelled Message", ex);
        }
    }
    
    /**
     * Sends a message that signals that a connection has been made.
     * 
     * @param socket the connected BluetoothSocket
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    private void signalBluetoothConnect(BluetoothSocket socket)
    {
        try
        {
            final Message msg = Message.obtain(null,
                InReachEvents.EVENT_BLUETOOTH_CONNECT);
            msg.obj = socket;
            
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.e(DEBUG_TAG, "Failed to send Bluetooth Connect Message", ex);
            
            // Never leave the socket open. This could cause bluetooth
            // instability. close the socket and exit the thread.
            try
            {
                socket.close();
            }
            catch (Exception bex)
            { 
                // fail silently 
            }
        }
    }
    
    //! Indicates that the thread should continue to loop
    private volatile boolean m_runThread = false;
    
    //! A messenger for passing the connected socket
    private Messenger m_messenger = null;
    
    //! The sleep period between conneciton attempts.
    private long m_sleepPeriod = THREAD_SLEEP_SHORT;
    
    //! The short or start sleep period
    private static final long THREAD_SLEEP_SHORT = 5000;
    
    //! The longer sleep period when connect attempts fail
    private static final long THREAD_SLEEP_LONG = 15000;
    
    //! The time in milliseconds that the sleep should be extended
    private static final long EXTEND_SLEEP_TIMEOUT = 30000;
    
    //! A debug tag for logcat
    private static final String DEBUG_TAG = "BluetoothConnectThread";
}
