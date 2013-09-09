package uk.co.armedpineapple.cth;

import static uk.co.armedpineapple.cth.CommandHandler.Command.*;

import uk.co.armedpineapple.cth.dialogs.GameSpeedDialog;
import uk.co.armedpineapple.cth.dialogs.MenuItems;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class NavDrawerListListener implements ListView.OnItemClickListener {

	SDLActivity			context;
	GameSpeedDialog	gameSpeedDialog;

	public NavDrawerListListener(SDLActivity context) {
		this.context = context;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

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
					gameSpeedDialog = new GameSpeedDialog(context);
				}
				gameSpeedDialog.show(context.app.configuration.getGameSpeed());
				return;
			case LOAD:
				SDLActivity.sendCommand(SHOW_LOAD_DIALOG, null);
				return;
			case QUICK_LOAD:
				SDLActivity.cthGameSpeed(context.app.configuration.getGameSpeed());
				SDLActivity.sendCommand(QUICK_LOAD, null);
				return;
			case QUICK_SAVE:
				SDLActivity.sendCommand(QUICK_SAVE, null);
				SDLActivity.cthGameSpeed(context.app.configuration.getGameSpeed());
				return;
			case RESTART:
				SDLActivity.cthGameSpeed(context.app.configuration.getGameSpeed());
				SDLActivity.sendCommand(RESTART_GAME, null);
				SDLActivity.sendCommand(HIDE_MENU, null);
				return;
			case SAVE:
				SDLActivity.sendCommand(SHOW_SAVE_DIALOG, null);
				return;
			case SETTINGS:
				context.startActivity(new Intent(context, PrefsActivity.class));
				return;
		}
	}
}
