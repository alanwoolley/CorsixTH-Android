package uk.co.armedpineapple.corsixth;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import com.bugsense.trace.BugSenseHandler;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/** Class to help with file manipulation */
public class Files {

	private Files() {
	}

	/**
	 * AsyncTask for discovering all the assets
	 * */
	static class DiscoverAssetsTask extends
			AsyncTask<Void, Void, AsyncTaskResult<ArrayList<String>>> {

		ArrayList<String> paths;
		Context ctx;
		String path;

		DiscoverAssetsTask(Context ctx, String path) {
			this.ctx = ctx;
			this.path = path;
		}

		@Override
		protected AsyncTaskResult<ArrayList<String>> doInBackground(
				Void... params) {

			paths = new ArrayList<String>();
			try {
				paths = listAssets(ctx, path);
			} catch (IOException e) {
				Log.e(Files.class.getSimpleName(),
						"I/O Exception whilst listing files", e);
				BugSenseHandler.log("File", e);
				return new AsyncTaskResult<ArrayList<String>>(e);

			}
			return new AsyncTaskResult<ArrayList<String>>(paths);
		}

	}

	/**
	 * AsyncTask for copying assets
	 */
	static class CopyAssetsTask extends
			AsyncTask<ArrayList<String>, Integer, AsyncTaskResult<Void>> {
		WakeLock copyLock;
		Context ctx;
		String root, message;

		CopyAssetsTask(Context ctx, String root) {
			this.ctx = ctx;
			this.root = root;

		}

		@Override
		protected void onPreExecute() {
			PowerManager pm = (PowerManager) ctx
					.getSystemService(Context.POWER_SERVICE);
			copyLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
					"copying");
			copyLock.acquire();
		}

		@Override
		protected AsyncTaskResult<Void> doInBackground(
				ArrayList<String>... params) {
			int max = params[0].size();
			for (int i = 0; i < max; i++) {
				try {
					copyAsset(ctx, params[0].get(i), root);
				} catch (IOException e) {
					return new AsyncTaskResult(e);
				}
				publishProgress(i + 1, max);
			}
			return new AsyncTaskResult(null);
		}

		@Override
		protected void onPostExecute(AsyncTaskResult<Void> result) {
			copyLock.release();
		}
	}

	/** Lists all the assets in a given path */
	public static ArrayList<String> listAssets(Context ctx, String path)
			throws IOException {
		ArrayList<String> assets = new ArrayList<String>();
		listAssetsInternal(ctx, path, assets);
		return assets;

	}

	private static void listAssetsInternal(Context ctx, String path,
			ArrayList<String> paths) throws IOException {
		AssetManager assetManager = ctx.getAssets();
		String assets[] = null;

		assets = assetManager.list(path);

		if (assets.length == 0) {
			paths.add(path);

		} else {
			for (int i = 0; i < assets.length; ++i) {
				listAssetsInternal(ctx, path + "/" + assets[i], paths);
			}
		}

	}

	/** Copies an asset to a given directory */
	public static void copyAsset(Context ctx, String assetFilename,
			String destination) throws IOException {
		AssetManager assetManager = ctx.getAssets();

		InputStream in = null;
		OutputStream out = null;

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

	}

	/** AsyncTask for downloading a file */
	public static class DownloadFileTask extends
			AsyncTask<String, Integer, AsyncTaskResult<File>> {
		String downloadTo;

		public DownloadFileTask(String downloadTo) {
			this.downloadTo = downloadTo;
		}

		@Override
		protected AsyncTaskResult<File> doInBackground(String... url) {
			URL downloadUrl;
			URLConnection ucon;
			try {
				downloadUrl = new URL(url[0]);

				File file = new File(downloadTo + "/" + downloadUrl.getFile());
				file.getParentFile().mkdirs();

				ucon = downloadUrl.openConnection();
				ucon.connect();
				int fileSize = ucon.getContentLength();

				InputStream input = new BufferedInputStream(
						downloadUrl.openStream());
				FileOutputStream fos = new FileOutputStream(file);

				byte data[] = new byte[1024];
				int current = 0, total = 0;

				while ((current = input.read(data)) != -1) {
					total += current;
					publishProgress((int) (total * 100 / fileSize));

					fos.write(data, 0, current);
				}

				fos.flush();
				fos.close();
				input.close();

				Log.d(Files.class.getSimpleName(), "Downloaded file to: "
						+ file.getAbsolutePath());
				return new AsyncTaskResult<File>(file);

			} catch (MalformedURLException e) {
				return new AsyncTaskResult<File>(e);
			} catch (IOException e) {
				return new AsyncTaskResult<File>(e);
			}

		}

	}

	/** AsyncTask for extracting a .zip file to a directory */
	public static class UnzipTask extends
			AsyncTask<File, Integer, AsyncTaskResult<String>> {
		String unzipTo;

		public UnzipTask(String unzipTo) {
			this.unzipTo = unzipTo;
		}

		@Override
		protected AsyncTaskResult<String> doInBackground(File... files) {
			try {
				ZipFile zf = new ZipFile(files[0]);
				int entryCount = zf.size();

				Enumeration entries = zf.entries();
				int count = 0;

				while (entries.hasMoreElements()) {
					ZipEntry ze = (ZipEntry) entries.nextElement();
					Log.v(Files.class.getSimpleName(),
							"Unzipping " + ze.getName());

					if (ze.isDirectory()) {
						File f = new File(unzipTo + ze.getName());

						if (!f.isDirectory()) {
							f.mkdirs();
						}
					} else {
						InputStream zin = zf.getInputStream(ze);

						FileOutputStream fout = new FileOutputStream(unzipTo
								+ ze.getName());

						byte[] buffer = new byte[1024];
						int read;

						while ((read = zin.read(buffer)) != -1) {
							fout.write(buffer, 0, read);
						}

						zin.close();
						fout.close();

					}

					count++;
					publishProgress(count * 100 / entryCount);

				}

			} catch (IOException e) {
				BugSenseHandler.log("File", e);
				return new AsyncTaskResult<String>(e);
			}

			return new AsyncTaskResult<String>(unzipTo);

		}
	}
}