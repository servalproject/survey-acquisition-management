package org.servalproject.succinctdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.magdaaproject.sam.config.BundleConfig;
import org.magdaaproject.sam.config.ConfigException;
import org.magdaaproject.sam.config.ConfigLoaderTask;
import org.magdaaproject.sam.config.FormVerifyTask;
import org.magdaaproject.sam.content.CategoriesContract;
import org.magdaaproject.sam.content.ConfigsContract;
import org.magdaaproject.sam.content.FormsContract;
import org.odk.collect.FormsProviderAPI;
import org.odk.collect.InstanceProviderAPI;
import org.servalproject.sam.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

public class DownloadForms extends FragmentActivity implements OnClickListener {

	final private DownloadForms me = this;
	Button mButton_cancel = null;
	TextView label_action = null;
	TextView error_label = null;
	ProgressBar progress = null;
		
	private ContentResolver contentResolver = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.downloading_forms);
		
		mButton_cancel = (Button) findViewById(R.id.download_cancel);
		mButton_cancel.setOnClickListener(this);
		label_action = (TextView) findViewById(R.id.download_action);
		error_label = (TextView) findViewById(R.id.error_message);
		progress = (ProgressBar) findViewById(R.id.download_progress);
		
		label_action.setText("Preparing HTTP request");
		
		// load any existing config parameters
		contentResolver = getContentResolver();
		
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
							progress_bar.postInvalidate();
						}
					});
					
					try {
						String mConfigPath = Environment.getExternalStorageDirectory().getPath();
						mConfigPath += getString(R.string.system_file_path_configs);
						new File(mConfigPath).mkdirs();
					    final File file = new File(mConfigPath, "default.succinct.config");
					    final OutputStream output = new FileOutputStream(file);
					    int bytes = 0;
					    try {
					        try {
					            final byte[] buffer = new byte[16384];
					            int read;

					            Looper.prepare();
					            
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
					            
					            // Finished downloading, so inform user					            
					            activity.runOnUiThread(new Runnable() {
					            	Activity activity = me;
									public void run() {
										label.setText("Configuring, please wait ...");
										button.setBackgroundColor(0xffffff00);
										progress_bar.setVisibility(android.view.View.GONE);
									}
					            });
									
					            // Now extract and load new configuration
					            // (wait for completion)
					    		new ConfigLoaderTask(progress, label, activity).execute().get();					    						    		
					    		
					            // Finished downloading, so inform user					            
					            activity.runOnUiThread(new Runnable() {
					            	Activity activity = me;
									public void run() {
										label.setText("Verifying, please wait ...");
										button.setBackgroundColor(0xffffff00);
										progress_bar.setVisibility(android.view.View.GONE);
									}
					            });

					    		// Verify forms (wait for completion)
					    		new FormVerifyTask(progress, label, activity).execute().get();

								activity.runOnUiThread(new Runnable() {
									public void run() {
										label.setText("Launching ODK ...");
										button.setBackgroundColor(0xffffff00);
										progress_bar.setVisibility(android.view.View.GONE);
										activity.launchOdkViaDialog();
										// Intent intent = new Intent(activity, org.magdaaproject.sam.LauncherActivity.class);
										// startActivity(intent);
										// finish();
									}
								});					        
					    										 
					        } finally {
					            output.close();
					        }
					    } catch (final Exception e) {
							activity.runOnUiThread(new Runnable() {
								public void run() {
									label.setText("Failed (download error?).");
									error_label.setText(e.toString());
									button.setBackgroundColor(0xffff0000);
									progress_bar.setVisibility(android.view.View.GONE);
								}
							});					        
					    }
					} finally {
					    input.close();
					}
					

				} catch (final Exception e) {
					Log.d("succinctdata",e.toString());
					activity.runOnUiThread(new Runnable() {
						public void run() {
							label.setText("Failed (no internet connection?).");
							error_label.setText(e.toString());
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
  
	/**
	 * method used to import new config values 
	 * @param newConfig a config bundle containing the new values
	 * @return true on success, false on failure
	 * @throws ConfigException if an error is detected in the config
	 */
	public void importNewConfig(BundleConfig newConfig) throws ConfigException {
		
		// build the list of values
		ContentValues mValues = new ContentValues();
		mValues.put(ConfigsContract.Table.TITLE, newConfig.getMetadataValue("title"));
		mValues.put(ConfigsContract.Table.DESCRIPTION, newConfig.getMetadataValue("description"));
		mValues.put(ConfigsContract.Table.VERSION, newConfig.getMetadataValue("version"));
		mValues.put(ConfigsContract.Table.AUTHOR, newConfig.getMetadataValue("author"));
		mValues.put(ConfigsContract.Table.AUTHOR_EMAIL, newConfig.getMetadataValue("email"));
		mValues.put(ConfigsContract.Table.GENERATED_DATE, newConfig.getMetadataValue("generated"));
		
		// insert the values
		contentResolver.insert(ConfigsContract.CONTENT_URI, mValues);
		
		// add the categories
		ArrayList<String[]> mCategories = newConfig.getCategories();
		
		for(String[] mElements: mCategories) {
			mValues = new ContentValues();
			
			mValues.put(CategoriesContract.Table.CATEGORY_ID, mElements[0]);
			mValues.put(CategoriesContract.Table.TITLE, mElements[1]);
			mValues.put(CategoriesContract.Table.DESCRIPTION, mElements[2]);
			mValues.put(CategoriesContract.Table.ICON, mElements[3]);
			
			contentResolver.insert(CategoriesContract.CONTENT_URI, mValues);
		}
		
		// add the forms
		ArrayList<String[]> mForms = newConfig.getForms();
		
		for(String[] mElements: mForms) {
			
			mValues = new ContentValues();
			
			mValues.put(FormsContract.Table.FORM_ID, mElements[0]);
			mValues.put(FormsContract.Table.CATEGORY_ID, mElements[1]);
			mValues.put(FormsContract.Table.TITLE, mElements[2]);
			mValues.put(FormsContract.Table.XFORMS_FILE, mElements[3]);
			
			contentResolver.insert(FormsContract.CONTENT_URI, mValues);
		}
	}
	
	/**
	 * empty the ODK databases
	 */
	public void emptyOdkDatabases() {
		
		try {
			contentResolver.delete(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null);
			contentResolver.delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null);
		} catch (SQLiteException e) {
			Log.w("SuccinctData", "error thrown while trying to empty ODK database", e);
		}
	}
	
	/**
	 * clean the MaGDAA SAM database
	 */
	public void cleanDatabase() {
		
		contentResolver.delete(ConfigsContract.CONTENT_URI, null, null);
		contentResolver.delete(FormsContract.CONTENT_URI, null, null);
		contentResolver.delete(CategoriesContract.CONTENT_URI, null, null);
	}
	
	/**
	 * undertake the validate forms task once the config has been loaded
	 */
	public void verifyForms() {
		
		// validate the installed forms
		ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.config_manager_ui_progress_bar);
		mProgressBar.setVisibility(View.VISIBLE);
		
		TextView mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_progress);
		mTextView.setVisibility(View.VISIBLE);
		
		
	}
	
	/**
	 * start the ODK form view list activity
	 */
	public void launchOdk() {
		
		// build an intent to launch the form
		Intent mIntent = new Intent();
		mIntent.setAction("android.intent.action.VIEW");
		mIntent.addCategory("android.intent.category.DEFAULT");
		mIntent.setComponent(new ComponentName("org.odk.collect.android","org.odk.collect.android.activities.FormChooserList"));
		
		// launch the form
		startActivityForResult(mIntent, 0);
		
	}
	
	/**
	 * confirm launching ODK to finalise installation
	 */
	public void launchOdkViaDialog() {
		
		DialogFragment newFragment = LaunchOdkDialog.newInstance(
				 getString(R.string.config_manager_ui_dialog_confirm_odk_title),
				 String.format(getString(R.string.config_manager_ui_dialog_confirm_odk_message), getString(R.string.system_application_name)));
	     newFragment.show(getSupportFragmentManager(), "dialog");
	}

	public void refreshDisplay() {
		// TODO Called from Corey's form installation update code
		
	}

	/*
	 * dialog to confirm the user wishes to continue loading a configuration
	 */
	public static class LaunchOdkDialog extends DialogFragment {

		/*
		 * 
		 */
        public static LaunchOdkDialog newInstance(String title, String message) {
            
        	if(TextUtils.isEmpty(title) == true) {
    			throw new IllegalArgumentException("the title parameter is required");
    		}
    		
    		if(TextUtils.isEmpty(message) == true) {
    			throw new IllegalArgumentException("the message parameter is required");
    		}
    		
    		LaunchOdkDialog mObject = new LaunchOdkDialog();
    		
    		// build a new bundle of arguments
    		Bundle mBundle = new Bundle();
    		mBundle.putString("title", title);
    		mBundle.putString("message", message);
    		
    		mObject.setArguments(mBundle);
    		
    		return mObject;
        }
        
        /*
    	 * (non-Javadoc)
    	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
    	 */
    	@Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
    		
    		if(savedInstanceState == null) {
    			savedInstanceState = getArguments();
    		}
    		
    		String mMessage = savedInstanceState.getString("message");
    		String mTitle = savedInstanceState.getString("title");
    		
    		// create and return the dialog
    		AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
    		
    		mBuilder.setMessage(mMessage)
    		.setCancelable(false)
    		.setTitle(mTitle)
    		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				((DownloadForms)getActivity()).launchOdk();
    			}
    		});
    		return mBuilder.create();
    	}
    }

	
}
