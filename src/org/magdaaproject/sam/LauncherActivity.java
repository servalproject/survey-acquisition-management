/*
 * Copyright (C) 2013-2014 The Serval Project
 * Portions Copyright (C) 2012, 2013 The MaGDAA Project
 * Portions derived from deLorme 2012:
 *  * A base class for maintaining the global state
 *  * of the InReachService.
 *  *
 *  * @author Eric Semle
 *  * @since inReachApp (07 May 2012)
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

import java.util.Arrays;

import org.magdaaproject.sam.adapters.CategoriesAdapter;
import org.magdaaproject.sam.content.CategoriesContract;
import org.magdaaproject.sam.content.ConfigsContract;
import org.magdaaproject.sam.fragments.BasicAlertDialogFragment;
import org.magdaaproject.utils.DeviceUtils;
import org.magdaaproject.utils.FileUtils;
import org.magdaaproject.utils.OpenDataKitUtils;
import org.magdaaproject.utils.serval.ServalUtils;
import org.servalproject.sam.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.servalproject.succinctdata.jni;

import com.delorme.inreachapp.service.InReachService;
import com.delorme.inreachapp.utils.LogEventHandler;

/**
 * the main activity for the application
 */
public class LauncherActivity extends FragmentActivity implements OnClickListener, ServiceConnection {
	
	/*
	 * public constants
	 */
	public static final String INTENT_EXTRA_NAME = "intent-data";

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

	public static LauncherActivity me = null;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		
		// populate the device id field		
		
		me=this;
		
		TextView mTextView = (TextView) findViewById(R.id.launcher_ui_lbl_device_id);
		mTextView.setText(String.format(getString(R.string.launcher_ui_lbl_device_id), DeviceUtils.getDeviceId(getApplicationContext())));
		mTextView = null;
				
		// setup the buttons
		Button mButton = (Button) findViewById(R.id.launcher_ui_btn_manage_inreach);
		mButton.setOnClickListener(this);

		mButton = (Button) findViewById(R.id.launcher_ui_btn_update_forms);
		mButton.setOnClickListener(this);

		mButton = (Button) findViewById(R.id.launcher_ui_btn_contact);
		if (mButton!=null)
			mButton.setOnClickListener(this);

		// check on external storage
		if(FileUtils.isExternalStorageAvailable() == false) {
			allowStart = false;
			
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_no_external_storage_title),
					getString(R.string.launcher_ui_dialog_no_external_storage_message));

			mAlert.show(getSupportFragmentManager(), "no-external-storage");
			
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
				
				mAlert.show(getSupportFragmentManager(), "no-serval");
			}
		}
		
		// check that ODK Collect is installed
		if(OpenDataKitUtils.isOdkCollectInstalled(getApplicationContext()) == false) {
			allowStart = false;
			
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					getString(R.string.launcher_ui_dialog_no_odk_collect_title),
					getString(R.string.launcher_ui_dialog_no_odk_collect_message));

			mAlert.show(getSupportFragmentManager(), "no-odk-collect");
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
		
//		if(mCursor == null || mCursor.getCount() == 0) {
//			
//			// send user to config manager screen
//			Intent mIntent = new Intent(this, org.magdaaproject.sam.ConfigManagerActivity.class);
//			startActivityForResult(mIntent, sReturnFromConfigManager);
//			
//			return;
//		} 
		
		mCursor.close();
		mCursor = null;
		
		// populate the UI
		populateUserInterface();
		
		startService();
	}
	
	private void populateUserInterface() {
		
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
		
		// double check the server
//		if(mCursor == null || mCursor.getCount() == 0) {
//			// send user to config manager screen
//			Intent mIntent = new Intent(this, org.magdaaproject.sam.ConfigManagerActivity.class);
//			startActivityForResult(mIntent, sReturnFromConfigManager);
//			return;
//		}
		
		// update the header
		if (mCursor!=null) {
			try {
				mCursor.moveToFirst();
				if (mCursor.getString(0)!=null) {
					TextView mTextView = (TextView) findViewById(R.id.launcher_ui_lbl_header);
					mTextView.setText(mCursor.getString(0));
				}
			} catch (Exception e) {
				Log.d("succinctdata",e.toString());
			}
		}
		
		// build the list of category data
		// the order of this array is very important
		// changes to the order will break the rendering of the buttons
		mProjection = new String[5];
		mProjection[0] = CategoriesContract.Table._ID;
		mProjection[1] = CategoriesContract.Table.CATEGORY_ID;
		mProjection[2] = CategoriesContract.Table.TITLE;
		mProjection[3] = CategoriesContract.Table.DESCRIPTION;
		mProjection[4] = CategoriesContract.Table.ICON;
		
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
		int[] mViews = new int[3];
		mViews[0] = R.id.list_view_category_header;
		mViews[1] = R.id.list_view_category_description;
		mViews[2] = R.id.list_view_category_icon;
		
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
				
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		// populate the UI
		populateUserInterface();
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
		case R.id.launcher_ui_btn_manage_inreach:
			mIntent = new Intent(this, com.delorme.inreachapp.InReachAppActivity.class);
			startActivity(mIntent);
			break;
		case R.id.launcher_ui_btn_update_forms:
			mIntent = new Intent(this, org.servalproject.succinctdata.DownloadForms.class);
			startActivity(mIntent);
			break;
		case R.id.launcher_ui_btn_contact:
			// show the contact information stuff
			// XXX fix
			contactUs();
			break;
		case R.id.list_view_categories_btn:
			// a category button has been touched
			Log.i(sLogTag, "category button touched");
			mIntent = new Intent(this, org.magdaaproject.sam.SurveyFormsActivity.class);
			mIntent.putExtra(INTENT_EXTRA_NAME, view.getTag().toString());
			startActivity(mIntent);
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
		
		stopService();
	}
	
    /**
     * Invoked when the service is binded
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        m_service = ((InReachService.InReachBinder)service).getService();
        
        LogEventHandler handler = LogEventHandler.getInstance();
        m_messenger = new Messenger(handler);
        m_service.registerMessenger(m_messenger);
    }

    /**
     * Invoked when the service is disconnected
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        if (m_service != null)
        {
            m_service.unregisterMessenger(m_messenger);
            m_service = null;
        }
    }
   
    /**
     * Returns the binded InReach Service
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public InReachService getService()
    {
        return m_service;
    }
    
    /**
     * Starts the InReachService and binds it to the application
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void startService()
    {
        if (m_serviceStarted)
            return;
        
        Intent intent = new Intent(this, InReachService.class);  
        
        startService(intent);
        bindService(intent, this, BIND_AUTO_CREATE); 
        
        m_serviceStarted = true;
    }
    
    /**
     * Unbinds the InReachService and stops the service.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void stopService()
    {
        if (!m_serviceStarted)
            return;
        
        Intent intent = new Intent(this, InReachService.class);  
        
        unbindService(this);
        stopService(intent);
        
        m_serviceStarted = false;
    }
    
    /** Boolean flag as to whether or not the service has been started */
    public boolean m_serviceStarted = false;
    
    /** The bound inReach service */
    public InReachService m_service = null;
    
    /** The messenger for the LogEventHandler */
    public Messenger m_messenger = null;
    
    /** A handler for all inReach events that logs them */
    public LogEventHandler m_eventHandler = null;

}
