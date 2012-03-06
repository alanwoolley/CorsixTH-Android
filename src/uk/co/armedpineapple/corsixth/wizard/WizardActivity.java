package uk.co.armedpineapple.corsixth.wizard;

import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.R;
import uk.co.armedpineapple.corsixth.R.id;
import uk.co.armedpineapple.corsixth.R.layout;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ViewFlipper;

public class WizardActivity extends Activity {

	private ViewFlipper flipper;
	private Button previousButton;
	private Button nextButton;
	private WizardButtonClickListener buttonClickListener;
	private Configuration config;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard);

		flipper = (ViewFlipper) findViewById(R.id.flipper);
		previousButton = (Button) findViewById(R.id.leftbutton);
		nextButton = (Button) findViewById(R.id.rightbutton);

		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		config = Configuration.loadFromPreferences(this, preferences);

		LayoutInflater inflater = getLayoutInflater();
		loadAndAdd(inflater, flipper,
				(WizardView) inflater.inflate(R.layout.wizard_welcome, null));
		loadAndAdd(inflater, flipper, (OriginalFilesWizard) inflater.inflate(
				R.layout.wizard_originalfiles, null));
		loadAndAdd(inflater, flipper,
				(DisplayWizard) inflater.inflate(R.layout.wizard_display, null));
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
				if (nextButton.getText() == "Play!") {

				} else {
					flipper.setInAnimation(WizardActivity.this,
							R.animator.wizard_anim_slideinleft);
					flipper.setOutAnimation(WizardActivity.this,
							R.animator.wizard_anim_slideoutleft);
					flipper.showNext();
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
