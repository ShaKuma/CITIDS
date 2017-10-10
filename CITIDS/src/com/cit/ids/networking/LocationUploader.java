package com.cit.ids.networking;

import java.util.Calendar;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

public class LocationUploader {
	
	private SharedPreferences vPref = null;
	Calendar vCalender = null;  
	
	public LocationUploader(Context vContext) {
		vPref = vContext.getSharedPreferences("CITIDSData", Context.MODE_PRIVATE);
		vCalender = Calendar.getInstance();
	}
	
	/*uploading location on server and returning result*/
	public int uploadLocOnServer(String vIPPort, String vEmail, String vVehicleNo, Location vLocation) {
		String vUrl = "http://" + vIPPort +  "/cittracker/restservices/location/" + vEmail + "/"
				+ vVehicleNo + "/" + vLocation.getLatitude() + "/" + vLocation.getLongitude();
		Log.e("Login Handler", vUrl);
		return runQuery(vUrl);
	}
	
	/*running query and processing returned data*/
	private int runQuery(String vLink) {
		try {
			String vJSON = new HTTPPOSTQueryHandler().executeQuery(vLink);
			JSONObject vJSONObject = new JSONObject(vJSON);
			Log.i("LocationUploader", "Query Completed");
			Boolean vStatus = vJSONObject.getBoolean("status");
			
			/*if any info is changed on the server, clear the user data and logged out*/
			if(vJSONObject.getBoolean("infoupdates")) {
				SharedPreferences.Editor vSPEdit = vPref.edit();
				vSPEdit.clear();
				vSPEdit.commit();
			}
			if(vStatus) {
				/*successful execution*/
				return 1; 
			}
			return 0;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
}