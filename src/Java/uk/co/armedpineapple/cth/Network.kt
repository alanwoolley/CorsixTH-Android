/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth

import android.content.Context
import android.net.ConnectivityManager

object Network {

    /**
     * Checks if there is an active network connection
     *
     * @return true if there is an active network connection
     */
    fun hasNetworkConnection(ctx: Context): Boolean {
        val cm = ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val netInfo = cm.activeNetworkInfo

        // getActiveNetworkInfo() can return null if there is no active connection.
        return netInfo != null && netInfo.isConnected
    }

    fun isMobileNetwork(ctx: Context): Boolean {
        val cm = ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val netInfo = cm.activeNetworkInfo

        return netInfo?.type != ConnectivityManager.TYPE_WIFI && netInfo?.type != ConnectivityManager.TYPE_ETHERNET

    }
}
