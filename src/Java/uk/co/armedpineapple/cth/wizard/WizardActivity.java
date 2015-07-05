/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.wizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ViewFlipper;

import uk.co.armedpineapple.cth.CTHActivity;
import uk.co.armedpineapple.cth.Configuration;
import uk.co.armedpineapple.cth.Configuration.ConfigurationException;
import uk.co.armedpineapple.cth.Files;
import uk.co.armedpineapple.cth.Files.StorageUnavailableException;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.Reporting;
import uk.co.armedpineapple.cth.SDLActivity;
import uk.co.armedpineapple.cth.dialogs.DialogFactory;

public class WizardActivity extends CTHActivity {

	private static final Reporting.Logger Log = Reporting.getLogger("Wizard");

	private ViewFlipper								flipper;
	private Button										previousButton;
	private Button										nextButton;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!Files.canAccessExternalStorage()) {
			Log.e("Can't get storage.");

			// Show dialog and end
			DialogFactory.createExternalStorageWarningDialog(this, true).show();
			return;

		}

        SharedPreferences preferences = app.getPreferences();
		boolean alreadyRun = preferences.getBoolean("wizard_run", false);
		if (alreadyRun) {
			if (Files.hasDataFiles(preferences.getString("originalfiles_pref", ""))) {
				Log.d("Wizard isn't going to run.");
				finish();
				startActivity(new Intent(this, SDLActivity.class));
				return;
			}
		}

		Log.d("Wizard is going to run.");
		setContentView(R.layout.wizard);
		flipper = (ViewFlipper) findViewById(R.id.flipper);
		previousButton = (Button) findViewById(R.id.leftbutton);
		nextButton = (Button) findViewById(R.id.rightbutton);

		if (app.configuration == null) {
			try {
				app.configuration = Configuration
						.loadFromPreferences(this, preferences);
			} catch (StorageUnavailableException e) {
				Log.e("Can't get storage.");

				// Show dialog and end
				DialogFactory.createExternalStorageWarningDialog(this, true).show();
				return;
			}
		}

		// Add all the wizard views

		LayoutInflater inflater = getLayoutInflater();
		loadAndAdd(inflater, flipper,
				(WizardView) inflater.inflate(R.layout.wizard_welcome, null));
		loadAndAdd(inflater, flipper,
				(LanguageWizard) inflater.inflate(R.layout.wizard_language, null));
		loadAndAdd(inflater, flipper, (OriginalFilesWizard) inflater.inflate(
				R.layout.wizard_originalfiles, null));

		// Setup Buttons
		previousButton.setVisibility(View.GONE);
        WizardButtonClickListener buttonClickListener = new WizardButtonClickListener();

		previousButton.setOnClickListener(buttonClickListener);
		nextButton.setOnClickListener(buttonClickListener);

	}

	public WizardView loadAndAdd(LayoutInflater inflater, ViewFlipper flipper,
			WizardView wv) {

		flipper.addView(wv);
		wv.loadConfiguration(app.configuration);
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
							.saveConfiguration(app.configuration);

					if (nextButton.getText().equals(getString(R.string.play_button))) {
						app.configuration.saveToPreferences();

						finish();
						WizardActivity.this.startActivity(new Intent(WizardActivity.this,
								SDLActivity.class));
					} else {

						flipper.setInAnimation(WizardActivity.this,
								R.animator.wizard_anim_slideinleft);
						flipper.setOutAnimation(WizardActivity.this,
								R.animator.wizard_anim_slideoutleft);
						flipper.showNext();
					}

				} catch (ConfigurationException e) {
					Reporting.report(e);
					// Couldn't save the configuration. Don't change the view.
					return;
				}

			}

			if (hasNext(flipper)) {
				nextButton.setText(R.string.nextButton);
			} else {
				nextButton.setText(R.string.play_button);
			}
			if (hasPrevious(flipper)) {
				previousButton.setVisibility(View.VISIBLE);
			} else {
				previousButton.setVisibility(View.GONE);
			}

		}

		public boolean hasNext(ViewFlipper flipper) {
            return flipper.indexOfChild(flipper.getCurrentView()) != flipper
                    .getChildCount() - 1;
        }

		public boolean hasPrevious(ViewFlipper flipper) {
			if (flipper.indexOfChild(flipper.getCurrentView()) == 0) {
				return false;
			}
			return true;
		}

	}

}
