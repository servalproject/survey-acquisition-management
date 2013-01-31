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
package org.magdaaproject.sam.config;

import org.magdaaproject.sam.ConfigManagerActivity;
import org.magdaaproject.sam.content.FormsContract;
import org.magdaaproject.sam.fragments.BasicAlertDialogFragment;
import org.magdaaproject.utils.FileUtils;
import org.magdaaproject.utils.xforms.XFormsException;
import org.magdaaproject.utils.xforms.XFormsUtils;
import org.servalproject.sam.R;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class FormVerifyTask extends AsyncTask<Void, Integer, Integer> {
	
	/*
	 * private class level constants
	 */
	private static final int sFailure = 0;
	private static final int sSuccess = 1;
	
	private static final String sLogTag = "FormVerifyTask";
	
	/*
	 * private class level variables
	 */
	private ProgressBar progressBar;
	private TextView textView;
	private ConfigManagerActivity context;
	
	private SparseArray<String> formList;
	
	/*
	 * construct a new instance of this object with reference to the status
	 * UI variables
	 */
	public FormVerifyTask(ProgressBar progressBar, TextView textView, ConfigManagerActivity context) {
		this.progressBar = progressBar;
		this.textView = textView;
		this.context = context;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Integer doInBackground(Void... arg0) {
		
		// get a list of forms to process
		// use a sparse array to minimise time that cursors are open
		formList = new SparseArray<String>();
		
		String[] mProjection = new String[2];
		mProjection[0] = FormsContract.Table.FORM_ID;
		mProjection[1] = FormsContract.Table.XFORMS_FILE;
		
		String mSelection = FormsContract.Table.FOR_DISPLAY + " = ?";
		
		String[] mSelectionArgs = new String[1];
		mSelectionArgs[0] = Integer.toString(FormsContract.YES);
		
		// get the data
		ContentResolver mContentResolver = context.getContentResolver();
		
		Cursor mCursor = mContentResolver.query(
				FormsContract.CONTENT_URI,
				mProjection,
				mSelection,
				mSelectionArgs,
				null);
		
		// check to make sure some data is there
		if(mCursor == null || mCursor.getCount() == 0) {
			publishProgress(
					R.string.config_manager_ui_dialog_verify_error_title,
					R.string.config_manager_ui_dialog_no_files_message
					);
			return Integer.valueOf(sFailure);
		}
		
		// build a list of forms to process
		while(mCursor.moveToNext()) {
			formList.put(
					mCursor.getInt(mCursor.getColumnIndex(FormsContract.Table.FORM_ID)),
					mCursor.getString(mCursor.getColumnIndex(FormsContract.Table.XFORMS_FILE))
					);
		}
		
		// play nice and tidy up
		mCursor.close();
		
		// get the path to the ODK directory
		String mOdkPath = Environment.getExternalStorageDirectory().getPath();
		mOdkPath += context.getString(R.string.system_file_path_odk_forms);
		
		Integer mKey;
		String  mFileName;
		
		// check to see if the file is readable
		for(int i = 0; i < formList.size(); i++) {
			
			// get the key
			mKey = formList.keyAt(i);
			
			// get the file name 
			mFileName = mOdkPath + formList.get(mKey);
			
			// debug
			Log.i(sLogTag, "Looking for file: " + mFileName);
			
			if(checkFileExists(mKey, mFileName, mContentResolver) == false) {
				publishProgress(
						R.string.config_manager_ui_dialog_verify_error_title,
						R.string.config_manager_ui_dialog_verify_missing_file_message
						);
				return Integer.valueOf(sFailure);
			}
		}
		
		// check to see if the file uses location question type
		for(int i = 0; i < formList.size(); i++) {
			
			// get the key
			mKey = formList.keyAt(i);
			
			// get the file name 
			mFileName = mOdkPath + formList.get(mKey);
			
			// check to see if the file uses location services
			try {
				if(XFormsUtils.hasLocationQuestion(mFileName) == true) {
					// update the database
					ContentValues mValues = new ContentValues();
					mValues.put(FormsContract.Table.USES_LOCATION, FormsContract.YES);
					
					mSelection = FormsContract.Table.FORM_ID + " = ?";
					
					mSelectionArgs = new String[1];
					mSelectionArgs[0] = Integer.toString(mKey);
					
					mContentResolver.update(
							FormsContract.CONTENT_URI,
							mValues,
							mSelection,
							mSelectionArgs);
					
				}
			} catch (XFormsException e) {
				Log.e(sLogTag, "error occured checking for location question", e);
			}
		}
		
		// everything went as expected
		return Integer.valueOf(sSuccess);
	}
	
	// private method to verify that a file exists
	private boolean checkFileExists(Integer formId, String formFile, ContentResolver contentResolver) {
		
		// check to ensure the file exists
		if(FileUtils.isFileReadable(formFile) == true) {
			return true;
		}
		
		ContentValues mValues = new ContentValues();
		mValues.put(FormsContract.Table.FOR_DISPLAY, FormsContract.NO);
		
		String mSelection = FormsContract.Table.FORM_ID + " = ?";
		
		String[] mSelectionArgs = new String[1];
		mSelectionArgs[0] = Integer.toString(formId);
		
		contentResolver.update(
				FormsContract.CONTENT_URI,
				mValues,
				mSelection,
				mSelectionArgs);
		
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(Integer... progress) {

		// determine what sort of progress to show
		if(progress.length == 2) {
			// show a basic alert dialog using the two references strings
			BasicAlertDialogFragment mAlert = BasicAlertDialogFragment.newInstance(
					context.getString(progress[0]),
					context.getString(progress[1]));
	
			mAlert.show(context.getSupportFragmentManager(), "no-config-files");
		} else {
			// update progress text field using the single referenced string
			textView.setText(progress[0]);
		}
    }
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
    protected void onPostExecute(Integer result) {
      
		// determine what option to take
		switch(result) {
		case sFailure:
			progressBar.setVisibility(View.INVISIBLE);
			textView.setText(R.string.config_manager_ui_lbl_verify_error);
			
			// place the text view, below the table
			RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
			        ViewGroup.LayoutParams.WRAP_CONTENT);

			mLayoutParams.addRule(RelativeLayout.BELOW, R.id.config_manager_ui_table);

			textView.setLayoutParams(mLayoutParams);
			
			break;
		case sSuccess:
			progressBar.setVisibility(View.GONE);
			textView.setVisibility(View.GONE);
			context.finaliseInstall();
			break;
		}
    }

}
