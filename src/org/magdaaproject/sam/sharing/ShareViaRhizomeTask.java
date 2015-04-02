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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.servalproject.sam.R;
import org.servalproject.succinctdata.SuccinctDataQueueService;
import org.servalproject.succinctdata.TransportSelectActivity;
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
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;

/**
 * a class used to archive and share an instance file on the Serval Mesh via Rhizome
 *
 */
public class ShareViaRhizomeTask extends AsyncTask<Void, Void, Integer> {

	/*
	 * private class level constants
	 */
	//private static final boolean sVerboseLog = true;
	private static final String sLogTag = "ShareVia logcRhizomeTask";

	private static final int sMaxLoops = 5;
	private static final int sSleepTime = 500;

	/*
	 * private class level variables
	 */
	private Context context;
	private Uri     instanceUri;

	/**
	 * construct a new task object with required variables
	 * 
	 * @param context a context that can be used to get system resources
	 * 
	 * @param instanceUri the URI to the new instance record
	 */
	public ShareViaRhizomeTask(Context context, Uri instanceUri) {
		this.context = context;
		this.instanceUri = instanceUri;
	}

	/*
	 * (non-Javadoc)
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
		int     mLoopCount = 0;

		String mInstancePath = null;

		// TODO: Figure out why the looping is here, since it just adds ~20sec delay from completion of
		// form to when it is processed, seemingly for no useful reason.
		while(mHaveInstance == false && mLoopCount <= sMaxLoops) {

			mCursor = mContentResolver.query(
					instanceUri,
					mProjection,
					null,
					null,
					null);

			// check on the status of the instance
			if(mCursor != null && mCursor.getCount() > 0) {

				mCursor.moveToFirst();

				// status is "complete" ODK has finished with the instance
				if(mCursor.getString(0).equals(InstanceProviderAPI.STATUS_COMPLETE) == true) {
					mInstancePath = mCursor.getString(1);					
				}

			}

			if(mCursor != null) {
				mCursor.close();
			}

			// sleep the thread, an extra sleep even if the instance is finalised won't hurt
			mLoopCount++;
			try {
				Thread.sleep(sSleepTime);
			} catch (InterruptedException e) {
				Log.w(sLogTag, "thread interrupted during sleep unexpectantly", e);
				return null;
			}
		}

		// check to see if an instance file was found
		if(mInstancePath == null) {
			return null;
		}

		// parse the instance path
		File mInstanceFile = new File(mInstancePath);

		// check to make sure file is accessible
		if(FileUtils.isFileReadable(mInstancePath) == false) {
			Log.w(sLogTag, "instance file is not accessible '" + mInstancePath + "'");
			return null;
		}

		// Succinct Data compression and spooling
		// Read file and generate succinct data file for dispatch by inReach, SMS or other similar transport.
		try {
			// Get XML of form instance
			String xmldata = new Scanner(new File(mInstancePath)).useDelimiter("\\Z").next();
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
			while (n!=null && formname == null) {
				NamedNodeMap nnm = n.getAttributes();
				Node id = nnm.getNamedItem("version");				
				if (id != null) 					
					// Canonical form name includes version 
					formname = n.getNodeName();
				formversion = id.getNodeValue();
				n = d.getNextSibling();
			}

			// XXX TODO Android does not reliably return the path to the external sdcard storage,
			// sometimes instead returning the path to the internal sdcard storage.  This breaks
			// succinct data.
			String recipeDir = "/sdcard/"+
					context.getString(R.string.system_file_path_succinct_specification_files_path);
			//			String recipeDir = Environment.getExternalStorageDirectory().getPath()+
			//					context.getString(R.string.system_file_path_succinct_specification_files_path);


			//We check if libsmac is here before continue.
			File lib = new File(context.getFilesDir().getPath()+ "/../lib/libsmac.so");
			if(!lib.isFile()) {
				Log.e(sLogTag, "Failed to load /lib/libsmac.so. Problem may be because ndk-build has not been done before building project.");
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(context, "Failed to load /lib/libsmac.so. Problem may be because ndk-build has not been done before building project.", Toast.LENGTH_LONG).show();
					}
				});
			}

			int result = enqueueSuccinctData(context,xmldata,null,formname,formversion,recipeDir,160,mInstanceFile);
			if ( result != 0) {
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
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Integer result) {

		//TODO determine if need do anything once file shared, especially on UI thread?

	}

	public static int enqueueSuccinctData(final Context context, 
			String xmldata, String xmlformspec,
			String formname,String formversion,
			String recipeDir,int mtu, File mInstanceFile) {

		String errorstring = null;
		String okstring = null;

		try {

			String [] res= org.servalproject.succinctdata.jni.xml2succinctfragments(
					xmldata,
					xmlformspec,
					formname,
					formversion,
					recipeDir,mtu);

			if (res.length<1) {

				// TODO Error producing succinct data -- report
				// XXX - we really need an error notification here, to say that succinct data has failed for this!
				errorstring = "Error making succinct data";

			} else {								
				// Pass message to queue
				Intent intent = new Intent(context, SuccinctDataQueueService.class);
				intent.putExtra("org.servalproject.succinctdata.SUCCINCT", res);
				intent.putExtra("org.servalproject.succinctdata.XML", xmldata);
				intent.putExtra("org.servalproject.succinctdata.FORMNAME", formname);
				intent.putExtra("org.servalproject.succinctdata.FORMVERSION", formversion);
				context.startService(intent);					

				// Now tell the user it has happened
				okstring = "Succinct data message spooled";

			}

			final String e = errorstring;
			final String o = okstring;

			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {

				@Override
				public void run() {
					if (e!=null)
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
			String mTempPath = Environment.getExternalStorageDirectory().getPath();
			mTempPath += context.getString(R.string.system_file_path_rhizome_data);

			if(FileUtils.isDirectoryWriteable(mTempPath) == false) {

				Log.e(sLogTag, "expected rhizome directory is missing");
				return -2;
			}

			// create a zip file of the instance directory
			mTempPath += mInstanceFile.getName() + context.getString(R.string.system_file_instance_extension);

			try {
				// create zip file, including parent directory
				ZipUtil.pack(
						new File(mInstanceFile.getParent()),
						new File(mTempPath),
						true);
			} catch (ZipException e) {
				Log.e(sLogTag, "unable to create the zip file", e);
				return -3;
			}

			// share the file via Rhizome
			try {
				if(RhizomeUtils.shareFile(context, mTempPath)) {
					Log.i(sLogTag, "new instance file shared via Rhizome '" + mTempPath + "'");
					return 0;
				} else {
					return -4;
				}
			} catch (IOException e) {
				Log.e(sLogTag, "unable to share the zip file", e);
				return -5;
			}
		}

		return 0;
	}
}
