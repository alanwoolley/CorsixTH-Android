/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.dialogs

import android.app.Dialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup.LayoutParams
import android.view.Window
import uk.co.armedpineapple.cth.*
import uk.co.armedpineapple.cth.persistence.PersistenceHelper
import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.util.*

abstract class FilesDialog(context: SDLActivity, protected var path: String, layout: Int, title: Int) : Dialog(context) {

    protected var ctx: CTHActivity

    init {

        if (title == -1) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        } else {
            this.setTitle(title)
        }

        this.ctx = context

        this.setContentView(layout)



        window!!.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    @Throws(IOException::class)
    fun updateSaves(ctx: Context, savesList: RecyclerView, directory: String) {

        val saves = Files.listFilesInDirectory(directory, FilenameFilter { dir, name ->
            (name.toLowerCase(Locale.US).endsWith(".sav")
                    && name.toLowerCase(Locale.US) != ctx.getString(R.string.quicksave_name)
                    && name.toLowerCase(Locale.US) != ctx.getString(R.string.autosave_name))
        })
        // Sort the saves to be most recent first.

        Collections.sort(saves, Collections.reverseOrder<Any>())


        // Update the adapter
        val arrayAdapter = FilesAdapter(saves, object : FilesAdapter.FilesClickListener {
            override fun onItemClick(details: FileDetails) {
                onSelectedFile(path, details.fileName)
            }
        }, PersistenceHelper(ctx))

        savesList.adapter = arrayAdapter
        savesList.setHasFixedSize(true)

    }

    abstract fun onSelectedFile(directory: String, filename: String)

    @Throws(IOException::class)
    abstract fun refreshSaves(ctx: Context)

}
