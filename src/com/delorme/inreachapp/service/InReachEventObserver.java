package com.delorme.inreachapp.service;

import java.util.List;

import android.location.Location;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.delorme.inreachcore.BatteryStatus;
import com.delorme.inreachcore.InReachDeviceSpecs;
import com.delorme.inreachcore.InReachManagerObserver;
import com.delorme.inreachcore.InboundMessage;
import com.delorme.inreachcore.InboundTeamTrackingMessage;
import com.delorme.inreachcore.OutboundMessage;
import com.delorme.inreachcore.TrackingStatus;

/**
 * The InReachObserver redirect all events to a messenger.
 * 
 * @author Eric Semle
 * @since inReachApp (07 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
public class InReachEventObserver implements InReachManagerObserver
{
    /**
     * The InReachObserver redirect all events to a messenger.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public InReachEventObserver(Messenger messenger) throws IllegalArgumentException
    {
        if (messenger == null)
            throw new IllegalArgumentException("The messenger cannot be null.");
        
        m_messenger = messenger;
    }
    
	/**
     * Invoked when state of tracking changes.
     * 
     * @param isEnabled True if tracking is enabled, false otherwise.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public void onTrackingModeUpdate(boolean isEnabled)
    {
	    final int value = (isEnabled ? InReachEvents.VALUE_TRUE
	        : InReachEvents.VALUE_FALSE);
	    
	    final Message msg = Message.obtain(null,
	        InReachEvents.EVENT_TRACKING_MODE_UPDATE);
	    msg.arg1 = value;
	    
	    try
	    {
	        m_messenger.send(msg);
	    }
	    catch (RemoteException ex)
	    {
	        Log.d(DEBUG_TAG, "Failed to send message", ex);
	    }
    }
    
    /**
     * Called when SOS mode is updated
     * @param isEnabled True if SOS mode is enabled, false otherwise
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public void onEmergencyModeUpdate(boolean isEnabled)
    {
	    final int value = (isEnabled ? InReachEvents.VALUE_TRUE
            : InReachEvents.VALUE_FALSE);
        
	    final Message msg = Message.obtain(null,
            InReachEvents.EVENT_EMERGENCY_MODE_UPDATE);
        msg.arg1 = value;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Invoked when the manager starts or stops receiving beacon pings
     * 
     * @param bool True if the device is connected. Otherwise false
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public void onConnectionStatusUpdate(boolean isConnected)
    {
	    final int value = (isConnected ? InReachEvents.VALUE_TRUE
            : InReachEvents.VALUE_FALSE);
	        
	    final Message msg = Message.obtain(null,
	        InReachEvents.EVENT_CONNECTION_STATUS_UPDATE);
        msg.arg1 = value;
	        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called when a message is created. This is called
     * before the message is queued
     * 
     * @param type The type of message that was queued
     * @param id The id of the message that was queued
     *
     * @return long Messages user data identifier
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public long onMessageCreated(OutboundMessage message, int type)
    {
	    final Message msg = Message.obtain(null,
	        InReachEvents.EVENT_MESSAGE_CREATED);
	    msg.obj = message;
	    msg.arg1 = type;
	    msg.arg2 = (int)(--m_messageId); // give these points negative values
	    
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
	    
    	return m_messageId;
    }
    
    /**
     * Called when a message is queued
     * @param ppMessage The message that was queued. May be null
     * @param type The type of message that was queued
     * @param id The id of the message that was queued
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public void onMessageQueued(OutboundMessage message, int type,
            long id)
    {
	    final Message msg = Message.obtain(null,
            InReachEvents.EVENT_MESSAGE_QUEUED);
	    msg.obj = message;
        msg.arg1 = type;
        msg.arg2 = (int)id;
	        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
	}
    
    /**
     * Called when a message successfully sends
     * @param messageType The type of message that sent
     * @param id The id of the message that sent
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public void onMessageSendSucceeded(int messageType, long id)
    {
	    final Message msg = Message.obtain(null,
            InReachEvents.EVENT_MESSAGE_SEND_SUCCEEDED);
        msg.arg1 = messageType;
        msg.arg2 = (int)id;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Invoked when session results indicate message send failed.
     * 
     * @param messageType The type of message that failed
     * @param errCode The error code for the failure.
     * @param status The status code of the failed message
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public void onMessageSendFailed(int messageType, long id)
    {
	    final Message msg = Message.obtain(null,
            InReachEvents.EVENT_MESSAGE_SEND_FAILED);
        msg.arg1 = messageType;
        msg.arg2 = (int)id;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called when a message is suspended
     * @param suspended True if messaging is suspended, false otherwise
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public void onMessagingSuspended(boolean suspended)
    {
	    final int value = (suspended ? InReachEvents.VALUE_TRUE
            : InReachEvents.VALUE_FALSE);
        
	    final Message msg = Message.obtain(null,
            InReachEvents.EVENT_MESSAGING_SUSPENDED);
        msg.arg1 = value;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called when a message is received
     * @param message The message that was received
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public void onMessageReceived(InboundMessage message)
    {
	    final Message msg = Message.obtain(null,
            InReachEvents.EVENT_MESSAGE_RECEIVED);
        msg.obj = message;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called when messages are flushed
     * @param type The message type that was flushed.
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public void onMessagesFlushed(int type)
    {
	    final Message msg = Message.obtain(null,
            InReachEvents.EVENT_MESSAGE_TYPE_FLUSHED);
        msg.arg1 = type;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called when a message is flushed
     * @param id The message id that was flushed.
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public void onMessageFlushed(long id)
    {
	    final Message msg = Message.obtain(null,
            InReachEvents.EVENT_MESSAGE_ID_FLUSHED);
        msg.arg1 = (int)id;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called whenever we need to update the statistics for tracking.
     * 
     * @param statistics the current tracking statistics
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public void onNewTrackingModeStatistics(TrackingStatus statistics)
    {
	    final Message msg = Message.obtain(null,
	        InReachEvents.EVENT_TRACKING_STATISTICS);
	    msg.obj = statistics;
	    
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called whenever we need to update the statistics for sos.
     * 
     * @param statistics the current sos statistics
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
    public void onNewEmergencyModeStatistics(TrackingStatus statistics)
    {
	    final Message msg = Message.obtain(null,
            InReachEvents.EVENT_EMERGENCY_STATISTICS);
        msg.obj = statistics;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called whenever there's an error caused by inconsistent SOS 
     * state. This can happen if the user declares SOS, yanks the batteries
     * out of their inReach, and then reconnects. 
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onEmergencySyncError()
    {
        final Message msg = Message.obtain(null,
            InReachEvents.EVENT_EMERGENCY_SYNC_ERROR);
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called when inReach starts or stops acquiring the signal strength
     * 
     * @param isAcquiring True if the InReach is acquiring signal strength
     * and false if it is not.
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onAcquiringSignalStrength(boolean isAcquiring)
    {
        final int value = (isAcquiring ? InReachEvents.VALUE_TRUE
            : InReachEvents.VALUE_FALSE);
        
        final Message msg = Message.obtain(null,
            InReachEvents.EVENT_ACQUIRING_SIGNAL_STRENGTH);
        msg.arg1 = value;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called when the signal strength is updated
     * 
     * @param strength Signal strength in range (0-5) as reported by puck
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onSignalStrengthUpdate(int strength)
    {
        final Message msg = Message.obtain(null,
            InReachEvents.EVENT_SIGNAL_STRENGTH_UPDATE);
        msg.arg1 = strength;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
        
    /**
     * Invoked when puck's battery strength changes
     *
     * @param battery The current status of the battery
     * 
     * @author Eric Semle
     * @since inReach SDK (26 April 2012)
     * @version 1.0
     * @bug AND-1009950
     */
    public void onBatteryStrengthUpdate(BatteryStatus battery)
    {
        final Message msg = Message.obtain(null,
            InReachEvents.EVENT_BATTERY_STRENGTH_UPDATE);
        msg.obj = battery;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Invoked when puck reports low battery
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onLowBattery()
    {
        final Message msg = Message.obtain(null,
            InReachEvents.EVENT_BATTERY_LOW);
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Invoked when puck reports (almost) empty battery
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onEmptyBattery()
    {
        final Message msg = Message.obtain(null,
            InReachEvents.EVENT_BATTERY_EMPTY);
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Invoked when a tracking point can't be created because device
     * doesn't have a GPS fix. Invoked once per tracking point.
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onTrackingDelay()
    {
        final Message msg = Message.obtain(null,
            InReachEvents.EVENT_TRACKING_DELAYED);
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }

    /**
     * Called when new device specs are received
     * @param majorFirmVers The major firmware version
     * @param minorFirmVers The minor firmware version
     * @param subminorFirmVers The subminor firmware version
     * @param majorIfaceVers The major interface version
     * @param minorIfaceVers The minor interface version
     * @param subminorIfaceVers The subminor interface version
     * @param imei The imei
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onReceiveDeviceSpecs(InReachDeviceSpecs deviceSpecs)
    {
        final Message msg = Message.obtain(null,
            InReachEvents.EVENT_DEVICES_SPECS);
        msg.obj = deviceSpecs;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called whenever a team tracking message is received.
     * 
     * @param message The team tracking message.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onTeamTrackingMessageReceived(
        InboundTeamTrackingMessage message)
    {
    	// NO IMPLEMENTED
    }
    
    /**
     * Information about which features are
     * available with the connected device.
     * 
     * @param hasGps True if the device supports reporting gps
     * location updates to application.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onDeviceFeatures(boolean hasGps)
    {
        final int value = (hasGps ? InReachEvents.VALUE_TRUE
            : InReachEvents.VALUE_FALSE);
        
        Message msg = Message.obtain(null,
            InReachEvents.EVENT_DEVICES_FEATURES);
        msg.arg1 = value;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called whenever location information has been received from the
     * inReach device.
     * 
     * @param location A GPS update from the device.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onReceivedLocation(Location location)
    {
        final Message msg = Message.obtain(null,
            InReachEvents.EVENT_DEVICE_LOCATION_UPDATE);
        msg.obj = location;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called when we receive a new device time
     * @param time The unix time of the device
     *
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onNewDeviceTime(long time)
    {
        final Message msg = Message.obtain(null,
            InReachEvents.EVENT_DEVICE_TIME_UPDATE);
        msg.obj = (Long) time;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    /**
     * Called after the inReach's list of queued messages is synchronized
     * with the client. This is a good opportunity to send any messages 
     * that are queued by the client but not the inReach.
     * 
     * Note that this callback only occurs on inReach 1.5 devices.
     * 
     * @param queuedGUIDs GUIDs of messages queued on the inReach.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onMessageQueueSynchronized(List<Long> queuedGUIDs)
    {
        final Message msg = Message.obtain(null,
            InReachEvents.EVENT_MESSSAGE_QUEUE_SYNCED);
        msg.obj = queuedGUIDs;
        
        try
        {
            m_messenger.send(msg);
        }
        catch (RemoteException ex)
        {
            Log.d(DEBUG_TAG, "Failed to send message", ex);
        }
    }
    
    //! The handler for messages
    private Messenger m_messenger = null;
    
    //! inReach created message ID counts backwards
    private long m_messageId = 0;
    
    //! A debug tab for logcat
    private static final String DEBUG_TAG = "InReachEventObserver";
}
