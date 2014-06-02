/*
 * Copyright (C) 2013 The Serval Project
 * Portions Copyright (C) 2012, 2013 The MaGDAA Project
 *
 * This file is part of the Serval SAM Software, a fork of the MaGDAA SAM software
 * which is located here: https://github.com/magdaaproject/survey-acquisition-management
 *
 * Serval SAM Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/*
 * Copyright (C) 2012, 2013 The MaGDAA Project
 *
 * This file is part of the MaGDAA SAM Software
 *
 * MaGDAA SAM Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.magdaaproject.sam;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Scanner;

import org.magdaaproject.sam.adapters.SurveyFormsAdapter;
import org.magdaaproject.sam.content.FormsContract;
import org.magdaaproject.sam.fragments.BasicAlertDialogFragment;
import org.magdaaproject.sam.sharing.ShareViaRhizomeTask;
import org.odk.collect.FormsProviderAPI;
import org.servalproject.sam.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * view a list of survey forms associated with the event category
 */
public class SurveyFormsActivity extends FragmentActivity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	//private static final boolean sVerboseLog = true;
	private static final String sLogTag = "SurveyFormsActivity";
	
	/*
	 * private class level variables
	 */
	private ListView listView;
	private Cursor   cursor;
	private HashMap<String, Integer> odkData;
	private boolean shareViaRhizome;
	
	private String categoryId;
	private String categoryTitle;
	
	private boolean locationListening;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey_forms);
		
		// get the data from the calling intent
		String mData = getIntent().getStringExtra(LauncherActivity.INTENT_EXTRA_NAME);
		String[] mTokens = mData.split("\\|");
		categoryId = mTokens[0];
		categoryTitle = mTokens[1];
		
		// update the title of the activity
		TextView mTextView = (TextView) findViewById(R.id.survey_forms_ui_lbl_header);
		mTextView.setText(categoryTitle);
		
		// complete init of back button
		Button mButton = (Button) findViewById(R.id.general_ui_btn_back);
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SurveyFormsActivity.this.finish();
			}
			
		});

		// Setup update & view CSV button
		Button mCSVButton = (Button) findViewById(R.id.getcsv);
		mCSVButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String succinctPath = Environment.getExternalStorageDirectory().getPath()+
				getString(R.string.system_file_path_succinct_specification_files_path);
				String rxSpoolDir = Environment.getExternalStorageDirectory().getPath()+
						getString(R.string.system_file_path_succinct_data_rxspool_dir);
				String outputDir = Environment.getExternalStorageDirectory().getPath()+
						getString(R.string.system_file_path_succinct_data_output_dir);
				
				// Read any new SMS messages and put in the rxspool directory for processing
				Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
				cursor.moveToFirst();

				do{
				   try {
					   String msgData = "";
					   int idx = cursor.getColumnIndex("body");				   
					   msgData = cursor.getString(idx);

					   byte[] decodedBytes = android.util.Base64.decode(msgData,android.util.Base64.DEFAULT);
						byte[] b = MessageDigest.getInstance("MD5").digest(decodedBytes);
						String filename = String.format("%02x%02x%02x%02x%02x%02x.sd", b[0],b[1],b[2],b[3],b[4],b[5]);
						File dir = new File(Environment.getExternalStorageDirectory(),
										getString(R.string.system_file_path_succinct_data_rxspool_dir));
						File file = new File(dir, filename);
						// Write succinct data to file
						dir.mkdirs();
						dir = new File(Environment.getExternalStorageDirectory(),
								getString(R.string.system_file_path_succinct_data_output_dir));
						dir.mkdirs();
						
						FileOutputStream f = new FileOutputStream(file);
						f.write(decodedBytes);
						f.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();					
					}
				}while(cursor.moveToNext());
				
				
				Log.d("SAM","About to call smac");
				// org.servalproject.succinctdata.jni.updatecsv(succinctPath,rxSpoolDir,outputDir);
				
				try {
					  AssetManager assetManager = getAssets();
			          InputStream in = assetManager.open("smac");
			          File outDir = new File(getFilesDir().getPath()+ "/bin");
			          outDir.mkdirs();
			          File outFile = new File(getFilesDir().getPath()+ "/bin", "smac");
			          FileOutputStream out = new FileOutputStream(outFile);
			          int len;
			          byte[] buff = new byte[8192];
			  		  while ((len = in.read(buff)) > 0) {
						out.write(buff, 0, len);
					  }
			          in.close();
			          in = null;
			          out.flush();
			          out.close();
			          out = null;
			          outFile.setExecutable(true);			          
			        } catch(IOException e) {
			            Log.e("tag", "Failed to copy asset file smac", e);
			        }       
				
				String cmd = getFilesDir().getPath()+ "/bin/smac"; 
				Process proc;
				try {
					proc = new ProcessBuilder(cmd,"recipe", "decompress", succinctPath,rxSpoolDir,outputDir).redirectErrorStream(true).start();
					DataInputStream in = new DataInputStream(proc.getInputStream());
					OutputStream out = proc.getOutputStream();
					proc.waitFor();
				} catch (Throwable e) {
					Log.e("tag", "Failed to run smac", e);
			}
				
				
				// Now open chooser to pick a file manager to view the directory
				// This doesn't actually work with most file managers.
				Intent intent = new Intent();
				File file = new File(outputDir);
				Uri uri = Uri.fromFile(file);				
				// intent.setAction(android.content.Intent.ACTION_VIEW);				
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(uri,"application/file");
				startActivityForResult(Intent.createChooser(intent,"Open folder"), 0);
		        // intent.setData(uri);
		        // intent.setDataAndType(uri, "*/*");
				// intent.setDataAndType(uri,"vnd.android.cursor.item/file");
				// intent.putExtra(Intent.EXTRA_TITLE,"Succinct Data Output");
		        // startActivity(intent);

				// More usefully for demo, show some CSV output from the first CSV file in the
				// output directory.
				TextView csvTextView = (TextView) findViewById(R.id.survey_forms_ui_csv_data);
				
				File[] csvFiles = new File(outputDir,"csv").listFiles();
				String csvData = "";
				for (File csvFile : csvFiles) {
					String fileContents = "";
					try {
						fileContents = new Scanner(csvFile).useDelimiter("\\Z").next();
					} catch (Exception e) {
					}
					csvData = csvData + fileContents;					
				}
				csvTextView.setText(csvData);
				csvTextView.setMovementMethod(new ScrollingMovementMethod());
			}
			
		});

		Button mXMLButton = (Button) findViewById(R.id.getxml);
		mXMLButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String succinctPath = Environment.getExternalStorageDirectory().getPath()+
				R.string.system_file_path_succinct_specification_files_path;
				String rxSpoolDir = Environment.getExternalStorageDirectory().getPath()+
						R.string.system_file_path_succinct_data_rxspool_dir;
				String outputDir = Environment.getExternalStorageDirectory().getPath()+
						R.string.system_file_path_succinct_data_output_dir;
				
				org.servalproject.succinctdata.jni.updatecsv(succinctPath,rxSpoolDir,outputDir);
				
				// Now open chooser to pick a file manager to view the directory
				Intent intent = new Intent();
				File file = new File(outputDir);
				Uri uri = Uri.fromFile(file);				
				intent.setAction(android.content.Intent.ACTION_VIEW);
		        intent.setData(uri);
		        intent.setDataAndType(uri, "text/xml");
		        startActivity(Intent.createChooser(intent, "Open folder"));		        
			}
			
		});

		
		// determine if we're sharing saved instance data
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		if(mPreferences.getBoolean("preferences_sharing_bundles", true) == true) {
			// we're undertaking some sort of sharing
			shareViaRhizome = mPreferences.getBoolean("preferences_sharing_rhizome", true);
		}
		
		// get a reference to the grid view
		listView = (ListView)findViewById(R.id.survey_ui_list_surveys);
		
		// setup the cursor
		String[] mProjection = new String[4];
		mProjection[0] = FormsContract.Table._ID;
		mProjection[1] = FormsContract.Table.TITLE;
		mProjection[2] = FormsContract.Table.XFORMS_FILE;
		mProjection[3] = FormsContract.Table.USES_LOCATION;
		
		String mSelection = FormsContract.Table.CATEGORY_ID + " = ?";
		mSelection += " AND " + FormsContract.Table.FOR_DISPLAY + " = ?";
		
		String[] mSelectionArgs = new String[2];
		mSelectionArgs[0] = categoryId;
		mSelectionArgs[1] = Integer.toString(FormsContract.YES);
		
		// TODO have a preference for different sort order
		String mSortOrder = FormsContract.Table.FORM_ID + " ASC";
		
		// get the data
		ContentResolver mContentResolver = getContentResolver();
		
		cursor = mContentResolver.query(
				FormsContract.CONTENT_URI,
				mProjection,
				mSelection,
				mSelectionArgs,
				mSortOrder);
		
		// check to make sure some data is there
		if(cursor == null || cursor.getCount() == 0) {
			
			// hide the grid view, show the label
			listView.setVisibility(View.GONE);
			
			mTextView = (TextView) findViewById(R.id.survey_forms_ui_lbl_no_forms);
			mTextView.setVisibility(View.VISIBLE);
			
			return;
			
		}
		
		// setup the cursor to view map related code
		String[] mColumns = new String[1];
		mColumns[0] = FormsContract.Table.TITLE;
		
		int[] mViews = new int[1];
		mViews[0] = R.id.list_view_surveys_button;
		
		SurveyFormsAdapter mAdapter = new SurveyFormsAdapter(
				this,
				R.layout.list_view_surveys,
				cursor,
				mColumns,
				mViews,
				0);
		
		listView.setAdapter(mAdapter);
		
		// get the ODK data
		mProjection = new String[2];
		mProjection[0] = FormsProviderAPI.FormsColumns._ID;
		mProjection[1] = FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
		
		Cursor mOdkCursor = null;
		
		try {
			mOdkCursor = mContentResolver.query(
					FormsProviderAPI.FormsColumns.CONTENT_URI,
					mProjection,
					null,
					null,
					null
					);
		} catch (android.database.sqlite.SQLiteDiskIOException e) {
			
			Log.e(sLogTag, "unable to access the ODK database", e);
			
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.surveys_ui_dialog_odk_database_error_title),
					getString(R.string.surveys_ui_dialog_odk_database_error_message));

			mAlert.show(getSupportFragmentManager(), "odk-database-error");
			return;
			
		} catch (SQLException e) {
			Log.e(sLogTag, "unable to access the ODK database", e);
			
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.surveys_ui_dialog_odk_database_error_title),
					getString(R.string.surveys_ui_dialog_odk_database_error_message));

			mAlert.show(getSupportFragmentManager(), "odk-database-error");
			return;
		}
		
		// check to make sure data was found
		if(mOdkCursor == null || mOdkCursor.getCount() == 0) {
			
			Log.e(sLogTag, "no data from the ODK database");
			
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.surveys_ui_dialog_odk_database_error_title),
					getString(R.string.surveys_ui_dialog_odk_database_error_message));

			mAlert.show(getSupportFragmentManager(), "odk-database-error");
			return;
		}
		
		odkData = new HashMap<String, Integer>();
		
		mTokens = null;
		
		while(mOdkCursor.moveToNext()) {
			
			mTokens = mOdkCursor.getString(1).split("/");
			
			odkData.put(
					mTokens[mTokens.length -1], 
					mOdkCursor.getInt(0)
				);
		}
		
		mOdkCursor.close();
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View view) {
		
		// get the details of the form that we want to load
		cursor.moveToPosition((Integer) view.getTag());
		
		if(odkData == null) {
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.surveys_ui_dialog_missing_form_title),
					getString(R.string.surveys_ui_dialog_missing_form_message));
	
			mAlert.show(getSupportFragmentManager(), "missing-odk-form");
			return;
		}
		
		// check to see if a matching form can be found
		Integer mFormId = odkData.get(
				cursor.getString(
						cursor.getColumnIndex(FormsContract.Table.XFORMS_FILE)
					)
				);
		
		if(mFormId == null) {
			// show error dialog
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.surveys_ui_dialog_missing_form_title),
					getString(R.string.surveys_ui_dialog_missing_form_message));
	
			mAlert.show(getSupportFragmentManager(), "missing-odk-form");
			return;
		}
		
		// do we need to start listening for location updates
		if(cursor.getInt(cursor.getColumnIndex(FormsContract.Table.USES_LOCATION)) == FormsContract.YES) {
			startLocationListener();
			
			locationListening = true;
			
			//debug code
			Log.d(sLogTag, "started location listener");
		}

		// build a Uri representing data for the form
		Uri mOdkFormUri = ContentUris.withAppendedId(
				FormsProviderAPI.FormsColumns.CONTENT_URI, 
				mFormId
				);
		
		// build an intent to launch the form
		Intent mIntent = new Intent();
		mIntent.setAction("android.intent.action.EDIT");
		mIntent.addCategory("android.intent.category.DEFAULT");
		mIntent.setComponent(new ComponentName("org.odk.collect.android","org.odk.collect.android.activities.FormEntryActivity"));
		mIntent.setDataAndType(mOdkFormUri, FormsProviderAPI.FormsColumns.CONTENT_TYPE);
		
		// launch the form
		startActivityForResult(mIntent, 0);
	}
	
	/*
	 * get the result code back from the ODK Collect activity
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		// turn off location listener
		if(locationListening == true) {
			stopLocationListener();
		}
		
		// check to see if everything went OK
		if(resultCode != Activity.RESULT_OK) {
			Log.w(sLogTag, "ODK returned without the Activity.RESULT_OK flag");
			return;
		}
		
		// get the uri for the saved instance
		Uri mInstanceUri = intent.getData();
		
		if(mInstanceUri == null) {
			Log.w(sLogTag, "ODK failed to return URI for new form instance");
			
			Toast.makeText(this, getString(R.string.surveys_ui_toast_msg_missing_uri), Toast.LENGTH_SHORT).show();
			
			return;
		}
		
		// share instance data via Rhizome?
		if(shareViaRhizome == true) {
			new ShareViaRhizomeTask(this, mInstanceUri).execute();
		}
		
	}
	
	private void startLocationListener() {
		
		Intent mIntent = new Intent(this, LocationService.class);
		startService(mIntent);
	}
	
	private void stopLocationListener() {
		
		Intent mIntent = new Intent(this, LocationService.class);
		stopService(mIntent);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		
		// play nice and tidy up
		super.onDestroy();
		
		if(cursor != null) {
			cursor.close();
		}
	}
}
