package com.delorme.inreachapp;

import org.magdaaproject.sam.InReachMessageHandler;
import org.magdaaproject.sam.LauncherActivity;

import android.app.Activity;
import android.app.Application;
import android.app.TabActivity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Messenger;
import android.widget.TabHost;

import com.delorme.inreachapp.service.InReachService;
import com.delorme.inreachcore.InReachManager;
import com.delorme.inreachcore.OutboundMessage;

/**
 * Base activity with helper methods.
 * 
 * @author Eric Semle
 * @since inReachApp (07 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
public class BaseTabActivity extends Activity
{
    /**
     * Returns the current InReachService. This will return null if
     * the service has not been started.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public InReachService getInReachService()
    {        
    	if (InReachMessageHandler.getInstance() != null) {
    		
    		final InReachService service = InReachMessageHandler.getInstance().getService();
        
    		return service;
    	} else return null;
    }
    
    /**
     * Returns the current InReachManager. This will return null if
     * the InReachService has not been started.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public InReachManager getInReachManager()
    {
        final InReachService service = getInReachService();
        
        InReachManager manager = null;
        
        if(service != null)
        {
            manager = service.getManager();
        }
        
        return manager;
    }
    
    /**
     * Registers a messenger with the InReachService
     *      
     * @param messenger the client that receives InReachEvent messages.
     * @return True if successful, or false if the service is not started
     * or the messenger is null.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public boolean registerMessenger(Messenger messenger)
    {
        if (messenger == null)
            return false;
        
        final InReachService service = getInReachService();
        
        if (service != null)
        {
            service.registerMessenger(messenger);
        }
        
        return (service != null);
    }
    
    /**
     * Unregisters a messenger from the InReachService
     * 
     * @param messenger the client that receives InReachEvent messages.
     * @return True if successful, or false if the service is not started
     * or the messenger is null.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public boolean unregisterMessenger(Messenger messenger)
    {
        if (messenger == null)
            return false;
        
        final InReachService service = getInReachService();
        
        if (service != null)
        {
            service.unregisterMessenger(messenger);
        }
        
        return (service != null);
    }
    
    /**
     * A convince method for setting the tab.
     * 
     * @param tab the index of the tab to set.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public void setCurrentTab(int tab)
    {
        final Activity parent = getParent();
        
        if (parent == null)
            return;
        
        TabActivity tabActivity = (TabActivity)parent;
        TabHost tabHost = tabActivity.getTabHost();
        tabHost.setCurrentTab(tab);
    }
    
    /**
     * Convince method for setting the current GPS location
     * for an outbound message
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public boolean setMessageLocation(OutboundMessage message)
    {
        if (message == null)
            return false;
        
        final Object obj = getSystemService(Context.LOCATION_SERVICE);
        
        // does this device have a location service?
        if (obj != null)
        {
            // It does, but does that location service have a last 
            // known location from the gps service provider?
            final LocationManager locManager = (LocationManager) obj;
            final Location location = locManager.getLastKnownLocation(
                LocationManager.GPS_PROVIDER);
         
            if (location != null)
            {
                // fill in what is available from the location
                message.setLatitude(location.getLatitude());
                message.setLongitude(location.getLongitude());
            
                if (location.hasAltitude())
                {
                    message.setAltitude((float)location.getAltitude());
                }
                if (location.hasBearing())
                {
                    message.setCourse(location.getBearing());
                }
                if (location.hasSpeed())
                {
                    message.setSpeed(location.getSpeed());
                }
                return true;
            }
        }
        // there was either no location service or last known location
        return false;
    }

}
