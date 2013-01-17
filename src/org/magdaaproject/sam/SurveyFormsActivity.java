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

import java.util.HashMap;

import org.magdaaproject.sam.adapters.SurveyFormsAdapter;
import org.magdaaproject.sam.content.FormsContract;
import org.magdaaproject.sam.fragments.BasicAlertDialogFragment;
import org.magdaaproject.sam.sharing.ShareViaRhizomeTask;
import org.odk.collect.FormsProviderAPI;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * view a list of survey forms associated with the event category
 */
public class SurveyFormsActivity extends Activity implements OnClickListener {
	
	/*
	 * event surveys and audience surveys are managed as separate activities
	 * due to the need in the future to undertake different tasks depending on
	 * the type of survey, and to make development faster in the short / medium term
	 */
	
	/*
	 * private class level constants
	 */
	//private static final boolean sVerboseLog = true;
	private static final String sLogTag = "SurveyFormsActivity";
	
	/*
	 * private class level variables
	 */
	private GridView gridView;
	private Cursor   cursor;
	private HashMap<String, Integer> odkData;
	private boolean shareViaRhizome;
	
	private String categoryId;
	private String categoryTitle;
	
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
				// TODO Auto-generated method stub
				SurveyFormsActivity.this.finish();
			}
			
		});
		
		// determine if we're sharing saved instance data
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		if(mPreferences.getBoolean("preferences_sharing_bundles", true) == true) {
			// we're undertaking some sort of sharing
			shareViaRhizome = mPreferences.getBoolean("preferences_sharing_rhizome", true);
		}
		
		// get a reference to the grid view
		gridView = (GridView)findViewById(R.id.survey_forms_ui_grid);
		
		// setup the cursor
		String[] mProjection = new String[3];
		mProjection[0] = FormsContract.Table._ID;
		mProjection[1] = FormsContract.Table.TITLE;
		mProjection[2] = FormsContract.Table.XFORMS_FILE;
		
		String mSelection = FormsContract.Table.CATEGORY_ID + " = ?";
		
		String[] mSelectionArgs = new String[1];
		mSelectionArgs[0] = categoryId;
		
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
			gridView.setVisibility(View.GONE);
			
			mTextView = (TextView) findViewById(R.id.survey_forms_ui_lbl_no_forms);
			mTextView.setVisibility(View.VISIBLE);
			
			return;
			
		}
		
		// setup the cursor to view map related code
		String[] mColumns = new String[1];
		mColumns[0] = FormsContract.Table.TITLE;
		
		int[] mViews = new int[1];
		mViews[0] = R.id.grid_view_events_text;
		
		SurveyFormsAdapter mAdapter = new SurveyFormsAdapter(
				this,
				R.layout.grid_view_events,
				cursor,
				mColumns,
				mViews,
				0);
		
		gridView.setAdapter(mAdapter);
		
		// get the ODK data
		mProjection = new String[2];
		mProjection[0] = FormsProviderAPI.FormsColumns._ID;
		mProjection[1] = FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
		
		Cursor odkCursor = mContentResolver.query(
				FormsProviderAPI.FormsColumns.CONTENT_URI,
				mProjection,
				null,
				null,
				null
				);
		
		odkData = new HashMap<String, Integer>();
		
		mTokens = null;
		
		while(odkCursor.moveToNext()) {
			
			mTokens = odkCursor.getString(1).split("/");
			
			odkData.put(
					mTokens[mTokens.length -1], 
					odkCursor.getInt(0)
				);
		}
		
		odkCursor.close();
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View view) {
		
		// get the details of the form that we want to load
		cursor.moveToPosition((Integer) view.getTag());
		
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
	
			mAlert.show(getFragmentManager(), "missing-odk-form");
			return;
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
