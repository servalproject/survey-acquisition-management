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

import java.util.ArrayList;

import org.magdaaproject.sam.config.BundleConfig;
import org.magdaaproject.sam.config.ConfigException;
import org.magdaaproject.sam.config.ConfigLoaderTask;
import org.magdaaproject.sam.content.ConfigsContract;
import org.magdaaproject.sam.content.CategoriesContract;
import org.magdaaproject.sam.content.FormsContract;
import org.odk.collect.FormsProviderAPI;
import org.odk.collect.InstanceProviderAPI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.TextUtils;
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
	 * private class level variables
	 */
	
	private ContentResolver contentResolver;
	
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
				ConfigManagerActivity.this.finish();
			}
			
		});
		
		//TODO manage multiple configs
		
		// load any existing config parameters
		contentResolver = getContentResolver();
		
		Cursor mCursor = contentResolver.query(ConfigsContract.CONTENT_URI, 
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
			 DialogFragment newFragment = ConfirmLoadConfig.newInstance(
					 getString(R.string.config_manager_ui_dialog_confirm_title),
					 getString(R.string.config_manager_ui_dialog_confirm_message));
		     newFragment.show(getFragmentManager(), "dialog");
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

	/**
	 * convenience method to delete any existing config from the database
	 * @return true on success, false on failure
	 */
	public void deleteExistingConfig() throws SQLException {

		contentResolver.delete(ConfigsContract.CONTENT_URI, null, null);
		contentResolver.delete(FormsContract.CONTENT_URI, null, null);

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
		
		contentResolver.delete(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null);
		contentResolver.delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null);
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
	 * refresh the display of config information
	 */
	public void refreshDisplay() {
		// update the table of config values
		Cursor mCursor = contentResolver.query(ConfigsContract.CONTENT_URI, 
				null, 
				null, 
				null, 
				null);
		
		populateTable(mCursor);
		
		TextView mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_subheader);
		mTextView.setText(R.string.config_manager_ui_lbl_subheader);
		
		TableLayout mLayout = (TableLayout) findViewById(R.id.config_manager_ui_table);
		mLayout.setVisibility(View.VISIBLE);
	}
	
	/**
	 * start the load config process
	 */
	public void loadConfig() {
		
		// load a configuration
		ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.config_manager_ui_progress_bar);
		mProgressBar.setVisibility(View.VISIBLE);
		
		TextView mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_progress);
		mTextView.setVisibility(View.VISIBLE);
		
		TableLayout mLayout = (TableLayout) findViewById(R.id.config_manager_ui_table);
		mLayout.setVisibility(View.GONE);
		
		// load the config file
		new ConfigLoaderTask(mProgressBar, mTextView, this).execute();
		
	}
	
	/*
	 * dialog to confirm the user wishes to continue loading a configuration
	 */
	public static class ConfirmLoadConfig extends DialogFragment {

		/*
		 * 
		 */
        public static ConfirmLoadConfig newInstance(String title, String message) {
            
        	if(TextUtils.isEmpty(title) == true) {
    			throw new IllegalArgumentException("the title parameter is required");
    		}
    		
    		if(TextUtils.isEmpty(message) == true) {
    			throw new IllegalArgumentException("the message parameter is required");
    		}
    		
    		ConfirmLoadConfig mObject = new ConfirmLoadConfig();
    		
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
    		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				((ConfigManagerActivity)getActivity()).loadConfig();
    			}
    		})
    		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				dialog.cancel();
    			}
    		});
    		return mBuilder.create();
        }
    }
}
