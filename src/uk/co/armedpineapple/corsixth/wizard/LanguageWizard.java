package uk.co.armedpineapple.corsixth.wizard;

import java.util.Arrays;

import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Spinner;

public class LanguageWizard extends WizardView {

	Spinner languageSpinner;
	Context ctx;
	String[] langValuesArray;
	String[] langArray;

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
	void saveConfiguration(Configuration config) {
		String langV = langValuesArray[languageSpinner
				.getSelectedItemPosition()];
		Log.d(getClass().getSimpleName(), "Setting language to: " + langV);
		config.setLanguage(langV);
	}

	@Override
	void loadConfiguration(Configuration config) {
		languageSpinner = ((Spinner) findViewById(R.id.languageSpinner));

		langValuesArray = ctx.getResources().getStringArray(
				R.array.languages_values);

		langArray = ctx.getResources().getStringArray(R.array.languages);

		languageSpinner.setSelection(0);

		for (int i = 0; i < langValuesArray.length; i++) {
			if (langValuesArray[i].equals(config.getLanguage())) {
				languageSpinner.setSelection(i);
				break;
			}
		}

	}

}
