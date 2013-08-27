package uk.co.armedpineapple.cth;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class Utils {
	/**
	 * Checks if Google Play Services is installed and ready to be used
	 * 
	 * @param ctx
	 *          Context
	 * @return true if GPS is available
	 */
	public static boolean hasGooglePlayServices(Context ctx) {
		return GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx) == ConnectionResult.SUCCESS;
	}
}
