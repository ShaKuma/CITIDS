package com.cit.ids.networking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;

public class HTTPGETQueryHandler {
	
	/*executing the HTTP query & returning the JSON result as a string*/
	public String executeQuery(String vLink) {	
		StringBuilder vResult = new StringBuilder();
		try {
			URL vURL = new URL(vLink);
			HttpURLConnection vHttpURLConnection = (HttpURLConnection)vURL.openConnection();
			vHttpURLConnection.connect();
			
			/*printing status code in logcat*/
			Log.e("HTTPGETQueryHandler", vHttpURLConnection.getResponseCode() + "");
			
			/*if the connection is successful*/
			if(vHttpURLConnection.getResponseCode() == 200) {
				BufferedReader vBufferedReader = new BufferedReader(
						new InputStreamReader(vHttpURLConnection.getInputStream()), 8);
				String vTemp;
				while((vTemp = vBufferedReader.readLine()) != null) {
					vResult.append(vTemp + "\n");
				}
				/*releasing resources*/
				vBufferedReader.close();
				vHttpURLConnection.disconnect();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return vResult.toString();
	}
}