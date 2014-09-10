package org.servalproject.succinctdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

import org.servalproject.sam.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

public class smsReceived extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent arg1) {
		String succinctPath = Environment.getExternalStorageDirectory().getPath()+
				c.getString(R.string.system_file_path_succinct_specification_files_path);
		String rxSpoolDir = Environment.getExternalStorageDirectory().getPath()+
				c.getString(R.string.system_file_path_succinct_data_rxspool_dir);
		String outputDir = Environment.getExternalStorageDirectory().getPath()+
				c.getString(R.string.system_file_path_succinct_data_output_dir);
				
		//Create Rxspool directory
		File dir = new File(Environment.getExternalStorageDirectory(),
				c.getString(R.string.system_file_path_succinct_data_rxspool_dir));
		dir.mkdirs();
				
		//Create ouput directory
		File dirOutput = new File(Environment.getExternalStorageDirectory(),
				c.getString(R.string.system_file_path_succinct_data_output_dir));
		dirOutput.mkdirs();
				
				
		// Read any new SMS messages and put in the rxspool directory for processing
		Cursor cursor = c.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
		cursor.moveToFirst();

		do{
			try {
				//Get content from SMS
				String msgData = "";
				int idx = cursor.getColumnIndex("body");				   
				msgData = cursor.getString(idx);
				
				//Check if no space in the SMS, avoid reading not succinct-data SMS.
				if (msgData.indexOf(' ')==-1) {
					byte[] decodedBytes = android.util.Base64.decode(msgData,android.util.Base64.DEFAULT);
					
					//Get MD5 Hash from content
					byte[] b = MessageDigest.getInstance("MD5").digest(decodedBytes);
					
					//Try to see if file already exists in Rxspool
					String filename = String.format("%02x%02x%02x%02x%02x%02x.sd", b[0],b[1],b[2],b[3],b[4],b[5]);
					File file = new File(dir, filename);
					if (!file.exists()) {
						// Write succinct data to file
						FileOutputStream f = new FileOutputStream(file);
						f.write(decodedBytes);
						f.close();
					} else {
						Log.d("SAM",filename+" already exists in Rxspool, dont need to read older SMS.");	
						break;
					}
				} else {
					Log.d("SAM","This SMS contains spaces,it's not a succinct-data.");	
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();					
			}
		}while(cursor.moveToNext());
						
		Log.d("SAM","About to call smac");
		// org.servalproject.succinctdata.jni.updatecsv(succinctPath,rxSpoolDir,outputDir);
						
		try {
			AssetManager assetManager = c.getAssets();
			InputStream in = assetManager.open("smac");

			File outDir = new File(c.getFilesDir().getPath()+ "/bin");
			outDir.mkdirs();
			File outFile = new File(c.getFilesDir().getPath()+ "/bin", "smac");
			FileOutputStream out = new FileOutputStream(outFile);
			int len;
			byte[] buff = new byte[8192];
			while ((len = in.read(buff)) > 0) {
				out.write(buff, 0, len);
			}
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			outFile.setExecutable(true);			          
		} catch(IOException e) {
			Log.e("tag", "Failed to load assets. Problem may be because ndk-build has not been done before building project.", e);
			Log.e("tag", "Failed to copy asset file smac", e);
		}       
				
		String cmd = c.getFilesDir().getPath()+ "/bin/smac"; 
		Process proc;
		try {
			proc = new ProcessBuilder(cmd,"recipe", "decompress", succinctPath,rxSpoolDir,outputDir).redirectErrorStream(true).start();
			DataInputStream in = new DataInputStream(proc.getInputStream());
			OutputStream out = proc.getOutputStream();
			proc.waitFor();
		} catch (Throwable e) {
			Log.e("tag", "Failed to run smac", e);
		}
				
		// Create map visualisations
		try {
			proc = new ProcessBuilder(cmd,"recipe", "map", succinctPath,outputDir).redirectErrorStream(true).start();
			DataInputStream in = new DataInputStream(proc.getInputStream());
			OutputStream out = proc.getOutputStream();
			proc.waitFor();
		} catch (Throwable e) {
			Log.e("tag", "Failed to run smac", e);
		}
				
		//Send map visualisations online
//		String [] allFilesToUpload = new File(outputDir+"/maps/").list();
//		for (int i=0; i<allFilesToUpload.length;i++){ 
//			uploadFile(outputDir+"/maps/"+allFilesToUpload[i]);
//		}
	}

	public int uploadFile(String sourceFileUri) {
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	StrictMode.setThreadPolicy(policy); 
    
    String fileName = sourceFileUri;
    String upLoadServerUri = "http://dev.malossane.fr/modules/serval/UploadToServer.php";
	
    HttpURLConnection conn = null;
    DataOutputStream dos = null; 
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024 * 1024;
    File sourceFile = new File(sourceFileUri);
     
    if (!sourceFile.isFile()) {
         Log.e("uploadFile", "Source File not exist :"+fileName);
         return 0; 
    }
    else
    {
         int serverResponseCode;
		try {
              
               // open a URL connection to the Servlet
             FileInputStream fileInputStream = new FileInputStream(sourceFile);
             URL url = new URL(upLoadServerUri);
              
             // Open a HTTP  connection to  the URL
             conn = (HttpURLConnection) url.openConnection();
             conn.setDoInput(true); // Allow Inputs
             conn.setDoOutput(true); // Allow Outputs
             conn.setUseCaches(false); // Don't use a Cached Copy
             conn.setRequestMethod("POST");
             conn.setRequestProperty("Connection", "Keep-Alive");
             conn.setRequestProperty("ENCTYPE", "multipart/form-data");
             conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
             conn.setRequestProperty("uploaded_file", fileName);
              
             dos = new DataOutputStream(conn.getOutputStream());
    
             dos.writeBytes(twoHyphens + boundary + lineEnd);
             dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                       + fileName + "\"" + lineEnd);
              
             dos.writeBytes(lineEnd);
    
             // create a buffer of  maximum size
             bytesAvailable = fileInputStream.available();
    
             bufferSize = Math.min(bytesAvailable, maxBufferSize);
             buffer = new byte[bufferSize];
    
             // read file and write it into form...
             bytesRead = fileInputStream.read(buffer, 0, bufferSize); 
                
             while (bytesRead > 0) {
                  
               dos.write(buffer, 0, bufferSize);
               bytesAvailable = fileInputStream.available();
               bufferSize = Math.min(bytesAvailable, maxBufferSize);
               bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
                
              }
    
             // send multipart form data necesssary after file data...
             dos.writeBytes(lineEnd);
             dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
    
             // Responses from the server (code and message)
             serverResponseCode = conn.getResponseCode();
             String serverResponseMessage = conn.getResponseMessage();
               
             Log.i("uploadFile", "HTTP Response is : "
                     + serverResponseMessage + ": " + serverResponseCode);
              
             if(serverResponseCode == 200){
            	 String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                         +" http://dev.malossane.fr/modules/serval/uploads/"
                         +fileName;
            	 Log.i("uploadFile", msg);
            	             
             }   
              
             //close the streams //
             fileInputStream.close();
             dos.flush();
             dos.close();
               
        } catch (MalformedURLException ex) {
            ex.printStackTrace();  
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex); 
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Upload file to server Exception", "Exception : "
                                             + e.getMessage(), e); 
        }   
        return serverResponseCode;
         
     } // End else block
   } 

}
