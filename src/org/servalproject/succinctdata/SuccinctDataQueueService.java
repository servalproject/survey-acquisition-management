package org.servalproject.succinctdata;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SuccinctDataQueueService extends Service {

	private SuccinctDataQueueDbAdapter db = null;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    
		// XXX - Create intent listeners data network state changes if we haven't
		// already.
		
		// Check if the passed intent has a message to queue
		try {
			String succinctData[] = intent.getStringArrayExtra("org.servalproject.succinctdata.SUCCINCT");
//			String xmlData = intent.getStringExtra("org.servalproject.succinctdata.XML");
			String formname = intent.getStringExtra("org.servalproject.succinctdata.FORMNAME");
			String formversion = intent.getStringExtra("org.servalproject.succinctdata.FORMVERSION");
			
			// For each piece, create a message in the queue
			if (db == null) {
				db = new SuccinctDataQueueDbAdapter(this);
				db.open();				
			}
			if (succinctData != null) {
				for(int i = 0; i< succinctData.length;i ++) {
					String piece = succinctData[i];
					String prefix = piece.substring(0, 10);
					db.createQueuedMessage(prefix, piece,formname+"/"+formversion);
				}
			}
			
		} catch (Exception e) {
			String s = e.toString();
			Log.e("SuccinctDataqQueueService","Exception: " + s);
		}
	
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
