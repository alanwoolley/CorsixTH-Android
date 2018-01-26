package uk.co.armedpineapple.cth

import android.app.Dialog
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.widget.Toast
import com.j256.ormlite.dao.Dao
import uk.co.armedpineapple.cth.CommandHandler.Command.*
import uk.co.armedpineapple.cth.dialogs.DialogFactory
import uk.co.armedpineapple.cth.dialogs.LoadDialog
import uk.co.armedpineapple.cth.dialogs.SaveDialog
import uk.co.armedpineapple.cth.persistence.PersistenceHelper
import uk.co.armedpineapple.cth.persistence.SaveData
import java.io.File
import java.io.IOException
import java.sql.SQLException

class CommandHandler(private val activityContext: SDLActivity) : Handler() {

    private val Log = Reporting.getLogger("CommandHandler")
    // Dialogs
    private var saveDialog: SaveDialog? = null
    private var loadDialog: LoadDialog? = null
    private var aboutDialog: Dialog? = null
    private val app: CTHApplication = activityContext.app
    var playingEarthquake: Boolean = false

    private val persistence: PersistenceHelper = PersistenceHelper(activityContext)


    fun cleanUp() {
        saveDialog = null
        loadDialog = null
        aboutDialog = null
    }

    override fun handleMessage(msg: Message) {
        // Receiving a message when the activity is not available will likely cause a fatal error.
        if (!SDLActivity.isActivityAvailable()) {
            Log.w("Received message when activity is not ready. Ignoring.")
            return
        }

        when (values()[msg.arg1]) {
            GAME_LOAD_ERROR -> {
                val prefs = activityContext.app.preferences
                val editor = prefs!!.edit()
                editor.putInt("last_version", 0)
                editor.putBoolean("wizard_run", false)
                editor.apply()
                val errorDialog = DialogFactory.createErrorDialog(activityContext)

                errorDialog.show()
            }

            SHOW_ABOUT_DIALOG -> {
                if (aboutDialog == null) {
                    aboutDialog = DialogFactory.createAboutDialog(activityContext)
                }
                aboutDialog!!.show()
            }

            HIDE_KEYBOARD -> {
            }
            SHOW_KEYBOARD -> SDLActivity.ShowTextInputTask(0, 0, 0, 0).run()
            QUICK_LOAD -> if (Files.doesFileExist(
                            activityContext.app.configuration!!.saveGamesPath
                                    + File.separator + activityContext.getString(R.string.quicksave_name))) {
                SDLActivity.cthLoadGame(activityContext.getString(R.string.quicksave_name))
            } else {
                Toast.makeText(activityContext, R.string.no_quicksave, Toast.LENGTH_SHORT)
                        .show()
            }
            QUICK_SAVE -> SDLActivity.cthSaveGame(activityContext.getString(R.string.quicksave_name))
            RESTART_GAME -> SDLActivity.cthRestartGame()

            SHOW_LOAD_DIALOG -> {
                if (loadDialog == null) {
                    loadDialog = LoadDialog(activityContext,
                            activityContext.app.configuration!!.saveGamesPath)
                }
                try {
                    loadDialog!!.refreshSaves(activityContext)
                    loadDialog!!.show()
                } catch (e: IOException) {
                    Reporting.reportWithToast(activityContext, "Problem loading load dialog", e)
                }

            }

            SHOW_SAVE_DIALOG -> {
                if (saveDialog == null) {
                    saveDialog = SaveDialog(activityContext,
                            activityContext.app.configuration!!.saveGamesPath)
                }
                try {
                    saveDialog!!.refreshSaves(activityContext)
                    saveDialog!!.show()
                } catch (e: IOException) {
                    Reporting.reportWithToast(activityContext, "Problem loading save dialog", e)
                }

            }
            SHOW_MENU -> {
                activityContext.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                activityContext.mDrawerLayout.openDrawer(GravityCompat.START)
            }
            HIDE_MENU -> {
                activityContext.mDrawerLayout.closeDrawers()
                activityContext.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            PAUSE_GAME -> SDLActivity.cthGameSpeed(0)
            SHOW_SETTINGS_DIALOG -> activityContext.startActivity(Intent(activityContext, PrefsActivity::class.java))
            GAME_SPEED_UPDATED -> activityContext.app.configuration!!.gameSpeed = msg.obj as Int
            START_VIBRATION -> {

                val vibrationCode = msg.obj as Int
                Log.d("Vibrating: " + vibrationCode)
                if (app.configuration!!.haptic) {
                    activityContext.playVibration(vibrationCode)
                }
            }
            STOP_VIBRATION -> {
                activityContext.stopVibration()
                playingEarthquake = false
            }
            CHANGE_TITLE -> {
            }
            UNUSED -> {
            }
            TEXTEDIT_HIDE -> activityContext.hideTextEdit()
            SET_KEEP_SCREEN_ON -> activityContext.setScreenOn(msg.obj as Int != 0)
            GAME_SAVE_UPDATED -> {
                Log.d("Game save updated")
                val data = msg.obj as SaveData

                try {
                    val dao = persistence.getDao<Dao<SaveData, String>, SaveData>(SaveData::class.java)
                    // This doesn't work for some reason
                    //Dao.CreateOrUpdateStatus status = dao.createOrUpdate(data);
                    //Log.d("Saved game entries changed: " + status.getNumLinesChanged() + ". Created? " + status.isCreated() + ". Updated? " + status.isUpdated());
                    // So delete and recreate
                    dao.delete(data)
                    dao.create(data)

                } catch (e: SQLException) {
                    Reporting.report(e)
                }

            }
            SHOW_JUKEBOX -> {
                Log.d("Showing jukebox")
                SDLActivity.cthShowJukebox()
            }
            else -> {
            }
        }/* mgr = (InputMethodManager) activityContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(SDLActivity.mSurface.getWindowToken(), 0); */// Do nothing
        // Do nothing
    }

    // Commands that can be sent from the game
    enum class Command {
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
        SHOW_JUKEBOX
    }

    companion object {

        const val VIBRATION_SHORT_CLICK = 1
        const val VIBRATION_LONG_CLICK = 2
        const val VIBRATION_EXPLOSION = 3
    }

}
