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

import java.util.Arrays;

import org.magdaaproject.sam.adapters.CategoriesAdapter;
import org.magdaaproject.sam.content.CategoriesContract;
import org.magdaaproject.sam.content.ConfigsContract;
import org.magdaaproject.sam.fragments.BasicAlertDialogFragment;
import org.magdaaproject.utils.DeviceUtils;
import org.magdaaproject.utils.FileUtils;
import org.magdaaproject.utils.OpenDataKitUtils;
import org.magdaaproject.utils.serval.ServalUtils;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
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
	
	private static final int sReturnFromConfigManager = 0;

	/*
	 * private class level variables
	 */
	private boolean allowStart = true;
	private ListView listView;
	private Cursor cursor;

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
//		if(mPreferences.getBoolean("preferences_sharing_rhizome", true) == true) {
//			if(ServalUtils.isServalMeshInstalled(getApplicationContext()) == false) {
//				allowStart = false;
//				
//				BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
//						getString(R.string.launcher_ui_dialog_no_serval_mesh_title),
//						getString(R.string.launcher_ui_dialog_no_serval_mesh_message));
//				
//				mAlert.show(getFragmentManager(), "no-serval");
//			}
//		}
		
		// check that ODK Collect is installed
		if(OpenDataKitUtils.isOdkCollectInstalled(getApplicationContext()) == false) {
			allowStart = false;
			
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_no_odk_collect_title),
					getString(R.string.launcher_ui_dialog_no_odk_collect_message));

			mAlert.show(getFragmentManager(), "no-odk-collect");
		}
		
		// check to see if we should continue
		if(allowStart == false) {
			return;
		}
		
		// check for an available config
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
			
			// send user to config manager screen
			Intent mIntent = new Intent(this, org.magdaaproject.sam.ConfigManagerActivity.class);
			startActivityForResult(mIntent, sReturnFromConfigManager);
		} else {
			
			// update the header
			mCursor.moveToFirst();
			mTextView = (TextView) findViewById(R.id.launcher_ui_lbl_header);
			mTextView.setText(mCursor.getString(0));
		}
		
		// play nice and tidy up
		if(mCursor != null) {
			mCursor.close();
		}
		
		// build the list of category data
		// the order of this array is very important
		// changes to the order will break the rendering of the buttons
		mProjection = new String[4];
		mProjection[0] = CategoriesContract.Table._ID;
		mProjection[1] = CategoriesContract.Table.CATEGORY_ID;
		mProjection[2] = CategoriesContract.Table.TITLE;
		mProjection[3] = CategoriesContract.Table.DESCRIPTION;
		
		String mOrderBy = CategoriesContract.Table.CATEGORY_ID + " ASC";
		
		cursor = mContentResolver.query(
				CategoriesContract.CONTENT_URI, 
				mProjection,
				null,
				null,
				mOrderBy);
		
		// get a reference to the list view
		listView = (ListView) findViewById(R.id.launcher_ui_list_categories);
		
		// prepare other layout variables
		int[] mViews = new int[1];
		mViews[0] = R.id.list_view_categories_btn;
		
		CategoriesAdapter mAdapter = new CategoriesAdapter(
				this,
				R.layout.list_view_categories,
				cursor,
				Arrays.copyOfRange(mProjection, 1, mProjection.length),
				mViews,
				0);
		
		listView.setAdapter(mAdapter);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == sReturnFromConfigManager) {
			
			// check for an available config
			ContentResolver mContentResolver = this.getContentResolver();
			
			String[] mProjection = new String[1];
			mProjection[0] = ConfigsContract.Table.TITLE;
			
			Cursor mCursor = mContentResolver.query(
					ConfigsContract.CONTENT_URI, 
					mProjection, 
					null, 
					null, 
					null);
			
			if(mCursor != null && mCursor.getCount() > 0) {
				// update the header
				mCursor.moveToFirst();
				TextView mTextView = (TextView) findViewById(R.id.launcher_ui_lbl_header);
				mTextView.setText(mCursor.getString(0));
			}
			
			// play nice and tidy up
			if(mCursor != null) {
				mCursor.close();
			}	
		}
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
		case R.id.launcher_ui_btn_event_survey:
			// show the event survey activity
			mIntent = new Intent(this, org.magdaaproject.sam.EventSurveysActivity.class);
			startActivity(mIntent);
			break;
		case R.id.launcher_ui_btn_audience_survey:
			// show the event survey activity
			mIntent = new Intent(this, org.magdaaproject.sam.AudienceSurveysActivity.class);
			startActivity(mIntent);
			break;
		case R.id.list_view_categories_btn:
			// a category button has been touched
			Log.i(sLogTag, "category button touched");
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
