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

import org.magdaaproject.sam.content.FormsContract;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;

/**
 * view a list of survey forms associated with the event category
 */
public class EventSurveysActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level variables
	 */
	private GridView gridView;

	/*
	 * private class level constants
	 */
	//private static final boolean sVerboseLog = true;
	//private static final String sLogTag = "LauncherActivity";
	
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
		String[] mProjection = new String[2];
		mProjection[0] = FormsContract.Table._ID;
		mProjection[1] = FormsContract.Table.TITLE;
		
		String mSelection = FormsContract.Table.CATEGORY_ID + " = ?";
		
		String[] mSelectionArgs = new String[1];
		mSelectionArgs[0] = FormsContract.CATEGORY_EVENT;
		
		String mSortOrder = FormsContract.Table.FORM_ID + " ASC";
		
		// get the data
		ContentResolver mContentResolver = getContentResolver();
		
		Cursor mCursor = mContentResolver.query(
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
		
		SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(
				getBaseContext(),
				R.layout.grid_view_events,
				mCursor,
				mColumns,
				mViews,
				0);
		
		gridView.setAdapter(mAdapter);
	}
	

	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View view) {
		// TODO Auto-generated method stub
		
	}

}
