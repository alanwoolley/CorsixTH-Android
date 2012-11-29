/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {
    
	/** Checks if there is an active network connection 
     *
     *  @return true if there is an active network connection
    */
	public static boolean HasNetworkConnection(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
                
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
        
        // getActiveNetworkInfo() can return null if there is no active connection.
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}
}
