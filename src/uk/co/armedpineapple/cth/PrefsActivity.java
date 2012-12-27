/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.WindowManager.LayoutParams;
import uk.co.armedpineapple.cth.R;

public class PrefsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		 PreferenceManager prefMgr = getPreferenceManager();
     prefMgr.setSharedPreferencesName(CTHApplication.PREFERENCES_KEY);
     prefMgr.setSharedPreferencesMode(MODE_PRIVATE);
     
		addPreferencesFromResource(R.xml.prefs);

	

		//getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

}
