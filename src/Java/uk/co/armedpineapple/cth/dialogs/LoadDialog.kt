/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.dialogs

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TabHost
import uk.co.armedpineapple.cth.CommandHandler.Command
import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.Reporting
import uk.co.armedpineapple.cth.SDLActivity
import java.io.File
import java.io.IOException

class LoadDialog(context: SDLActivity, path: String) : FilesDialog(context, path, R.layout.files_dialog, R.string.load_game_dialog_title) {
    private val tabsView: LinearLayout
    private val tabHost: TabHost
    private val userSavesList: RecyclerView
    private val autoSavesList: RecyclerView

    init {

        val flayout = findViewById<View>(R.id.files_frame) as FrameLayout
        tabsView = layoutInflater.inflate(R.layout.files_load_tabs, null) as LinearLayout
        flayout.addView(tabsView)

        tabHost = findViewById<View>(R.id.tabHost) as TabHost
        tabHost.setup()
        val userSavesSpec = tabHost.newTabSpec("Saves")
        userSavesSpec.setIndicator("Saves")
        userSavesSpec.setContent(R.id.user_files)
        val autoSavesSpec = tabHost.newTabSpec("Autosaves")
        autoSavesSpec.setIndicator("Autosaves")
        autoSavesSpec.setContent(R.id.autosave_files)

        tabHost.addTab(userSavesSpec)
        tabHost.addTab(autoSavesSpec)
        tabHost.currentTab = 0

        userSavesList = findViewById<View>(R.id.user_files) as RecyclerView
        autoSavesList = findViewById<View>(R.id.autosave_files) as RecyclerView

        val llm = LinearLayoutManager(context)
        userSavesList.layoutManager = llm

        val llmb = LinearLayoutManager(context)
        autoSavesList.layoutManager = llmb


        findViewById<View>(R.id.fab).visibility = View.GONE
    }

    override fun onSelectedFile(directory: String, filename: String) {
        SDLActivity.sendCommand(Command.HIDE_MENU, null)

        Log.d("Loading: " + filename)
        if (directory.endsWith(File.separator + AUTOSAVES)) {
            SDLActivity.cthLoadGame(AUTOSAVES + File.separator + filename)
        }
        SDLActivity.cthLoadGame(filename)


        dismiss()
    }

    @Throws(IOException::class)
    override fun refreshSaves(ctx: Context) {
        updateSaves(ctx, userSavesList, path)
        updateSaves(ctx, autoSavesList, path + File.separator + AUTOSAVES)
    }

    companion object {

        private val Log = Reporting.getLogger("LoadDialog")
        private const val AUTOSAVES = "Autosaves"
    }


}
