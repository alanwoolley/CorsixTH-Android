/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Vibrator
import android.support.multidex.MultiDex
import java.io.IOException
import java.util.*

class CTHApplication : android.app.Application() {
    var configuration: Configuration? = null
    var hasVibration = false
    var preferences: SharedPreferences? = null
        private set
    val properties = Properties()

    @SuppressLint("NewApi")
    override fun onCreate() {
        super.onCreate()
        preferences = getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        hasVibration = vib.hasVibrator()


        try {
            val inputStream = assets.open(APPLICATION_PROPERTIES_FILE)
            Log.d("Loading properties")
            properties.load(inputStream)

        } catch (e: IOException) {
            Log.i("No properties file found")
        }

    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {

        private val Log = Reporting.getLogger("CorsixTH Application")

        const val PREFERENCES_KEY = "cthprefs"
        private const val APPLICATION_PROPERTIES_FILE = "application.properties"
        private const val LOG_TAG = "CTHApp"

        val isTestingVersion: Boolean
            get() = (BuildConfig.BUILD_TYPE.equals("debug", ignoreCase = true)
                    || BuildConfig.FLAVOR.equals("alpha", ignoreCase = true)
                    || BuildConfig.FLAVOR.equals("dev", ignoreCase = true)
                    || BuildConfig.FLAVOR.equals("beta", ignoreCase = true))
    }
}
