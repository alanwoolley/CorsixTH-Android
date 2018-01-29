/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import com.google.common.io.ByteStreams
import com.google.common.io.CharStreams
import com.google.common.io.Closeables
import org.apache.commons.io.output.CountingOutputStream
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.util.*
import java.util.zip.ZipFile

/**
 * Class to help with file manipulation
 */
class Files private constructor() {

    open class FindFilesTask : AsyncTask<Void, Void, AsyncTaskResult<String>>() {

        override fun doInBackground(vararg arg0: Void): AsyncTaskResult<String> {
            return AsyncTaskResult(findGameFiles())
        }

        private fun findGameFiles(): String? {
            var result: String?
            val searchPaths = ArrayList(
                    Arrays.asList(*SearchRoot))
            val sdcard = trimPath(Environment.getExternalStorageDirectory()
                    .absolutePath)

            if (!searchPaths.contains(sdcard)) {
                searchPaths.add(sdcard)
            }

            // Search common locations first
            for (root in searchPaths) {
                if (isCancelled) {
                    Log.d("Task cancelled")
                    return null
                }
                for (dir in SearchDirs) {
                    val toSearch = root + File.separator + dir
                    val r = findGameFilesInternal(toSearch)
                    if (r != null) {
                        return r
                    }
                }
            }

            for (root in searchPaths) {
                if (isCancelled) {
                    Log.d("Task cancelled")
                    return null
                }

                val r = findGameFilesInternal(root)
                if (r != null) {
                    Log.d("Found game files in: " + r)
                    return r
                }
            }
            return null
        }

        private fun findGameFilesInternal(root: String): String? {
            if (!isCancelled) {
                val dir = File(root)

                if (hasDataFiles(root)) {
                    return dir.absolutePath
                }

                if (dir.exists() && dir.isDirectory) {
                    val sub = dir.listFiles()
                    if (sub != null) {
                        for (f in sub) {
                            if (f.isDirectory) {
                                val r = findGameFilesInternal(trimPath(f
                                        .absolutePath))
                                if (r != null) {
                                    Log.d("Found game files in: " + r)
                                    return r
                                }
                            }
                        }
                    }
                }
            } else {
                Log.d("Task cancelled")
            }
            return null
        }

    }

    /**
     * AsyncTask for downloading a file
     */
    open class DownloadFileTask(private val downloadTo: String, private val pm: PowerManager) : AsyncTask<String, Int, AsyncTaskResult<File>>() {
        private var downloadLock: WakeLock? = null

        override fun onPreExecute() {
            super.onPreExecute()
            downloadLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                    "downloading")
            downloadLock?.acquire()
        }

        override fun onPostExecute(result: AsyncTaskResult<File>) {
            super.onPostExecute(result)
            downloadLock?.release()
        }

        override fun doInBackground(vararg url: String): AsyncTaskResult<File> {
            val downloadUrl: URL
            val ucon: URLConnection
            var input: InputStream? = null
            var fos: FileOutputStream? = null
            var cos: CountingOutputStream? = null

            try {
                downloadUrl = URL(url[0])

                val file = File(downloadTo + "/" + downloadUrl.file)
                file.parentFile.mkdirs()

                ucon = downloadUrl.openConnection()
                ucon.connectTimeout = CONNECT_TIMEOUT
                ucon.connect()

                if (ucon.contentType == null) {
                    throw IOException("Could not connect to server")
                }

                val fileSize = ucon.contentLength

                input = BufferedInputStream(ucon.getInputStream())
                fos = FileOutputStream(file)
                cos = object : CountingOutputStream(fos) {

                    internal var total = 0

                    @Throws(IOException::class)
                    override fun afterWrite(n: Int) {
                        super.afterWrite(n)
                        total += n
                        publishProgress(total, fileSize)
                    }

                }

                ByteStreams.copy(input, cos)

                Log.d("Downloaded file to: " + file.absolutePath)

                return AsyncTaskResult(file)

            } catch (e: MalformedURLException) {
                return AsyncTaskResult(e)
            } catch (e: IOException) {
                return AsyncTaskResult(e)
            } finally {
                Closeables.closeQuietly(input)
            }
        }

        companion object {
            const val CONNECT_TIMEOUT = 30000
        }

    }

    /**
     * AsyncTask for extracting a .zip file to a directory
     */
    open class UnzipTask(internal val unzipTo: String, private val pm: PowerManager) : AsyncTask<File, Int, AsyncTaskResult<String>>() {
        private var unzipLock: WakeLock? = null

        override fun onPreExecute() {
            super.onPreExecute()
            unzipLock = pm
                    .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "unzipping")
            unzipLock?.acquire()
        }

        override fun onPostExecute(result: AsyncTaskResult<String>) {
            super.onPostExecute(result)
            unzipLock?.release()
        }

        override fun doInBackground(vararg files: File): AsyncTaskResult<String> {
            try {

                val to = File(unzipTo)
                to.mkdirs()

                val zf = ZipFile(files[0])
                val entryCount = zf.size()

                val entries = zf.entries()
                var count = 0

                while (entries.hasMoreElements()) {
                    val ze = entries.nextElement()
                    Log.v("Unzipping " + ze.name)

                    val f = File(unzipTo + ze.name)
                    if (!f.parentFile.exists()) {
                        f.parentFile.mkdirs()
                    }

                    if (ze.isDirectory) {

                        if (!f.isDirectory) {
                            f.mkdirs()
                        }
                    } else {

                        var zin: InputStream? = null
                        var fout: FileOutputStream? = null
                        try {
                            zin = zf.getInputStream(ze)
                            fout = FileOutputStream(unzipTo + ze.name)
                            ByteStreams.copy(zin!!, fout)
                        } finally {
                            Closeables.closeQuietly(zin)

                        }

                    }

                    count++
                    publishProgress(count, entryCount)
                }

            } catch (e: IOException) {
                return AsyncTaskResult(e)
            }

            return AsyncTaskResult(unzipTo)

        }
    }

    inner class StorageUnavailableException : Exception()

    companion object {

        private val Log = Reporting.getLogger("Files")

        // Look for these files when trying to work out if the original Theme
        // Hospital files are present

        private val RequiredSoundFiles = arrayOf("Sound/Data/Sound-0.dat")

        private val RequiredMusicFiles = arrayOf("Sound/Midi/ATLANTIS.XMI")
        private val RequiredDataFiles = arrayOf("Data/VBlk-0.tab", "Levels/Level.L1", "QData/SPointer.dat")

        // Places to look for files
        @SuppressLint("SdCardPath")
        private val SearchRoot = arrayOf("/mnt/sdcard", "/mnt/sdcard/external_sd", "/mnt/emmc", "/mnt/sdcard/emmc")

        private val SearchDirs = arrayOf("th", "themehospital", "theme_hospital")

        /**
         * Checks if a file exists on the filesystem
         *
         * @param filename the file to check
         * @return true if file exists
         */
        fun doesFileExist(filename: String?): Boolean {
            return if (filename == null) {
                false
            } else File(filename).exists()
        }

        /**
         * Removes path separators from the end of path strings
         *
         * @param path the path to trim
         * @return the trimmed path
         */
        fun trimPath(path: String): String {
            return if (path.endsWith(File.separator))
                path.substring(0, path.length - 1)
            else
                path
        }

        /**
         * Gets the external storage path including a trailing file separator.
         *
         * @return the external storage path
         */
        val extStoragePath: String
            get() = trimPath(Environment.getExternalStorageDirectory().absolutePath) + File.separator

        /**
         * Checks if Theme Hospital data files exist in a directory
         *
         * @param directory the directory to search
         * @return true if data files exist
         */
        fun hasDataFiles(directory: String): Boolean {
            return doFilesExist(RequiredDataFiles, directory)
        }

        /**
         * Checks if Theme Hospital music files exist in a directory
         *
         * @param directory the directory to search
         * @return true if music files exist
         */
        fun hasMusicFiles(directory: String): Boolean {
            return doFilesExist(RequiredMusicFiles, directory)
        }

        /**
         * Checks if Theme Hospital sound files exist in a directory
         *
         * @param directory the directory to search
         * @return true if sound files exist
         */
        fun hasSoundFiles(directory: String): Boolean {
            return doFilesExist(RequiredSoundFiles, directory)
        }

        /**
         * Checks if all the given files exist in a given directory
         *
         * @param files     an array of filenames to check for
         * @param directory the directory to search
         * @return true if all the files are found
         */
        private fun doFilesExist(files: Array<String>, directory: String?): Boolean {

            if (directory == null) {
                return false
            }

            val dir = File(directory)
            if (!dir.exists() || !dir.isDirectory) {
                return false
            }

            // As soon as a file is not found in the directory, fail.

            return files.all { locateFileCaseInsensitive(it, directory) }
        }

        private fun locateFileCaseInsensitive(file: String, directory: String?): Boolean {

            if (directory == null) {
                return false
            }

            val dir = File(directory)
            if (!dir.exists() || !dir.isDirectory) {
                return false
            }

            if (file.contains(File.separator)) {
                val firstPart = file.substring(0, file.indexOf(File.separator))
                for (f in dir.listFiles()) {
                    if (f.isDirectory && f.name.toLowerCase() == firstPart.toLowerCase()) {
                        if (locateFileCaseInsensitive(file.substring(file.indexOf(File.separator) + 1), f.absolutePath)) {
                            return true
                        }
                    }
                }
            } else {
                for (f in dir.list()) {
                    if (f.toLowerCase() == file.toLowerCase()) {
                        return true
                    }
                }
            }

            return false

        }


        /**
         * Checks if external storage can be accessed. This should be called any time
         * external storage is used to make sure that it is accessible. Reasons that
         * it may not be accessible include SD card missing, mounted on a computer,
         * not formatted etc.
         *
         * @return true if external storage can be accessed
         */
        fun canAccessExternalStorage(): Boolean {
            return Environment.MEDIA_MOUNTED == Environment
                    .getExternalStorageState()
        }

        /**
         * Lists all the files in a directory. It will not list files in
         * subdirectories.
         *
         * @param directory the directory to search in
         * @param filter    a [FilenameFilter] to filter the search by
         * @return a String array of filenames
         * @throws IOException if the directory doesn't exist or cannot be accessed
         */
        @Throws(IOException::class)
        fun listFilesInDirectory(directory: String,
                                 filter: FilenameFilter): List<FileDetails> {

            val f = File(directory)

            if (!f.exists() || !f.isDirectory) {
                return emptyList()
            }

            val files = ArrayList<FileDetails>()
            if (f.isDirectory) {

                val filesArray = f.list(filter)

                for (fileName in filesArray) {

                    val file = File(directory + File.separator + fileName)
                    val lastModified = file.lastModified()
                    files.add(FileDetails(fileName, directory, Date(lastModified)))
                }

                return files
            }

            // The directory doesn't exist
            Log.d("Directory " + directory
                    + " doesn't exist")
            throw FileNotFoundException()

        }

        /**
         * Returns a string containing the text from a raw resource
         *
         * @param ctx      a activityContext
         * @param resource the resource to read
         * @return a String containing the text contents of the resource
         * @throws IOException if the resource cannot be found or read
         */
        @Throws(IOException::class)
        fun readTextFromResource(ctx: Context, resource: Int): String {

            var inputStream: InputStream? = null
            try {
                inputStream = ctx.resources.openRawResource(resource)
                return CharStreams.toString(InputStreamReader(inputStream!!))
            } finally {
                if (inputStream != null) inputStream.close()

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
        @Throws(IOException::class)
        fun copyAsset(ctx: Context, assetFilename: String,
                      destination: String) {
            var `in`: InputStream? = null
            var out: FileOutputStream? = null

            try {
                val assetManager = ctx.assets

                `in` = assetManager.open(assetFilename)

                val newFileName = destination + "/" + assetFilename
                val newFile = File(newFileName)
                val dir = newFile.parentFile
                if (!dir.exists()) {
                    dir.mkdirs()
                }

                Log.v("Copying file [" + assetFilename
                        + "] to [" + newFileName + "]")

                out = FileOutputStream(newFile)
                ByteStreams.copy(`in`!!, out)


            } finally {
                if (`in` != null) `in`.close()
                if (out != null) out.close()
            }

        }
    }

}
