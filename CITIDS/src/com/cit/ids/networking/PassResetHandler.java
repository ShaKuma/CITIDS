package com.cit.ids.networking;

import org.json.JSONObject;

public class PassResetHandler {
	
	public int resetPassword(String IPPort, String vEmail) {
		return runQuery("http://" + IPPort + "/cittracker/restservices/resetpass/"+vEmail);
	}
	
	private int runQuery(String vLink) {
		try {
			String vJSON = new HTTPPOSTQueryHandler().executeQuery(vLink);
			JSONObject vJSONObject = new JSONObject(vJSON);
			Boolean vStatus = vJSONObject.getBoolean("status");
			if(vStatus) {
				return 1;  /*if password reset request successfully submitted*/
			}
			return 0;		/*if user with specified email doesn't exists*/
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1;	/*if any network error occurred e.g. timeout*/
	}
}
