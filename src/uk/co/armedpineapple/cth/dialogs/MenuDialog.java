package uk.co.armedpineapple.cth.dialogs;

import uk.co.armedpineapple.cth.CTHApplication;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.SDLActivity;
import static uk.co.armedpineapple.cth.SDLActivity.Command.*;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

public class MenuDialog extends Dialog implements OnItemClickListener {

	SDLActivity			ctx;
	ImageButton			backButton;

	ListView				mainList;
	MenuAdapter			adapter;
	GameSpeedDialog	gameSpeedDialog;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		SDLActivity.cthGameSpeed(ctx.config.getGameSpeed());
	}

	public MenuDialog(SDLActivity context) {
		super(context);
		this.ctx = context;
		setTitle(R.string.game_paused);
		setCancelable(true);

		setContentView(R.layout.main_menu);
		mainList = (ListView) findViewById(R.id.MenuDialogListView);

		adapter = new MenuAdapter(context, MenuItems.getItems());
		mainList.setAdapter(adapter);

		mainList.setOnItemClickListener(this);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		// We've clicked on something, so hide the menu.
		hide();

		MenuItems clicked = (MenuItems) parent.getItemAtPosition(position);

		switch (clicked) {
			case ABOUT:
				SDLActivity.sendCommand(SHOW_ABOUT_DIALOG, null);
				return;
			case EXIT:
				SDLActivity.nativeQuit();
				return;
			case GAME_SPEED:
				if (gameSpeedDialog == null) {
					gameSpeedDialog = new GameSpeedDialog(ctx);
				}
				gameSpeedDialog.show(ctx.config.getGameSpeed());
				return;
			case LOAD:
				SDLActivity.sendCommand(SHOW_LOAD_DIALOG, null);
				return;
			case QUICK_LOAD:
				SDLActivity.sendCommand(QUICK_LOAD, null);
				return;
			case QUICK_SAVE:
				SDLActivity.sendCommand(QUICK_SAVE, null);
				SDLActivity.cthGameSpeed(ctx.config.getGameSpeed());
				return;
			case RESTART:
				SDLActivity.sendCommand(RESTART_GAME, null);
				return;
			case SAVE:
				SDLActivity.sendCommand(SHOW_SAVE_DIALOG, null);
				return;
			case WIZARD:
				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
				DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences preferences = ((CTHApplication) ctx
								.getApplication()).getPreferences();

						Editor editor = preferences.edit();
						editor.putBoolean("wizard_run", false);
						editor.commit();

						SDLActivity.cthGameSpeed(ctx.config.getGameSpeed());
					}

				};
				builder
						.setMessage(
								ctx.getResources().getString(R.string.setup_wizard_dialog))
						.setCancelable(false).setNeutralButton(R.string.ok, alertListener);

				AlertDialog alert = builder.create();
				alert.show();
				return;

		}
	}

}
