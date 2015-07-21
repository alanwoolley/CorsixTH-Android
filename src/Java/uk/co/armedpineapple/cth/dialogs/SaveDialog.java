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
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.io.IOException;

import uk.co.armedpineapple.cth.CommandHandler.Command;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.Reporting;
import uk.co.armedpineapple.cth.SDLActivity;

public class SaveDialog extends FilesDialog {

    private static final Reporting.Logger Log = Reporting.getLogger("SaveDialog");
    private AlertDialog newSaveDialog;
    private final ListView filesList;

    public SaveDialog(SDLActivity context, final String path) {
        super(context, path, R.layout.files_dialog, R.string.save_game_dialog_title);


        FrameLayout flayout = (FrameLayout) findViewById(R.id.files_frame);
        filesList = (ListView) getLayoutInflater().inflate(R.layout.files_list, null);
        flayout.addView(filesList);

        final EditText editTextBox = new EditText(context);
        Builder builder = new Builder(context);
        builder.setMessage(R.string.save_game_dialog_message);
        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                onSelectedFile(path, editTextBox.getText().toString() + ".sav");
                dismiss();
            }

        });

        builder.setView(editTextBox);
        newSaveDialog = builder.create();

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newSaveDialog.show();

            }
        });

    }

    @Override
    public void onSelectedFile(String directory, String file) {
        Log.d("Saving: " + file);

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
    public void refreshSaves(Context ctx) throws IOException {
        updateSaves(ctx,filesList,path);
    }
}
