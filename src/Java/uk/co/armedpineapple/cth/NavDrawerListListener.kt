package uk.co.armedpineapple.cth

import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.ListView

import uk.co.armedpineapple.cth.dialogs.GameSpeedDialog
import uk.co.armedpineapple.cth.dialogs.HelpDialog

import uk.co.armedpineapple.cth.CommandHandler.Command.*

class NavDrawerListListener(internal val context: SDLActivity) : AdapterView.OnItemClickListener {
    private var gameSpeedDialog: GameSpeedDialog? = null
    private var helpDialog: HelpDialog? = null

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int,
                             id: Long) {

        val clicked = parent.getItemAtPosition(position) as MenuItems

        when (clicked) {
            MenuItems.HELP -> {
                if (helpDialog == null) {
                    helpDialog = HelpDialog(context)
                }
                helpDialog!!.show()
                return
            }
            MenuItems.ABOUT -> {
                SDLActivity.sendCommand(SHOW_ABOUT_DIALOG, null)
                return
            }
            MenuItems.EXIT -> {
                SDLActivity.nativeQuit()
                return
            }
            MenuItems.GAME_SPEED -> {
                if (gameSpeedDialog == null) {
                    gameSpeedDialog = GameSpeedDialog(context)
                }
                gameSpeedDialog!!.show(context.app.configuration!!.gameSpeed)
                return
            }
            MenuItems.LOAD -> {
                SDLActivity.sendCommand(SHOW_LOAD_DIALOG, null)
                return
            }
            MenuItems.QUICK_LOAD -> {
                SDLActivity.sendCommand(QUICK_LOAD, null)
                SDLActivity.sendCommand(HIDE_MENU, null)
                return
            }
            MenuItems.QUICK_SAVE -> {
                SDLActivity.sendCommand(QUICK_SAVE, null)
                SDLActivity.sendCommand(HIDE_MENU, null)
                return
            }
            MenuItems.RESTART -> {
                SDLActivity.cthGameSpeed(context.app.configuration!!.gameSpeed)
                SDLActivity.sendCommand(RESTART_GAME, null)
                SDLActivity.sendCommand(HIDE_MENU, null)
                return
            }
            MenuItems.SAVE -> {
                SDLActivity.sendCommand(SHOW_SAVE_DIALOG, null)
                return
            }
            MenuItems.SETTINGS -> {
                context.startActivity(Intent(context, PrefsActivity::class.java))
                return
            }
            MenuItems.CHEATS -> {
                SDLActivity.sendCommand(HIDE_MENU, null)
                SDLActivity.cthShowCheats()
                return
            }
            MenuItems.JUKEBOX -> {
                SDLActivity.sendCommand(HIDE_MENU, null)
                SDLActivity.sendCommand(SHOW_JUKEBOX, null)
                return
            }
        }
    }
}
