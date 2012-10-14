/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth.wizard;

import uk.co.armedpineapple.corsixth.CTHActivity;
import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.ConfigurationException;
import uk.co.armedpineapple.corsixth.Files;
import uk.co.armedpineapple.corsixth.R;
import uk.co.armedpineapple.corsixth.SDLActivity;
import uk.co.armedpineapple.corsixth.dialogs.DialogFactory;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.ViewFlipper;

public class WizardActivity extends CTHActivity {

	private ViewFlipper flipper;
	private Button previousButton;
	private Button nextButton;
	private WizardButtonClickListener buttonClickListener;
	private Configuration config;
	private SharedPreferences preferences;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!Files.canAccessExternalStorage()) {
			Log.e(getClass().getSimpleName(), "Can't get storage.");

			// Show dialog and end
			DialogFactory.createExternalStorageDialog(this, true).show();
			return;

		}
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		if (preferences.getBoolean("wizard_run", false)) {
			Log.d(getClass().getSimpleName(), "Wizard isn't going to run.");
			finish();
			startActivity(new Intent(this, SDLActivity.class));
		} else {
			Log.d(getClass().getSimpleName(), "Wizard is going to run.");
			setContentView(R.layout.wizard);
			flipper = (ViewFlipper) findViewById(R.id.flipper);
			previousButton = (Button) findViewById(R.id.leftbutton);
			nextButton = (Button) findViewById(R.id.rightbutton);

			config = Configuration.loadFromPreferences(this, preferences);

			// Add all the wizard views

			LayoutInflater inflater = getLayoutInflater();
			loadAndAdd(inflater, flipper, (WizardView) inflater.inflate(
					R.layout.wizard_welcome, null));
			loadAndAdd(inflater, flipper, (LanguageWizard) inflater.inflate(
					R.layout.wizard_language, null));
			loadAndAdd(inflater, flipper,
					(OriginalFilesWizard) inflater.inflate(
							R.layout.wizard_originalfiles, null));
			loadAndAdd(inflater, flipper, (DisplayWizard) inflater.inflate(
					R.layout.wizard_display, null));
			loadAndAdd(inflater, flipper,
					(AudioWizard) inflater.inflate(R.layout.wizard_audio, null));
			loadAndAdd(inflater, flipper, (AdvancedWizard) inflater.inflate(
					R.layout.wizard_advanced, null));

			// Setup Buttons
			previousButton.setVisibility(View.GONE);
			buttonClickListener = new WizardButtonClickListener();

			previousButton.setOnClickListener(buttonClickListener);
			nextButton.setOnClickListener(buttonClickListener);

		}
	}

	public WizardView loadAndAdd(LayoutInflater inflater, ViewFlipper flipper,
			WizardView wv) {

		flipper.addView(wv);
		wv.loadConfiguration(config);
		return wv;
	}

	class WizardButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (v.equals(previousButton)) {
				flipper.setInAnimation(WizardActivity.this,
						R.animator.wizard_anim_slideinright);
				flipper.setOutAnimation(WizardActivity.this,
						R.animator.wizard_anim_slideoutright);
				flipper.showPrevious();
			} else if (v.equals(nextButton)) {
				try {
					((WizardView) flipper.getCurrentView())
							.saveConfiguration(config);

					if (nextButton.getText() == "Play!") {
						config.saveToPreferences(preferences);

						finish();
						WizardActivity.this.startActivity(new Intent(
								WizardActivity.this, SDLActivity.class));
					} else {

						flipper.setInAnimation(WizardActivity.this,
								R.animator.wizard_anim_slideinleft);
						flipper.setOutAnimation(WizardActivity.this,
								R.animator.wizard_anim_slideoutleft);
						flipper.showNext();
					}

				} catch (ConfigurationException e) {
					// Couldn't save the configuration. Don't change the view.
					return;
				}

			}

			if (hasNext(flipper)) {
				nextButton.setText("Next");
			} else {
				nextButton.setText("Play!");
			}
			if (hasPrevious(flipper)) {
				previousButton.setVisibility(View.VISIBLE);
			} else {
				previousButton.setVisibility(View.GONE);
			}

		}

		public boolean hasNext(ViewFlipper flipper) {
			if (flipper.indexOfChild(flipper.getCurrentView()) == flipper
					.getChildCount() - 1) {
				return false;
			}
			return true;
		}

		public boolean hasPrevious(ViewFlipper flipper) {
			if (flipper.indexOfChild(flipper.getCurrentView()) == 0) {
				return false;
			}
			return true;
		}

	}

}
