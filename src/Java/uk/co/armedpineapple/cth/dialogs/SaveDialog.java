/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.dialogs;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.io.IOException;

import uk.co.armedpineapple.cth.CommandHandler.Command;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.SDLActivity;

public class SaveDialog extends FilesDialog {

    private static final String LOG_TAG = "SaveDialog";
    private AlertDialog newSaveDialog;
    private final ListView filesList;

    public SaveDialog(SDLActivity context, final String path) {
        super(context, path, R.layout.files_dialog);
        setTitle(R.string.save_game_dialog_title);

        FrameLayout flayout = (FrameLayout) findViewById(R.id.files_frame);
        filesList = (ListView) getLayoutInflater().inflate(R.layout.files_list, null);
        flayout.addView(filesList);

        final EditText editTextBox = new EditText(context);
        Builder builder = new Builder(context);
        builder.setMessage(R.string.save_game_dialog_message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                onSelectedFile(path, editTextBox.getText().toString() + ".sav");
                dismiss();
            }

        });

        builder.setView(editTextBox);
        newSaveDialog = builder.create();

    }

    @Override
    public void onSelectedFile(String directory, String file) {
        Log.d(LOG_TAG, "Saving: " + file);

        // Reset the game speed - we don't want to save when the game is paused!
        SDLActivity.cthGameSpeed(ctx.app.configuration.getGameSpeed());

        // Save the game
        SDLActivity.cthSaveGame(file);

        // Pause game
        SDLActivity.cthGameSpeed(0);

        SDLActivity.sendCommand(Command.HIDE_MENU, null);

        dismiss();

    }

    @Override
    public void onNewClicked() {
        newSaveDialog.show();

    }

    @Override
    public void refreshSaves(Context ctx) throws IOException {
        updateSaves(ctx,filesList,path, true);
    }
}
