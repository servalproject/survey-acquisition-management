/*
 * Copyright (C) 2012 The MaGDAA Project
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

import java.io.IOException;

import org.magdaaproject.sam.config.BundleConfig;
import org.magdaaproject.sam.config.ConfigException;
import org.magdaaproject.sam.content.ConfigsContract;
import org.magdaaproject.sam.fragments.BasicAlertDialogFragment;
import org.magdaaproject.utils.DeviceUtils;
import org.magdaaproject.utils.FileUtils;
import org.magdaaproject.utils.OpenDataKitUtils;
import org.magdaaproject.utils.serval.ServalUtils;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * the main activity for the application
 */
public class LauncherActivity extends Activity implements OnClickListener {

	/*
	 * private class level constants
	 */
	//private static final boolean sVerboseLog = true;
	private static final String sLogTag = "LauncherActivity";

	/*
	 * private class level variables
	 */
	private boolean allowStart = true;
	private BundleConfig newConfig = null;

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		
		// populate the device id field
		TextView mTextView = (TextView) findViewById(R.id.launcher_ui_lbl_device_id);
		mTextView.setText(String.format(getString(R.string.launcher_ui_lbl_device_id), DeviceUtils.getDeviceId(getApplicationContext())));
		mTextView = null;

		// check on external storage
		if(FileUtils.isExternalStorageAvailable() == false) {
			allowStart = false;
			
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_no_external_storage_title),
					getString(R.string.launcher_ui_dialog_no_external_storage_message));

			mAlert.show(getFragmentManager(), "no-external-storage");
			
		}
		
		// get the shared preferences object
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		// check that Serval Mesh is installed
		if(mPreferences.getBoolean("preferences_sharing_rhizome", true) == true) {
			if(ServalUtils.isServalMeshInstalled(getApplicationContext()) == false) {
				allowStart = false;
				
				BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
						getString(R.string.launcher_ui_dialog_no_serval_mesh_title),
						getString(R.string.launcher_ui_dialog_no_serval_mesh_message));
				
				mAlert.show(getFragmentManager(), "no-serval");
			}
		}
		
		// check that ODK Collect is installed
		if(OpenDataKitUtils.isOdkCollectInstalled(getApplicationContext()) == false) {
			allowStart = false;
			
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_no_odk_collect_title),
					getString(R.string.launcher_ui_dialog_no_odk_collect_message));

			mAlert.show(getFragmentManager(), "no-odk-collect");
		}
		
		//setup the buttons
		Button mButton = (Button) findViewById(R.id.launcher_ui_btn_settings);
		mButton.setOnClickListener(this);

		mButton = (Button) findViewById(R.id.launcher_ui_btn_contact);
		mButton.setOnClickListener(this);
		
		// check to see if we should continue
		if(allowStart) {
			buildLauncherUI();
		}
	}
	
	private void buildLauncherUI() {
		
		ContentResolver mContentResolver = this.getContentResolver();
		
		String[] mProjection = new String[1];
		mProjection[0] = ConfigsContract.Table.TITLE;
		
		Cursor mCursor = mContentResolver.query(
				ConfigsContract.CONTENT_URI, 
				mProjection, 
				null, 
				null, 
				null);
		
		if(mCursor == null || mCursor.getCount() == 0) {
			
			// try to load a config file
			loadConfigFile();
			
		} else {
			
			mCursor.moveToFirst();
			TextView mTextView = (TextView) findViewById(R.id.launcher_ui_lbl_header);
			mTextView.setText(mCursor.getString(0));
			
			// build the rest of the UI
		}
		
		// play nice and tidy up
		if(mCursor != null) {
			mCursor.close();
		}
		
	}
	
	private void loadConfigFile() {
		String[] mConfigFiles = null;
		String mConfigIndex = null;
		
		String mConfigPath = Environment.getExternalStorageDirectory().getPath();
		mConfigPath += getString(R.string.system_file_path_configs);
		
		// get list of config files
		try {
			 mConfigFiles = FileUtils.listFilesInDir(
					mConfigPath, 
					getString(R.string.system_file_config_extension)
				);
		} catch (IOException e) {
			// unable to get the list of config files
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_no_config_files_title),
					getString(R.string.launcher_ui_dialog_no_config_files_message));

			mAlert.show(getFragmentManager(), "no-config-files");
			return;
		}
		
		// check to see if at least one config file was found
		if(mConfigFiles == null || mConfigFiles.length == 0) {
			// unable to get the list of config files
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_no_config_files_title),
					getString(R.string.launcher_ui_dialog_no_config_files_message));

			mAlert.show(getFragmentManager(), "no-config-files");
			return;
		}
		
		// check if there is more than one file
		if(mConfigFiles.length > 1) {
			// unable to get the list of config files
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_too_many_config_files_title),
					getString(R.string.launcher_ui_dialog_too_many_config_files_message));

			mAlert.show(getFragmentManager(), "too-many-config-files");
			return;
		}
		
		// load the index
		try {
			mConfigIndex = FileUtils.getMagdaaBundleIndex(mConfigFiles[0]);
		} catch (IOException e) {
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_unable_open_config_title),
					getString(R.string.launcher_ui_dialog_unable_open_config_message));

			mAlert.show(getFragmentManager(), "unable-open-config-file");
			
			Log.e(sLogTag, "unable to open the index file", e);
			Log.v(sLogTag, "file path :'" + mConfigFiles[0] + "'");
			return;
		}
		
		// check to see if the index was loaded
		if(mConfigIndex == null) {
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_unable_open_config_title),
					getString(R.string.launcher_ui_dialog_unable_open_config_message));

			mAlert.show(getFragmentManager(), "unable-open-config-files");
			return;
		}
		
		// parse the config
		try {
			newConfig = new BundleConfig(mConfigIndex);
			newConfig.parseConfig();
		} catch (ConfigException e) {
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_unable_parse_config_title),
					getString(R.string.launcher_ui_dialog_unable_parse_config_message));

			mAlert.show(getFragmentManager(), "unable-parse-config-file");
			
			Log.e(sLogTag, "configException thrown:", e);
			return;
		}
		
		try {
			newConfig.validateConfig();
		} catch (ConfigException e) {
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_invalid_config_title),
					getString(R.string.launcher_ui_dialog_invalid_config_message));

			mAlert.show(getFragmentManager(), "unable-parse-config-file");
			
			Log.e(sLogTag, "configException thrown:", e);
			return;
		}
		
		/*
		 *  import the content
		 */
		
		// build the list of values
		ContentValues mValues = new ContentValues();
		mValues.put(ConfigsContract.Table.TITLE, newConfig.getMetadataValue("title"));
		mValues.put(ConfigsContract.Table.DESCRIPTION, newConfig.getMetadataValue("description"));
		mValues.put(ConfigsContract.Table.VERSION, newConfig.getMetadataValue("version"));
		mValues.put(ConfigsContract.Table.AUTHOR, newConfig.getMetadataValue("author"));
		mValues.put(ConfigsContract.Table.AUTHOR_EMAIL, newConfig.getMetadataValue("email"));
		mValues.put(ConfigsContract.Table.GENERATED_DATE, newConfig.getMetadataValue("generated"));
		
		// save them to the database
		ContentResolver mContentResolver = this.getContentResolver();
		
		mContentResolver.insert(ConfigsContract.CONTENT_URI, mValues);
		
		// add the other config data
		
		// rebuild the ui
		buildLauncherUI();
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
		case R.id.launcher_ui_btn_settings:
			mIntent = new Intent(this, org.magdaaproject.sam.PreferencesActivity.class);
			startActivity(mIntent);
			break;
		case R.id.launcher_ui_btn_contact:
			// show the contact information stuff
			contactUs();
			break;
		default:
			Log.w(sLogTag, "an unknown view fired an onClick event");
		}

	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_launcher, menu);
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent mIntent;

		switch(item.getItemId()){
		case R.id.launcher_menu_acknowledgements:
			// open the acknowledgments uri in the default browser
			mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.system_acknowledgments_uri)));
			startActivity(mIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * method to start the send an email process so that the user can contact us
	 */
	private void contactUs() {

		// send an email to us
		Intent mIntent = new Intent(Intent.ACTION_SEND);
		mIntent.setType("plain/text");
		mIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getString(R.string.system_contact_email)});
		mIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.system_contact_email_subject));

		startActivity(Intent.createChooser(mIntent, getString(R.string.system_contact_email_chooser)));
	}
}
