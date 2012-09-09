/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth.dialogs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import uk.co.armedpineapple.corsixth.Files;
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

	private String[] saves;
	private ArrayAdapter<String> arrayAdapter;
	private String savePath;
	private ListView savesList;
	private Button cancelButton;
	protected SDLActivity ctx;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		SDLActivity.cthGameSpeed(ctx.config.getGameSpeed());
	}

	public FilesDialog(SDLActivity context, String path, int layout) {
		super(context);
		this.ctx = context;

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
				return filename.toLowerCase().endsWith(".sav");
			}
		});

		arrayAdapter = new ArrayAdapter<String>(ctx, R.layout.files_list, saves);
		savesList.setAdapter(arrayAdapter);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		onSelectedFile(saves[position]);
	}

	public abstract void onSelectedFile(String file);

}
