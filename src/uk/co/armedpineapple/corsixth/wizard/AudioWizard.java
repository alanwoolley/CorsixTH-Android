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

public class AudioWizard extends WizardView {

	CheckBox audioCheck;
	CheckBox fxCheck;
	CheckBox announcerCheck;
	CheckBox musicCheck;

	public AudioWizard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AudioWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AudioWizard(Context context) {
		super(context);
	}

	@Override
	void saveConfiguration(Configuration config) {

		config.setPlayAnnouncements(announcerCheck.isChecked());
		config.setPlayMusic(musicCheck.isChecked());
		config.setPlaySoundFx(fxCheck.isChecked());

	}

	@Override
	void loadConfiguration(Configuration config) {

		audioCheck = ((CheckBox) findViewById(R.id.audioCheck));
		fxCheck = ((CheckBox) findViewById(R.id.fxCheck));
		announcerCheck = ((CheckBox) findViewById(R.id.announcerCheck));
		musicCheck = ((CheckBox) findViewById(R.id.musicCheck));

		audioCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				fxCheck.setEnabled(isChecked);
				announcerCheck.setEnabled(isChecked);
			}

		});

		fxCheck.setChecked(config.getPlaySoundFx());
		announcerCheck.setChecked(config.getPlayAnnouncements());
		musicCheck.setChecked(config.getPlayMusic());
		audioCheck.setChecked(config.getPlaySoundFx()
				|| config.getPlayAnnouncements() || config.getPlayMusic());

	}
}
