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

import com.j256.ormlite.dao.Dao;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import uk.co.armedpineapple.cth.dialogs.DialogFactory;
import uk.co.armedpineapple.cth.dialogs.LoadDialog;
import uk.co.armedpineapple.cth.dialogs.SaveDialog;
import uk.co.armedpineapple.cth.persistence.PersistenceHelper;
import uk.co.armedpineapple.cth.persistence.SaveData;

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

    private PersistenceHelper persistence;


    public CommandHandler(SDLActivity context) {
        super();
        this.activityContext = context;
        this.app = context.app;
        this.persistence = new PersistenceHelper(context);
    }

    public void cleanUp() {
        saveDialog = null;
        loadDialog = null;
        aboutDialog = null;
    }

    public void handleMessage(Message msg) {
        // Receiving a message when the activity is not available will likely cause a fatal error.
        if (!SDLActivity.isActivityAvailable()) {
            Log.w("Received message when activity is not ready. Ignoring.");
            return;
        }

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
               /* mgr = (InputMethodManager) activityContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(SDLActivity.mSurface.getWindowToken(), 0); */
                break;
            case SHOW_KEYBOARD:
                new SDLActivity.ShowTextInputTask(0,0,0,0).run();
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
            case CHANGE_TITLE:
                // Do nothing
                break;
            case UNUSED:
                // Do nothing
                break;
            case TEXTEDIT_HIDE:
                activityContext.hideTextEdit();
                break;
            case SET_KEEP_SCREEN_ON:
                activityContext.setScreenOn((Integer) msg.obj != 0);
                break;
            case GAME_SAVE_UPDATED:
                Log.d("Game save updated");
                SaveData data = (SaveData) msg.obj;

                try {
                    Dao<SaveData, String> dao = persistence.getDao(SaveData.class);
                    // This doesn't work for some reason
                    //Dao.CreateOrUpdateStatus status = dao.createOrUpdate(data);
                    //Log.d("Saved game entries changed: " + status.getNumLinesChanged() + ". Created? " + status.isCreated() + ". Updated? " + status.isUpdated());
                    // So delete and recreate
                    dao.delete(data);
                    dao.create(data);

                } catch (SQLException e) {
                    Reporting.report(e);
                }

                break;
            case SHOW_JUKEBOX:
                Log.d("Showing jukebox");
                SDLActivity.cthShowJukebox();
                break;
            default:
                break;
        }
    }

    // Commands that can be sent from the game
    public enum Command {
        SHOW_MENU,
        CHANGE_TITLE,
        UNUSED,
        TEXTEDIT_HIDE,
        UNUSED2,
        SET_KEEP_SCREEN_ON,
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
        STOP_VIBRATION,
        GAME_SAVE_UPDATED,
        SHOW_JUKEBOX,
    }

}
