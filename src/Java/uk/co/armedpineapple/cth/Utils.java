package uk.co.armedpineapple.cth;

import android.content.Context;

public class Utils {

    private Utils() {

    }

	/**
	 * Checks if Google Play Services is installed and ready to be used
	 * 
	 * @param ctx
	 *          Context
	 * @return true if GPS is available
	 */
	public static boolean hasGooglePlayServices(Context ctx) {
		//return GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx) == ConnectionResult.SUCCESS;
		return false;
	}
}
