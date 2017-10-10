package com.cit.ids.utilities;

import com.cit.ids.CITMainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootAppStarter extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context vContext, Intent vReceivedIntent) {
		
		/*starting the application*/
		Intent vIntent = new Intent(vContext, CITMainActivity.class);
        vIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        vContext.startActivity(vIntent);
	}
}