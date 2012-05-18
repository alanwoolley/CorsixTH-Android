/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}

}
