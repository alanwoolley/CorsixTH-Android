// Copyright (C) 2012 Alan Woolley
// 
// See LICENSE.TXT for full license
//
package uk.co.armedpineapple.cth;

import java.util.Properties;
import uk.co.armedpineapple.cth.R;

import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public abstract class CTHActivity extends Activity {

	private static final String	LOG_TAG	= "CTHActivity";
	
	public CTHApplication	app;
	boolean								trackingSession	= false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (CTHApplication) getApplication();

	}

	@SuppressWarnings("nls")
	@Override
	protected void onStart() {
		super.onStart();

		Properties p = app.getProperties();

		// Check if Flurry is enabled

		if (p != null && p.containsKey("flurry.key")) {
			Log.d(LOG_TAG, "Starting Flurry Session");
			FlurryAgent.setCaptureUncaughtExceptions(Boolean.parseBoolean(p
					.getProperty("flurry.catcherrors", "false")));
			FlurryAgent.onStartSession(this, p.getProperty("flurry.key"));

			// Log Flurry events if app.debug=true
			FlurryAgent.setLogEnabled(app.debugMode);
			trackingSession = true;
		} else {
			Log.d(LOG_TAG, "Flurry is not enabled");
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (trackingSession) {
			FlurryAgent.onEndSession(this);
		}

	}

}
