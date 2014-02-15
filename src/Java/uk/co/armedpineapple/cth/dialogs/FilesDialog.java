/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
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
import uk.co.armedpineapple.cth.Files.FileDetails;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.SDLActivity;

public abstract class FilesDialog extends Dialog implements OnItemClickListener {

    protected CTHActivity ctx;
    protected String path;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

		/*
         * Unpause the game if the back button is pressed. Note that it is possible
		 * for getGameSpeed() to return null if the game is on the main menu screen,
		 * for example.
		 */

        Integer speed;
        if ((speed = ctx.app.configuration.getGameSpeed()) != null) {
            SDLActivity.cthGameSpeed(speed);
        }
    }

    public FilesDialog(SDLActivity context, String path, int layout) {
        super(context);

        this.ctx = context;
        this.path=path;

        setContentView(layout);

        Button cancelButton = (Button) findViewById(R.id.dismissDialogButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);


    }

    public void updateSaves(final Context ctx, ListView savesList, String directory,
                            boolean hasNewButton) throws IOException {

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
        FilesAdapter arrayAdapter = new FilesAdapter(ctx, saves, hasNewButton);
        savesList.setAdapter(arrayAdapter);
        savesList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        FilesAdapter adapter = (FilesAdapter) parent.getAdapter();
        if (adapter.hasNewButton() && position == 0) {
            onNewClicked();
        } else {
            FileDetails clicked = (FileDetails) adapter.getItem(
                    adapter.hasNewButton() ? position - 1 : position);
            onSelectedFile(clicked.getDirectory(),clicked.getFileName());
        }
    }

    public abstract void onSelectedFile(String directory, String filename);

    public abstract void refreshSaves(Context ctx) throws IOException;

    public void onNewClicked() {

    }

}
