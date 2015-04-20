package com.delorme.inreachapp.service.gps;

import java.util.Iterator;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import com.delorme.inreachcore.GPSDelegate;

/**
 * The GPS Delegate provides GPS locaiton and status data to the inReachManager.
 * This is required for the inReachManager.
 * 
 * @author Eric Semle
 * @since inReachApp (07 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
public class InReachGPSDelegate implements LocationListener, GpsStatus.Listener, GPSDelegate
{
    /**
     * Constructor that initializes the request of GPS status and location updates.
     * 
     * @param context The Application context. This cannot be null.
     * 
     * @throws IllegalArgumentException This is thrown when the context
     * parameter is null.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public InReachGPSDelegate(Context context) throws IllegalArgumentException
    {
        if (context == null)
            throw new IllegalArgumentException("The context should not be null.");
        
        // Does this device have a location manager?
        final Object obj = context.getSystemService(Context.LOCATION_SERVICE);
        if (obj != null)
        {
            m_locManager = (LocationManager) obj;
            
            m_locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
               1000, 0.0f, this, Looper.getMainLooper());
            
            m_locManager.addGpsStatusListener(this);
        }  
    }
    
    /**
     * Stops the location manager GPS location and status updates. Create a new
     * GPS delegate to restart.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	public synchronized void close()
	{
	    if (m_locManager == null)
	        return;
	    
        m_locManager.removeUpdates(this);
        m_locManager.removeGpsStatusListener(this);
        m_locManager = null;
	}
	
	/**
	 * Called when the location has changed.
	 * 
	 * @param location the new positional data from the Service Provider
	 * 
	 * @author Eric Semle
	 * @since inReachApp (07 May 2012)
	 * @version 1.0
	 * @bug AND-1009
	 */
	@Override
	public synchronized void onLocationChanged(Location location)
	{
	    if (m_location != null)
	    {
	        m_location.set(location);
	    }
	    else
	    {
	        m_location = new Location(location);
	    }
	}

	/**
	 * Called when a service provider is disabled.
	 * 
	 * @param provider the location service that was disabled.
	 * 
	 * @author Eric Semle
	 * @since inReachApp (07 May 2012)
	 * @version 1.0
	 * @bug AND-1009
	 */
	@Override
	public synchronized void onProviderDisabled(String provider)
	{
	    // Nothing to do..
	}

	/**
	 * Called when a service provider is enabled.
	 * 
	 * @param provider the location service that was enabled
	 * @author Eric Semle
	 * @since inReachApp (07 May 2012)
	 * @version 1.0
	 * @bug AND-1009
	 */
	@Override
	public synchronized void onProviderEnabled(String provider)
	{
        // Nothing to do..
	}

	/**
	 * Called when the provider status changes. This method is called when a 
	 * provider is unable to fetch a location or if the provider has recently
	 * become available after a period of unavailability.
	 * 
	 * @param provider the location service that changed
	 * @param status the new status of the provider
	 * @param extra A bundle of more information about the status change
	 * 
	 * @author Eric Semle
	 * @since inReachApp (07 May 2012)
	 * @version 1.0
	 * @bug AND-1009
	 */
	@Override
	public synchronized void onStatusChanged(String provider, int status, Bundle extras)
	{
        // Nothing to do..
	}
	
	/**
	 * Called when the gps status has changed
	 * 
	 * @param event the event that triggered the status change
	 * @author Eric Semle
	 * @since inReachApp (07 May 2012)
	 * @version 1.0
	 * @bug AND-1009
	 */
    @Override
    public synchronized void onGpsStatusChanged(int event) 
    {
        if (m_locManager == null)
        	return;
        
        m_gpsStatus = m_locManager.getGpsStatus(m_gpsStatus);
    }

    /**
     * Returns the GPS Fix
     * 
     * @return int the current GPS fix based on satellite coverage.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
	public synchronized int getGPSFix() 
	{
       if (m_gpsStatus == null)
            return 0;
        
        final Iterable<GpsSatellite> satellites = m_gpsStatus.getSatellites();
        if (satellites == null)
            return GPSDelegate.GPS_FIX_NONE;
            
        int usedSatellites = 0;
        
        final Iterator<GpsSatellite> satIter = satellites.iterator();
        while (satIter.hasNext())
        {
            GpsSatellite satellite = satIter.next();
            if (satellite.usedInFix())
                ++usedSatellites;
        }                
        
        if (usedSatellites > 3)
            return GPSDelegate.GPS_FIX_3D;
        else
        if (usedSatellites == 3)
            return GPSDelegate.GPS_FIX_2D;
        else
            return GPSDelegate.GPS_FIX_NONE;
	}

	/**
	 * Returns the current state of the GPS service provider.
	 * 
	 * @return 0 for enabled, 1 for disabled
	 * 
	 * @author Eric Semle
	 * @since inReachApp (07 May 2012)
	 * @version 1.0
	 * @bug AND-1009
	 */
	@Override
	public synchronized int getGPSState() 
	{
	    if (m_locManager == null)
	    {
	        return GPSDelegate.GPS_STATE_OFF;
	    }
	    
	    final boolean isEnabled = m_locManager.isProviderEnabled(
	        LocationManager.GPS_PROVIDER);
	
	    return (isEnabled ? GPSDelegate.GPS_STATE_ON : GPSDelegate.GPS_STATE_OFF);
	}

	/**
	 * Returns the latitude in decimal degrees.
	 * 
	 * @author Eric Semle
	 * @since inReachApp (07 May 2012)
	 * @version 1.0
	 * @bug AND-1009
	 */
	@Override
	public synchronized double getLatitude()
	{
	    double latitude = 0;
        if (m_location != null)
        {
            latitude = m_location.getLatitude();
        }
        return latitude;
	}

	/**
	 * Returns the longitude in decimal degrees
	 * 
	 * @author Eric Semle
	 * @since inReachApp (07 May 2012)
	 * @version 1.0
	 * @bug AND-1009
	 */
	@Override
	public synchronized double getLongitude()
	{
	    double longitude = 0;
	    if (m_location != null)
	    {
	        longitude = m_location.getLongitude();
	    }
	    return longitude;
	}

	/**
	 * Returns the speed in meters per second
	 * 
	 * @author Eric Semle
	 * @since inReachApp (07 May 2012)
	 * @version 1.0
	 * @bug AND-1009
	 */
	@Override
	public synchronized float getSpeed()
	{
	    float speed = 0;
	    if (m_location != null && m_location.hasSpeed())
	    {
	        speed = m_location.getSpeed();
	    }
	    return speed;
	}
	
	/**
	 * Returns the bearing in degrees rrue
	 * 
	 * @author Eric Semle
	 * @since inReachApp (07 May 2012)
	 * @version 1.0
	 * @bug AND-1009
	 */
    @Override
    public synchronized float getBearing()
    {
        float bearing = 0;
        if (m_location != null && m_location.hasBearing())
        {
            bearing = m_location.getBearing();
        }
        return bearing;
    }

    /**
     * Returns the elevation in meters
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public double getElevation()
    {
        double altitude = 0;
        if (m_location != null && m_location.hasAltitude())
        {    
            // convert from milliseconds to seconds.
            altitude = m_location.getAltitude();
        }
        return altitude;
    }

    /**
     * Returns the time from the GPS as seconds since Jan 1, 1970.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
	@Override
	public synchronized long getTime()
	{
	    long timeInSeconds = 0;
	    if (m_location != null)
	    {    
	        // convert from milliseconds to seconds.
	        timeInSeconds = m_location.getTime() / 1000;
	    }
	    return timeInSeconds;
	}
	
	//! The device location manager
	private LocationManager m_locManager = null;
    
	//! The GpsStatus that reflects the last status update.
    private GpsStatus m_gpsStatus = null;  
	
    //! The last known location
    private Location m_location = null;
}
