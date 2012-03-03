package uk.co.armedpineapple.corsixth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.bugsense.trace.BugSenseHandler;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class Files {
	public static ArrayList<String> listAssets(Context ctx, String path) {
		ArrayList<String> assets = new ArrayList<String>();
		listAssetsInternal(ctx, path, assets);
		return assets;

	}

	private static void listAssetsInternal(Context ctx, String path,
			ArrayList<String> paths) {
		AssetManager assetManager = ctx.getAssets();
		String assets[] = null;
		try {
			assets = assetManager.list(path);

			if (assets.length == 0) {
				paths.add(path);

			} else {
				for (int i = 0; i < assets.length; ++i) {
					listAssetsInternal(ctx, path + "/" + assets[i], paths);
				}
			}
		} catch (IOException e) {
			Log.e(Files.class.getSimpleName(),
					"I/O Exception whilst listing files", e);
			BugSenseHandler.log("Files", e);
		}
	}

	public static void copyAsset(Context ctx, String assetFilename,
			String destination) {
		AssetManager assetManager = ctx.getAssets();

		InputStream in = null;
		OutputStream out = null;

		try {
			in = assetManager.open(assetFilename);

			String newFileName = destination + "/" + assetFilename;

			File dir = new File(newFileName).getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}

			out = new FileOutputStream(newFileName);

			Log.i(Files.class.getSimpleName(), "Copying file [" + assetFilename
					+ "] to [" + newFileName + "]");

			byte[] buffer = new byte[1024];
			int read;

			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}

			in.close();

			out.flush();

			out.close();
		} catch (IOException e) {
			Log.e(Files.class.getSimpleName(),
					"I/O Exception whilst copying file", e);
			BugSenseHandler.log("File", e);
		}

	}

}
