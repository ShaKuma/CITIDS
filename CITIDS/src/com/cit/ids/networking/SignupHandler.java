package com.cit.ids.networking;

import org.json.JSONObject;
import com.cit.ids.utilities.TelephonyInfoHandler;
import android.content.Context;
import android.util.Log;

public class SignupHandler {

	private Context vContext = null;
	
	public SignupHandler(Context vContext) {
		this.vContext = vContext;
	}
	
	public Integer signUpUser(String vIPPort, String vFName, String vLName, String vPass, String vEmail, 
				String vFContact, String vSContact, String vSecretCode, String vVehicleNo, String vAddress) {
		
		/*getting the SIM serial and device IMEI number*/
		String vSIMSerial = new TelephonyInfoHandler(vContext).getSimSerial();
		String vIMEINo = new TelephonyInfoHandler(vContext).getIMEI();
		int vFContactVerified = 0;
		
		/*sending the URL and getting the result*/
		return runQuery("http://" + vIPPort + "/cittracker/restservices/signup/"+ vFName + "/" + vLName + "/" 
							+ vPass + "/" + vEmail+ "/" + vFContact + "/" + vSContact + "/" + vSecretCode + "/" 
							+ vVehicleNo + "/" + vSIMSerial + "/" + vIMEINo + "/" + vAddress + "/" + vFContactVerified);
	}
	
	/*running query and processing returned data*/
	private Integer runQuery(String vLink) {
		try {
			String vJSON = new HTTPPOSTQueryHandler().executeQuery(vLink);
			JSONObject vJSONObject = new JSONObject(vJSON);
			if (vJSONObject.getBoolean("status")) {
				Log.e("SignupHandler", "1");
				return 1;
			}
			Log.e("SignupHandler", "0");
			return 0;
		} catch(Exception e) {
			e.printStackTrace();
		}
		Log.e("SignupHandler", "-1");
		return -1;
	}
}