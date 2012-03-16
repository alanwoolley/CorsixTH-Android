package uk.co.armedpineapple.corsixth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.bugsense.trace.BugSenseHandler;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class Files {

	static class DiscoverAssetsTask extends
			AsyncTask<Void, Void, ArrayList<String>> {

		ProgressDialog dialog;
		ArrayList<String> paths;
		Context ctx;
		String message, path;

		DiscoverAssetsTask(Context ctx, String message, String path) {
			this.ctx = ctx;
			this.message = message;
			this.path = path;
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(ctx);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMessage(message);
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			paths = new ArrayList<String>();
			paths = listAssets(ctx, path);
			return paths;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			dialog.hide();
		}
	}

	static class CopyAssetsTask extends
			AsyncTask<ArrayList<String>, Integer, Void> {
		ProgressDialog dialog;
		WakeLock copyLock;
		Context ctx;
		String root, message;

		CopyAssetsTask(Context ctx, String message, String root) {
			this.ctx = ctx;
			this.root = root;
			this.message = message;
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(ctx);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setMessage(message);
			dialog.setIndeterminate(false);
			dialog.setCancelable(false);
			dialog.show();
			PowerManager pm = (PowerManager) ctx
					.getSystemService(Context.POWER_SERVICE);
			copyLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
					"copying");
			copyLock.acquire();
		}

		@Override
		protected Void doInBackground(ArrayList<String>... params) {
			int max = params[0].size();
			for (int i = 0; i < max; i++) {
				copyAsset(ctx, params[0].get(i), root);
				publishProgress(i + 1, max);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			dialog.setMax(values[1]);
			dialog.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			copyLock.release();
			dialog.hide();

		}
	}

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
