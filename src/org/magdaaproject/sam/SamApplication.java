package org.magdaaproject.sam;

import android.app.Application;
import android.content.Intent;

import org.servalproject.succinctdata.SuccinctDataQueueService;

public class SamApplication extends Application {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		InReachMessageHandler i = InReachMessageHandler.createInstance(this);
		startService(new Intent(this, SuccinctDataQueueService.class));
		i.startService();
	}

}
