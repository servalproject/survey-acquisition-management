package org.servalproject.succinctdata;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.servalproject.sam.R;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadForms extends Activity implements OnClickListener {

	final private DownloadForms me = this;
	Button mButton_cancel = null;
	TextView label_action = null;
	ProgressBar progress = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.downloading_forms);
		
		mButton_cancel = (Button) findViewById(R.id.download_cancel);
		mButton_cancel.setOnClickListener(this);
		label_action = (TextView) findViewById(R.id.download_action);
		progress = (ProgressBar) findViewById(R.id.download_progress);
		
		Thread thread = new Thread(new Runnable(){

			DownloadForms activity = me;
			TextView label = label_action; 
			Button button = mButton_cancel;
			ProgressBar progress_bar = progress;
			
			@Override
			public void run() {
				try {
					// XXX make configurable!
					String url = "http://serval1.csem.flinders.edu.au/succinctdata/default.succinct.config";

					HttpClient httpclient = new DefaultHttpClient();

					HttpGet httpget = new HttpGet(url);

					HttpResponse response = httpclient.execute(httpget);
					// Do something with response...
					final int httpStatus = response.getStatusLine().getStatusCode();
					activity.runOnUiThread(new Runnable() {
												
						public void run() {
							if (httpStatus != 200 ) {
								// request failed - make red
								label.setText("Failed (HTTP status " + httpStatus + ").");
								button.setBackgroundColor(0xffff0000);
								progress_bar.setVisibility(android.view.View.GONE);
							} else {            	    					
								// request succeeded - make green/blue for colour blind people
								label.setText("Downloaded: installing new forms.");
								// XXX save form file
								// XXX launch activity to install new forms 
								// finish();
							}
						}
					});            	    		
				} catch (Exception e) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							label.setText("Failed (no internet connection?).");
							button.setBackgroundColor(0xffff0000);
							progress_bar.setVisibility(android.view.View.GONE);
						}
					});
				}
			}
		});
		thread.start();            	

		
	}
	
	public void onClick(View view) {
		// There is only one button: cancel
		// XXX - Stop any download currently in progress
		finish();
	}
	
}
