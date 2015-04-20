package com.delorme.inreachapp;

import java.util.Date;

import org.servalproject.servalsatellitemessenger.MessageComposeActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.servalproject.sam.R;
import com.delorme.inreachapp.service.InReachEvents;
import com.delorme.inreachcore.InReachManager;
import com.delorme.inreachcore.OutboundMessage;

/**
 * An activity for all of the controls associated with an inReach
 * 
 * @author Eric Semle
 * @since inReachApp (07 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
public class DeviceControlsActivity extends BaseTabActivity
{
    /**
     * Called when the activity is created.
     * 
     * @param savedInstanceStat If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). 
     * Note: Otherwise it is null.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.control_activity);
        
        m_trackingButton = (Button)findViewById(R.id.trackingButton);
        m_emergencyButton = (Button)findViewById(R.id.emergencyButton);
        m_gpsButton = (Button)findViewById(R.id.gpsButton);
    }
    
    /**
     * Called when the activity resumes. This is a good time to register
     * a messenger for events.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onResume()
    {
        super.onResume();

        // update the device specs
        updateButtons();
        
        // A messenger is registered to receive callbacks
        // on the device specifications and features.
        m_messenger = new Messenger(new EventHandler());
        if (!registerMessenger(m_messenger))
        {
            m_messenger = null;
        }
    }
    
    /**
     * Called when the activity pauses. This is a good time to unregister
     * a messenger for events
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onPause()
    {
        super.onPause();
        
        // Do not keep the messenger registered when the activity pauses.
        // Instead, update the state when the activity resumes.  Always
        // unregister messengers when they aren't required.
        if (m_messenger != null)
        {
            unregisterMessenger(m_messenger);
        }
    }

    /**
     * Called when the tracking button is "clicked"
     * 
     * @param view the button that was pressed
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void onTrackingButton(View view)
    {
        final InReachManager manager = getInReachManager();
        
        if (manager == null)
            return;
        
        if (manager.isTracking())
        {
            // This stops tracking. Stop tracking is an event. So one
            // more track point will be sent, but the type will be
            // tracking stopped
            manager.stopTracking();
        }
        else
        {
            // Start tracking at a 10 minute interval.
            // 600 seconds or 10 minutes = (60 seconds * 10 minutes)
            manager.startTracking(600);
        }
    }
    
    /**
     * Called when the emergency button is "clicked"
     * 
     * @param view the button that was pressed
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void onEmergencyButton(View view)
    {
        final InReachManager manager = getInReachManager();
        
        if (manager == null)
            return;
        
        if (manager.isInEmergency())
        {
            // Canceling the emergency does not instantly stop the emergency.
            // The inReach will stay in emergency state until the emergency
            // response center responds with a cancel acknowledgment.
            manager.cancelSOS();
        }
        else
        {
            // Declaring an emergency cancels any tracking state. This cannot be
            // stopped until the emergency is canceled and the emergency response
            // center responds with a cancel acknowledgement.
            manager.declareSOS();
        }    
    }
    
    /**
     * Called when the request GPS button is "clicked"
     * 
     * @param view the button that was pressed
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void onRequestGpsButton(View view)
    {
        final InReachManager manager = getInReachManager();
        
        if (manager == null || !manager.supportsGpsReporting())
            return;
        
        // Set the GPS update interval to 1 second. Please note that
        // requesting an interval update at 1 second will keep the inReach
        // GPS on at all times and will sacrifice battery life.
        manager.setGPSUpdateInterval(1);
        
        // view the event log
        setCurrentTab(0);
    }
    
    public void textMessageClicked(View view)
    {
    	Intent intent = new Intent(this, MessageComposeActivity.class);
    	startActivity(intent);
    }
    
    
    /**
     * Called when the reference point button is "clicked"
     * 
     * @param view the button that was pressed
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void onReferencePointButton(View view)
    {
        final InReachManager manager = getInReachManager();
        
        if (manager == null)
            return;
        
        // This is the "meat and potatoes" setup.
        final OutboundMessage message = new OutboundMessage();
        // Use PPAC free form for all free text or reference point messages.
        message.setAddressCode(OutboundMessage.AC_FreeForm);
        // This specific type of message is a reference point.
        message.setMessageCode(OutboundMessage.MC_ReferencePoint);
        
        // This is kind of hacky, but this sample application uses
        // a static variable to increment the message identifier.
        // Another option would be to drive this with a table in a
        // database with an auto increment primary key. The table could
        // store when the message is successfully sent.
        message.setIdentifier(ms_messageIdentifier++);
        
        // who are we sending it to?
        message.addAddress("test@inreach.delorme.com");
        // a description of what the point is about.
        message.setText("Meet here @ 0900");
        // the date and time the message was created.
        message.setDate(new Date());
        
        // The location for this message should not be where the message was
        // send to, but a point on a map. For example, the coordinates below
        // are to DeLorme's offices.
        message.setLatitude(43.807921);
        message.setLongitude(-70.163493);
        
        // queue the message for sending
        if (!manager.sendMessage(message))
        {
            Toast.makeText(this,
                "Failed to send reference point",
                Toast.LENGTH_LONG).show();
        }
        else
        {
            // view the event log
            setCurrentTab(0);
        }
    }
        
    /**
     * Updates the Button UI based on current state.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void updateButtons()
    {
        final InReachManager manager = getInReachManager();
        if (manager == null)
        {
            m_trackingButton.setEnabled(false);
            m_emergencyButton.setEnabled(false);
            m_gpsButton.setEnabled(false);
        
            return;
        }
        
        // get current state.
        final boolean isTracking = manager.isTracking();
        final boolean isInEmergency = manager.isInEmergency();
        final boolean supportsGps = manager.supportsGpsReporting();
        
        // Configure the tracking button.
        // The tracking button should be disabled when
        // the device is in an emergency. Emergencies take
        // precedence over tracking.
        final String tracking = (isTracking ?
            "Stop Tracking" : "Start Tracking");
        
        m_trackingButton.setText(tracking);
        m_trackingButton.setEnabled(!isInEmergency);
        
        // Configure the emergency button.
        final String emergency = (isInEmergency ? 
            "Cancel Emergency" : "Declare Emergency");
        
        m_emergencyButton.setText(emergency);
        
        // Configure the tracking button.
        m_gpsButton.setEnabled(supportsGps);
    }
    
    /**
     * An event handler for InReach events.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    final class EventHandler extends Handler
    {
        /**
         * Handles incoming messages from InReach events.
         * 
         * @param msg InReach event messages
         * 
         * @author Eric Semle
         * @since inReachApp (07 May 2012)
         * @version 1.0
         * @bug AND-1009
         */
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case InReachEvents.EVENT_TRACKING_MODE_UPDATE:
                case InReachEvents.EVENT_EMERGENCY_MODE_UPDATE:
                case InReachEvents.EVENT_DEVICES_FEATURES:
                case InReachEvents.EVENT_BLUETOOTH_CONNECT:
                {
                    // It is okay to invoke this method, because the
                    // messages are handled from the main thread.
                    updateButtons();
                    
                    break;
                }
                default:
                    super.handleMessage(msg);
                    break;
            };
        }
    }
    
    /** Button for starting and stopping tracking */
    private Button m_trackingButton = null;
    
    /** Button for declaring and canceling an emergency */
    private Button m_emergencyButton = null;
    
    /** Button for requesting GPS updates from inReach */
    private Button m_gpsButton = null;
    
    /** Receives messages from the InReachService*/
    private Messenger m_messenger = null;

    /**
     * This static long is used to generate a unique identifier
     * for each message.
     */
    private static long ms_messageIdentifier = 0;
}
