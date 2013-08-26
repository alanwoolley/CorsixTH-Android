/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import uk.co.armedpineapple.cth.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

public class CTHApplication extends android.app.Application {

	private static final String	LOG_TAG	= "CTHApp";
	
	public static final String	PREFERENCES_KEY	= "cthprefs";
	private SharedPreferences		preferences;
	public Configuration				configuration;
	private Properties					properties			= new Properties();
	public boolean							debugMode				= false;

	@Override
	public void onCreate() {
		super.onCreate();

		preferences = getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);

		/*
		 * BugSense helps to discover bugs by reporting unhandled exceptions. If you
		 * want to use this, create a file called application.properties in the
		 * assets folder and insert the line:
		 * 
		 * bugsense.key=<your API key>
		 */

		try {
			InputStream inputStream = getAssets().open("application.properties");
			Log.d(LOG_TAG, "Loading properties");
			properties.load(inputStream);
			debugMode = Boolean.parseBoolean(properties.getProperty("app.debug",
					"false"));
			setupBugsense();

		} catch (IOException e) {
			Log.i(LOG_TAG, "No properties file found");
		}

	}

	private void setupBugsense() {
		if (properties.containsKey("bugsense.key") && !debugMode) {
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
