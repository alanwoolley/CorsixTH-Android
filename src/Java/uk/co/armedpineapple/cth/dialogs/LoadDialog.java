/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.dialogs;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.io.IOException;

import uk.co.armedpineapple.cth.CommandHandler.Command;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.SDLActivity;

public class LoadDialog extends FilesDialog {

    private static final String LOG_TAG = "LoadDialog";
    private final ListView filesList;

    public LoadDialog(SDLActivity context, String path) {
        super(context, path, R.layout.files_dialog);
        setTitle(R.string.load_game_dialog_title);

        FrameLayout flayout = (FrameLayout) findViewById(R.id.files_frame);
        filesList = (ListView) getLayoutInflater().inflate(R.layout.files_list, null);
        flayout.addView(filesList);
    }

    @Override
    public void onSelectedFile(String directory, String file) {
        Log.d(LOG_TAG, "Loading: " + file);
        SDLActivity.cthLoadGame(file);

        SDLActivity.sendCommand(Command.HIDE_MENU, null);
        dismiss();
    }

    @Override
    public void refreshSaves(Context ctx) throws IOException {
        updateSaves(ctx, filesList, path, false);
    }


}
