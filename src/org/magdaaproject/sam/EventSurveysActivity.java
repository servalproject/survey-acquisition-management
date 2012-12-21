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

import java.util.HashMap;

import org.magdaaproject.sam.adapters.EventSurveysAdapter;
import org.magdaaproject.sam.content.FormsContract;
import org.magdaaproject.sam.fragments.BasicAlertDialogFragment;
import org.odk.collect.FormsProviderAPI;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;

/**
 * view a list of survey forms associated with the event category
 */
public class EventSurveysActivity extends Activity implements OnClickListener {
	
	/*
	 * event surveys and audience surveys are managed as separate activities
	 * due to the need in the future to undertake different tasks depending on
	 * the type of survey, and to make development faster in the short / medium term
	 */
	
	/*
	 * private class level variables
	 */
	private GridView gridView;
	private Cursor   cursor;
	private HashMap<String, Integer> odkData;

	/*
	 * private class level constants
	 */
	//private static final boolean sVerboseLog = true;
	//private static final String sLogTag = "EventSurveysActivity";
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_surveys);
		
		// complete init of back button
		Button mButton = (Button) findViewById(R.id.general_ui_btn_back);
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				EventSurveysActivity.this.finish();
			}
			
		});
		
		// get a reference to the grid view
		gridView = (GridView)findViewById(R.id.event_surveys_ui_grid);
		
		// setup the cursor
		String[] mProjection = new String[3];
		mProjection[0] = FormsContract.Table._ID;
		mProjection[1] = FormsContract.Table.TITLE;
		mProjection[2] = FormsContract.Table.XFORMS_FILE;
		
		String mSelection = FormsContract.Table.CATEGORY_ID + " = ?";
		
		String[] mSelectionArgs = new String[1];
		mSelectionArgs[0] = FormsContract.CATEGORY_EVENT;
		
		String mSortOrder = FormsContract.Table.FORM_ID + " ASC";
		
		// get the data
		ContentResolver mContentResolver = getContentResolver();
		
		cursor = mContentResolver.query(
				FormsContract.CONTENT_URI,
				mProjection,
				mSelection,
				mSelectionArgs,
				mSortOrder);
		
		// setup the cursor to view map related code
		String[] mColumns = new String[1];
		mColumns[0] = FormsContract.Table.TITLE;
		
		int[] mViews = new int[1];
		mViews[0] = R.id.grid_view_events_text;
		
		EventSurveysAdapter mAdapter = new EventSurveysAdapter(
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
		
		String[] mTokens;
		
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
					getString(R.string.event_surveys_ui_dialog_missing_form_title),
					getString(R.string.event_surveys_ui_dialog_missing_form_message));
	
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
