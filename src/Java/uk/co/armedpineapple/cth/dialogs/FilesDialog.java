/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import uk.co.armedpineapple.cth.CTHActivity;
import uk.co.armedpineapple.cth.Files;
import uk.co.armedpineapple.cth.FileDetails;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.SDLActivity;
import uk.co.armedpineapple.cth.persistence.PersistenceHelper;

public abstract class FilesDialog extends Dialog {

    protected CTHActivity ctx;
    protected String      path;

    public FilesDialog(SDLActivity context, String path, int layout, int title) {
        super(context);

        if (title == -1) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        } else {
            setTitle(title);
        }

        this.ctx = context;
        this.path = path;

        setContentView(layout);



        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public void updateSaves(final Context ctx, RecyclerView savesList, String directory) throws IOException {

        List<FileDetails> saves = Files.listFilesInDirectory(directory, new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {

				/*
                 * Filter out any files that don't end with .sav, or are the quicksave
				 * or autosave files
				 */

                return filename.toLowerCase(Locale.US).endsWith(".sav")
                        && !filename.toLowerCase(Locale.US).equals(
                        ctx.getString(R.string.quicksave_name))
                        && !filename.toLowerCase(Locale.US).equals(
                        ctx.getString(R.string.autosave_name));
            }
        });

        // Sort the saves to be most recent first.

        Collections.sort(saves, Collections.reverseOrder());


        // Update the adapter
        FilesAdapter arrayAdapter = new FilesAdapter(saves, new FilesAdapter.FilesClickListener() {
            @Override
            public void onItemClick(FileDetails details) {
                onSelectedFile(path, details.getFileName());
            }
        }, new PersistenceHelper(ctx));

        savesList.setAdapter(arrayAdapter);
        savesList.setHasFixedSize(true);

    }

    public abstract void onSelectedFile(String directory, String filename);

    public abstract void refreshSaves(Context ctx) throws IOException;

}
