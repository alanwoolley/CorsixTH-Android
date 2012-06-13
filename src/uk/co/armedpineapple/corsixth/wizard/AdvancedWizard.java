/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth.wizard;

import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

public class AdvancedWizard extends WizardView {

	CheckBox debugCheck;
	CheckBox screenOnCheck;

	public AdvancedWizard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AdvancedWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AdvancedWizard(Context context) {
		super(context);
	}

	@Override
	void saveConfiguration(Configuration config) {
		config.setDebug(debugCheck.isChecked());
		config.setKeepScreenOn(screenOnCheck.isChecked());
	}

	@Override
	void loadConfiguration(Configuration config) {

		debugCheck = ((CheckBox) findViewById(R.id.debugCheck));
		debugCheck.setChecked(config.getDebug());
		
		screenOnCheck = ((CheckBox) findViewById(R.id.screenOnCheck));
		screenOnCheck.setChecked(config.getKeepScreenOn());
		
	}
}
