package org.magdaaproject.sam;

import org.servalproject.sam.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class RCLauncherActivity extends FragmentActivity implements OnClickListener {

	/*
	 * private class level constants
	 */

	//private static final boolean sVerboseLog = true;
	private static final String sLogTag = "RCLauncherActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rc_launcher);
		
		// setup the buttons
		Button mButton = (Button) findViewById(R.id.launcher_rc_number_of_message_queued);
		mButton.setOnClickListener(this);
		
		mButton = (Button) findViewById(R.id.launcher_rc_connection_to_inreach);
		mButton.setOnClickListener(this);
		
		mButton = (Button) findViewById(R.id.launcher_rc_go_to_regular_launcher);
		mButton.setOnClickListener(this);
		
		// setup the checkboxes
		CheckBox mcheckBox = (CheckBox) findViewById(R.id.launcher_rc_notify_ui_SD);
		mcheckBox.setChecked(true);
	}
	
	private void updateUI()
	{
		/* Update user interface:
	     * 1. Show SMS status.
	     * 2. Show WiFi/cellular data status.
	     * 3. Show inReach status.
	     * 4. Show number of messages in the queue.
	     */
		
		CheckBox mcheckBox = (CheckBox) findViewById(R.id.launcher_rc_notify_ui_inreach);
		boolean inReachConnected = false;
		
		Button mButton = (Button) findViewById(R.id.launcher_rc_connection_to_inreach);
		if (InReachMessageHandler.getInReachNumber() < 1){
			mButton.setText("There is no paired inReach device" +
					"\nPlease pair to a device and restart the application");
		} else if (InReachMessageHandler.getInReachNumber() > 1){
			mButton.setText("This phone has paired with more than one inReach device." +
					"\nYou must exit this application, unpair from all inReach devices,\n and then re-pair with the one you want to use, and then start this application again.");
		} else {
			mButton.setText("This phone is paired with an inReach device, but it isn't connected right now.\n  Try turning it off and on, or re-pairing it with this phone."); 
		}
		
		//if the UI shows this, then the phone is not totally connected to the inReach
		if ((InReachMessageHandler.getConnecting() == true) 
				//&& (InReachMessageHandler.getQueueSynced() == false)
				){
			mButton.setText("I am trying to connect to the inReach device right now...\nUnpair it, and then re-pair it if it doesn't connect within 30 seconds.");
		}
		if (InReachMessageHandler.getQueueSynced() == true){
			
			mButton.setText("Connected to inReach.");
			inReachConnected = true;
		}
		mcheckBox.setChecked(inReachConnected);
		return;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View view) {

		Intent mIntent;

		// determine which button was touched
		switch(view.getId()){
		case R.id.launcher_rc_number_of_message_queued:
			// Show message queue
			mIntent = new Intent(this, org.servalproject.succinctdata.SuccinctDataQueueListViewActivity.class);
			startActivity(mIntent);			
			break;
			
		case R.id.launcher_rc_connection_to_inreach:
			updateUI();
			break;			
		case R.id.launcher_rc_go_to_regular_launcher:
			mIntent = new Intent(this, org.magdaaproject.sam.LauncherActivity.class);
			startActivity(mIntent);
			break;
			
		default:
			Log.w(sLogTag, "an unknown view fired an onClick event");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		
	}
		
}
