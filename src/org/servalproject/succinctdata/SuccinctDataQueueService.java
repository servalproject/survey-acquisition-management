package org.servalproject.succinctdata;

import java.io.File;
import java.util.Scanner;

import org.servalproject.sam.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SuccinctDataQueueService extends Service {

	private String SENT = "SMS_SENT";
	private String DELIVERED = "SMS_DELIVERED";

	public static int sms_tx_result = -1; 
	public static PendingIntent sms_tx_pintent = null;

	private SuccinctDataQueueDbAdapter db = null;
	Thread messageSenderThread = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// Create background thread that continuously checks for messages, and sends them if it can
		final Service theService = this;
		if (messageSenderThread == null) {
			messageSenderThread = new Thread(new Runnable() { public void run() { try {
				messageSenderLoop(theService);
			} catch (Exception e) {
				//	TODO Auto-generated catch block
				e.printStackTrace();
			} } });
			messageSenderThread.start();
		}         

		// Create pending intent and broadcast listener for SMS dispatch if not done 
		// already
		if (sms_tx_pintent == null) {	 
			sms_tx_pintent = PendingIntent.getBroadcast(this, 0,
					new Intent(SENT), 0);

			//---when the SMS has been sent---
			registerReceiver(new BroadcastReceiver(){
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					sms_tx_result=getResultCode();
				}
			}, new IntentFilter(SENT));
		}

		// Check if the passed intent has a message to queue
		try {
			String succinctData[] = intent.getStringArrayExtra("org.servalproject.succinctdata.SUCCINCT");
			//			String xmlData = intent.getStringExtra("org.servalproject.succinctdata.XML");
			String formname = intent.getStringExtra("org.servalproject.succinctdata.FORMNAME");
			String formversion = intent.getStringExtra("org.servalproject.succinctdata.FORMVERSION");

			// For each piece, create a message in the queue
			Log.d("SuccinctData","Opening queue database");
			if (db == null) {
				db = new SuccinctDataQueueDbAdapter(this);
				db.open();				
			}
			Log.d("SuccinctData","Opened queue database");
			if (succinctData != null) {
				for(int i = 0; i< succinctData.length;i ++) {
					String piece = succinctData[i];
					String prefix = piece.substring(0, 10);
					db.createQueuedMessage(prefix, piece,formname+"/"+formversion);
				}
				Intent i = new Intent("SD_MESSAGE_QUEUE_UPDATED");
				LocalBroadcastManager lb = LocalBroadcastManager.getInstance(this);
				lb.sendBroadcastSync(i);
			}

		} catch (Exception e) {
			String s = e.toString();
			Log.e("SuccinctDataqQueueService","Exception: " + s);
		}

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	// Detecting cellular service (SMS, not data)
	// This method by santacrab from:
	// http://stackoverflow.com/questions/6435861/android-what-is-the-correct-way-of-checking-for-mobile-network-available-no-da
	public static Boolean isSMSAvailable(Context appcontext) {       
		TelephonyManager tel = (TelephonyManager) appcontext.getSystemService(Context.TELEPHONY_SERVICE);       
		return ((tel.getNetworkOperator() != null && tel.getNetworkOperator().equals("")) ? false : true);      
	}

	// Detecting internet access by Alexandre Jasmin from:
	// http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
	public boolean isMobileInternetAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public int sendSMS(String smsnumber,String message)
	{
		SuccinctDataQueueService.sms_tx_result = 0xbeef;

		Intent sentIntent = new Intent(SENT);
		/*Create Pending Intents*/
		PendingIntent p = PendingIntent.getBroadcast(
				getApplicationContext(), 0, sentIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Dispatch SMS
		SmsManager manager = SmsManager.getDefault();
		
		try {
			manager.sendTextMessage(smsnumber, null, message, p, null);
		} catch (Exception e) {
			return -1;
		}
		
		// Then wait for pending intent to indicate delivery.
		// We catch the intent in this class, and then poll the result flag to
		// see what happened.
		// Give 60 seconds for the SMS to get sent		
		for(int i=0;i<60;i++) {
			if (SuccinctDataQueueService.sms_tx_result == 0xbeef)
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			else if ( SuccinctDataQueueService.sms_tx_result == Activity.RESULT_OK )
				return 0;				
		}

		return -1;
	}

	public void messageSenderLoop(Service s)
	{
		// XXX - This really is ugly. We should edge detect everything instead of
		// polling.
		Looper.prepare();
		SuccinctDataQueueDbAdapter db = new SuccinctDataQueueDbAdapter(this);
		db.open();

		String smsnumber = getString(R.string.succinct_data_sms_number);

		long next_timeout = 5000;
		
		while(true) {
			// Wait a little while before trying again
			try {
				Thread.sleep(next_timeout);
			} catch (Exception e) {
			} 

			// Get number of messages in database
			Cursor c = db.fetchAllMessages();
			if (c.getCount()==0) {
				// If no queued messages, wait only a few seconds
				next_timeout = 5000;
				continue;
			} 

			c.moveToFirst();
			while (c.isAfterLast() == false) {
				String prefix = c.getString(1);
				String piece = c.getString(4);

				// If data service is available, try to send messages that way
				boolean messageSent = false;
				if (false) {

					// Else, if SMS is available, try to send messages that way
				} else if (isSMSAvailable(s)) {
					if (sendSMS(smsnumber,piece) == 0) messageSent=true;
				} else if (false) {    		    
					// Else, if inReach is available, try to send messages that way
				}

				if (messageSent == true) {
					// Delete message from database
					db.delete(piece);
					Intent i = new Intent("SD_MESSAGE_QUEUE_UPDATED");
					LocalBroadcastManager lb = LocalBroadcastManager.getInstance(s);
					lb.sendBroadcastSync(i);
				}

				c.moveToNext();
			}
			
			// Check if we still have messages queued. If so, there is some problem
			// with sending them, so hold off for a couple of minutes before trying again.
			c = db.fetchAllMessages();
			if (c.getCount()==0) {
				// If no queued messages, wait only a few seconds
				next_timeout = 5000;
			} else {
			    next_timeout = 120000;
			}

		}    
	}
}
