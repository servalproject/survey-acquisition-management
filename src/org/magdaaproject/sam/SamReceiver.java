package org.magdaaproject.sam;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by jeremy on 20/04/15.
 */
public class SamReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
			InReachMessageHandler.getInstance().onBluetoothStateChanged();
	}
}
