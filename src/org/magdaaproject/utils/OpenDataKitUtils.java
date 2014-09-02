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
package org.magdaaproject.utils;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * a utility class which exposes methods to format data for use with 
 * the Open Data Kit suite of applications
 */
@SuppressLint("SimpleDateFormat")
public class OpenDataKitUtils {
	
	/*
	 * public class level constants
	 */
	
	/**
	 * identify the package name of the Serval Mesh software
	 */
	public static final String ODK_COLLECT_PACKAGE_NAME = "org.odk.collect.android";
	
	
	/**
	 * build a location string in the style used by Open Data Kit XForm instances
	 * 
	 * @param latitude the latitude geo-coordinate
	 * @param longitude the longitude geo-coordinate
	 * @param altitude the altitude part of the geo-coordinate
	 * @param accuracy the accuracy part of the geo-coordiante
	 * 
	 * @return a string containing the formatted location information, or an empty string
	 */
	public static String getLocationString(String latitude, String longitude, String altitude, String accuracy) {
		
		if(latitude == null || longitude == null) {
			return "";
		}
		
		// build the location string
		StringBuilder mBuilder = new StringBuilder();
		
		mBuilder.append(latitude);
		mBuilder.append(" ");
		mBuilder.append(longitude);
		
		// add altitude and accuracy information if present
		if(altitude != null && accuracy != null) {
			
			mBuilder.append(" ");
			mBuilder.append(altitude);
			mBuilder.append(" ");
			mBuilder.append(accuracy);
		}
		
		// return the string
		return mBuilder.toString();		
	}
	
	/**
	 * using a timestamp generate a string which can be used in directory and file names for ODK Collect
	 * 
	 * @param timestamp the timestamp to convert
	 * 
	 * @return a formatted string
	 */
	public static String getInstanceFileName(long timestamp) {
		
		/*
		 * use the same format string as that used by ODK collect
		 */
		SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		return mFormat.format(timestamp);
	}
	
	/**
	 * check to see if the ODK Collect package is installed
	 * @param context a context object used to gain access to system resources
	 * @return true if the ODK Collect software is installed
	 */
	public static boolean isOdkCollectInstalled(Context context) {
		try {
			context.getPackageManager().getApplicationInfo(ODK_COLLECT_PACKAGE_NAME, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			return false;
		}

		return true;
	}
}
