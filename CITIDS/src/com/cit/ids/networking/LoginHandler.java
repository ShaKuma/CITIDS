package com.cit.ids.networking;

import org.json.JSONObject;

import com.cit.ids.utilities.TelephonyInfoHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class LoginHandler {
	
	private SharedPreferences vPref = null;
	private String vEmail = null, vPass = null;
	private Context vContext = null;
	
	public LoginHandler(Context vContext) {
		/*initializing the shared preference database*/
		vPref = vContext.getSharedPreferences("CITIDSData", Context.MODE_PRIVATE);
		this.vContext = vContext;
	}

	public int loginUser(String vIPPort, String vEmail, String vPass) {
		this.vEmail = vEmail;
		this.vPass = vPass;
		TelephonyInfoHandler vTeleInfoHandler = new TelephonyInfoHandler(vContext);
		String vUrl = "http://" + vIPPort + "/cittracker/restservices/login/" + vEmail + "/" + vPass + "/"
										+ vTeleInfoHandler.getSimSerial() + "/" + vTeleInfoHandler.getIMEI();
		Log.e("Login Handler", vUrl);
		return runQuery(vUrl);
	}
	
	/*running query and processing returned data*/
	private int runQuery(String vLink) {
		try {
			String vJSON = new HTTPPOSTQueryHandler().executeQuery(vLink);
			JSONObject vJSONObject = new JSONObject(vJSON);
			Boolean vStatus = vJSONObject.getBoolean("status");
			if(vStatus) {
				SharedPreferences.Editor vSPEdit = vPref.edit();
				vSPEdit.putBoolean("loginstatus", true);
	    		vSPEdit.putString("fname", vJSONObject.getString("fname").trim());
	    		vSPEdit.putString("lname", vJSONObject.getString("lname").trim());
	    		vSPEdit.putString("pass", vPass);
	    		vSPEdit.putString("email", vEmail);
	    		vSPEdit.putString("fcontact", vJSONObject.getString("fcontact").trim());
	    		vSPEdit.putString("scontact", vJSONObject.getString("scontact").trim());
	    		vSPEdit.putString("scode", vJSONObject.getString("scode").trim());
	    		vSPEdit.putString("vehicleno", vJSONObject.getString("vehicleno").trim());
	    		vSPEdit.putInt("fcverified", vJSONObject.getInt("fcverified"));
	    		vSPEdit.putBoolean("isnotifysimchange", false);
	    		vSPEdit.putString("simserial", new TelephonyInfoHandler(vContext).getSimSerial());
	    		vSPEdit.commit();
			}
			if(vStatus) {
				return 1; 	/*successful execution*/
			}
			return 0;		/*error on server side*/
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1;			/*if exception occurred*/
	}
}