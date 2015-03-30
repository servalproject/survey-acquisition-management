package org.magdaaproject.sam;

import org.servalproject.sam.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

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
		mcheckBox.setEnabled(false);

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
