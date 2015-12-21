/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

import org.apache.commons.io.output.CountingOutputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Class to help with file manipulation
 */
@SuppressWarnings("nls")
public class Files {

    private static final Reporting.Logger Log = Reporting.getLogger("Files");

    // Look for these files when trying to work out if the original Theme
    // Hospital files are present

    private static final String[] RequiredSoundFiles = {"Sound/Data/Sound-0.dat"};

    private static final String[] RequiredMusicFiles = {"Sound/Midi/ATLANTIS.XMI"};
    private static final String[] RequiredDataFiles  = {"Data/VBlk-0.tab",
            "Levels/Level.L1", "QData/SPointer.dat"};

    // Places to look for files
    @SuppressLint("SdCardPath")
    private static final String[] SearchRoot = {"/mnt/sdcard",
            "/sdcard", "/mnt/sdcard/external_sd", "/mnt/emmc", "/mnt/sdcard/emmc"};

    private static final String[] SearchDirs = {"th", "TH",
            "themehospital", "ThemeHospital", "Themehospital", "theme_hospital",
            "Theme_Hospital"};

    private Files() {
    }

    /**
     * Checks if a file exists on the filesystem
     *
     * @param filename the file to check
     * @return true if file exists
     */
    public static Boolean doesFileExist(String filename) {
        if (filename == null) { return false; }
        return new File(filename).exists();
    }

    /**
     * Removes path separators from the end of path strings
     *
     * @param path the path to trim
     * @return the trimmed path
     */
    public static String trimPath(String path) {
        return path.endsWith(File.separator) ? path.substring(0, path.length() - 1)
                : path;
    }

    /**
     * Gets the external storage path including a trailing file separator.
     *
     * @return the external storage path
     */
    public static String getExtStoragePath() {
        return trimPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                + File.separator;
    }

    /**
     * Checks if Theme Hospital data files exist in a directory
     *
     * @param directory the directory to search
     * @return true if data files exist
     */
    public static Boolean hasDataFiles(String directory) {
        return doFilesExist(RequiredDataFiles, directory);
    }

    /**
     * Checks if Theme Hospital music files exist in a directory
     *
     * @param directory the directory to search
     * @return true if music files exist
     */
    public static Boolean hasMusicFiles(String directory) {
        return doFilesExist(RequiredMusicFiles, directory);
    }

    /**
     * Checks if Theme Hospital sound files exist in a directory
     *
     * @param directory the directory to search
     * @return true if sound files exist
     */
    public static Boolean hasSoundFiles(String directory) {
        return doFilesExist(RequiredSoundFiles, directory);
    }

    /**
     * Checks if all the given files exist in a given directory
     *
     * @param files     an array of filenames to check for
     * @param directory the directory to search
     * @return true if all the files are found
     */
    private static Boolean doFilesExist(String[] files, String directory) {

        if (directory == null) {
            return false;
        }

        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }

        // As soon as a file is not found in the directory, fail.
        for (String file : files) {
            File f = new File(directory + File.separator + file);
            if (!f.exists()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if external storage can be accessed. This should be called any time
     * external storage is used to make sure that it is accessible. Reasons that
     * it may not be accessible include SD card missing, mounted on a computer,
     * not formatted etc.
     *
     * @return true if external storage can be accessed
     */
    public static boolean canAccessExternalStorage() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    /**
     * Lists all the files in a directory. It will not list files in
     * subdirectories.
     *
     * @param directory the directory to search in
     * @param filter    a {@link FilenameFilter} to filter the search by
     * @return a String array of filenames
     * @throws IOException if the directory doesn't exist or cannot be accessed
     */
    public static List<FileDetails> listFilesInDirectory(String directory,
                                                         FilenameFilter filter) throws IOException {

        File f = new File(directory);

        if (!f.exists() || !f.isDirectory()) {
            return Collections.emptyList();
        }

        List<FileDetails> files = new ArrayList<>();
        if (f.isDirectory()) {

            String[] filesArray = f.list(filter);

            for (String fileName : filesArray) {

                File file = new File(directory + File.separator + fileName);
                long lastModified = file.lastModified();
                files.add(new FileDetails(fileName, directory, new Date(lastModified)));
            }

            return files;
        }

        // The directory doesn't exist
        Log.d("Directory " + directory
                + " doesn't exist");
        throw new FileNotFoundException();

    }

    /**
     * Returns a string containing the text from a raw resource
     *
     * @param ctx      a activityContext
     * @param resource the resource to read
     * @return a String containing the text contents of the resource
     * @throws IOException if the resource cannot be found or read
     */
    public static String readTextFromResource(Context ctx, int resource)
            throws IOException {

        InputStream inputStream = null;
        try {
            inputStream = ctx.getResources().openRawResource(resource);
            String r = CharStreams.toString(new InputStreamReader(inputStream));
            return r;
        } finally {
            if (inputStream != null) inputStream.close();

        }
    }


    /**
     * Copies an assets
     *
     * @param ctx           a activityContext
     * @param assetFilename the filename of the asset
     * @param destination   the destination directory
     * @throws IOException if the asset cannot be copied
     */
    public static void copyAsset(Context ctx, String assetFilename,
                                 String destination) throws IOException {
        InputStream in = null;
        FileOutputStream out = null;

        try {
            AssetManager assetManager = ctx.getAssets();

            in = assetManager.open(assetFilename);

            String newFileName = destination + "/" + assetFilename;
            File newFile = new File(newFileName);
            File dir = newFile.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            Log.v("Copying file [" + assetFilename
                    + "] to [" + newFileName + "]");

            out = new FileOutputStream(newFile);
            ByteStreams.copy(in, out);


        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
        }

    }

    public static class FindFilesTask extends
            AsyncTask<Void, Void, AsyncTaskResult<String>> {

        @Override
        protected AsyncTaskResult<String> doInBackground(Void... arg0) {
            return new AsyncTaskResult<>(findGameFiles());
        }

        private String findGameFiles() {
            String result;
            List<String> searchPaths = new ArrayList<>(
                    Arrays.asList(SearchRoot));
            String sdcard = trimPath(Environment.getExternalStorageDirectory()
                    .getAbsolutePath());

            if (!searchPaths.contains(sdcard)) {
                searchPaths.add(sdcard);
            }

            // Search common locations first
            for (String root : searchPaths) {
                if (isCancelled()) {
                    Log.d("Task cancelled");
                    return null;
                }
                for (String dir : SearchDirs) {
                    String toSearch = root + File.separator + dir;
                    if ((result = findGameFilesInternal(toSearch)) != null) {

                        return result;
                    }
                }
            }

            for (String root : searchPaths) {
                if (isCancelled()) {
                    Log.d("Task cancelled");
                    return null;
                }

                if ((result = findGameFilesInternal(root)) != null) {
                    Log.d("Found game files in: " + result);
                    return result;
                }
            }
            return null;
        }

        private String findGameFilesInternal(String root) {
            if (!isCancelled()) {
                String result;
                File dir = new File(root);

                if (hasDataFiles(root)) {
                    return root;
                }

                if (dir.exists() && dir.isDirectory()) {
                    File[] sub = dir.listFiles();
                    if (sub != null) {
                        for (File f : sub) {
                            if (f.isDirectory()) {
                                if ((result = findGameFilesInternal(trimPath(f
                                        .getAbsolutePath()))) != null) {
                                    Log.d("Found game files in: "
                                            + result);
                                    return result;
                                }
                            }
                        }
                    }
                }
            } else {
                Log.d("Task cancelled");
            }
            return null;
        }

    }

    /**
     * AsyncTask for downloading a file
     */
    public static class DownloadFileTask extends
            AsyncTask<String, Integer, AsyncTaskResult<File>> {
        public static final int CONNECT_TIMEOUT = 30000;
        final String  downloadTo;
        final Context ctx;
        WakeLock downloadLock;

        public DownloadFileTask(String downloadTo, Context ctx) {
            this.downloadTo = downloadTo;
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager pm = (PowerManager) ctx
                    .getSystemService(Context.POWER_SERVICE);
            downloadLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                    "downloading");
            downloadLock.acquire();
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<File> result) {
            super.onPostExecute(result);
            downloadLock.release();
        }

        @Override
        protected AsyncTaskResult<File> doInBackground(String... url) {
            URL downloadUrl;
            URLConnection ucon;
            InputStream input = null;
            FileOutputStream fos = null;
            CountingOutputStream cos = null;

            try {
                downloadUrl = new URL(url[0]);

                File file = new File(downloadTo + "/" + downloadUrl.getFile());
                file.getParentFile().mkdirs();

                ucon = downloadUrl.openConnection();
                ucon.setConnectTimeout(CONNECT_TIMEOUT);
                ucon.connect();

                if (ucon.getContentType() == null) {
                    throw new IOException("Could not connect to server");
                }

                final int fileSize = ucon.getContentLength();

                input = new BufferedInputStream(ucon.getInputStream());
                fos = new FileOutputStream(file);
                cos = new CountingOutputStream(fos) {

                    int total = 0;

                    @Override
                    protected void afterWrite(int n) throws IOException {
                        super.afterWrite(n);
                        publishProgress(total += n, fileSize);
                    }

                };

                ByteStreams.copy(input, cos);

                Log.d("Downloaded file to: " + file.getAbsolutePath());

                return new AsyncTaskResult<>(file);

            } catch (MalformedURLException e) {
                return new AsyncTaskResult<>(e);
            } catch (IOException e) {
                return new AsyncTaskResult<>(e);
            } finally {
                Closeables.closeQuietly(input);
            }
        }

    }

    /**
     * AsyncTask for extracting a .zip file to a directory
     */
    public static class UnzipTask extends
            AsyncTask<File, Integer, AsyncTaskResult<String>> {
        final String  unzipTo;
        final Context ctx;
        WakeLock unzipLock;

        public UnzipTask(String unzipTo, Context ctx) {
            this.unzipTo = unzipTo;
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager pm = (PowerManager) ctx
                    .getSystemService(Context.POWER_SERVICE);
            unzipLock = pm
                    .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "unzipping");
            unzipLock.acquire();
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<String> result) {
            super.onPostExecute(result);
            unzipLock.release();
        }

        @Override
        protected AsyncTaskResult<String> doInBackground(File... files) {
            try {

                File to = new File(unzipTo);
                to.mkdirs();

                ZipFile zf = new ZipFile(files[0]);
                int entryCount = zf.size();

                Enumeration<? extends ZipEntry> entries = zf.entries();
                int count = 0;

                while (entries.hasMoreElements()) {
                    ZipEntry ze = entries.nextElement();
                    Log.v("Unzipping " + ze.getName());

                    File f = new File(unzipTo + ze.getName());
                    if (!f.getParentFile().exists()) {
                        f.getParentFile().mkdirs();
                    }

                    if (ze.isDirectory()) {

                        if (!f.isDirectory()) {
                            f.mkdirs();
                        }
                    } else {

                        InputStream zin = null;
                        FileOutputStream fout = null;
                        try {
                            zin = zf.getInputStream(ze);
                            fout = new FileOutputStream(unzipTo + ze.getName());
                            ByteStreams.copy(zin, fout);
                        } finally {
                            Closeables.closeQuietly(zin);

                        }

                    }

                    count++;
                    publishProgress(count, entryCount);
                }

            } catch (IOException e) {
                return new AsyncTaskResult<>(e);
            }

            return new AsyncTaskResult<>(unzipTo);

        }
    }

    public class StorageUnavailableException extends Exception {

    }

}
