package uk.co.armedpineapple.corsixth.wizard;

import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.R;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class OriginalFilesWizard extends WizardView {

	RadioGroup originalFilesRadioGroup;
	RadioButton automaticRadio;
	RadioButton manualRadio;
	RadioButton downloadDemoRadio;

	String customLocation;

	Context ctx;

	public OriginalFilesWizard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ctx = context;
	}

	public OriginalFilesWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;
	}

	public OriginalFilesWizard(Context context) {
		super(context);
		ctx = context;
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

		final EditText editTextBox = new EditText(ctx);
		editTextBox.setText("/mnt/sdcard/th");
		Builder builder = new Builder(ctx);
		builder.setMessage("Theme Hospital Game Files location");
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				customLocation = editTextBox.getText().toString();
				manualRadio.setText("Custom (" + customLocation + ")");
			}
		});

		builder.setView(editTextBox);

		final AlertDialog d = builder.create();

		manualRadio.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				d.show();
			}
		});

		if (config.getOriginalFilesPath().equals("/mnt/sdcard/th")) {
			automaticRadio.setChecked(true);
		} else {
			manualRadio.setChecked(true);
		}
	}
}
