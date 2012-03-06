package uk.co.armedpineapple.corsixth.wizard;

import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class DisplayWizard extends WizardView {

	RadioGroup displayRadioGroup;
	RadioButton defaultResolutionRadio;
	RadioButton nativeResolutionRadio;
	RadioButton customResolutionRadio;

	int customWidth;
	int customHeight;

	public DisplayWizard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public DisplayWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DisplayWizard(Context context) {
		super(context);
	}

	@Override
	void saveConfiguration(Configuration config) {

		if (defaultResolutionRadio.isChecked()) {
			config.setResolutionMode(Configuration.RESOLUTION_DEFAULT);
		} else if (nativeResolutionRadio.isChecked()) {
			config.setResolutionMode(Configuration.RESOLUTION_NATIVE);
		} else if (customResolutionRadio.isChecked()) {
			config.setResolutionMode(Configuration.RESOLUTION_CUSTOM);
			config.setDisplayHeight(customHeight);
			config.setDisplayWidth(customWidth);
		}
	}

	@Override
	void loadConfiguration(Configuration config) {

		displayRadioGroup = ((RadioGroup) findViewById(R.id.displayRadioGroup));
		defaultResolutionRadio = ((RadioButton) findViewById(R.id.defaultResolutionRadio));
		nativeResolutionRadio = ((RadioButton) findViewById(R.id.nativeResolutionRadio));
		customResolutionRadio = ((RadioButton) findViewById(R.id.customResolutionRadio));

		switch (config.getResolutionMode()) {
		case Configuration.RESOLUTION_DEFAULT:
			defaultResolutionRadio.setChecked(true);

			break;
		case Configuration.RESOLUTION_NATIVE:
			nativeResolutionRadio.setChecked(true);
			break;
		case Configuration.RESOLUTION_CUSTOM:
			customResolutionRadio.setChecked(true);
			customWidth = config.getDisplayWidth();
			customHeight = config.getDisplayHeight();
			break;
		}
	}

}
