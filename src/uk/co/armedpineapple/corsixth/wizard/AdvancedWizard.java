package uk.co.armedpineapple.corsixth.wizard;

import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class AdvancedWizard extends WizardView {

	CheckBox advancedCheck;
	CheckBox debugCheck;
	CheckBox customscriptsCheck;

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
	}

	@Override
	void loadConfiguration(Configuration config) {

		advancedCheck = ((CheckBox) findViewById(R.id.advancedCheck));
		debugCheck = ((CheckBox) findViewById(R.id.debugCheck));
		customscriptsCheck = ((CheckBox) findViewById(R.id.customscriptsCheck));

		advancedCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				debugCheck.setEnabled(isChecked);

			}

		});

		debugCheck.setChecked(config.getDebug());

		advancedCheck.setChecked(config.getDebug());

	}
}
