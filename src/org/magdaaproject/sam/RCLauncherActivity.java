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
			break;
			
		case R.id.launcher_rc_connection_to_inreach:
			
			if (InReachMessageHandler.getInReachNumber() <= 0){
				Toast.makeText(this, "There is no paired inReach device" +
						"\nPlease pair to a device and restart the application", Toast.LENGTH_LONG).show();
			} else if (InReachMessageHandler.getInReachNumber() > 1){
				Toast.makeText(this, "There are more than one inReach device" +
						"\nPlease remove the unsed devices and restart the application", Toast.LENGTH_LONG).show();
			}
			
			Button mButton = (Button) findViewById(R.id.launcher_rc_connection_to_inreach);
			//if the UI shows this, then the phone is not totally connected to the inReach
			if ((InReachMessageHandler.getConnecting() == true) 
					//&& (InReachMessageHandler.getQueueSynced() == false)
					){
				mButton.setText("Bluetooth socket connected to inReach");
			}
			if (InReachMessageHandler.getQueueSynced() == true){
				
				mButton.setText("connected to inReach");
				CheckBox mcheckBox = (CheckBox) findViewById(R.id.launcher_rc_notify_ui_inreach);
				mcheckBox.setChecked(true);
			}
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
