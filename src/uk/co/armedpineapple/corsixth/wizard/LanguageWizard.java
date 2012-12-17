/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth.wizard;

import java.util.Locale;

import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.ConfigurationException;
import uk.co.armedpineapple.corsixth.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Spinner;

public class LanguageWizard extends WizardView {

	Spinner		languageSpinner;
	Context		ctx;
	String[]	langValuesArray;
	String[]	langArray;

	public LanguageWizard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ctx = context;
	}

	public LanguageWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;
	}

	public LanguageWizard(Context context) {
		super(context);
		ctx = context;
	}

	@Override
	void saveConfiguration(Configuration config) throws ConfigurationException {
		config.setLanguage(langValuesArray[languageSpinner
				.getSelectedItemPosition()]);
	}

	@Override
	void loadConfiguration(Configuration config) {
		languageSpinner = ((Spinner) findViewById(R.id.languageSpinner));
		langValuesArray = ctx.getResources().getStringArray(
				R.array.languages_values);
		langArray = ctx.getResources().getStringArray(R.array.languages);

		languageSpinner.setSelection(0);

		// Look for the language in the values array
		Log.d(getClass().getSimpleName(), "System Language: " + Locale.getDefault().getLanguage());
		
		for (int i = 0; i < langValuesArray.length; i++) {
			if (langValuesArray[i].equals(Locale.getDefault().getLanguage())) {
				languageSpinner.setSelection(i);
				break;
			}
			
		}

	}

}
