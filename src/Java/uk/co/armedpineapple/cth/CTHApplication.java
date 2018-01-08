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
import android.support.multidex.MultiDex;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CTHApplication extends android.app.Application {

    private static final Reporting.Logger Log = Reporting.getLogger("CorsixTH Application");

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
        preferences = getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        Vibrator vib = ((Vibrator) getSystemService(VIBRATOR_SERVICE));
        if (Build.VERSION.SDK_INT >= 11) {
            hasVibration = vib.hasVibrator();
        } else {
            hasVibration = true;
        }



        try {
            InputStream inputStream = getAssets().open(APPLICATION_PROPERTIES_FILE);
            Log.d("Loading properties");
            properties.load(inputStream);

        } catch (IOException e) {
            Log.i("No properties file found");
        }

    }

    public SharedPreferences getPreferences() {
        return preferences;

    }

    public static boolean isTestingVersion() {
        return BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")
                || BuildConfig.FLAVOR.equalsIgnoreCase("alpha")
                || BuildConfig.FLAVOR.equalsIgnoreCase("dev")
                || BuildConfig.FLAVOR.equalsIgnoreCase("beta");
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
