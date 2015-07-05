package uk.co.armedpineapple.cth;

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

import java.io.File;
import java.io.IOException;

import uk.co.armedpineapple.cth.dialogs.DialogFactory;
import uk.co.armedpineapple.cth.dialogs.LoadDialog;
import uk.co.armedpineapple.cth.dialogs.SaveDialog;

public class CommandHandler extends Handler {

    private Reporting.Logger Log = Reporting.getLogger("CommandHandler");

    public static final int VIBRATION_SHORT_CLICK = 1;
    public static final int VIBRATION_LONG_CLICK  = 2;
    public static final int VIBRATION_EXPLOSION   = 3;
    private final SDLActivity    activityContext;
    // Dialogs
    private       SaveDialog     saveDialog;
    private       LoadDialog     loadDialog;
    private       Dialog         aboutDialog;
    private final CTHApplication app;
    public        boolean        playingEarthquake;


    public CommandHandler(SDLActivity context) {
        super();
        this.activityContext = context;
        app = context.app;
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
                SharedPreferences prefs = activityContext.app.getPreferences();
                Editor editor = prefs.edit();
                editor.putInt("last_version", 0);
                editor.putBoolean("wizard_run", false);
                editor.apply();
                Dialog errorDialog = DialogFactory.createErrorDialog(activityContext);

                errorDialog.show();

                break;

            case SHOW_ABOUT_DIALOG:
                if (aboutDialog == null) {
                    aboutDialog = DialogFactory.createAboutDialog(activityContext);
                }
                aboutDialog.show();
                break;

            case HIDE_KEYBOARD:
                mgr = (InputMethodManager) activityContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(SDLActivity.mSurface.getWindowToken(), 0);
                break;
            case SHOW_KEYBOARD:
                mgr = (InputMethodManager) activityContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.showSoftInput(SDLActivity.mSurface, InputMethodManager.SHOW_FORCED);
                break;
            case QUICK_LOAD:
                if (Files.doesFileExist(activityContext.app.configuration.getSaveGamesPath()
                        + File.separator + activityContext.getString(R.string.quicksave_name))) {
                    SDLActivity.cthLoadGame(activityContext.getString(R.string.quicksave_name));
                } else {
                    Toast.makeText(activityContext, R.string.no_quicksave, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case QUICK_SAVE:
                SDLActivity.cthSaveGame(activityContext.getString(R.string.quicksave_name));
                break;
            case RESTART_GAME:
                SDLActivity.cthRestartGame();
                break;

            case SHOW_LOAD_DIALOG:
                if (loadDialog == null) {
                    loadDialog = new LoadDialog(activityContext,
                            activityContext.app.configuration.getSaveGamesPath());
                }
                try {
                    loadDialog.refreshSaves(activityContext);
                    loadDialog.show();
                } catch (IOException e) {
                    Reporting.reportWithToast(activityContext, "Problem loading load dialog", e);
                }
                break;

            case SHOW_SAVE_DIALOG:
                if (saveDialog == null) {
                    saveDialog = new SaveDialog(activityContext,
                            activityContext.app.configuration.getSaveGamesPath());
                }
                try {
                    saveDialog.refreshSaves(activityContext);
                    saveDialog.show();
                } catch (IOException e) {
                    Reporting.reportWithToast(activityContext, "Problem loading save dialog", e);
                }

                break;
            case SHOW_MENU:
                activityContext.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                activityContext.mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case HIDE_MENU:
                activityContext.mDrawerLayout.closeDrawers();
                activityContext.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                break;
            case PAUSE_GAME:
                SDLActivity.cthGameSpeed(0);
                break;
            case SHOW_SETTINGS_DIALOG:
                activityContext.startActivity(new Intent(activityContext, PrefsActivity.class));
                break;
            case GAME_SPEED_UPDATED:
                activityContext.app.configuration.setGameSpeed((Integer) msg.obj);
                break;
            case START_VIBRATION:

                Integer vibrationCode = (Integer) msg.obj;
                Log.d("Vibrating: " + vibrationCode);
                if (app.configuration.getHaptic()) {
                     activityContext.playVibration(vibrationCode);
                }
                break;
            case STOP_VIBRATION:
                activityContext.stopVibration();
                playingEarthquake = false;
                break;
            default:
                break;
        }
    }

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
        HIDE_MENU,
        START_VIBRATION,
        STOP_VIBRATION
    }

}
