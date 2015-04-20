package com.delorme.inreachapp.service.bluetooth;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import org.magdaaproject.sam.InReachMessageHandler;
import org.magdaaproject.sam.RCLauncherActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Utilizes that demonstrate connecting to an inReach. These methods
 * should not be invoked on the main thread.
 * 
 * @author Eric Semle
 * @since inReachApp (07 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
public class BluetoothUtils
{
    /**
     * This method will return an inReach that is bonded to
     * the device. If uses the prefix "inReach" to determine
     * if the device is an inReach. Kind of a hack, but it
     * does work. Calling this from a different thread will
     * require that Looper.prepare() is called before this
     * method is invoked.
     * 
     * @return BluetoothDevice A bonded inReach or null if nothing
     * is found.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public static BluetoothDevice findBondedInReachDevice()
    {
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null)
        {
            return null;
        }
      
        final Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
        // changes do to be able to know how many inReach are paired to the phone
        BluetoothDevice theDevice = null;
        InReachMessageHandler.setInReachNumber(0);
        for (BluetoothDevice device : bondedDevices)
        {
            if (BluetoothUtils.isInReachDevice(device))
            {   
           		InReachMessageHandler.setInReachNumber(InReachMessageHandler.getInReachNumber() + 1);
            	theDevice = device;      	                
            }
        }

        return theDevice;
    }
    
    /**
     * This method will return true if the name of the Bluetooth device
     * starts with the "inReach" prefix
     * 
     * @return boolean True if inReach, otherwise returns false
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public static boolean isInReachDevice(BluetoothDevice device)
    {
        if (device == null || device.getName() == null)
            return false;
        
        return device.getName().startsWith(INREACH_DEVICE_PREFIX);
    }
    
    
    /**
     * This method will return true if the default BluetoothAdapter is enabled.
     * 
     * @return boolean True if BluetoothAdapter enabled, otherwise returns false
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public static boolean isBluetoothAdapterOn()
    {
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null)
        {
            return false;
        }
        
        return btAdapter.isEnabled();
    }
    
    /**
     * A simplified method for connected to a bonded inReach.
     * 
     * @return BluetoothSocket A connected socket or null if their are no
     * devices or the connection.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public static BluetoothSocket connectToBondedInReach()
    {
        final BluetoothDevice device = findBondedInReachDevice();
        
        if (device == null)
        {
            // there are not any bonded inReachs
            return null;
        }
        
        return BluetoothUtils.connectToDevice(device);
    }
    
    /**
     * This is a helper method to connect to a Bluetooth device. It should not
     * be called from the main thread of execution.
     * 
     * It isn't the best example. The biggest issue is that socket.connect()
     * blocks. If the user attempts to stop the thread, the thread will block
     * until this returns. A better example would be to store the socket as
     * a local member variable. When the thread is stopped call socket.close()
     * and that will release the socket.connect().
     * 
     * Please note that there is another interesting Android dead lock bug
     * that is associated with calling the close while connecting. It could
     * deadlock.. The easiest solution is to let the thread go and zombify.
     * The only issue with that is you can only build up so many of those
     * until Bluetooth stops working. Another fix is to toggle Bluetooth off / on.
     * 
     * http://code.google.com/p/android/issues/detail?id=14549
     * 
     * @param device A Bluetooth device that supports SPP.
     * @return BluetoothSocket A connected socket or null if it fails.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public static BluetoothSocket connectToDevice(BluetoothDevice device)
    {  
        if (device == null)
            return null;
        
        BluetoothSocket socket = createSocket(device);
        
        if (socket != null)
        {
            final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btAdapter != null)
            {
                // this should be called before a connect.
                btAdapter.cancelDiscovery();
            }
            
            
            RCLauncherActivity.notifyBluetoothInSocketConnect(true);
            try
            {
                // On Samsung devices this can hang forever if the previous Bluetooth socket was not properly closed.
            	// We work around this by requesting bluetooth be turned off in 20 seconds unless we return in that time.
                socket.connect();
            }
            catch (IOException e)
            {
                Log.d(DEBUG_TAG, "Failed to connect to device.", e);
                socket = null;
            }
            RCLauncherActivity.notifyBluetoothInSocketConnect(false);
            RCLauncherActivity.requestUpdateUI();
        }

        return socket;
    }
    
    /**
     * This method will create the "best" BluetoothSocket based on what is
     * available. There are three possible methods to create a socket.
     * 
     * @param device A Bluetooth device that supports SPP (Serial Port Protocol)
     * @return BluetoothSocket A Bluetooth socket that is ready to call connect
     * or null if all methods of creating a socket fail.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public static BluetoothSocket createSocket(BluetoothDevice device)
    {
        if (device == null)
            return null;
        
        // First try to create an insecure socket. This was first introduced
        // with Android 2.3.3 (Gingerbread). It usually works between with
        // anything Gingerbread and above..
        BluetoothSocket socket = createInsecureRfCommSocket(device, SPP_UUID);
        
        if (socket != null)
            return socket;
        
        // The second method is to try a hidden method. There are numerous
        // stackoverflow posts on this method. It isn't as good as the first
        // method, but it is much better then the last.
        socket = createRfCommSocket(device, 1);
        
        if (socket != null)
            return socket;
        
        // This is the last documented method. It will create a secure socket.
        // It will work with inReach 1.0, but might not work with 1.5. Hopefully
        // it will not come down to this method..
        return createSecureRfCommSocket(device, SPP_UUID);
    }
        
    /**
     * Creates an Insecure RFCOMM Socket. This is available with Gingerbread
     * and above. The InReach SDK support Eclair 2.1 or above. This method
     * uses reflection to attempt to invoke the method if it is available.
     * Works best for Gingerbread 2.3.3 or above. 
     * 
     * @param device A Bluetooth device that is already bonded.
     * @param uuid service record UUID to lookup RFCOMM channel
     * @return A RFCOMM BluetoothSocket ready for an outgoing connection
     * or null if this method is not available.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public static BluetoothSocket createInsecureRfCommSocket(BluetoothDevice device, UUID uuid)
    {
        BluetoothSocket socket = null;
        
        try
        {
            Method method = device.getClass().getMethod(
                "createInsecureRfcommSocketToServiceRecord", 
                new Class[] { UUID.class });
        
            socket = (BluetoothSocket) method.invoke(device, 
                    new Object[] {uuid});
        }
        catch (Exception e)
        {
            Log.d(DEBUG_TAG, "Failed to create insecure rfcomm socket.", e);
            socket = null;
        }
        
        return socket;
    }
    
    /**
     * Creates an RFCOMM BluetoothSocket. This uses a hidden method. It is
     * recommended before attempting to use the Secure RFCOMM method.
     * 
     * @param device A Bluetooth device that is already bonded.
     * @param channel RFCOMM channel to for the socket. Usually 1.
     * @return A RFCOMM BluetoothSocket ready for an outgoing connection
     * or null if this method is not available.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public static BluetoothSocket createRfCommSocket(BluetoothDevice device, int channel)
    {
        BluetoothSocket socket = null;
        
        try
        {
            Method method = device.getClass().getMethod(
                "createRfcommSocket", new Class[] { int.class });
    
            socket = (BluetoothSocket) method.invoke(device, channel);
        }
        catch (Exception e)
        {
            Log.d(DEBUG_TAG, 
                "Failed to create rfcomm socket for channel " + channel, e);
            socket = null;
        }
        
        return socket;
    }
    
    /**
     * Creates a Secure RFCOMM BluetoothSocket. This is the last available. It
     * is not recommended for inReach 1.5.
     * 
     * @param device A Bluetooth device that is already bonded.
     * @param uuid service record UUID to lookup RFCOMM channel
     * @return A RFCOMM BluetoothSocket ready for an outgoing connection
     * or null if this method is not available.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    public static BluetoothSocket createSecureRfCommSocket(BluetoothDevice device, UUID uuid)
    {
        BluetoothSocket socket = null;
        
        try
        {
            socket = device.createRfcommSocketToServiceRecord(uuid);
        }
        catch (Exception e)
        {
            Log.d(DEBUG_TAG, 
                "Failed to create rfcomm socket with uuid " + uuid.toString(), e);
            socket = null;
        }
        
        return socket;
    }
    
    //! The prevent for the name of an inReach Bluetooth device.
    private static final String INREACH_DEVICE_PREFIX = "inReach";
    
    //! Serial Port Protocol service record UUID 
    private static final UUID SPP_UUID = 
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");    

    //! A tag for logcat
    private static final String DEBUG_TAG = "BluetoothUtils";
    
}
