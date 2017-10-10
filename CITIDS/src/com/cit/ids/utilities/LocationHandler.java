package com.cit.ids.utilities;

import com.cit.ids.networking.LocationUploader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationHandler extends Service implements 
			ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	private static final int MIN_TIME_FOR_LOC_UPDATE = 5000;
	
	private LocationClient vLocationClient = null;
	private Context vContext = null;
	private Location vLocation = null;
	private boolean vIsUpdatedLocation = false;
	private boolean vIsSendSmsOnConnected = false;
	private boolean vIsUpdateOnServer = false;
	private Boolean vIsLocClientConnected = false;
	private String vRecipient = null;
	private String vVehicleNo = null;
	private String vEmail = null;
	private String vIPPort = null;
	
	/*Location updates request for high accuracy updates*/
	private static final LocationRequest LOCATIONREQUEST = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(MIN_TIME_FOR_LOC_UPDATE);
	
	/*constructor if no SMS is required*/
	public LocationHandler(Context vContext, Boolean vIsUpdateOnServer) {
		vLocationClient = new LocationClient(vContext, this, this);
    	if(vLocationClient != null) {
    		vLocationClient.connect();
    		vIsSendSmsOnConnected = false;
    		this.vIsUpdateOnServer = vIsUpdateOnServer;
    		this.vContext = vContext;
    		
    		/*initializing the shared preference database*/
    		SharedPreferences vPref = vContext.getSharedPreferences("CITIDSData", Context.MODE_PRIVATE);
    		SharedPreferences vIPPortPref = vContext.getSharedPreferences("CITIDSIPPort", Context.MODE_PRIVATE);
    		vVehicleNo = vPref.getString("vehicleno", "ABC");
    		vEmail = vPref.getString("email", "ABC");
    		vIPPort = vIPPortPref.getString("ipport", "localhost:8085");
    	}
	}
	
	/*constructor if SMS is required*/
	public LocationHandler(Context vContext, String vRecipient) {
		this.vRecipient = vRecipient;
		vLocationClient = new LocationClient(vContext, this, this);
    	if(vLocationClient != null) {
    		vLocationClient.connect();
    		vIsSendSmsOnConnected = true;
    		this.vIsUpdateOnServer = false;
    	}
	}

	/*return the most recent location*/
	public Location getLocation() {
		vIsUpdatedLocation = false;
		return vLocation;
	}
	
	/*returning the status of loc client*/
	public Boolean isLocClientConnected() {
		return vIsLocClientConnected;
	}
	
	/*if location updated after reading previous value*/
	public boolean isUpdatedLocation() {
		return vIsUpdatedLocation;
	}
	
	/*send location SMS*/
	public Boolean sendLocationSms(String vRecipient) {
		Boolean vResult = false;
		String vSmsBody = null;
		if(vLocation != null) {
			vSmsBody = "Latitude: " + vLocation.getLatitude() + "\n"
							+ "Longitude: " + vLocation.getLongitude();
			vResult = true;
		} else {
			vSmsBody = "Sorry! something happend. we can't send you the location.";
		}
		new SmsSender().sendSms(vRecipient, vSmsBody);
		return vResult;
	}
	
	/*uploading location on the server*/
	public void updateLocOnServer() {
		if(vLocation != null) {
			new UploadLocationAsync().execute();
		}
	}
	
	/*called when location is changed*/
	@Override
	public void onLocationChanged(Location vLocation) {
		this.vLocation = vLocation;
		vIsUpdatedLocation = true;
		Log.d("Location", "Location Changed");
	}

	/*connected to the location client*/
	@Override
	public void onConnected(Bundle vBundle) {
		
		/*client is connected*/
		vIsLocClientConnected = true;
		/*getting last known good location*/
		vLocation = vLocationClient.getLastLocation();
		
		if(vIsUpdateOnServer) {
			vIsUpdateOnServer = false;
			updateLocOnServer();
			/*implicitly exiting the service*/
			disconnectLocationHandler();
		} else if(vIsSendSmsOnConnected) {
			vIsSendSmsOnConnected = false;
			sendLocationSms(vRecipient);
			/*implicitly exiting the service*/
			disconnectLocationHandler();
		} else {
			vLocationClient.requestLocationUpdates(LOCATIONREQUEST, this);
			Log.d("Location", "Connected");
		}
	}
	
	/*async task to upload location on server*/
	private class UploadLocationAsync extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... vArgs) {
			Log.e("LocHandlerAsync", vLocation.getLatitude() + " " + vLocation.getLongitude());
			try {
				return new LocationUploader(vContext).uploadLocOnServer(vIPPort, vEmail, vVehicleNo, vLocation);
			} catch(Exception e) {
				e.printStackTrace();
			}
			return -1;
		}
		
		@Override
		protected void onPostExecute(Integer vResult) {	
			
			Log.e("LocHandlerAsync", vResult + "");
			if(vResult == 1) {
				/*successful*/
				
			} else if(vResult == 0) {
				/*error at server side*/
				
			} else {
				/*timeout or host unreachable*/
				
			}
			super.onPostExecute(vResult);
		}
	}
	
	/*explicitly terminating the location service*/
	public void disconnectLocationHandler() {
		this.onDestroy();
	}
	
	/*releasing the resources*/
	@Override
	public void onDestroy() {
		vLocationClient.disconnect();
		vIsLocClientConnected = false;
		super.onDestroy();
	}

	@Override
	public void onDisconnected() {}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {}
	
	@Override
	public IBinder onBind(Intent vIntent) {
		return null;
	}
}