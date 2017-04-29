package org.servalproject.succinctdata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.magdaaproject.sam.InReachMessageHandler;
import org.magdaaproject.sam.LauncherActivity;

import org.servalproject.sam.R;

import com.delorme.inreachcore.InReachManager;
import com.delorme.inreachcore.OutboundMessage;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class TransportSelectActivity extends Activity implements OnClickListener {

	private String xmlData = null;
	private String [] succinctData = null;
	private String formname = null;
	private String formversion = null;

	private Button mButton_cell = null;
	private Button mButton_sms = null;
	private Button mButton_inreach = null;
	private Button mButton_cancel = null;

	final private TransportSelectActivity me = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transport_select_layout);

		Intent intent  = getIntent();

		succinctData = intent.getStringArrayExtra("org.servalproject.succinctdata.SUCCINCT");
		xmlData = intent.getStringExtra("org.servalproject.succinctdata.XML");
		formname = intent.getStringExtra("org.servalproject.succinctdata.FORMNAME");
		formversion = intent.getStringExtra("org.servalproject.succinctdata.FORMVERSION");

		mButton_cell = (Button) findViewById(R.id.transport_select_cellulardata);
		mButton_cell.setOnClickListener(this);
		mButton_cell.setText("WiFi/Cellular data (" + xmlData.length() + " bytes)");

		int len = 0;
		int messages = succinctData.length;
		for(int i=0;i<messages;i++) len+=succinctData[i].length();

		mButton_sms = (Button) findViewById(R.id.transport_select_sms);
		mButton_sms.setOnClickListener(this);
		mButton_sms.setText("SMS (" + len + " bytes, " + messages + "messages)");

		mButton_inreach = (Button) findViewById(R.id.transport_select_inreach);
		mButton_inreach.setOnClickListener(this);
		mButton_inreach.setText("inReach(satellite) (" + len + " bytes, " + messages + "messages)");

		mButton_cancel = (Button) findViewById(R.id.transport_cancel);
		mButton_cancel.setOnClickListener(this);

	}

	public void onClick(View view) {

		Intent mIntent;

		final String[] smstexts = succinctData;		

		String smsnumber = null;
		try {
			String smsnumberfile = 
					Environment.getExternalStorageDirectory()
					+ getString(R.string.system_file_path_succinct_specification_files_path)
					+ formname + "." + formversion + ".sms";
			smsnumber = new Scanner(new File(smsnumberfile)).useDelimiter("\\Z").next();
		} catch (Exception e) {

		}

		// determine which button was touched
		switch(view.getId()){
		case R.id.transport_select_cellulardata:
			// Push XML by HTTP
			// make button yellow while attempting to send
			mButton_cell.setBackgroundColor(0xffffff00);
			mButton_cell.setText("Attempting to send by WiFi/cellular");			
			sendViaCellular(xmlData);
			break;
		case R.id.transport_select_sms:
			// Send SMS
			// Now also consider sending by SMS

			mButton_sms.setBackgroundColor(0xffffff00);
			mButton_sms.setText("Attempting to send by SMS");
			sendViaSMS(smsnumber,succinctData);
			break;
		case R.id.transport_select_inreach:
			// Send intent to inReach
			mButton_inreach.setBackgroundColor(0xffffff00);
			mButton_inreach.setText("Attempting to send by inReach");
			sendViaInReach(smsnumber,succinctData);				        
			break;
		case R.id.transport_cancel:
			finish();
			break;
		}
	}

	private int sendViaSMS(String smsnumber,final String[] succinctData)
	{
		if (smsnumber != null) {
			try {		
				Intent sentIntent = new Intent("SUCCINCT_DATA_SMS_SEND_STATUS");
				/*Create Pending Intents*/
				PendingIntent sentPI = PendingIntent.getBroadcast(
						getApplicationContext(), 0, sentIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				// XXX - We should register a single instance of this receiver onResume, and destroy it in onPause
				// XXX - getResultCode() is probably not returning what we think it is, instead giving the result
				// of the activity, not of the intent.
				getApplicationContext().registerReceiver(new BroadcastReceiver() {
					public void onReceive(Context context, Intent intent) {
						String result = "";
						int colour = 0xffff0000;

						switch (getResultCode()) {
						case Activity.RESULT_OK:
							result = "Succinct Data Successfully Sent";
							colour = 0xff00ff60;
							break;
						case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
							result = "Transmission failed";
							break;
						case SmsManager.RESULT_ERROR_RADIO_OFF:
							result = "Radio off";
							break;
						case SmsManager.RESULT_ERROR_NULL_PDU:
							result = "No PDU defined";
							break;
						case SmsManager.RESULT_ERROR_NO_SERVICE:
							result = "No service";
							break;
						}

						mButton_sms.setBackgroundColor(colour);
						if (colour==0xffff0000) {
							// sending failed
							mButton_sms.setText("Failed to send by SMS ("+result+"). Touch to retry.");
						} else {
							// sending succeeded
							mButton_sms.setText("Sent " + succinctData.length + " messages.");
							mButton_cancel.setText("Done");
						}
					}
				}, new IntentFilter("SUCCINCT_DATA_SMS_SEND_STATUS"));

				int result =sendSMS(smsnumber,succinctData,sentPI);
				if (result!=0) {
					mButton_sms.setBackgroundColor(0xffff0000);
					mButton_sms.setText("Failed to send by SMS. Touch to retry.");
					return -1;
				}
			} catch (Exception e) {
				// Now tell the user it has happened					
				mButton_sms.setBackgroundColor(0xffff0000);
				mButton_sms.setText("Failed to send by SMS. Touch to retry.");
				return -1;
			}	
			return 0;
		} else return -1;
	}

	private int sendSMS(String smsnumber,String [] succinctData,PendingIntent p) 
	{
		SmsManager manager = SmsManager.getDefault();
		try {
			for(int i=0;i<succinctData.length;i++)
				manager.sendTextMessage(smsnumber, null, succinctData[i],
						p, null);
			return 0;
		} catch (Exception e) {
			return -1;
		}
	}

	private int sendViaInReach(String phoneNumber, final String [] succinctData)
	{
		if (phoneNumber != null) {
			try {
				Intent sentIntent = new Intent("SUCCINCT_DATA_INREACH_SEND_STATUS");
				/*Create Pending Intents*/
				PendingIntent sentPI = PendingIntent.getBroadcast(
						getApplicationContext(), 0, sentIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				// XXX - We should register a single instance of this receiver onResume, and destroy it in onPause
				// XXX - getResultCode() is probably not returning what we think it is, instead giving the result
				// of the activity, not of the intent.
				getApplicationContext().registerReceiver(new BroadcastReceiver() {
					public void onReceive(Context context, Intent intent) {
						String result = "";
						int colour = 0xffff0000;

						switch (getResultCode()) {
						case Activity.RESULT_OK:
							result = "Succinct Data Successfully Sent";
							colour = 0xff00ff60;
							break;
						case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
							result = "Transmission failed";
							break;
						case SmsManager.RESULT_ERROR_NO_SERVICE:
							result = "No service";
							break;
						}
						mButton_inreach.setBackgroundColor(colour);
						if (colour==0xffff0000) {
							// sending failed
							mButton_inreach.setText("Failed to send by inReach ("+result+"). Touch to retry.");
						} else {
							// sending succeeded
							mButton_inreach.setText("Sent " + succinctData.length + " messages.");
							mButton_cancel.setText("Done");
						}
					}
				}, new IntentFilter("SUCCINCT_DATA_INREACH_SEND_STATUS"));

				int result = sendInReach(phoneNumber,succinctData,sentPI);
				if (result!=0) {
					mButton_inreach.setBackgroundColor(0xffff0000);
					mButton_inreach.setText("Failed to send by inReach. Touch to retry.");
					return -1;
				}
			} catch (Exception e) {
				mButton_inreach.setBackgroundColor(0xffff0000);
				mButton_inreach.setText("Failed to send by inReach. Touch to retry.");
				return -1;    
			}
			return 0;
		} else return -1;
	}

	private int sendInReach(String phonenumber, String [] succinctData, PendingIntent p)
	{
		InReachManager manager = InReachMessageHandler.getInstance().getService().getManager();
		for(int i=0;i<succinctData.length;i++) {
			final OutboundMessage message = new OutboundMessage();
			message.setAddressCode(OutboundMessage.AC_FreeForm);
			message.setMessageCode(OutboundMessage.MC_FreeTextMessage);
			message.setTrackInterval(message.getTrackInterval());
			// Set message identifier to first few bytes of hash of data
			int ms_messageIdentifier = 0;
			try {
				MessageDigest md;
				md = MessageDigest.getInstance("SHA-1");
				md.update(succinctData[0].getBytes("iso-8859-1"), 0, succinctData[0].length());
				byte[] sha1hash = md.digest();
				ms_messageIdentifier = sha1hash[0] + (sha1hash[1]<<8) + (sha1hash[2]<<16)+ (sha1hash[3]<<24);
			} catch (Exception e) {
				Random r = new Random();
				int i1 = r.nextInt(1000000000);
				ms_messageIdentifier = i1;
			}
			message.setIdentifier(ms_messageIdentifier);
			message.addAddress(phonenumber);
			message.setText(succinctData[i]);

			// queue the message for sending
			if (!manager.sendMessage(message))
			{
				// Failed
				return -1;
			}
			
		}        
		return 0;
	}

	private int sendViaCellular(final String xmlData)
	{
		Thread thread = new Thread(new Runnable(){

			Button button = mButton_cell;
			Button cancelButton = mButton_cancel;
			int len = xmlData.length();
			TransportSelectActivity activity = me;

			@Override
			public void run() {
				try {
					// XXX make configurable!
					String url = getResources().getString(R.string.record_upload_url);

					HttpClient httpclient = new DefaultHttpClient();

					HttpPost httppost = new HttpPost(url);

					InputStream stream = new ByteArrayInputStream(xmlData.getBytes());
					InputStreamEntity reqEntity = new InputStreamEntity(stream, -1);
					reqEntity.setContentType("text/xml");
					reqEntity.setChunked(true); // Send in multiple parts if needed						
					httppost.setEntity(reqEntity);
					HttpResponse response = httpclient.execute(httppost);
					// Do something with response...
					final int httpStatus = response.getStatusLine().getStatusCode();
					activity.runOnUiThread(new Runnable() {
						public void run() {
							if (httpStatus != 200 ) {
								// request failed - make red
								button.setBackgroundColor(0xffff0000);
								button.setText("Failed (HTTP status " + httpStatus + "). Touch to retry.");
							} else {            	    					
								// request succeeded - make green/blue for colour blind people
								button.setBackgroundColor(0xff00ff60);
								button.setText("Sent " + len + " bytes.");   
								cancelButton.setText("Done");
							}
						}
					});            	    		
				} catch (Exception e) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							button.setBackgroundColor(0xffff0000);
							button.setText("Failed (no internet connection?). Touch to retry.");
						}
					});
				}
			}
		});
		thread.start();
		return 0;
	}
}
