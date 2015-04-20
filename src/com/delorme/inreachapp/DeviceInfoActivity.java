package com.delorme.inreachapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.widget.TextView;

import org.servalproject.sam.R;
import com.delorme.inreachapp.service.InReachEvents;
import com.delorme.inreachcore.InReachDeviceSpecs;
import com.delorme.inreachcore.InReachManager;

/**
 * An activity for viewing the device specifications.
 * 
 * @author Eric Semle
 * @since inReachApp (07 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
public class DeviceInfoActivity extends BaseTabActivity
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
        
        setContentView(R.layout.info_activity);
        
        m_imeiTextView = (TextView)findViewById(R.id.textViewIMEI);
        m_firmwareTextView = (TextView)findViewById(R.id.textViewFirmware);
        m_gpsTextView = (TextView)findViewById(R.id.textViewGPS);
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
        updateDeviceSpecs();
        
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
     * Update the UI with device specification if they are available.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void updateDeviceSpecs()
    {
        InReachManager manager = getInReachManager();
        if (manager == null)
            return;
        
        InReachDeviceSpecs deviceSpecs = manager.getDeviceSpecs();
        if (deviceSpecs == null)
            return;
        
        m_imeiTextView.setText("IMEI: " + deviceSpecs.getIMEI());
        m_firmwareTextView.setText(
            "Firmware: "+ deviceSpecs.getFormattedFirmwareVersion());
        m_gpsTextView.setText(
            "Support GPS: " + manager.supportsGpsReporting());
    }
    
    /**
     * An event handler for InReachEvent messages.
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
                case InReachEvents.EVENT_DEVICES_SPECS:
                case InReachEvents.EVENT_DEVICES_FEATURES:
                {
                    // runs on the main thread, so it is okay
                    // to update UI from here.
                    updateDeviceSpecs();
                    
                    break;
                }
                default:
                    super.handleMessage(msg);
                    break;
            };
        }
    }
    
    /** TextView that shows the paired devices IMEI */
    private TextView m_imeiTextView = null;
    
    /** TextView that shows the devices firmware version */
    private TextView m_firmwareTextView = null;
    
    /** TextView that shows if the paired inReach supports reporting GPS
     * location over bluetooth.
     */
    private TextView m_gpsTextView = null;
    
    /** A messenger for InReachEvent messages */
    private Messenger m_messenger = null;
}
