/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth.wizard;

import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.ConfigurationException;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public abstract class WizardView extends RelativeLayout {

	public WizardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public WizardView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public WizardView(Context context) {
		super(context);

	}

	/**
	 * Stores the wizard settings in a configuration. This is called when the
	 * wizard page is navigated away from
	 */
	abstract void saveConfiguration(Configuration config) throws ConfigurationException;

	/**
	 * Populates the wizard using a configuration. Called when the wizard page
	 * is attached
	 */
	abstract void loadConfiguration(Configuration config);

}
