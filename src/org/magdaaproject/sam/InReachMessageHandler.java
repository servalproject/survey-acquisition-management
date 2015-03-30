package org.magdaaproject.sam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.delorme.inreachapp.service.InReachEvents;
import com.delorme.inreachcore.BatteryStatus;
import com.delorme.inreachcore.InReachDeviceSpecs;
import com.delorme.inreachcore.InboundMessage;
import com.delorme.inreachcore.MessageTypes;

import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Message;


/**
 * A class to handle the inReach messages coming from the inReach service
 * 
 * @author Guillaume
 *
 */

public class InReachMessageHandler extends Handler{



    
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
        if (ms_logInstance == null)
        {
            ms_logInstance = new InReachMessageHandler();
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
        m_listener = listener;
    }
    
    /**
     * Returns a list of events as strings that contain
     * HTML.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public synchronized List<String> getEvents()
    {
        return m_events;
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
                
                queuesynced = true;
                		
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
                break;
            }
            case InReachEvents.EVENT_BLUETOOTH_CONNECTING:
            {
                final String text = "Bluetooth Connecting..";
                
                addEvent(text);
                break;
            }
            case InReachEvents.EVENT_BLUETOOTH_CANCELLED:
            {
                final String text = "Bluetooth Cancelled";
                
                addEvent(text);
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
     * @param color the ARGB color for the event
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
        
        m_events.add(event);
        
        // update listener
        if (m_listener != null)
        {
            m_listener.onNewEvent(event);
        }
    }
    
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

    /** An array of events that contains strings with HTML*/
    private List<String> m_events = new ArrayList<String>();
    
    
    public boolean queuesynced = false;
}
