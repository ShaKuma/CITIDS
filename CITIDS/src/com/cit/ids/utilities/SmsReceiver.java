package com.cit.ids.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context vContext, Intent vIntent) {
		SharedPreferences vPref = vContext.getSharedPreferences("CITIDSData", Context.MODE_PRIVATE);
		if(vPref.getBoolean("loginstatus", false)){
			Bundle vBundle = vIntent.getExtras();
			SmsMessage[] vSmsMessage = null;
			if(vBundle != null) {
				Object[] vPdus = (Object[])vBundle.get("pdus");
				vSmsMessage = new SmsMessage[vPdus.length];
				
				/*Loop if receive multiple SMS at the same time*/
				for(int i = 0; i < vSmsMessage.length; i++) {
					vSmsMessage[i] = SmsMessage.createFromPdu((byte[])vPdus[i]);
					/*if received data is "secretcode password getlocation"*/
					if(vSmsMessage[i].getMessageBody().toString().equals(vPref.getString("scode", null) + " " 
													+ vPref.getString("pass", null) + " " + " getlocation")) {  
					
						/*return location*/
						Log.i("SmsReceiver", "location sms received");
						new LocationHandler(vContext, vSmsMessage[i].getOriginatingAddress());
						
						/*aborting further broadcasting of SMS*/
						abortBroadcast();
					}
				}
			}
		}
	}
}
