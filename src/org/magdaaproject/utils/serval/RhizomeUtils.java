/*
 * Copyright (C) 2012, 2013 The MaGDAA Project
 *
 * This file is part of the MaGDAA Library Software
 *
 * MaGDAA Library Software is free software; you can redistribute it and/or modify
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
package org.magdaaproject.utils.serval;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.magdaaproject.utils.FileUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * a utility class which exposes utility methods for interacting with the Rhizome 
 * component of the Serval Mesh software
 */
public class RhizomeUtils {
	
	/*
	 * private class level constants
	 */
	private static final boolean sVerboseLog = true;
	private static final String sLogTag = "RhizomeUtils";
	
	/**
	 * share a file using the Rhizome component of the Serval Mesh software
	 * 
	 * @param context a context used to gain access to system resources
	 * @param path the path to the file to share
	 * @return true if the request to share the file is successfully sent
	 * 
	 * @throws IOException if the file to share cannot be found
	 */
	public static boolean shareFile(Context context, String path) throws IOException {
		
		// check on the parameters
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}

		if(FileUtils.isFileReadable(path) == false) {
			throw new IOException("unable to access the specified file '" + path + "'");
		}
		
		// build the intent
		Intent mIntent = new Intent("org.servalproject.rhizome.ADD_FILE");

		mIntent.putExtra("path", path);

		File mManifestFile = getManifestPath(path);
		
		if (mManifestFile.exists()){
			// pass in the previous manifest, so rhizome can update it
			mIntent.putExtra("previous_manifest", mManifestFile.getAbsolutePath());
		}

		// ask rhizome to save the new manifest here
		mIntent.putExtra("save_manifest", mManifestFile.getAbsolutePath());

		// ensure a lack of permission doesn't crash the app
		try {
			ComponentName mServiceName = context.getApplicationContext().startService(mIntent);
			
			if(mServiceName == null) {
				Log.e(sLogTag, "unable to start the required service");
				return false;
			}
		} catch (SecurityException e) {
			Log.e(sLogTag, "security exception thrown when trying to add file to rhizome", e);
			return false;
		}
		
		// output verbose debug log info
		if (sVerboseLog) {
			Log.v(sLogTag, "file shared using Rhizome, manifest path '" + mManifestFile.getAbsolutePath() + "'");
		}
		
		return true;
	}
	
	/**
	 * return a file handle for a manifest file derived from the shared file name
	 * 
	 * @param path the path to the shared file
	 * @return a file handle to the manifest file
	 */
	public static File getManifestPath(String path){
		File mManifestPath = new File(path);
		File mManifestFile = new File(mManifestPath.getParent(), ".manifest-" + mManifestPath.getName());
		return mManifestFile;
	}
	
	/**
	 * check to see if a file has a corresponding manifest file stored with it
	 * 
	 * @param path to the shared file
	 * @return true if the manifest exists and can be read otherwise return false;
	 */
	public static boolean hasManifest(String path) {
		
		File mFile = getManifestPath(path);
		
		try {
			return FileUtils.isFileReadable(mFile.getCanonicalPath());
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * convert a Rhizome bundle into a JSON representation
	 * 
	 * @param bundle the bundle to convert
	 * @return the bundle contents as JSON
	 */
	public static String bundleToJson(Bundle bundle) {
		
		JSONObject mObject = new JSONObject();
		
		Set<String> mBundleKeys = bundle.keySet();
		
		for(String mKey: mBundleKeys) {
			try {
				mObject.put(mKey, bundle.get(mKey).toString());
			} catch (JSONException e) {
				Log.e(sLogTag, "unable to add Rhizome bundle element to JSON object", e);
			}
		}
		
		return mObject.toString();
	}
}
