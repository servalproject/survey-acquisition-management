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

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * a utility class which exposes methods related to the device
 */
public class DeviceUtils {
	
	/**
	 * determine the unique id for this device using similar logic to that which is 
	 * used by the OpenDataKit Collect application
	 * 
	 * @param context a context which can be used to access system resources
	 * @return a unique identifier for this device
	 */
	public static String getDeviceId(Context context) {
		
		// double check the parameters
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		// get an instance of the telephony manager to use to get the device id
		TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		String mDeviceId = mTelephonyManager.getDeviceId();
		
		// double check the device id
		if(mDeviceId != null) {
			if ((mDeviceId.contains("*") || mDeviceId.contains("000000000000000"))) { // buggy device id detected, use alternative
				mDeviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
			}
		} else {
			// possibly a Wi-Fi only device and therefore the normal device id is not available
			// use the wifi mac address as the id
			WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			
			// Get WiFi info
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            if (mWifiInfo != null ) {
            	mDeviceId = mWifiInfo.getMacAddress(); // use the wifi mac address as the id
            } else {
            	// wifi info not available
            	mDeviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
		}
		
		return mDeviceId;
	}
}
