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
package org.magdaaproject.sam.config;

import java.io.IOException;

import org.magdaaproject.sam.ConfigManagerActivity;
import org.magdaaproject.sam.R;
import org.magdaaproject.sam.fragments.BasicAlertDialogFragment;
import org.magdaaproject.utils.FileUtils;

import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * a background task used to load the configuration file and
 * install the related forms
 */
public class ConfigLoaderTask extends AsyncTask<Void, Integer, Integer> {
	
	/*
	 * private class level constants
	 */
	private static final int sFailure = 0;
	private static final int sSuccess = 1;
	
	/*
	 * private class level variables
	 */
	private ProgressBar progressBar;
	private TextView textView;
	private ConfigManagerActivity context;
	
	/*
	 * construct a new instance of this object with reference to the status
	 * UI variables
	 */
	public ConfigLoaderTask(ProgressBar progressBar, TextView textView, ConfigManagerActivity context) {
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
		
		String[] mConfigFiles = null;
		String mConfigIndex = null;
		BundleConfig newConfig = null;
		
		String mConfigPath = Environment.getExternalStorageDirectory().getPath();
		mConfigPath += context.getString(R.string.system_file_path_configs);
		
		// output progress information
		publishProgress(R.string.config_manager_ui_lbl_progress_01);
		
		// get list of config files
		try {
			 mConfigFiles = FileUtils.listFilesInDir(
					mConfigPath, 
					context.getString(R.string.system_file_config_extension)
				);
		} catch (IOException e) {
			publishProgress(
					R.string.config_manager_ui_dialog_error_title,
					R.string.config_manager_ui_dialog_no_config_files_message
				);
			return Integer.valueOf(sFailure);
		}
		
		// check to see if at least one config file was found
		if(mConfigFiles == null || mConfigFiles.length == 0) {
			publishProgress(
					R.string.config_manager_ui_dialog_error_title,
					R.string.config_manager_ui_dialog_no_config_files_message
					);
			return Integer.valueOf(sFailure);
		}
		
		// check if there is more than one file
		if(mConfigFiles.length > 1) {
			publishProgress(
					R.string.config_manager_ui_dialog_error_title,
					R.string.config_manager_ui_dialog_too_many_config_files_message
					);
			return Integer.valueOf(sFailure);
		}
		
		// output progress information
		publishProgress(R.string.config_manager_ui_lbl_progress_02);
		
		// load the index
		try {
			mConfigIndex = FileUtils.getMagdaaBundleIndex(mConfigFiles[0]);
		} catch (IOException e) {
			publishProgress(
					R.string.config_manager_ui_dialog_error_title,
					R.string.config_manager_ui_dialog_unable_open_config_index_message
					);
			return Integer.valueOf(sFailure);
		}
		
		// check to see if the index was loaded
		if(mConfigIndex == null) {
			publishProgress(
					R.string.config_manager_ui_dialog_error_title,
					R.string.config_manager_ui_dialog_unable_open_config_index_message
					);
			return Integer.valueOf(sFailure);
		}
		
		// parse the config index
		try {
			newConfig = new BundleConfig(mConfigIndex);
			newConfig.parseConfig();
		} catch (ConfigException e) {
			publishProgress(
					R.string.config_manager_ui_dialog_error_title,
					R.string.config_manager_ui_dialog_unable_parse_config_message
					);
			return Integer.valueOf(sFailure);
		}
		
		// validate the config index
		try {
			newConfig.validateConfig();
		} catch (ConfigException e) {
			publishProgress(
					R.string.config_manager_ui_dialog_error_title,
					R.string.config_manager_ui_dialog_invalid_config_message
					);
			return Integer.valueOf(sFailure);
		}
		
		// delete the existing config
		// output progress information
		publishProgress(R.string.config_manager_ui_lbl_progress_03);
		
		try {
			context.deleteExistingConfig();
		} catch (SQLException e){ 
			publishProgress(
					R.string.config_manager_ui_dialog_error_title,
					R.string.config_manager_ui_dialog_unable_parse_config_message
					);
			return Integer.valueOf(sFailure);
		}
		
		// load the new config
		publishProgress(R.string.config_manager_ui_lbl_progress_04);
		
		try {
			context.importNewConfig(newConfig);
		} catch (SQLException e){ 
			publishProgress(
					R.string.config_manager_ui_dialog_error_title,
					R.string.config_manager_ui_dialog_unable_import_config_message
					);
			return Integer.valueOf(sFailure);
		} catch (ConfigException e) {
			publishProgress(
					R.string.config_manager_ui_dialog_error_title,
					R.string.config_manager_ui_dialog_unable_import_config_message
					);
			return Integer.valueOf(sFailure);
		}
		
		
		
		
		// everything went as expected
		return Integer.valueOf(sSuccess);
		
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
	
			mAlert.show(context.getFragmentManager(), "no-config-files");
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
			progressBar.setVisibility(View.GONE);
			textView.setText(R.string.config_manager_ui_lbl_progress_error);
			break;
		case sSuccess:
			progressBar.setVisibility(View.GONE);
			textView.setVisibility(View.GONE);
			context.refreshDisplay();
			break;
		}
    }
}
