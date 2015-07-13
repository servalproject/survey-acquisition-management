package org.servalproject.succinctdata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Random;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.magdaaproject.sam.InReachMessageHandler;
import org.magdaaproject.sam.RCLauncherActivity;
import org.servalproject.sam.R;

import com.delorme.inreachcore.InReachManager;
import com.delorme.inreachcore.OutboundMessage;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

class UploadFormSpecificationTask extends AsyncTask<String, Integer, Long>
{
	public static Handler handler = null;
	public static Context context = null;
	
    protected Long doInBackground(String... forms)
    {
    	for (int i = 0; i < forms.length ; i++)
    	{
    		String xmlForm = forms[i];
    		String resultMessage = "Unknown error while uploading Magpi form to Succinct Data server";
			
    		{    		
			// Upload form specification to Succinct Data server
			String url = "http://serval1.csem.flinders.edu.au/succinctdata/upload-form.php";

			HttpClient httpclient = new DefaultHttpClient();
			
			HttpPost httppost = new HttpPost(url);

			InputStream stream = new ByteArrayInputStream(xmlForm.getBytes());
			InputStreamEntity reqEntity = new InputStreamEntity(stream, -1);
			reqEntity.setContentType("text/xml");
			reqEntity.setChunked(true); // Send in multiple parts if needed						
			httppost.setEntity(reqEntity);
			int httpStatus = -1;
			try {
				HttpResponse response = httpclient.execute(httppost);
				httpStatus = response.getStatusLine().getStatusCode();
				if (httpStatus != 200 ) {
					resultMessage = "Failed to upload Magpi form to Succinct Data server: http result = " + httpStatus;
				}
				else {
					resultMessage = "Successfully uploaded Magpi form to Succinct Data server: http result = " + httpStatus;
					// Remember uploaded form so that we don't try to send it again after we have
					// done so once.
					SuccinctDataQueueDbAdapter db = new SuccinctDataQueueDbAdapter(UploadFormSpecificationTask.context);
					db.open();
					db.rememberThing(xmlForm);
					db.close();
				}
			} catch (Exception e) {
				resultMessage = "Failed to upload Magpi form to "
							+"Succinct Data server due to exception: " + e.toString();
				
			}
			// Do something with response...
			Log.d("succinctdata",resultMessage);
			
			final String finalResultMessage = resultMessage;
			if (handler == null) handler = new Handler();
			handler.post(new Runnable() {

				@Override
				public void run() {
						Toast.makeText(context, finalResultMessage, Toast.LENGTH_LONG).show();
				}
			});
    		}
    	}
        Long status = -1L;
        return status;
    }
}


public class SuccinctDataQueueService extends Service {

	private static final String SENT = "SMS_SENT";
	private static final String EXTRA_ROW = "ROWID";
	private static final String TAG = "SuccinctQueueService";
	private Thread messageSenderThread = null;
	private static final long maxSMSQueued=1;
	private static final long maxInreachQueued=1;
	public static final String ACTION_QUEUE_UPDATED = "SD_MESSAGE_QUEUE_UPDATED";
	public static SuccinctDataQueueService instance = null;

	private Handler handler = null;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(SENT) ) {
				long rowId = intent.getLongExtra(EXTRA_ROW, -1);
				if (rowId !=-1) {
					ContentValues values = new ContentValues();
					if (getResultCode() == Activity.RESULT_OK) {
						values.put(SuccinctDataQueueDbAdapter.COL_STATUS, SuccinctDataQueueDbAdapter.STATUS_SMS_SENT);
					}else{
						values.put(SuccinctDataQueueDbAdapter.COL_STATUS, SuccinctDataQueueDbAdapter.STATUS_SMS_FAILED);
					}
					db.update(rowId, values);
					// TODO schedule service again!
				}
			}
		}
	};

	private SuccinctDataQueueDbAdapter db;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		instance = this;

		//---when the SMS has been sent---
		registerReceiver(receiver, new IntentFilter(SENT));

		// Check if the passed intent has a message to queue
		try {
			String succinctData[] = intent.getStringArrayExtra("org.servalproject.succinctdata.SUCCINCT");
			String xmlData = intent.getStringExtra("org.servalproject.succinctdata.XML");
			String xmlForm = intent.getStringExtra("org.servalproject.succinctdata.XMLFORM");
			String formname = intent.getStringExtra("org.servalproject.succinctdata.FORMNAME");
			String formversion = intent.getStringExtra("org.servalproject.succinctdata.FORMVERSION");			
			
			if (db == null) {
				db = new SuccinctDataQueueDbAdapter(this);
				db.open();		
			}

			if (xmlForm != null && db.isThingNew(xmlForm) ) {
				UploadFormSpecificationTask.handler = handler;
				UploadFormSpecificationTask.context = getBaseContext();
				new UploadFormSpecificationTask().execute(xmlForm);								
			}
			
			
			// For each piece, create a message in the queue
			Log.d("SuccinctData","Opening queue database");
			Log.d("SuccinctData","Opened queue database");
			if (succinctData != null && !db.isThingNew(xmlData)) {

				for(int i = 0; i< succinctData.length;i ++) {
					String piece = succinctData[i];
					String prefix = piece.substring(0, 10);
					db.createQueuedMessage(prefix, piece,formname+"/"+formversion,xmlData);
				}
				db.rememberThing(xmlData);
				queueUpdated();
			}

		} catch (Exception e) {
			String s = e.toString();
			Log.e(TAG,"Exception: " + s);
		}

		// Create background thread that continuously checks for messages, and sends them if it can
		final Service theService = this;
		if (messageSenderThread == null) {
			messageSenderThread = new Thread(new Runnable() { public void run() { try {
				Looper.prepare();
				messageSenderLoop(theService);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			} } });
			messageSenderThread.start();
		}

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	private void queueUpdated() {
		Intent i = new Intent(ACTION_QUEUE_UPDATED);
		LocalBroadcastManager lb = LocalBroadcastManager.getInstance(this);
		lb.sendBroadcastSync(i);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	// Detecting cellular service (SMS, not data)
	// This method by santacrab from:
	// http://stackoverflow.com/questions/6435861/android-what-is-the-correct-way-of-checking-for-mobile-network-available-no-da
	public static Boolean isSMSAvailable(Context appcontext) {       
		TelephonyManager tel = (TelephonyManager) appcontext.getSystemService(Context.TELEPHONY_SERVICE);
		String network = tel.getNetworkOperator();
		return network != null && !network.equals("");
	}

	// Detecting internet access by Alexandre Jasmin from:
	// http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
	public boolean isInternetAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private boolean sendViaCellular(String succinctData)
	{
		// XXX make configurable!
		String url = "http://serval1.csem.flinders.edu.au/succinctdata/upload.php";

		HttpClient httpclient = new DefaultHttpClient();
		
		HttpPost httppost = new HttpPost(url);

		InputStream stream = new ByteArrayInputStream(succinctData.getBytes());
		InputStreamEntity reqEntity = new InputStreamEntity(stream, -1);
		reqEntity.setContentType("text/succinctdata");
		reqEntity.setChunked(true); // Send in multiple parts if needed						
		httppost.setEntity(reqEntity);
		int httpStatus = -1;
		try {
			HttpResponse response = httpclient.execute(httppost);
			httpStatus = response.getStatusLine().getStatusCode();
		} catch (Exception e) {
			return false;
		}
		// Do something with response...
		return httpStatus==200;
    }
	
	public boolean sendSMS(long rowId, String smsnumber, String message)
	{
		/* Create Pending Intent */
		Intent sentIntent = new Intent(SENT);
		sentIntent.putExtra(EXTRA_ROW, rowId);
		PendingIntent p = PendingIntent.getBroadcast(
				getApplicationContext(), 0, sentIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Dispatch SMS
		SmsManager manager = SmsManager.getDefault();
		
		try {
			manager.sendTextMessage(smsnumber, null, message, p, null);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private int sendInReach(String phonenumber, String succinctData)
	{
		int ms_messageIdentifier = 0;
		InReachManager manager = InReachMessageHandler.getInstance().getService().getManager();
		final OutboundMessage message = new OutboundMessage();
		message.setAddressCode(OutboundMessage.AC_FreeForm);
		message.setMessageCode(OutboundMessage.MC_FreeTextMessage);
		// Set message identifier to first few bytes of hash of data
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-1");
			md.update(succinctData.getBytes("iso-8859-1"), 0, succinctData.length());
			byte[] sha1hash = md.digest();
			ms_messageIdentifier = sha1hash[0] + (sha1hash[1]<<8) + (sha1hash[2]<<16)+ (sha1hash[3]<<24);
			if (ms_messageIdentifier == -1)
				ms_messageIdentifier = 0;
		} catch (Exception e) {
			return -1;
		}
		message.setIdentifier(ms_messageIdentifier);
		message.addAddress(phonenumber);
		message.setText(succinctData);

		// queue the message for sending
		if (!manager.sendMessage(message))
			// Failed
			return -1;
		return ms_messageIdentifier;
	}

	public void sendMessages(Context context){
		// Get number of messages in database
		long smsQueued=0;
		long inreachQueued=0;
		{
			Cursor c = db.getMessageCounts();
			try {
				while (c.moveToNext()) {
					String state = c.getString(0);
					long count = c.getLong(1);
					if (SuccinctDataQueueDbAdapter.STATUS_SMS_QUEUED.equals(state))
						smsQueued = count;
					else if (SuccinctDataQueueDbAdapter.STATUS_INREACH_QUEUED.equals(state))
						inreachQueued = count;
				}
			}finally{
				c.close();
			}
		}
		String smsnumber = getString(R.string.succinct_data_sms_number);

		Cursor c = db.fetchAllMessages();
		try {

			int cRowID = c.getColumnIndexOrThrow(SuccinctDataQueueDbAdapter.KEY_ROWID);
			int cPrefix = c.getColumnIndexOrThrow(SuccinctDataQueueDbAdapter.KEY_PREFIX);
			int cPiece = c.getColumnIndexOrThrow(SuccinctDataQueueDbAdapter.KEY_SUCCINCTDATA);
			int cXml = c.getColumnIndexOrThrow(SuccinctDataQueueDbAdapter.KEY_XMLDATA);
			int cStatus = c.getColumnIndexOrThrow(SuccinctDataQueueDbAdapter.COL_STATUS);
			int cInreachId = c.getColumnIndexOrThrow(SuccinctDataQueueDbAdapter.COL_INREACH_ID);

			while (c.moveToNext()) {
				long rowId = c.getLong(cRowID);
				String prefix = c.getString(cPrefix);
				String piece = c.getString(cPiece);
				String xml = c.getString(cXml);
				String status = c.getString(cStatus);
				String inreachId = c.getString(cInreachId);

				// If data service is available, try to send messages that way
				if (isInternetAvailable()) {
					if (!sendViaCellular(xml))
						break;
					db.delete(rowId);
					queueUpdated();
					continue;
				}

				// Else, if SMS is available, try to send messages that way
				if (status == null && isSMSAvailable(context)) {
					if (smsQueued >= maxSMSQueued)
						break;
					if (!sendSMS(rowId, smsnumber, piece))
						break;
					smsQueued++;
					ContentValues values = new ContentValues();
					values.put(SuccinctDataQueueDbAdapter.COL_STATUS, SuccinctDataQueueDbAdapter.STATUS_SMS_QUEUED);
					db.update(rowId, values);
					queueUpdated();
					continue;
				}

				// Else, if inReach is available, try to send messages that way
				if ((status == null || SuccinctDataQueueDbAdapter.STATUS_SMS_FAILED.equals(status)) && InReachMessageHandler.isInreachAvailable()) {
					if (inreachQueued >= maxInreachQueued)
						break;
					int id = sendInReach(smsnumber, piece);
					if (id == -1)
						break;
					inreachQueued++;
					ContentValues values = new ContentValues();
					values.put(SuccinctDataQueueDbAdapter.COL_STATUS, SuccinctDataQueueDbAdapter.STATUS_INREACH_QUEUED);
					values.put(SuccinctDataQueueDbAdapter.COL_INREACH_ID, Integer.toString(id));
					db.update(rowId, values);
					queueUpdated();
					continue;
				}
			}
		}finally{
			c.close();
		}
	}

	public void messageSenderLoop(Service s)
	{
		// XXX - This really is ugly. We should edge detect everything instead of
		// polling.
		instance = this;

		long next_timeout = 5000;
		
		while(true) {
			// Wait a little while before trying again
			try {
				Thread.sleep(next_timeout);
			} catch (Exception e) {
			} 
			sendMessages(s);
			// Check if we still have messages queued. If so, there is some problem
			// with sending them, so hold off for a couple of minutes before trying again.
			Cursor c = db.fetchAllMessages();
			if (c.getCount()==0) {
				// If no queued messages, wait only a few seconds
				next_timeout = 5000;
			} else {
			    next_timeout = 120000;
			}

		}    
	}
}
