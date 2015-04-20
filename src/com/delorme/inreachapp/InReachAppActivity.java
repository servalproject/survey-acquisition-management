package com.delorme.inreachapp;

import org.servalproject.sam.R;
import com.delorme.inreachapp.service.bluetooth.BluetoothUtils;

import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * Main activity for the InReachApp. This activity
 * is a tab activity with log, controls and information
 * about the inReach.
 * 
 * @author Eric Semle
 * @since inReachApp (07 May 2012)
 * @version 1.0
 * @bug AND-10091009
 */
public class InReachAppActivity extends TabActivity
{
    /**
     * Called when the activity is created.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-10091009
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
  
        setContentView(R.layout.main);

        // Initialize the tabs
        initTabs();

        // If Bluetooth is disabled, prompt the user to
        // turn it on. 
        if (!BluetoothUtils.isBluetoothAdapterOn())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    
    /**
     * Initializes the Tabhost for the TabActivity
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-10091009
     */
    private void initTabs()
    {
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        
        // This tab shows a log of all inReach events.
        spec = tabHost.newTabSpec("Event Log");
        spec.setIndicator("Event Log");
        spec.setContent(new Intent(this, DeviceLogActivity.class));
        tabHost.addTab(spec);
        
        // This tab contains examples of all the inReach controls
        spec = tabHost.newTabSpec("Controls");
        spec.setIndicator("Controls");
        spec.setContent(new Intent(this, DeviceControlsActivity.class));
        tabHost.addTab(spec);
        
        // This tab shows the currently paired inReach specifications
        spec = tabHost.newTabSpec("inReach Info");
        spec.setIndicator("inReach Info");
        spec.setContent(new Intent(this, DeviceInfoActivity.class));
        tabHost.addTab(spec);

        // Default to the log tab
        tabHost.setCurrentTab(0);
    }
    
    /** The returned request code. */
    private static final int REQUEST_ENABLE_BT = 1;
}