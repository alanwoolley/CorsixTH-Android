/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.dialogs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import uk.co.armedpineapple.cth.CTHActivity;
import uk.co.armedpineapple.cth.Files;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.SDLActivity;
import uk.co.armedpineapple.cth.Files.FileDetails;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public abstract class FilesDialog extends Dialog implements OnItemClickListener {

	private String							savePath;
	private Button							cancelButton;

	protected List<FileDetails>	saves;
	protected FilesAdapter			arrayAdapter;
	protected ListView					savesList;
	protected CTHActivity				ctx;

	private boolean							hasNewButton;

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

	public FilesDialog(SDLActivity context, String path, int layout,
			boolean hasNewButton) {
		super(context);

		this.ctx = context;
		this.hasNewButton = hasNewButton;

		savePath = path;
		setContentView(layout);
		savesList = (ListView) findViewById(R.id.filesList);
		savesList.setOnItemClickListener(this);
		cancelButton = (Button) findViewById(R.id.dismissDialogButton);

		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Integer speed;
				if ((speed = ctx.app.configuration.getGameSpeed()) != null) {

					// Unpause the game if the cancel button is pressed
					SDLActivity.cthGameSpeed(speed);

				}
				dismiss();
			}

		});

	}

	public void updateSaves(final Context ctx) throws IOException {
		saves = Files.listFilesInDirectory(savePath, new FilenameFilter() {

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

		arrayAdapter = new FilesAdapter(ctx, saves, hasNewButton);
		savesList.setAdapter(arrayAdapter);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (hasNewButton && position == 0) {
			onNewClicked();
		} else {
			onSelectedFile(saves.get(hasNewButton ? position - 1 : position)
					.getFileName());
		}
	}

	public abstract void onSelectedFile(String file);

	public void onNewClicked() {

	}

}
