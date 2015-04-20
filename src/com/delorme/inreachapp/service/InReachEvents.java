package com.delorme.inreachapp.service;

/**
 * The event types that are relayed through messengers
 * 
 * @author Eric Semle
 * @since inReachApp (09 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
public abstract class InReachEvents
{
    /**
     * Event that indicates that the connection status has changed.
     * 
     * @param arg1 1 for connected, 0 for disconnected
     */
    public static final int EVENT_CONNECTION_STATUS_UPDATE = 1;
    
    /**
     * Event that indicates that a message was created. This is
     * only called for tracking or SOS messages that were generated
     * by the inReachManager.
     * 
     * @param arg1 the message type (InReachConstants)
     * @param arg2 the unique message identifier
     * @param obj the OutboundMessage message
     */
    public static final int EVENT_MESSAGE_CREATED = 2;
    
    /**
     * Event that indicates a message was queued for delivery.
     * 
     * @param arg1 the message type (InReachConstants)
     * @param arg2 the unique message identifier
     * @param obj the OutboundMessage message
     */
    public static final int EVENT_MESSAGE_QUEUED = 3;
    
    /**
     * Event that indicates a message was successfully sent.
     * 
     * @param arg1 the message type (InReachConstants)
     * @param arg2 the unique message identifier
     */
    public static final int EVENT_MESSAGE_SEND_SUCCEEDED = 4;
    
    /**
     * Event that indicates a message failed to send.
     * 
     * @param arg1 the message type (InReachConstants)
     * @param arg2 the unique message identifier
     */
    public static final int EVENT_MESSAGE_SEND_FAILED = 5;
    
    /**
     * Event that indicates that messaging has been suspended.
     * This will normally not occur. It does not require
     * the resumeSending to be called
     * 
     * @param arg1 1 for connected, 0 for disconnected
     */
    public static final int EVENT_MESSAGING_SUSPENDED = 6;
    
    /**
     * Event that indicates that a message was received.
     * 
     * @param obj a InboundMessage object
     */
    public static final int EVENT_MESSAGE_RECEIVED = 7;
    
    /**
     * Event that indicates that all of messages of a specific
     * type were flushed from the queue.
     * 
     * @param arg1 the message type that was flushed
     */
    public static final int EVENT_MESSAGE_TYPE_FLUSHED = 8;
    
    /**
     * Event that indicates that a message with ID arg1 has
     * been flushed from the queue.
     * 
     * @param arg1 the id of the message that was flush
     */
    public static final int EVENT_MESSAGE_ID_FLUSHED = 9;
    
    /**
     * Called after the inReach's list of queued messages is synchronized
     * with the client. This is a good opportunity to send any messages 
     * that are queued by the client but not the inReach.
     * 
     * @param obj A list of queued id's (List<Long>)
     */
    public static final int EVENT_MESSSAGE_QUEUE_SYNCED = 10;
    
    /**
     * Called when the emergency mode is updated.
     * 
     * @param arg1 1 for enabled, 0 for disabled
     */
    public static final int EVENT_EMERGENCY_MODE_UPDATE = 11;
    
    /**
     * Called when new tracking statistics are received
     * 
     * @param obj the current TrackingStatus object
     */
    public static final int EVENT_EMERGENCY_STATISTICS = 12;
    
    /**
     * Called whenever there's an error caused by inconsistent SOS 
     * state. This can happen if the user declares SOS, yanks the batteries
     * out of their inReach, and then reconnects. 
     */
    public static final int EVENT_EMERGENCY_SYNC_ERROR = 13;
    
    /**
     * Called when the tracking mode is updated.
     * 
     * @param arg1 1 for enabled, 0 for disabled
     */
    public static final int EVENT_TRACKING_MODE_UPDATE = 14;
    
    /**
     * Called when new tracking statistics are received
     * 
     * @param obj the current TrackingStatus object
     */
    public static final int EVENT_TRACKING_STATISTICS = 15;
    
    /**
     * Called when tracking delayed because the inReach acquiring GPS.
     */
    public static final int EVENT_TRACKING_DELAYED = 16;
    
    /**
     * Called when the inReach starts or stop acquring Iridium signal strength
     * 
     * @param arg1 1 = started, 0 = stopped
     */
    public static final int EVENT_ACQUIRING_SIGNAL_STRENGTH = 17;
    
    /**
     * Called when the inReach starts or stop acquring Iridium signal strength
     * 
     * @param arg1 The Iridium Signal strength from 0 to 5. Where
     * 0 is no signal and 5 is full signal.
     */
    public static final int EVENT_SIGNAL_STRENGTH_UPDATE = 18;
    
    /**
     * Called when the inReach batteru status has an update
     * 
     * @param obj the current inReach battery status (BatteryStatus)
     */
    public static final int EVENT_BATTERY_STRENGTH_UPDATE = 19;
    
    /**
     * Called when the battery is low
     */
    public static final int EVENT_BATTERY_LOW = 20;
    
    /**
     * Called when the battery is empty
     */
    public static final int EVENT_BATTERY_EMPTY = 21;
    
    /**
     * This is called when the device specifics are received from the inReach
     * 
     * @param obj A reference to the inReachDeviceSpecs object
     */
    public static final int EVENT_DEVICES_SPECS = 22;
    
    /**
     * This is called when the device features are receives.
     * 
     * @param arg1 1 if support sharing GPS, otherwise 0
     */
    public static final int EVENT_DEVICES_FEATURES = 23;
    
    /**
     * This is called when a GPS location is received from the inReach
     * 
     * @param obj A reference to a Location object
     */
    public static final int EVENT_DEVICE_LOCATION_UPDATE = 24;
    
    /**
     * This is invoked when the GPS time is received from the inReach
     * 
     * @param obj (Long) UNIX time from the device. Seconds since Jan 1, 1970
     * 
     */
    public static final int EVENT_DEVICE_TIME_UPDATE = 25;
    

    /**
     * This is invoked when a Bluetooth connection is established
     * 
     * @param obj (BluetoothSocket) A connected BluetoothSocket
     */
    public static final int EVENT_BLUETOOTH_CONNECT = 26;
    
    /**
     * This is invoked when the service starts attempting to
     * connect to a Bluetooth device
     * 
     */
    public static final int EVENT_BLUETOOTH_CONNECTING = 27;
    
    /**
     * This is invoked when the bluetooth connect is cancelled
     */
    public static final int EVENT_BLUETOOTH_CANCELLED = 28;


    /**
     * Integer value for true
     */
    public static final int VALUE_TRUE  = 1;
    
    /**
     * Interger value for false
     */
    public static final int VALUE_FALSE = 0;
}
