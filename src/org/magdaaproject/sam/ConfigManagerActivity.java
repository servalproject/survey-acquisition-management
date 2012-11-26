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
import java.util.ArrayList;

import org.magdaaproject.sam.config.BundleConfig;
import org.magdaaproject.sam.config.ConfigException;
import org.magdaaproject.sam.content.ConfigsContract;
import org.magdaaproject.sam.content.FormsContract;
import org.magdaaproject.sam.fragments.BasicAlertDialogFragment;
import org.magdaaproject.utils.FileUtils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * activity used to view details of the current config and manage other config related tasks
 */
public class ConfigManagerActivity extends Activity implements OnClickListener {

	/*
	 * private class level constants
	 */
	//private static final boolean sVerboseLog = true;
	private static final String sLogTag = "ConfigManagerActivity";
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config_manager);
		
		// setup ui elements
		Button mButton = (Button)findViewById(R.id.config_manager_ui_btn_load);
		mButton.setOnClickListener(this);
		
		mButton = (Button) findViewById(R.id.general_ui_btn_back);
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ConfigManagerActivity.this.finish();
			}
			
		});
		
		//TODO manage multiple configs
		
		// load any existing config parameters
		ContentResolver mContentResolver = getContentResolver();
		
		Cursor mCursor = mContentResolver.query(ConfigsContract.CONTENT_URI, 
				null, 
				null, 
				null, 
				null);
		
		if(mCursor != null && mCursor.getCount() > 0) {
			// populate the activity
			mCursor.moveToFirst();
			
			populateTable(mCursor);
			
			
		} else {
			// hide the table as it isn't needed
			TableLayout mLayout = (TableLayout) findViewById(R.id.config_manager_ui_table);
			mLayout.setVisibility(View.GONE);
			
			TextView mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_subheader);
			mTextView.setText(R.string.config_manager_ui_lbl_subheader_alt);
		}
		
		// play nice and tidy up
		if(mCursor != null) {
			mCursor.close();
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View view) {
		
		// determine which view fired the event
		switch(view.getId()) {
		case R.id.config_manager_ui_btn_load:
			// load a configuration
			ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.config_manager_ui_progress_bar);
			mProgressBar.setVisibility(View.VISIBLE);
			
			TextView mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_progress);
			mTextView.setVisibility(View.VISIBLE);
			
			// load the config file
			boolean mStatus = loadConfigFile();
			
			// update the UI as appropriate
			mProgressBar.setVisibility(View.GONE);
			mTextView.setVisibility(View.GONE);
			
			if(mStatus == true) {
				// finish updating the ui
				mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_subheader);
				mTextView.setText(R.string.config_manager_ui_lbl_subheader);
				
				TableLayout mLayout = (TableLayout) findViewById(R.id.config_manager_ui_table);
				mLayout.setVisibility(View.VISIBLE);
			}
			break;
		default:
			Log.w(sLogTag, "an unknown view fired the click event");
		}
	}
	
	// method to populate the config table
	private void populateTable(Cursor cursor) {
		
		// always ensure cursor is positioned at the first record
		cursor.moveToFirst();
		
		TextView mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_title_text);
		mTextView.setText(
				cursor.getString(
						cursor.getColumnIndex(ConfigsContract.Table.TITLE)
					)
				);
		
		mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_description_text);
		mTextView.setText(
				cursor.getString(
						cursor.getColumnIndex(ConfigsContract.Table.DESCRIPTION)
					)
				);
		
		mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_version_text);
		mTextView.setText(
				cursor.getString(
						cursor.getColumnIndex(ConfigsContract.Table.VERSION)
					)
				);
		
		mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_author_text);
		mTextView.setText(
				cursor.getString(
						cursor.getColumnIndex(ConfigsContract.Table.AUTHOR)
					)
				);
		
		mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_author_email_text);
		mTextView.setText(
				cursor.getString(
						cursor.getColumnIndex(ConfigsContract.Table.AUTHOR_EMAIL)
					)
				);
		
		mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_last_updated_text);
		mTextView.setText(
				cursor.getString(
						cursor.getColumnIndex(ConfigsContract.Table.GENERATED_DATE)
					)
				);
		
		// play nice and tidy up
		cursor.close();
		
	}
	
	// method to load the configuration file
	private boolean loadConfigFile() {
		String[] mConfigFiles = null;
		String mConfigIndex = null;
		BundleConfig newConfig = null;
		
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
			return false;
		}
		
		// check to see if at least one config file was found
		if(mConfigFiles == null || mConfigFiles.length == 0) {
			// unable to get the list of config files
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_no_config_files_title),
					getString(R.string.launcher_ui_dialog_no_config_files_message));

			mAlert.show(getFragmentManager(), "no-config-files");
			return false;
		}
		
		// check if there is more than one file
		if(mConfigFiles.length > 1) {
			// unable to get the list of config files
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_too_many_config_files_title),
					getString(R.string.launcher_ui_dialog_too_many_config_files_message));

			mAlert.show(getFragmentManager(), "too-many-config-files");
			return false;
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
			return false;
		}
		
		// check to see if the index was loaded
		if(mConfigIndex == null) {
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_unable_open_config_title),
					getString(R.string.launcher_ui_dialog_unable_open_config_message));

			mAlert.show(getFragmentManager(), "unable-open-config-files");
			return false;
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
			return false;
		}
		
		try {
			newConfig.validateConfig();
		} catch (ConfigException e) {
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_invalid_config_title),
					getString(R.string.launcher_ui_dialog_invalid_config_message));

			mAlert.show(getFragmentManager(), "invalid-config-file");
			
			Log.e(sLogTag, "configException thrown:", e);
			return false;
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
		
		// add the forms
		ArrayList<String[]> mForms = newConfig.getForms();
		
		for(String[] mElements: mForms) {
			
			mValues = new ContentValues();
			
			mValues.put(FormsContract.Table.FORM_ID, mElements[0]);
			mValues.put(FormsContract.Table.CATEGORY_ID, mElements[1]);
			mValues.put(FormsContract.Table.TITLE, mElements[2]);
			mValues.put(FormsContract.Table.XFORMS_FILE, mElements[3]);
			
			mContentResolver.insert(FormsContract.CONTENT_URI, mValues);	
		}
		
		/* 
		 * update the activity
		 */
		Cursor mCursor = mContentResolver.query(ConfigsContract.CONTENT_URI, 
				null, 
				null, 
				null, 
				null);
		
		populateTable(mCursor);
		
		return true;
	}
}
