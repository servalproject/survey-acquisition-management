package org.magdaaproject.sam;

import org.servalproject.sam.R;
import org.servalproject.succinctdata.SuccinctDataQueueService;

import com.delorme.inreachcore.InReachManager;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class RCLauncherActivity extends FragmentActivity implements OnClickListener, InReachMessageHandler.Listener {

	/*
	 * private class level constants
	 */

	//private static final boolean sVerboseLog = true;
	private static final String sLogTag = "RCLauncherActivity";
	private static long messageQueueLength = 0;
	public static RCLauncherActivity instance = null;
	public static boolean upload_form_specifications = false;

	private Handler mHandler = null;
	final Runnable mStatusChecker = new Runnable() {
		@Override
		public void run() {
			updateUI();
			mHandler.postDelayed(mStatusChecker, 5000);
		}
	};

	private int knocks =0;
	long last_knock = 0;

	private int upload_knocks =0;
	long last_upload_knock = 0;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rc_launcher);
		
		mHandler = new Handler();
		
		// setup the buttons
		Button mButton = (Button) findViewById(R.id.launcher_rc_go_to_regular_launcher);
		mButton.setOnClickListener(this);
		mButton = (Button) findViewById(R.id.launcher_rc_message_queue_heading);
		mButton.setOnClickListener(this);
		mButton = (Button) findViewById(R.id.launcher_rc_inReach_status_heading);
		mButton.setOnClickListener(this);
		mButton = (Button) findViewById(R.id.launcher_rc_channel_availability_heading);
		mButton.setOnClickListener(this);
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
		
		TextView mTextView = (TextView) findViewById(R.id.launcher_rc_connection_to_inreach);
		int inreachDevices = InReachMessageHandler.getInReachNumber();

		if (inreachDevices < 1){
			mTextView.setText("There is no paired inReach device." +
					"\nIf this message persists, please pair to an inReach device and restart the application");
		} else if (inreachDevices > 1){
			mTextView.setText("This phone has paired with more than one inReach device." +
					"\nYou must exit this application, unpair from all inReach devices,\n and then re-pair with the one you want to use, and then start this application again.");
		} else {
			switch(InReachMessageHandler.getBluetoothstate()) {
				case 1:
					mTextView.setText("Attempting to connect to paired inReach device. This can take a minute or two.\n");
					break;
				case 2:
					mTextView.setText("Restarting bluetooth.\n");
					break;
				default:
					mTextView.setText("This phone is paired with an inReach device, but it isn't connected right now.\n"
							+ "  If it doesn't connect within a couple of minutes, try turning it off and on, or re-pairing it with this phone.");
			}
		}
		
		//if the UI shows this, then the phone is not totally connected to the inReach
		if ((InReachMessageHandler.getConnecting())
				//&& (InReachMessageHandler.getQueueSynced() == false)
				){
			mTextView.setText("I am trying to connect to the inReach device right now...\nUnpair it, and then re-pair it if it doesn't connect within 30 seconds.");
		}
		if (InReachMessageHandler.getQueueSynced()){
			
			mTextView.setText("Connected to inReach.");
			inReachConnected = true;
		}
		mcheckBox.setChecked(inReachConnected);
		
		mTextView = (TextView) findViewById(R.id.launcher_rc_number_of_message_queued);
		mTextView.setText("" + messageQueueLength + " message(s) waiting to be transmitted.");
		
		mcheckBox = (CheckBox) findViewById(R.id.launcher_rc_notify_ui_SMS);
		mcheckBox.setChecked(SuccinctDataQueueService.isSMSAvailable(this));
		mcheckBox = (CheckBox) findViewById(R.id.launcher_rc_notify_ui_wifi_cellular_internet);
		mcheckBox.setChecked(isInternetAvailable());

	}
	
	// Detecting internet access by Alexandre Jasmin from:
	// http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
	public boolean isInternetAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
		case R.id.launcher_rc_message_queue_heading:
			// Show message queue
			mIntent = new Intent(this, org.servalproject.succinctdata.SuccinctDataQueueListViewActivity.class);
			startActivity(mIntent);			
			break;
			
		case R.id.launcher_rc_inReach_status_heading:
			updateUI();
			if ((System.currentTimeMillis()-last_upload_knock)<2000) {
				upload_knocks++;
				if (upload_knocks>=7) {
					TextView t = (TextView) findViewById(R.id.launcher_rc_upload_form_specifications);
					t.setVisibility(t.VISIBLE);
					RCLauncherActivity.upload_form_specifications  = true;
				}
			} else {
				if (upload_knocks>=7) {
					TextView t = (TextView) findViewById(R.id.launcher_rc_upload_form_specifications);
					t.setVisibility(t.INVISIBLE);
					RCLauncherActivity.upload_form_specifications = false;
				}
				upload_knocks = 0;
			}
			last_upload_knock = System.currentTimeMillis();
			break;
		case R.id.launcher_rc_channel_availability_heading:
			if ((System.currentTimeMillis()-last_knock)<2000) {
				knocks++;
				if (knocks>=7) {
					Button b = (Button) findViewById(R.id.launcher_rc_go_to_regular_launcher);
					b.setVisibility(b.VISIBLE);
				}
			}else {
				if (knocks>=7) {
					Button b = (Button) findViewById(R.id.launcher_rc_go_to_regular_launcher);
					b.setVisibility(b.INVISIBLE);
				}
				knocks = 0;
			}
			last_knock = System.currentTimeMillis();
			break;
		case R.id.launcher_rc_go_to_regular_launcher:
			mIntent = new Intent(this, org.magdaaproject.sam.LauncherActivity.class);
			startActivity(mIntent);
			break;
			
		default:
			Log.w(sLogTag, "an unknown view fired an onClick event");
		}
	}

	public static void set_message_queue_length(long count) {
		messageQueueLength = count;
		requestUpdateUI();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		instance = this;
		updateUI();
		startRepeatingTask();
		InReachMessageHandler.getInstance().setListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		InReachMessageHandler.getInstance().setListener(null);
		stopRepeatingTask();
		instance = null;
	}
	
	public static void requestUpdateUI() {
		if (RCLauncherActivity.instance != null) {
			RCLauncherActivity.instance.runOnUiThread(new Runnable() {
				public void run() {
					RCLauncherActivity.instance.updateUI();
				}
			});
		}
	}
		
	private void startRepeatingTask() {
	    mStatusChecker.run(); 
	  }

	private void stopRepeatingTask() {
	    mHandler.removeCallbacks(mStatusChecker);
	}

	@Override
	public void onNewEvent(String event) {

	}
}
