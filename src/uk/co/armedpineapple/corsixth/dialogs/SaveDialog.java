/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth.dialogs;

import uk.co.armedpineapple.corsixth.R;
import uk.co.armedpineapple.corsixth.SDLActivity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SaveDialog extends FilesDialog {

	private Button newButton;
	private AlertDialog newSaveDialog;

	public SaveDialog(SDLActivity context, String path) {
		super(context, path, R.layout.save_dialog);
		setTitle("Save Game");

		newButton = (Button) findViewById(R.id.newSaveButton);
		newButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				newSaveDialog.show();
			}

		});

		final EditText editTextBox = new EditText(context);
		Builder builder = new Builder(context);
		builder.setMessage("Please enter a save game name");
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				onSelectedFile(editTextBox.getText().toString() + ".sav");
				dismiss();
			}

		});

		builder.setView(editTextBox);
		newSaveDialog = builder.create();

	}

	@Override
	public void onSelectedFile(String file) {
		Log.d(getClass().getSimpleName(), "Saving: " + file);
		SDLActivity.cthGameSpeed(ctx.config.getGameSpeed());
		SDLActivity.cthSaveGame(file);
		dismiss();

	}
}
