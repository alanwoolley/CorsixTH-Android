/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.dialogs

import android.app.AlertDialog
import android.app.AlertDialog.Builder
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import uk.co.armedpineapple.cth.CommandHandler.Command
import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.Reporting
import uk.co.armedpineapple.cth.SDLActivity
import java.io.IOException

class SaveDialog(context: SDLActivity, path: String) : FilesDialog(context, path, R.layout.files_dialog, R.string.save_game_dialog_title) {
    private val newSaveDialog: AlertDialog
    private val filesList: RecyclerView

    init {


        val flayout = findViewById<View>(R.id.files_frame) as FrameLayout
        filesList = layoutInflater.inflate(R.layout.files_list, null) as RecyclerView

        val llm = LinearLayoutManager(context)
        filesList.layoutManager = llm

        flayout.addView(filesList)

        val editTextBox = EditText(context)
        val builder = Builder(context)
        builder.setMessage(R.string.save_game_dialog_message)
        builder.setNeutralButton("Save") { dialog, which ->
            onSelectedFile(path, editTextBox.text.toString() + ".sav")
            dismiss()
        }

        builder.setView(editTextBox)
        newSaveDialog = builder.create()

        findViewById<View>(R.id.fab).setOnClickListener { newSaveDialog.show() }

    }

    override fun onSelectedFile(directory: String, filename: String) {
        Log.d("Saving: " + filename)

        // Reset the game speed - we don't want to save when the game is paused!
        SDLActivity.cthGameSpeed(ctx.app.configuration!!.gameSpeed)

        // Save the game
        SDLActivity.cthSaveGame(filename)

        SDLActivity.sendCommand(Command.HIDE_MENU, null)

        dismiss()

    }


    @Throws(IOException::class)
    override fun refreshSaves(ctx: Context) {
        updateSaves(ctx, filesList, path)
    }

    companion object {

        private val Log = Reporting.getLogger("SaveDialog")
    }
}
