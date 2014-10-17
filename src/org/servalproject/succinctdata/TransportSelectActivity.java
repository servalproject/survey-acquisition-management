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
	private byte [] succinctData = null;
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

		succinctData = intent.getByteArrayExtra("org.servalproject.succinctdata.SUCCINCT");
		xmlData = intent.getStringExtra("org.servalproject.succinctdata.XML");
		formname = intent.getStringExtra("org.servalproject.succinctdata.FORMNAME");
		formversion = intent.getStringExtra("org.servalproject.succinctdata.FORMVERSION");

		mButton_cell = (Button) findViewById(R.id.transport_select_cellulardata);
		mButton_cell.setOnClickListener(this);
		mButton_cell.setText("WiFi/Cellular data (" + xmlData.length() + " bytes)");

		int len = android.util.Base64.encodeToString(succinctData, android.util.Base64.NO_WRAP).length();       

		mButton_sms = (Button) findViewById(R.id.transport_select_sms);
		mButton_sms.setOnClickListener(this);
		mButton_sms.setText("SMS (" + len + " bytes)");

		mButton_inreach = (Button) findViewById(R.id.transport_select_inreach);
		mButton_inreach.setOnClickListener(this);
		mButton_inreach.setText("inReach(satellite) (" + len + " bytes)");

		mButton_cancel = (Button) findViewById(R.id.transport_cancel);
		mButton_cancel.setOnClickListener(this);

	}

	public void onClick(View view) {

		Intent mIntent;

		final String smstext = android.util.Base64.encodeToString(succinctData, android.util.Base64.NO_WRAP);;		

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
			Thread thread = new Thread(new Runnable(){

				Button button = mButton_cell;
				Button cancelButton = mButton_cancel;
				int len = xmlData.length();
				TransportSelectActivity activity = me;

				@Override
				public void run() {
					try {
						// XXX make configurable!
						String url = "http://serval1.csem.flinders.edu.au/succinctdata/upload.php";

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
			break;
		case R.id.transport_select_sms:
			// Send SMS
			// Now also consider sending by SMS

			mButton_sms.setBackgroundColor(0xffffff00);
			mButton_sms.setText("Attempting to send by SMS");

			if (smsnumber != null) try {
				Intent sentIntent = new Intent("SUCCINCT_DATA_SMS_SEND_STATUS");
				/*Create Pending Intents*/
				PendingIntent sentPI = PendingIntent.getBroadcast(
						getApplicationContext(), 0, sentIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);

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
							mButton_sms.setText("Sent " + smstext.length() + " bytes.");
							mButton_cancel.setText("Done");
						}
					}
				}, new IntentFilter("SUCCINCT_DATA_SMS_SEND_STATUS"));

				int result = sendSms(smsnumber,smstext,sentPI);
				if (result!=0) {
					mButton_sms.setBackgroundColor(0xffff0000);
					mButton_sms.setText("Failed to send by SMS. Touch to retry.");
				}
			} catch (Exception e) {
				// Now tell the user it has happened					
				mButton_sms.setBackgroundColor(0xffff0000);
				mButton_sms.setText("Failed to send by SMS. Touch to retry.");	            	
			}				
			break;
		case R.id.transport_select_inreach:
			// Send intent to inReach
			InReachManager manager = LauncherActivity.me.getService().getManager();
	        final OutboundMessage message = new OutboundMessage();
	        message.setAddressCode(OutboundMessage.AC_FreeForm);
	        message.setMessageCode(OutboundMessage.MC_FreeTextMessage);
	        // Set message identifier to first few bytes of hash of data
	        int ms_messageIdentifier = 0;
	        try {
	        	MessageDigest md;
				md = MessageDigest.getInstance("SHA-1");
		        md.update(smstext.getBytes("iso-8859-1"), 0, smstext.length());
		        byte[] sha1hash = md.digest();
		        ms_messageIdentifier = sha1hash[0] + (sha1hash[1]<<8) + (sha1hash[2]<<16)+ (sha1hash[3]<<24);
			} catch (Exception e) {
				Random r = new Random();
				int i1 = r.nextInt(1000000000);
				ms_messageIdentifier = i1;
			}
	        message.setIdentifier(ms_messageIdentifier);
	        message.addAddress(smsnumber);
	        message.setText(smstext);
	        
	        // queue the message for sending
	        if (!manager.sendMessage(message))
	        {
	        	// Failed
	        	mButton_inreach.setText("Failed to send by inReach. Touch to retry.");
		        mButton_inreach.setBackgroundColor(0xffff0000);
	        }
	        else
	        {
	        	// Success
	        	mButton_inreach.setText("Queued " + smstext.length() + " bytes.");
	        	mButton_inreach.setBackgroundColor(0xff00ff60);
	        }			
			break;
		case R.id.transport_cancel:
			finish();
			break;
		}
	}

	private int sendSms(String phonenumber,String message,PendingIntent p)
	{
		SmsManager manager = SmsManager.getDefault();
		try {
			manager.sendTextMessage(phonenumber, null, message,
					p, null);
			return 0;
		} catch (Exception e) {
			return -1;
		}
	}

	private int sendInReach(String message,PendingIntent p)
	{
		return 0;
	}
}
