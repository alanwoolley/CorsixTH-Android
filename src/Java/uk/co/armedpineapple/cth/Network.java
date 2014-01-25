/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

public class Network {

	/**
	 * Checks if there is an active network connection
	 * 
	 * @return true if there is an active network connection
	 */
	public static boolean hasNetworkConnection(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		// getActiveNetworkInfo() can return null if there is no active connection.
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	@SuppressLint("InlinedApi")
	public static boolean isMobileNetwork(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (Build.VERSION.SDK_INT < 13) {
			return netInfo.getType() != ConnectivityManager.TYPE_WIFI;
		}

		return netInfo.getType() != ConnectivityManager.TYPE_WIFI
				&& netInfo.getType() != ConnectivityManager.TYPE_ETHERNET;

	}
}
