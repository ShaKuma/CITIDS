package com.cit.ids.networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityChecker {
	
	private Context vContext = null;
	
	public ConnectivityChecker(Context vContext) {
		this.vContext = vContext;
	}

	/*returning the internet connectivity status of device*/
	public boolean isConnectedToInternet() {
	    ConnectivityManager vConnManager = (ConnectivityManager)vContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	    if(vConnManager != null) {
	    	NetworkInfo vNetworkInfo = vConnManager.getActiveNetworkInfo();
	        if(vNetworkInfo != null && vNetworkInfo.isConnected()) {    
	        	/*Internet connection is available*/
	        	return true;
	        }
		}
	    /*Internet connection is not available*/
	    return false;
	}
}
