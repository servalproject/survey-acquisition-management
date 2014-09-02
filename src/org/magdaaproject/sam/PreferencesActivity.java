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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import org.servalproject.sam.R;

/**
 * allow the user to change settings and preferences that control various aspects of the software
 */
public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	/*
	 * private class level constants
	 */
//	private static final boolean sVerboseLog = true;
//	private static final String sTag = "PreferencesActivity";
	
	/*
	 * (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
		//TODO adapt code to fragments when deprecated api no longer works
		addPreferencesFromResource(R.xml.preferences);

		// listen for changes to the preferences
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mPreferences.registerOnSharedPreferenceChangeListener(this);
		
		// complete init of back button
		Button mButton = (Button) findViewById(R.id.general_ui_btn_back);
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				PreferencesActivity.this.finish();
			}
			
		});
	}

	/*
	 * (non-Javadoc)
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// TODO Auto-generated method stub
		
	}
}
