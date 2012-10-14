/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth.dialogs;

import uk.co.armedpineapple.corsixth.R;
import uk.co.armedpineapple.corsixth.SDLActivity;
import android.util.Log;

public class LoadDialog extends FilesDialog {


	public LoadDialog(SDLActivity context, String path) {
		super(context, path, R.layout.files_dialog, false);
		setTitle("Load Game");
	}

	@Override
	public void onSelectedFile(String file) {
		Log.d(getClass().getSimpleName(), "Loading: " + file);
		SDLActivity.cthLoadGame(file);
		dismiss();
	}

	
}
