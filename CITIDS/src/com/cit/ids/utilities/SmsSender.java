package com.cit.ids.utilities;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;

public class SmsSender extends Service {

	private SmsManager vSmsMgr = null;
	
	public SmsSender() {
		vSmsMgr = SmsManager.getDefault();
	}
	
	/*Send SMS to the number defined in vRecipient Variable*/
	public void sendSms(String vRecipient, String vSmsBody) {
		vSmsMgr.sendTextMessage(vRecipient, null, vSmsBody, null, null);
		this.stopSelf();
	}
	
	@Override
	public IBinder onBind(Intent vIntent) {
		return null;
	}
}
