package uk.co.armedpineapple.cth;

import java.io.File;
import java.io.IOException;

import uk.co.armedpineapple.cth.dialogs.DialogFactory;
import uk.co.armedpineapple.cth.dialogs.LoadDialog;
import uk.co.armedpineapple.cth.dialogs.SaveDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

public class CommandHandler extends Handler {

	// Commands that can be sent from the game
	public enum Command {
		SHOW_MENU,
		SHOW_LOAD_DIALOG,
		SHOW_SAVE_DIALOG,
		RESTART_GAME,
		QUICK_LOAD,
		QUICK_SAVE,
		SHOW_KEYBOARD,
		HIDE_KEYBOARD,
		SHOW_ABOUT_DIALOG,
		PAUSE_GAME,
		SHOW_SETTINGS_DIALOG,
		GAME_SPEED_UPDATED,
		GAME_LOAD_ERROR,
		HIDE_MENU
	}

	// Dialogs
	private SaveDialog	saveDialog;
	private LoadDialog	loadDialog;
	private Dialog			aboutDialog;

	SDLActivity					context;

	public CommandHandler(SDLActivity context) {
		super();
		this.context = context;

	}

	public void cleanUp() {
		saveDialog = null;
		loadDialog = null;
		aboutDialog = null;
	}

	public void handleMessage(Message msg) {
		InputMethodManager mgr;
		switch (Command.values()[msg.arg1]) {
			case GAME_LOAD_ERROR:
				SharedPreferences prefs = context.app.getPreferences();
				Editor editor = prefs.edit();
				editor.putInt("last_version", 0);
				editor.putBoolean("wizard_run", false);
				editor.commit();
				Dialog errorDialog = DialogFactory.createErrorDialog(context);

				errorDialog.show();

				break;

			case SHOW_ABOUT_DIALOG:
				if (aboutDialog == null) {
					aboutDialog = DialogFactory.createAboutDialog(context);
				}
				aboutDialog.show();
				break;

			case HIDE_KEYBOARD:
				mgr = (InputMethodManager) context
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(SDLActivity.mSurface.getWindowToken(), 0);
				break;
			case SHOW_KEYBOARD:
				mgr = (InputMethodManager) context
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.showSoftInput(SDLActivity.mSurface, InputMethodManager.SHOW_FORCED);
				break;
			case QUICK_LOAD:
				if (Files.doesFileExist(context.app.configuration.getSaveGamesPath()
						+ File.separator + context.getString(R.string.quicksave_name))) {
					SDLActivity.cthLoadGame(context.getString(R.string.quicksave_name));
				} else {
					Toast.makeText(context, R.string.no_quicksave, Toast.LENGTH_SHORT)
							.show();
				}
				break;
			case QUICK_SAVE:
				SDLActivity.cthSaveGame(context.getString(R.string.quicksave_name));
				break;
			case RESTART_GAME:
				SDLActivity.cthRestartGame();
				break;

			case SHOW_LOAD_DIALOG:
				if (loadDialog == null) {
					loadDialog = new LoadDialog(context,
							context.app.configuration.getSaveGamesPath());
				}
				try {
					loadDialog.updateSaves(context);
					loadDialog.show();
				} catch (IOException e) {
					BugSenseHandler.sendException(e);

					Toast.makeText(context, "Problem loading load dialog",
							Toast.LENGTH_SHORT).show();

				}
				break;

			case SHOW_SAVE_DIALOG:
				if (saveDialog == null) {
					saveDialog = new SaveDialog(context,
							context.app.configuration.getSaveGamesPath());
				}
				try {
					saveDialog.updateSaves(context);
					saveDialog.show();
				} catch (IOException e) {
					BugSenseHandler.sendException(e);
					Toast.makeText(context, "Problem loading save dialog",
							Toast.LENGTH_SHORT).show();
				}

				break;
			case SHOW_MENU:
				context.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
				context.mDrawerLayout.openDrawer(GravityCompat.START);
				break;
			case HIDE_MENU:
				context.mDrawerLayout.closeDrawers();
				context.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
				break;
			case PAUSE_GAME:
				SDLActivity.cthGameSpeed(0);
				break;
			case SHOW_SETTINGS_DIALOG:
				context.startActivity(new Intent(context, PrefsActivity.class));
				break;
			case GAME_SPEED_UPDATED:
				context.app.configuration.setGameSpeed((Integer) msg.obj);
				break;

			default:
				break;
		}
	}

}
