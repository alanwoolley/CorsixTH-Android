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

import com.bugsense.trace.BugSenseHandler;

public class CTHApplication extends android.app.Application {

	public static final String	PREFERENCES_KEY	= "cthprefs";
	private SharedPreferences		preferences;
	private Configuration				configuration;
	private Properties					properties			= new Properties();

	@Override
	public void onCreate() {
		super.onCreate();

		preferences = getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);

		configuration = Configuration.loadFromPreferences(this, preferences);

		/*
		 * BugSense helps to discover bugs by reporting unhandled exceptions. If you
		 * want to use this, create a file called application.properties in the
		 * assets folder and insert the line:
		 * 
		 * bugsense.key=<your API key>
		 */

		try {
			InputStream inputStream = getAssets().open("application.properties");
			Log.d(getClass().getSimpleName(), "Loading properties");
			properties.load(inputStream);
			setupBugsense();

		} catch (IOException e) {
			Log.i(getClass().getSimpleName(), "No properties file found");
		}

	}

	private void setupBugsense() {
		if (properties.containsKey("bugsense.key")) {
			Log.d(getClass().getSimpleName(), "Setting up bugsense");
			BugSenseHandler.initAndStartSession(this,
					(String) properties.get("bugsense.key"));
		}
	}

	public SharedPreferences getPreferences() {
		return preferences;

	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public Properties getProperties() {
		return properties;
	}

}
