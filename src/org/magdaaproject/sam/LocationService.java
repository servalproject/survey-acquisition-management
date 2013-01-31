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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * a very basic service, used to request location updates
 * so that a location is more readily available to the ODK Collect application
 * @author techxplorer
 *
 */
public class LocationService extends Service {
	
	/*
	 * private class level constants
	 */
	//private static final boolean sVerboseLog = true;
	private static final String sLogTag = "LocationService";
	
//	private static final int sOneMinute = 1000 * 60 * 1;
//	private static final float sFiveMetres = 5;

	/*
	 * private class level variables
	 */
	private boolean locationListening = false;
	LocationManager locationManager;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.i(sLogTag, "service started");
		
		if(locationListening == false) {
			startLocationListener();
		}
		
		return Service.START_NOT_STICKY;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		
		Log.i(sLogTag, "service stopped");
		
		if(locationListening) {
			stopLocationListener();
		}
	}
	
	private void startLocationListener() {
		
		Log.i(sLogTag, "start listening for location updates");
		
		// get reference to system wide location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		//debug code
//		Log.d(sLogTag, "is GPS enabled? " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
		
		// start requesting location updates
		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, sOneMinute, sFiveMetres, locationListener);
		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, sOneMinute, sFiveMetres, locationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		
		locationListening = true;
	}
	
	private void stopLocationListener() {
		
		Log.i(sLogTag, "stop listening for location updates");
		
		// stop listening for updates
		locationManager.removeUpdates(locationListener);
		
		locationListening = false;
	}
	
	/*
	 * basic stub class to use with location services, 
	 * not really interested in results, rather need to start listening
	 * as soon as practicable
	 */
	private LocationListener locationListener = new LocationListener() {

		/*
		 * (non-Javadoc)
		 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
		 */
		@Override
		public void onLocationChanged(Location location) {
//			Log.d(sLogTag, "location changed");
//			
//			if(location != null) {
//				Log.d(sLogTag, location.toString());
//			} else {
//				Log.d(sLogTag, "location is null");
//			}
		}

		/*
		 * (non-Javadoc)
		 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
		 */
		@Override
		public void onProviderDisabled(String provider) {
//			Log.d(sLogTag, "provider disabled: " + provider);
			
		}

		/*
		 * (non-Javadoc)
		 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
		 */
		@Override
		public void onProviderEnabled(String provider) {
//			Log.d(sLogTag, "provider enabled: " + provider);
		}

		/*
		 * (non-Javadoc)
		 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
		 */
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
//			Log.d(sLogTag, "status changed: " + provider + " status: " + status);
		}
		
	};

	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// do not bind to this service so return null
		return null;
	}

}
