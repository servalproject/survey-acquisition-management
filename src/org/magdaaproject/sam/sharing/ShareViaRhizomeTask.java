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
package org.magdaaproject.sam.sharing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.servalproject.sam.R;
import org.servalproject.succinctdata.SuccinctDataQueueDbAdapter;
import org.servalproject.succinctdata.SuccinctDataQueueService;
import org.servalproject.succinctdata.TransportSelectActivity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.magdaaproject.sam.RCLauncherActivity;
import org.magdaaproject.utils.FileUtils;
import org.magdaaproject.utils.serval.RhizomeUtils;
import org.odk.collect.InstanceProviderAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zeroturnaround.zip.ZipException;
import org.zeroturnaround.zip.ZipUtil;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;


/**
 * a class used to archive and share an instance file on the Serval Mesh via
 * Rhizome
 *
 */

class UploadBadRecordTask extends AsyncTask<String, String, Long> {
	
	protected Long doInBackground(String... forms) {
		for (int i = 0; i < forms.length; i++) {
			String xmlForm = forms[i];
			String resultMessage = "Unknown error while uploading indigestible Magpi record to Succinct Data server";

			{
				// Upload bad record to Succinct Data server
				String url = "http://serval1.csem.flinders.edu.au/succinctdata/bad-record-form.php";

				HttpClient httpclient = new DefaultHttpClient();

				HttpPost httppost = new HttpPost(url);

				InputStream stream = new ByteArrayInputStream(
						xmlForm.getBytes());
				InputStreamEntity reqEntity = new InputStreamEntity(stream, -1);
				reqEntity.setContentType("text/xml");
				reqEntity.setChunked(true); // Send in multiple parts if needed
				httppost.setEntity(reqEntity);
				int httpStatus = -1;
				try {
					HttpResponse response = httpclient.execute(httppost);
					httpStatus = response.getStatusLine().getStatusCode();
					if (httpStatus != 200) {
						resultMessage = "Failed to upload indigestible record to Succinct Data server: http result = "
								+ httpStatus;
					} else {
						resultMessage = "Successfully uploaded indigestible record to Succinct Data server: http result = "
								+ httpStatus;
					}
				} catch (Exception e) {
					// resultMessage = "Failed to upload Magpi form to "
					// + "Succinct Data server due to exception: "
					// 		+ e.toString();
					// Just return, don't display an error when trying to upload the form
					return -1L;
				}
				// Do something with response...
				Log.d("succinctdata", resultMessage);
				this.publishProgress(resultMessage);
			}
		}
		Long status = -1L;
		return status;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		// Toast.makeText(context, values[0], Toast.LENGTH_LONG).show();
	}

}



public class ShareViaRhizomeTask extends AsyncTask<Void, Void, Integer> {

	/*
	 * private class level constants
	 */
	// private static final boolean sVerboseLog = true;
	private static final String sLogTag = "ShareVia logcRhizomeTask";

	private static final int sMaxLoops = 5;
	private static final int sSleepTime = 500;

	// Disable sharing via rhizome for now
	private static boolean rhizome_enabled = false;

	private Handler handler = null;
	
	/*
	 * private class level variables
	 */
	private Context context;
	private Uri instanceUri;

	/**
	 * construct a new task object with required variables
	 * 
	 * @param context
	 *            a context that can be used to get system resources
	 * 
	 * @param instanceUri
	 *            the URI to the new instance record
	 */
	public ShareViaRhizomeTask(Context context, Uri instanceUri) {
		this.context = context;
		this.instanceUri = instanceUri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Integer doInBackground(Void... arg0) {

		// get information about this instance
		ContentResolver mContentResolver = context.getContentResolver();

		String[] mProjection = new String[2];
		mProjection[0] = InstanceProviderAPI.InstanceColumns.STATUS;
		mProjection[1] = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;

		Cursor mCursor;

		// get the data, checking until the instance is finalised
		boolean mHaveInstance = false;
		int mLoopCount = 0;

		String mInstancePath = null;

		// TODO: Figure out why the looping is here, since it just adds ~20sec
		// delay from completion of
		// form to when it is processed, seemingly for no useful reason.
		while (mHaveInstance == false && mLoopCount <= sMaxLoops) {

			mCursor = mContentResolver.query(instanceUri, mProjection, null,
					null, null);

			// check on the status of the instance
			if (mCursor != null && mCursor.getCount() > 0) {

				mCursor.moveToFirst();

				// status is "complete" ODK has finished with the instance
				if (mCursor.getString(0).equals(
						InstanceProviderAPI.STATUS_COMPLETE) == true) {
					mInstancePath = mCursor.getString(1);
				}

			}

			if (mCursor != null) {
				mCursor.close();
			}

			// sleep the thread, an extra sleep even if the instance is
			// finalised won't hurt
			mLoopCount++;
			try {
				Thread.sleep(sSleepTime);
			} catch (InterruptedException e) {
				Log.w(sLogTag, "thread interrupted during sleep unexpectantly",
						e);
				return null;
			}
		}

		// check to see if an instance file was found
		if (mInstancePath == null) {
			return null;
		}

		// parse the instance path
		File mInstanceFile = new File(mInstancePath);

		// check to make sure file is accessible
		if (FileUtils.isFileReadable(mInstancePath) == false) {
			Log.w(sLogTag, "instance file is not accessible '" + mInstancePath
					+ "'");
			return null;
		}

		// Succinct Data compression and spooling
		// Read file and generate succinct data file for dispatch by inReach,
		// SMS or other similar transport.
		try {
			// Get XML of form instance
			String xmldata = new Scanner(new File(mInstancePath)).useDelimiter(
					"\\Z").next();
			// Convert to succinct data
			DocumentBuilderFactory factory;
			factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			StringReader sr = new StringReader(xmldata);
			InputSource is = new InputSource(sr);
			Document d = builder.parse(is);
			Node n = d.getFirstChild();
			String formname = null;
			String formversion = null;
			while (n != null && formname == null) {
				NamedNodeMap nnm = n.getAttributes();
				Node id = nnm.getNamedItem("version");
				if (id != null)
					// Canonical form name includes version
					formname = n.getNodeName();
				formversion = id.getNodeValue();
				n = d.getNextSibling();
			}

			// XXX TODO Android does not reliably return the path to the
			// external sdcard storage,
			// sometimes instead returning the path to the internal sdcard
			// storage. This breaks
			// succinct data.
			String recipeDir = "/sdcard/"
					+ context
							.getString(R.string.system_file_path_succinct_specification_files_path);
			// String recipeDir =
			// Environment.getExternalStorageDirectory().getPath()+
			// context.getString(R.string.system_file_path_succinct_specification_files_path);

			// We check if libsmac is here before continue.
			File lib = new File(context.getFilesDir().getPath()
					+ "/../lib/libsmac.so");
			if (!lib.isFile()) {
				Log.e(sLogTag,
						"Failed to load /lib/libsmac.so. Problem may be because ndk-build has not been done before building project.");
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(
								context,
								"Failed to load /lib/libsmac.so. Problem may be because ndk-build has not been done before building project.",
								Toast.LENGTH_LONG).show();
					}
				});
			}

			int result = enqueueSuccinctData(context, xmldata, null, formname,
					formversion, recipeDir, 160, mInstanceFile);
			if (result != 0) {
				// Error queueing SD
				Log.e(sLogTag, "Failed to enqueue succinct data.");
			}
			return result;
		} catch (IOException e) {
			// TODO Error producing succinct data -- report
		} catch (SAXException e) {
			// TODO Couldn't parse XML form instance
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -99;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Integer result) {

		// TODO determine if need do anything once file shared, especially on UI
		// thread?

	}

	public static int enqueueSuccinctData(final Context context,
			String xmldata, String xmlformspec, String formname,
			String formversion, String recipeDir, int mtu, File mInstanceFile) {

		String errorstring = null;
		String okstring = null;

		try {
			SuccinctDataQueueDbAdapter db = new SuccinctDataQueueDbAdapter(context);
			db.open();
			if (db.isThingNew(xmldata) == false) 
				return 0;
			db.close();
		} catch (Exception e) {
		
		}
		
		// smac.dat is called smac.png in assets so that it doesn't get
		// compressed,
		// and thus fall victim to the 1MB asset size limit bug.
		String smacdatfilename = assetToFilename(context, "smac.dat");

		try {
			// Always use same encryption key if debug=1, so that we get deterministic output.
			// This is helpful for debugging SD compression and encryption.
			int debug = 0;
			
			// In case we get a force-quit, we should save the form spec and record to a file
			// Then when we restart, we can check if those files exist, and if so, send a bug
			// report to the SD server
			
			// Create fail-safe file
			String failSafeDirName = Environment.getExternalStorageDirectory().getPath()+
                    context.getString(R.string.system_file_path_succinct_specification_files_path);
			File failSafeDir = new File(failSafeDirName);
			if ( failSafeDir.exists() == false) { 
				boolean result = failSafeDir.mkdirs();
			}
			String failSafeFileName =
                    Environment.getExternalStorageDirectory().getPath()+
                    context.getString(R.string.system_file_path_succinct_specification_files_path)+"/failsafe-form.txt";
			File failSafeFormFile = new File(failSafeFileName);
			failSafeFileName =
                    Environment.getExternalStorageDirectory().getPath()+
                    context.getString(R.string.system_file_path_succinct_specification_files_path)+"/failsafe-record.txt";
			File failSafeRecordFile = new File(failSafeFileName);
			if (failSafeFormFile.exists()) failSafeFormFile.delete();
			if (failSafeRecordFile.exists()) failSafeRecordFile.delete();
			FileOutputStream stream = new FileOutputStream(failSafeFormFile);
            stream.write(xmlformspec.getBytes());
            stream.close();
			stream = new FileOutputStream(failSafeRecordFile);
            stream.write(xmldata.getBytes());
            stream.close();
            			
			String[] res = org.servalproject.succinctdata.jni
					.xml2succinctfragments(xmldata, xmlformspec, formname,
							formversion, recipeDir, smacdatfilename, mtu, debug);

			if ((res.length < 1)
				||(res[0].compareTo("ERROR") == 0)) {

				// TODO Error producing succinct data -- report
				// XXX - we really need an error notification here, to say that
				// succinct data has failed for this!
				errorstring = "Error making succinct data";
				if (res.length > 1) {
					errorstring = errorstring + ": "+ res[1];
					RCLauncherActivity.sawError(res[1]);
				}
				
				// Long vibrate for bad records
				Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
				 // Vibrate for 500 milliseconds
				 v.vibrate(1500);
				
				// Attempt to upload error causing material to SD server.
				try {
					SuccinctDataQueueDbAdapter db = new SuccinctDataQueueDbAdapter(context);
					db.open();
					if (db.isThingNew(xmldata) == false) return 0;
					db.logBadRecord(xmlformspec, xmldata);
					db.close();
				} catch (Exception e) {
				
				}
				
			} else {
				
				// Remove fail-safe file
				if (failSafeFormFile.exists()) failSafeFormFile.delete();
				if (failSafeRecordFile.exists()) failSafeRecordFile.delete();				
				
				// Pass message to queue
				Intent intent = new Intent(context,
						SuccinctDataQueueService.class);
				intent.putExtra("org.servalproject.succinctdata.SUCCINCT", res);
				intent.putExtra("org.servalproject.succinctdata.XML", xmldata);
				intent.putExtra("org.servalproject.succinctdata.XMLFORM",
						xmlformspec);
				intent.putExtra("org.servalproject.succinctdata.FORMNAME",
						formname);
				intent.putExtra("org.servalproject.succinctdata.FORMVERSION",
						formversion);
				context.startService(intent);

				// Now tell the user it has happened
				okstring = "Succinct data message spooled";

				// Short vibrate for good records
				Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
				 // Vibrate for 500 milliseconds
				 v.vibrate(500);
				
			}

			final String e = errorstring;
			final String o = okstring;

			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {

				@Override
				public void run() {
					if (e != null)
						Toast.makeText(context, e, Toast.LENGTH_LONG).show();
					else
						Toast.makeText(context, o, Toast.LENGTH_SHORT).show();
				}
			});

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (false) {

			// check to make sure the rhizome data directory exists
			String mTempPath = Environment.getExternalStorageDirectory()
					.getPath();
			mTempPath += context
					.getString(R.string.system_file_path_rhizome_data);

			if (FileUtils.isDirectoryWriteable(mTempPath) == false) {

				Log.e(sLogTag, "expected rhizome directory is missing");
				return -2;
			}

			// create a zip file of the instance directory
			mTempPath += mInstanceFile.getName()
					+ context
							.getString(R.string.system_file_instance_extension);

			try {
				// create zip file, including parent directory
				ZipUtil.pack(new File(mInstanceFile.getParent()), new File(
						mTempPath), true);
			} catch (ZipException e) {
				Log.e(sLogTag, "unable to create the zip file", e);
				return -3;
			}

			// share the file via Rhizome
			if (rhizome_enabled ) {
				try {
					if (RhizomeUtils.shareFile(context, mTempPath)) {
						Log.i(sLogTag, "new instance file shared via Rhizome '"
								+ mTempPath + "'");
						return 0;
					} else {
						return -4;
					}
				} catch (IOException e) {
					Log.e(sLogTag, "unable to share the zip file", e);
					return -5;
				}
			}
		}

		return 0;
	}

	private static String assetToFilename(Context context, String assetName) {
		// XXX - Should check if file exists, and if so, not extract again.

		AssetManager am = context.getAssets();
		InputStream in = null;
		try {
			in = am.open(assetName);

			// Create new file to copy into.
			File file = new File(context.getApplicationInfo().dataDir
					+ java.io.File.separator + assetName);
			file.createNewFile();

			ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int len = 0;
			while ((len = in.read(buffer)) != -1) {
				byteBuffer.write(buffer, 0, len);
			}
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(byteBuffer.toByteArray());
			stream.close();

			return file.getAbsolutePath();

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
