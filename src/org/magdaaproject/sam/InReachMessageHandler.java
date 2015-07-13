package org.magdaaproject.sam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.servalproject.succinctdata.SuccinctDataQueueDbAdapter;
import org.servalproject.succinctdata.SuccinctDataQueueService;

import com.delorme.inreachapp.service.InReachEvents;
import com.delorme.inreachapp.service.InReachService;
import com.delorme.inreachapp.service.bluetooth.BluetoothUtils;
import com.delorme.inreachcore.BatteryStatus;
import com.delorme.inreachcore.InReachDeviceSpecs;
import com.delorme.inreachcore.InboundMessage;
import com.delorme.inreachcore.MessageTypes;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ContentValues;
import 	android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;


/**
 * A class to handle the inReach messages coming from the inReach service
 * 
 * @author Guillaume
 *
 */

public class InReachMessageHandler extends Handler implements ServiceConnection {


	private final Context context;
	private static final BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
	private static int bluetoothstate = 0;
	private final SuccinctDataQueueDbAdapter db;

    
    private InReachMessageHandler(Context c) {
    	this.context = c;
		db = new SuccinctDataQueueDbAdapter(c);
		db.open();
	}
	/**
     * Returns a single instance of the LogEventHandler
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public static InReachMessageHandler getInstance()
    {
        return ms_logInstance;
    }
    
    /**
     * create a single instance of the LogEventHandler
     * @param c the context
     * @return
     */
    public static InReachMessageHandler createInstance(Context c)
    {
        if (ms_logInstance == null)
        {
            ms_logInstance = new InReachMessageHandler(c);
            
            if (BA == null) {
				
			}
			else if  (!BA.isEnabled()) {
				BA.enable();
			}
        }
        return ms_logInstance;
    	
    }
    
    /**
     * Sets the listener for callbacks about new events
     * 
     * @param listener an implementation of the Listener class
     * that receives callbacks about new events. The callbacks
     * are invoked on the main thread. Pass null to stop receives
     * events
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public synchronized void setListener(Listener listener)
    {
		if (m_lastevent!=null && listener !=null)
			listener.onNewEvent(m_lastevent);
        m_listener = listener;
    }
    
    /**
     * Handler for messages.
     * 
     * @param msg An event that was received from the InReachEventHandler.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public synchronized void handleMessage(Message msg)
    {
        switch (msg.what)
        {
            case InReachEvents.EVENT_CONNECTION_STATUS_UPDATE:
            {
            	
            	// restarts all the variables to their default value if the inReach is disconnected
            	if (msg.arg1 == InReachEvents.VALUE_FALSE) {
            		m_queuesynced = false;
            		m_queued_count = 0;
            		m_connecting = false;
            	}else if (msg.arg1 == InReachEvents.VALUE_TRUE){
            		m_connecting = true;
            	}
            	
                final String text = (msg.arg1 == InReachEvents.VALUE_TRUE ?
                    "inReachManager connected to device." :
                    "inReachManager disconnected from device.");
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_MESSAGE_CREATED:
            {
                final String type = getMessageType(msg.arg1);
                final String text = String.format("%s created with id: %d",
                    type, msg.arg2);
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_MESSAGE_QUEUED:
            {
                final String type = getMessageType(msg.arg1);
                final String text = String.format("%s queued with id: %d",
                    type, msg.arg2);
                
                 m_queued_count++;
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_MESSAGE_SEND_SUCCEEDED:
            {
                final String type = getMessageType(msg.arg1);
                final String text = String.format(
                    "%s successfully sent with id: %d",
                    type, msg.arg2);
                addEvent(text);
                m_queued_count--;                

				// update database
				ContentValues values = new ContentValues();
				values.put(SuccinctDataQueueDbAdapter.COL_STATUS, SuccinctDataQueueDbAdapter.STATUS_INREACH_SENT);
				db.update(SuccinctDataQueueDbAdapter.COL_INREACH_ID + " = ? ",
						new String[]{Integer.toString(msg.arg2)},
						values);
				// TODO schedule service again!

				break;
            }
            case InReachEvents.EVENT_MESSAGE_SEND_FAILED:
            {
                final String type = getMessageType(msg.arg1);
                final String text = String.format(
                    "%s failed to send with id: %d",
                    type, msg.arg2);
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_MESSAGING_SUSPENDED:
            {
                final String text = "Messaging suspended";
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_MESSAGE_RECEIVED:
            {
                final InboundMessage recvMsg = 
                    (InboundMessage) msg.obj;
                
                final String text = String.format(
                    "Received message type %d",
                    recvMsg.getMessageCode());
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_MESSAGE_TYPE_FLUSHED:
            {
                final String type = getMessageType(msg.arg1);
                final String text = String.format(
                    "Flushed all %s messages.",
                    type);
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_MESSAGE_ID_FLUSHED:
            {
                final String text = String.format(
                    "Flushed message with id: %d",
                    msg.arg1);
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_MESSSAGE_QUEUE_SYNCED:
            {
                final String text = "Message queue synchronized.";
                addEvent(text);
                
                m_queuesynced = true;
				m_queued_count = 0;


				// TODO run service now
                break;
            }
            case InReachEvents.EVENT_EMERGENCY_MODE_UPDATE:
            {
                final String text = (msg.arg1 == InReachEvents.VALUE_TRUE ?
                    "Emergency mode enabled." :
                    "Emergency mode disabled");
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_EMERGENCY_STATISTICS:
            {
                final String text = "Emergency Statistics Updated";
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_EMERGENCY_SYNC_ERROR:
            {
                final String text = "Emergency Sync Error";
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_TRACKING_MODE_UPDATE:
            {
                final String text = (msg.arg1 == InReachEvents.VALUE_TRUE ?
                    "Tracking mode enabled." :
                    "Tracking mode disabled");
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_TRACKING_STATISTICS:
            {
                final String text = "Tracking Statistics Updated";
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_TRACKING_DELAYED:
            {
                final String text = "Tracking delayed";
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_ACQUIRING_SIGNAL_STRENGTH:
            {
                final String text = (msg.arg1 == InReachEvents.VALUE_TRUE ?
                    "Acquiring signal strength enabled." :
                    "Acquiring signal strength disabled");
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_SIGNAL_STRENGTH_UPDATE:
            {
                final String text = String.format(
                    "Signal strength updated to: %d", 
                    msg.arg1);
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_BATTERY_STRENGTH_UPDATE:
            {
                final BatteryStatus status = (BatteryStatus)msg.obj;
                final String text = String.format(
                    "Battery strength updated to: %d",
                    status.getBatteryStrength());
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_BATTERY_LOW:
            {
                final String text = "Battery is low!";
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_BATTERY_EMPTY:
            {
                final String text = "Battery is empty!";
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_DEVICES_SPECS:
            {
                final InReachDeviceSpecs deviceSpecs = (InReachDeviceSpecs)msg.obj;
                final String text = String.format(
                    "Device IMEI: %d", deviceSpecs.getIMEI());
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_DEVICES_FEATURES:
            {
                final String text = (msg.arg1 == InReachEvents.VALUE_TRUE ?
                    "inReach supports sharing GPS." :
                    "inReach does not support sharing GPS");
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_DEVICE_LOCATION_UPDATE:
            {
                final Location location = (Location)msg.obj;
                
                final String text = String.format("Device Location: (%.2f,%.2f)",
                    location.getLatitude(), location.getLongitude());
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_DEVICE_TIME_UPDATE:
            {
                // from seconds to milliseconds
                final long timeStamp = 1000 * (Long)msg.obj;
                final Date date = new Date(timeStamp);
                final String text = "Device Time: " + date.toGMTString();

                addEvent(text);
                
                break;
            }
            case InReachEvents.EVENT_BLUETOOTH_CONNECT:
            {
                final String text = "Bluetooth Connected!";
                
                addEvent(text);
				// bluetooth socket connect successful.
				bluetoothstate = 0;
				this.removeCallbacks(m_bluetooth_timedout);
                break;
            }
            case InReachEvents.EVENT_BLUETOOTH_CONNECTING:
            {
                final String text = "Bluetooth Connecting..";
                
                addEvent(text);
                m_queuesynced = false;

				// start timer, connect might disappear into a blackhole
				if (bluetoothstate == 0){
					bluetoothstate = 1;
					this.postDelayed(m_bluetooth_timedout, 20000);
				}
                break;
            }
            case InReachEvents.EVENT_BLUETOOTH_CANCELLED:
            {
                final String text = "Bluetooth Cancelled";
                
                addEvent(text);

				// bluetooth failed, perhaps because we turned off the adapter
                break;
            }
            default: 
            {
                final String text = "Unknown message type";
                
                addEvent(text);
                break;
            }
        };
    }

	private Runnable m_bluetooth_timedout = new Runnable() {
		@Override
		public void run() {
			if (bluetoothstate==1){
				if (BA.getState() == BluetoothAdapter.STATE_ON && BA.disable())
					bluetoothstate++;
				else
					postDelayed(m_bluetooth_timedout, 1000);
			}
		}
	};

	public void onBluetoothStateChanged() {
		if (BA.getState() == BluetoothAdapter.STATE_OFF && bluetoothstate==2) {
			BA.enable();
		}
	}

	/**
     * A helper method to convert messages types to a human
     * readable string.
     * 
     * @param type the constant for message from MessagesTypes
     * @return string a human readable version of the message type
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    private synchronized String getMessageType(int type)
    {
        switch (type)
        {
            case MessageTypes.INREACH_MESSAGE:
                return "Message";
            case MessageTypes.INREACH_TRACKING_POINT:
                return "Tracking Point";
            case MessageTypes.INREACH_EMERGENCY_POINT:
                return "Emergency Point";
            case MessageTypes.INREACH_LOCATE_RESPONSE:
                return "Locate Response";
            default:
                return "Unknown";
        }
    }

    /**
     * Adds an event to the array and invokes the listener callback
     * 
     * @param text description of the event
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public synchronized void addEvent(String text)
    {

        
        // store event as html string
        final String event = text;

		m_lastevent = event;

        // update listener
        if (m_listener != null)
        {
            m_listener.onNewEvent(event);
        }
    }
    
    
    /**
     * Tells if the inReach's queue is synchronized
     * @return The status of the queue in the inReach 
     */
    public static final boolean getQueueSynced(){
    	return m_queuesynced;
    }
    
    /**
     * Tells if the inReach has a bluetooth socket with the phone.
     * This does not mean the inReach is ready to receive messages.
     * The inReach is ready when its queue is synchronized, so see getQueueSynced.
     * @return The basic status of the bluetooth connection with the inReach
     */
    public static boolean getConnecting(){
    	return m_connecting;
    }
    
    public static boolean isInreachAvailable(){
    	return m_queuesynced &&(m_queued_count == 0);
    }

	public static int getBluetoothstate(){
		return bluetoothstate;
	}
    
    public static int getInReachNumber(){
		int count = 0;
		if (BA.isEnabled()) {
			for (BluetoothDevice device : BA.getBondedDevices()) {
				if (BluetoothUtils.isInReachDevice(device))
					count++;
			}
		}
    	return count;
    }
    

    /**
     * Invoked when the service is binded
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        m_service = ((InReachService.InReachBinder)service).getService();
        
        InReachMessageHandler handler = InReachMessageHandler.getInstance();
        m_messenger = new Messenger(handler);
        m_service.registerMessenger(m_messenger);
        
        
    }

    /**
     * Invoked when the service is disconnected
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        if (m_service != null)
        {
            m_service.unregisterMessenger(m_messenger);
            m_service = null;
        }
    }
   
    /**
     * Returns the binded InReach Service
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public InReachService getService()
    {
        return m_service;
    }
    
    /**
     * Starts the InReachService and binds it to the application
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void startService()
    {
        if (m_serviceStarted)
            return;
        
        Intent intent = new Intent(this.context, InReachService.class);  
        
        this.context.startService(intent);
        this.context.bindService(intent, this, this.context.BIND_AUTO_CREATE); 
        
        m_serviceStarted = true;
    }
    
    /**
     * Unbinds the InReachService and stops the service.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void stopService()
    {
        if (!m_serviceStarted)
            return;
        
        Intent intent = new Intent(this.context, InReachService.class);  
        
        this.context.unbindService(this);
        this.context.stopService(intent);
        
        m_serviceStarted = false;
    }
    
    /** Boolean flag as to whether or not the service has been started */
    public boolean m_serviceStarted = false;
    
    /** The bound inReach service */
    public InReachService m_service = null;
    
    /** The messenger for the LogEventHandler */
    public Messenger m_messenger = null;
    
    /** A handler for all inReach events that logs them */
    public InReachMessageHandler m_eventHandler = null;


	/**
     * A listener for events.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public interface Listener
    {
        /**
         * Invoked when a new event is received. The string contains HTML.
         * This is invoked from the main thread.
         * 
         * @author Eric Semle
         * @since inReachApp (07 May 2012)
         * @version 1.0
         * @bug AND-1009
         */
        public void onNewEvent(String event);
    }
    
    /** The singleton instance of the LogEventHandler */
    private static InReachMessageHandler ms_logInstance = null;
    
    /** A listener for new events */
    private Listener m_listener = null;
	private String m_lastevent = null;

    private static boolean m_queuesynced = false;
    private static int m_queued_count = 0; 
    private static boolean m_connecting = false;

}
