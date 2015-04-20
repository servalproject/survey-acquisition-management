package com.delorme.inreachapp.service;

import com.delorme.inreachapp.service.bluetooth.BluetoothUtils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

/**
 * A handler for Bluetooth events. This class monitors
 * BluetoothAdapter state as well as the inReachManager
 * messenger.
 * 
 * @author Eric Semle
 * @since inReach SDK (09 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
class BluetoothEventHandler extends BroadcastReceiver
{
    /**
     * Constructor for the Bluetooth Event Handler. This registers all
     * of the listeners.
     * 
     * @param service a reference to the InReachService
     * 
     * @author Eric Semle
     * @since inReach SDK (09 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public BluetoothEventHandler(InReachService service)
    {
        if (service == null)
            throw new IllegalArgumentException("InReachService should not be null");
        
        m_service = service;

        // register to receive messages from the inReachManager
        m_btMessenger = new Messenger(new BluetoothHandler());
        m_service.registerMessenger(m_btMessenger);
        
        // register to receive broadcast about the adapter state and the
        // device bonded state.
        m_service.registerReceiver(this, 
            new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        m_service.registerReceiver(this, 
            new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }
    
    /**
     * Unregisters all of the listeners.
     * 
     * @author Eric Semle
     * @since inReach SDK (09 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public synchronized void close()
    {
        if (m_service == null)
            return;
        
        m_service.unregisterMessenger(m_btMessenger);
        m_btMessenger = null;
        
        m_service.unregisterReceiver(this);
        m_service = null;
    }
    
    /**
     * Called when an Intent broadcast is received.
     * 
     * @param[in] context The Context in which the receiver is running.
     * @param[in] intent The Intent being received. 
     * 
     * @author Eric Semle
     * @since inReach SDK (09 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onReceive(Context context, Intent intent) 
    {
        final String action = intent.getAction();
        
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
        {   
            onAdapterStateChanged(intent);
        }
        else
        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
        {
            onDeviceBondStateChanged(intent);
        }
    }
    
    /**
     * Called whenever the state of the Bluetooth Adapter has changed.
     * 
     * @param[in] intent The intent being received.
     * 
     * @author Eric Semle
     * @since inReach SDK (09 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public synchronized void onAdapterStateChanged(Intent intent)
    {
        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        
        if (m_service == null)
            return;
        
        if (state == BluetoothAdapter.STATE_ON)
        {
            // Bluetooth was turned on, lets try to connect!
            m_service.connect();
        }
        else
        if (state == BluetoothAdapter.STATE_TURNING_OFF)
        {
            // Bluetooth was turned off. Close all connections
            m_service.disconnect();
        }
    }
    
    /**
     * Called whenever the bond state of a device changes
     * 
     * @param[in] intent The intent being received.
     * 
     * @author Eric Semle
     * @since inReach SDK (09 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public synchronized void onDeviceBondStateChanged(Intent intent)
    {   
        final BluetoothDevice device = intent.getParcelableExtra(
            BluetoothDevice.EXTRA_DEVICE);
        
        if (m_service == null || !BluetoothUtils.isInReachDevice(device))
        {
            // not our concern
            return;
        }
        
        final int curState = intent.getIntExtra(
            BluetoothDevice.EXTRA_BOND_STATE, -1);
        final int prevState = intent.getIntExtra(
            BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
        
        // did the state change from bonding to bonded?
        if (prevState == BluetoothDevice.BOND_BONDING &&
            curState == BluetoothDevice.BOND_BONDED)
        {
            if (!m_service.isConnecting() || !m_service.isConnected())
            {
                // A new inReach connected. Try to connect to it.
                m_service.connect();
            }
        } 
    }
    
    /**
     * Handler for Bluetooth Messages
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    class BluetoothHandler extends Handler
    {
        @Override
        public synchronized void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case InReachEvents.EVENT_CONNECTION_STATUS_UPDATE:
                    if (msg.arg1 == InReachEvents.VALUE_FALSE)
                    {
                        if (m_service != null)
                        {
                            Toast.makeText(m_service,
                                "Disconnected from inReach",
                                Toast.LENGTH_LONG).show();
                            
                            // the connection was closed.
                            // lets try to reconnect.
                            m_service.connect();
                        }
                    }
                    break;
                case InReachEvents.EVENT_BLUETOOTH_CONNECT:
                    if (m_service != null)
                    {
                        Toast.makeText(m_service,
                            "Connected to inReach",
                            Toast.LENGTH_LONG).show();
                        
                        m_service.setBluetoothSocket((BluetoothSocket)msg.obj);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    
    //! A reference to the InReach Service.
    private InReachService m_service = null;
    
    //! A handler for Bluetooth connect and disconnect notifications
    private Messenger m_btMessenger = null;
}