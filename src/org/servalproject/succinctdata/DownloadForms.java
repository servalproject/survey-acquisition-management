package org.servalproject.succinctdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
		
		label_action.setText("Preparing HTTP request");
		
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
								label.setText("Downloading ...");
								// XXX save form file
								// XXX launch activity to install new forms 
								// finish();
							}
						}
					});

					// Prepare to write data to file, and tell progress bar what we are doing.
					InputStream input = response.getEntity().getContent();
					final long length = response.getEntity().getContentLength();
					activity.runOnUiThread(new Runnable() {						
						public void run() {
							progress_bar.setMax((int)length);
							progress_bar.setProgress(0);
							progress_bar.setIndeterminate(false);
							progress_bar.postInvalidate();
						}
					});
					
					try {
						String mConfigPath = Environment.getExternalStorageDirectory().getPath();
						mConfigPath += getString(R.string.system_file_path_configs);
					    final File file = new File(mConfigPath, "default.succinct.config");
					    final OutputStream output = new FileOutputStream(file);
					    int bytes = 0;
					    try {
					        try {
					            final byte[] buffer = new byte[16384];
					            int read;

					            while ((read = input.read(buffer)) != -1) {
					                output.write(buffer, 0, read);
					                bytes += read;
					                final int readBytes = bytes;
					                activity.runOnUiThread(new Runnable() {						
										public void run() {
											progress_bar.setProgress(readBytes);
											label.setText("Downloading ("+readBytes+" bytes)");
											progress_bar.postInvalidate();
											label.postInvalidate();
										}
									});
					            }
					            output.flush();
					            
					            // All done
					            activity.runOnUiThread(new Runnable() {
									public void run() {
										label.setText("Download succeeded.");
										button.setBackgroundColor(0xff00ff60);
										progress_bar.setVisibility(android.view.View.GONE);
									}
								}); 
					        } finally {
					            output.close();
					        }
					    } catch (Exception e) {
							activity.runOnUiThread(new Runnable() {
								public void run() {
									label.setText("Failed (download error?).");
									button.setBackgroundColor(0xffff0000);
									progress_bar.setVisibility(android.view.View.GONE);
								}
							});					        
					    }
					} finally {
					    input.close();
					}
					

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
