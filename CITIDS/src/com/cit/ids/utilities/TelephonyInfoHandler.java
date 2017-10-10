package com.cit.ids.utilities;

import android.content.Context;
import android.telephony.TelephonyManager;

public class TelephonyInfoHandler {

	TelephonyManager vTelephoneMgr = null;
	
	public TelephonyInfoHandler(Context vContext) {
		vTelephoneMgr = (TelephonyManager)vContext.getSystemService(Context.TELEPHONY_SERVICE);
	}
	
	/*returning the SIM serial number*/
	public String getSimSerial() {
		return vTelephoneMgr.getSimSerialNumber();
	}
	
	/*returning the IMEI number*/
	public String getIMEI() {
		return vTelephoneMgr.getDeviceId();
	}
	
	/*returning status whether SIM card is present or not*/
	public Boolean isSimPresent() {
		return vTelephoneMgr.getSimState() != TelephonyManager.SIM_STATE_ABSENT;
	}
}