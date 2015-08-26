package org.servalproject.succinctdata;

import java.io.File;

import org.magdaaproject.sam.RCLauncherActivity;
import org.magdaaproject.sam.sharing.ShareViaRhizomeTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ReceiveNewMagpiRecord extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String recordUUID =  intent.getStringExtra("recordUUID");
		String completedRecord = intent.getStringExtra("recordData");
		String recordBundle = intent.getStringExtra("recordBundle");
		String formSpecification =  intent.getStringExtra("formSpecification");
		
		RCLauncherActivity.sawMagpiRecord();			
		
		Bundle b = intent.getExtras();
		
		int result = ShareViaRhizomeTask.enqueueSuccinctData(context, 
					completedRecord, formSpecification,
					null, null, null, 160, null);
		if ( result != 0) {
			// Error queueing SD
			Log.e("SuccinctData", "Failed to enqueue succinct data received from magpi.");
		}
		
	}

}
