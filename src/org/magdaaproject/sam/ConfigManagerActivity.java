package org.magdaaproject.sam;

import org.magdaaproject.sam.content.ConfigsContract;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class ConfigManagerActivity extends Activity {

	/*
	 * private class level constants
	 */
	private static final boolean sVerboseLog = true;
	private static final String sLogTag = "ConfigManagerActivity";
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config_manager);
		
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
			
			TextView mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_title_text);
			mTextView.setText(
					mCursor.getString(
							mCursor.getColumnIndex(ConfigsContract.Table.TITLE)
						)
					);
			
			mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_description_text);
			mTextView.setText(
					mCursor.getString(
							mCursor.getColumnIndex(ConfigsContract.Table.DESCRIPTION)
						)
					);
			
			mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_version_text);
			mTextView.setText(
					mCursor.getString(
							mCursor.getColumnIndex(ConfigsContract.Table.VERSION)
						)
					);
			
			mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_author_text);
			mTextView.setText(
					mCursor.getString(
							mCursor.getColumnIndex(ConfigsContract.Table.AUTHOR)
						)
					);
			
			mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_author_email_text);
			mTextView.setText(
					mCursor.getString(
							mCursor.getColumnIndex(ConfigsContract.Table.AUTHOR_EMAIL)
						)
					);
			
			mTextView = (TextView) findViewById(R.id.config_manager_ui_lbl_last_updated_text);
			mTextView.setText(
					mCursor.getString(
							mCursor.getColumnIndex(ConfigsContract.Table.GENERATED_DATE)
						)
					);
		}
		
	}
}
