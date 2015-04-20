package org.magdaaproject.sam;

import android.app.Application;

public class SamApplication extends Application {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		InReachMessageHandler.createInstance(this).startService();
	}

}
