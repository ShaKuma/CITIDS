package com.cit.ids.networking;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class HTTPPOSTQueryHandler {
	
	/*returning the JSON result as string*/
	public String executeQuery(String vLink) {
		StringBuilder vResult = new StringBuilder();
		try {
			/*creating HTTP request*/
			HttpClient vHttpClient = new DefaultHttpClient();
			
			/*Execute HTTP Post Request*/
		    HttpResponse vResponse = vHttpClient.execute(new HttpPost(vLink));
		    Log.e("HTTPPOSTQueryHandler", "" + vResponse.getStatusLine());
		    HttpEntity vEntity = vResponse.getEntity();
		    
		    /*if returned data is not null*/
		    if(vEntity != null) {
		        BufferedReader vBufferedReader = new BufferedReader(
						new InputStreamReader(vEntity.getContent()), 8);
				String vTemp;
				while((vTemp = vBufferedReader.readLine()) != null) {
					vResult.append(vTemp + "\n");
				}
				/*releasing resources*/
				vBufferedReader.close();
		    }
		} catch(Exception e) {
			e.printStackTrace();
		}
		return vResult.toString();
	}
}
