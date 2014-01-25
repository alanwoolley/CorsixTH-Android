/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CTHApplication extends android.app.Application {

    public static final  String PREFERENCES_KEY             = "cthprefs";
    private static final String APPLICATION_PROPERTIES_FILE = "application.properties";
    private static final String LOG_TAG                     = "CTHApp";
    public Configuration configuration;
    public boolean hasVibration = false;
    private SharedPreferences preferences;
    private final Properties properties = new Properties();

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();
        Vibrator vib = ((Vibrator) getSystemService(VIBRATOR_SERVICE));
        if (Build.VERSION.SDK_INT >= 11) {
            hasVibration = vib.hasVibrator();
        } else {
            hasVibration = true;
        }
        preferences = getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);

		/*
         * BugSense helps to discover bugs by reporting unhandled exceptions. If you
		 * want to use this, create a file called application.properties in the
		 * assets folder and insert the line:
		 * 
		 * bugsense.key=<your API key>
		 */

        try {
            InputStream inputStream = getAssets().open(APPLICATION_PROPERTIES_FILE);
            Log.d(LOG_TAG, "Loading properties");
            properties.load(inputStream);
            setupBugsense();

        } catch (IOException e) {
            Log.i(LOG_TAG, "No properties file found");
        }

    }

    private void setupBugsense() {
        if (properties.containsKey("bugsense.key") && BuildConfig.USE_BUGSENSE) {
            Log.d(LOG_TAG, "Setting up bugsense");
            BugSenseHandler.initAndStartSession(this,
                    (String) properties.get("bugsense.key"));
        }
    }

    public SharedPreferences getPreferences() {
        return preferences;

    }

    public Properties getProperties() {
        return properties;
    }

}
