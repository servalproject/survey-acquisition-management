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

import org.magdaaproject.sam.fragments.BasicAlertDialogFragment;
import org.magdaaproject.utils.FileUtils;
import org.magdaaproject.utils.OpenDataKitUtils;
import org.magdaaproject.utils.serval.ServalUtils;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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
	private boolean allowStart = false;

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);

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
