package uk.co.armedpineapple.corsixth;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public class CorsixTHApplication extends android.app.Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Properties properties = new Properties();
		try {
			InputStream inputStream = getAssets()
					.open("application.properties");
			Log.d(getClass().getSimpleName(), "Loading properties");
			properties.load(inputStream);

			if (properties.containsKey("bugsense.key")) {
				Log.d(getClass().getSimpleName(), "Setting up bugsense");
				BugSenseHandler.setup(this,
						(String) properties.get("bugsense.key"));
			}

		} catch (IOException e) {
			Log.i(getClass().getSimpleName(), "No properties file found");
		}

	}

}
