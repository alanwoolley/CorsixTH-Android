package uk.co.armedpineapple.corsixth.wizard;

import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class OriginalFilesWizard extends WizardView {

	RadioGroup originalFilesRadioGroup;
	RadioButton automaticRadio;
	RadioButton manualRadio;
	RadioButton downloadDemoRadio;

	String customLocation;

	public OriginalFilesWizard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public OriginalFilesWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public OriginalFilesWizard(Context context) {
		super(context);
	}

	@Override
	void saveConfiguration(Configuration config) {
		if (automaticRadio.isChecked()) {
			config.setOriginalFilesPath("/mnt/sdcard/th");
		} else if (manualRadio.isChecked()) {
			config.setOriginalFilesPath(customLocation);
		}
	}

	@Override
	void loadConfiguration(Configuration config) {

		originalFilesRadioGroup = ((RadioGroup) findViewById(R.id.originalFilesRadioGroup));
		automaticRadio = ((RadioButton) findViewById(R.id.automaticRadio));
		manualRadio = ((RadioButton) findViewById(R.id.manualRadio));
		downloadDemoRadio = ((RadioButton) findViewById(R.id.downloadDemoRadio));
		
		

		if (config.getOriginalFilesPath().equals("/mnt/sdcard/th")) {
			automaticRadio.setChecked(true);
		} else {
			manualRadio.setChecked(true);
		}
	}

}
