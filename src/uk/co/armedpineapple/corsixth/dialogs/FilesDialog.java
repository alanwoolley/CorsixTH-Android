/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth.dialogs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.co.armedpineapple.corsixth.Files;
import uk.co.armedpineapple.corsixth.Files.FileDetails;
import uk.co.armedpineapple.corsixth.R;
import uk.co.armedpineapple.corsixth.SDLActivity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public abstract class FilesDialog extends Dialog implements OnItemClickListener {

	private String savePath;
	private Button cancelButton;

	protected List<FileDetails> saves;
	protected FilesAdapter arrayAdapter;
	protected ListView savesList;
	protected SDLActivity ctx;

	private boolean hasNewButton;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		SDLActivity.cthGameSpeed(ctx.config.getGameSpeed());
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
				if ((speed = ctx.config.getGameSpeed()) != null) {
					SDLActivity.cthGameSpeed(ctx.config.getGameSpeed());
				}
				dismiss();
			}

		});

	}

	public void updateSaves(Context ctx) throws IOException {
		saves = Files.listFilesInDirectory(savePath, new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				return filename.toLowerCase().endsWith(".sav") && !filename.toLowerCase().equals("quicksave.sav");
			}
		});

		
		// Sort the saves to be most recent first.
		
		Collections.sort(saves, Collections.reverseOrder());

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
