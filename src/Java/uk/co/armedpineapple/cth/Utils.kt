package uk.co.armedpineapple.cth

import android.content.Context

object Utils {

    /**
     * Checks if Google Play Services is installed and ready to be used
     *
     * @param ctx
     * Context
     * @return true if GPS is available
     */
    fun hasGooglePlayServices(ctx: Context): Boolean {
        //return GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx) == ConnectionResult.SUCCESS;
        return false
    }
}
