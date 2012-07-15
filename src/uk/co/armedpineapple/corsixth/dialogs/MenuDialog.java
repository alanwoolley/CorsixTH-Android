package uk.co.armedpineapple.corsixth.dialogs;

import uk.co.armedpineapple.corsixth.R;
import uk.co.armedpineapple.corsixth.SDLActivity;
import static uk.co.armedpineapple.corsixth.SDLActivity.Command.*;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

public class MenuDialog extends Dialog implements View.OnClickListener {

	Context ctx;
	ImageButton backButton;
	Button quickLoadButton, quickSaveButton, loadButton, saveButton,
			restartButton, settingsButton, aboutButton, exitButton;

	public MenuDialog(SDLActivity context) {
		super(context, R.style.Theme_Dialog_Translucent);
		this.ctx = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setCancelable(true);

		setContentView(R.layout.main_menu);

		// Setup buttons

		backButton = (ImageButton) findViewById(R.id.menu_button_back);
		quickLoadButton = getButton(R.id.menu_button_quickload);
		quickSaveButton = getButton(R.id.menu_button_quicksave);
		loadButton = getButton(R.id.menu_button_load);
		saveButton = getButton(R.id.menu_button_save);
		restartButton = getButton(R.id.menu_button_restart);
		settingsButton = getButton(R.id.menu_button_settings);
		aboutButton = getButton(R.id.menu_button_about);
		exitButton = getButton(R.id.menu_button_exit);

		backButton.setOnClickListener(this);
		quickLoadButton.setOnClickListener(this);
		quickSaveButton.setOnClickListener(this);
		loadButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		restartButton.setOnClickListener(this);
		settingsButton.setOnClickListener(this);
		aboutButton.setOnClickListener(this);
		exitButton.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		hide();

		// Back Button
		if (v.equals(backButton)) {
			return;
		}

		// Quick Load Button
		if (v.equals(quickLoadButton)) {
			SDLActivity.sendCommand(QUICK_LOAD, null);
			return;
		}

		// Quick Save Button
		if (v.equals(quickSaveButton)) {
			SDLActivity.sendCommand(QUICK_SAVE, null);
			return;
		}

		// Load Button
		if (v.equals(loadButton)) {
			SDLActivity.sendCommand(SHOW_LOAD_DIALOG, null);
			return;
		}

		// Save Button
		if (v.equals(saveButton)) {
			SDLActivity.sendCommand(SHOW_SAVE_DIALOG, null);
			return;
		}

		// Restart Button
		if (v.equals(restartButton)) {
			SDLActivity.sendCommand(RESTART_GAME, null);
			return;
		}

		// About Button
		if (v.equals(aboutButton)) {
			SDLActivity.sendCommand(SHOW_ABOUT_DIALOG, null);
			return;
		}

		// Exit Button
		if (v.equals(exitButton)) {
			SDLActivity.nativeQuit();
			return;
		}

		// Settings Button
		if (v.equals(settingsButton)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences preferences = PreferenceManager
							.getDefaultSharedPreferences(ctx
									.getApplicationContext());
					Editor editor = preferences.edit();
					editor.putBoolean("wizard_run", false);
					editor.commit();
				}

			};
			builder.setMessage(
					ctx.getResources().getString(R.string.setup_wizard_dialog))
					.setCancelable(false).setNeutralButton("OK", alertListener);

			AlertDialog alert = builder.create();
			alert.show();
			return;
		}

	}

	public Button getButton(int id) {
		return (Button) findViewById(id);
	}

}
