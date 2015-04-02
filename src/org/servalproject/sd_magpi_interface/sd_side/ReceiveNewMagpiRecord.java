package org.servalproject.sd_magpi_interface.sd_side;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReceiveNewMagpiRecord extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String recordUUID =  intent.getStringExtra("recordUUID");
		String completedRecord = intent.getStringExtra("recordData");
		String recordBundle = intent.getStringExtra("recordBundle");
		String formSpecification =  intent.getStringExtra("formSpecification");
		

	}

}
