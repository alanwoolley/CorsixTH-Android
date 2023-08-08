package uk.co.armedpineapple.cth.files

import android.content.Context
import android.os.storage.StorageManager
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn
import uk.co.armedpineapple.cth.GameConfiguration
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipFile

/**
 * Files service
 *
 * @constructor Create empty Files service
 */
class FilesService(val ctx: Context) : AnkoLogger {

    private val storageManager: StorageManager = ctx.getSystemService(StorageManager::class.java)

    data class DeterminateFileOperationProgress(val progress: Long, val max: Long)
    data class EstimatedFileOperationProgress(val progress: Float)

    /**
     * Gets all the save game files.
     *
     * @param config The configuration
     * @return An array of save game files
     */
    fun getSaveGameFiles(config: GameConfiguration): Array<out File> {
        return getSaveDirectoryContents(config.saveFiles)
    }

    /**
     * Gets all the autosave game files.
     *
     * @param config The configuration
     * @return An array of save game files
     */
    fun getAutoSaveGameFiles(config: GameConfiguration): Array<out File> {
        return getSaveDirectoryContents(config.autosaveFiles)
    }

    /**
     * Checks whether the game files exist in the location given in the config.
     *
     * @param config The configuration
     * @return whether the game files are installed
     */
    fun hasGameFiles(config: GameConfiguration): Boolean {
        // There's little  point searching for all the files here. If one is missing, then they
        // probably are all missing. We could do something like checking the integrity with a
        // checksum, but it doesn't seem worth the extra overhead.
        //
        // The game script is the important one to get us up and running anyway. We can detect
        // in-game whether any of the additional files are missing or corrupt.
        return config.cthLaunchScript.exists()
    }

    /**
     * Checks whether the original TH files are installed in the location given in the config.
     *
     * This doesn't do a thorough integrity check and is only indicative of missing files.
     *
     * @param config The configuration
     * @return whether the original TH files are installed
     */
    fun hasOriginalFiles(config: GameConfiguration): Boolean {
        // We should be a bit more thorough with checking for the original files, as we have less
        // opportunity to detect if there's an issue here.
        val expectedFiles = arrayOf(
            "QDATA/AREA01V.DAT",
            "DATA/LANG-0.DAT",
        )

        for (expectedFile in expectedFiles) {
            if (!File(config.thFiles, expectedFile).exists()) {
                warn { "Wanted TH file $expectedFile but was not found" }
                return false
            }
        }

        return true
    }

    /**
     * Installs the CorsixTH game files.
     *
     * @param config The configuration that determines the installation location.
     * @param ctx The context.
     * @param progress An optional channel to send progress updates to.
     */
    suspend fun installGameFiles(
        config: GameConfiguration, progress: SendChannel<DeterminateFileOperationProgress>? = null
    ) {
        val assetOut = File(ctx.cacheDir, ENGINE_ZIP_FILE)
        copyAsset(ENGINE_ZIP_FILE, ctx, assetOut)

        try {
            val target = config.cthFiles
            extractZipFile(assetOut, target, progress)
        } finally {
            assetOut.delete()
        }
    }

    /**
     * Removes the original game files entirely.
     *
     * @param config The configuration the determines the installation location.
     */
    fun nukeOriginalFiles(config: GameConfiguration) {
        val target = config.thFiles
        if (target.exists()) target.deleteRecursively()
    }

    /**
     * Installs the original game files.
     *
     * @param source The source document tree
     * @param config The configuration that determines the installation location.
     * @param progress An optional channel to send progress updates to.
     */
    suspend fun installOriginalFiles(
        source: DocumentFileCompat,
        config: GameConfiguration,
        progress: SendChannel<EstimatedFileOperationProgress>? = null
    ) {
        nukeOriginalFiles(config)
        copyDirectoryTree(source, config.thFiles, progress)
    }

    /**
     * Gets a File corresponding to the given save name.
     *
     * @param saveName The save name.
     * @param config The configuration that determines the save file locations.
     * @return A file for the given save name.
     */
    fun getSaveFile(saveName: String, config: GameConfiguration): File {
        return if (saveName.startsWith("Autosave")) {
            File(config.autosaveFiles, saveName)
        } else {
            File(config.saveFiles, saveName)
        }
    }

    private suspend fun copyDirectoryTree(
        root: DocumentFileCompat,
        destinationDirectory: File,
        progress: SendChannel<EstimatedFileOperationProgress>? = null
    ) {
        // Create the destination directory if it doesn't exist
        destinationDirectory.mkdirs()

        if (root.isDirectory()) {
            val contents = root.listFiles()
            val totalContents = contents.size
            var currentFile = 0
            contents.forEach { file ->
                if (file.isDirectory()) {
                    val newDestination = File(destinationDirectory, file.name)
                    val childProgress =
                        if (progress == null) null else Channel<EstimatedFileOperationProgress>(
                            Channel.CONFLATED
                        ) { p ->
                            // Report the progress back up the chain.
                            progress.trySend(EstimatedFileOperationProgress((currentFile + p.progress) / totalContents.toFloat()))
                        }

                    // If child is a directory, recursively copy its contents to the new destination
                    copyDirectoryTree(file, newDestination, childProgress)
                } else {
                    // If child is a file, directly copy it to the destination directory
                    copyFile(file, destinationDirectory)
                }
                currentFile++
                progress?.trySend(EstimatedFileOperationProgress(currentFile.toFloat() / totalContents.toFloat()))
            }
        } else {
            // If the root itself is a file, copy it to the destination directory
            copyFile(root, destinationDirectory)
            progress?.trySend(EstimatedFileOperationProgress(1f))
        }
    }

    private suspend fun copyFile(
        file: DocumentFileCompat,
        destinationDirectory: File,
    ) {
        val sourceUri = file.uri
        ctx.contentResolver.openInputStream(sourceUri)?.use { input ->
            withContext(Dispatchers.IO) {
                val outputFile = File(destinationDirectory, file.name)
                copyStreamToFile(outputFile, input.available().toLong(), input)
            }
        }
    }

    suspend fun extractZipFile(
        source: File, target: File, progress: SendChannel<DeterminateFileOperationProgress>?
    ) {
        withContext(Dispatchers.IO) {
            ZipFile(source).use { zipFile ->
                val zipEntries = zipFile.entries()

                val entriesCount = zipFile.size().toLong()
                var currentEntryIndex = 0L
                while (zipEntries.hasMoreElements()) {
                    val zipEntry = zipEntries.nextElement()
                    currentEntryIndex++
                    val outputFile = File(target, zipEntry.name)
                    outputFile.parentFile?.mkdirs()

                    if (zipEntry.isDirectory) {

                        if (!outputFile.isDirectory) {
                            outputFile.mkdirs()
                        }
                    } else {
                        zipFile.getInputStream(zipEntry).use { zin ->
                            copyStreamToFile(outputFile, zipEntry.size, zin)
                        }
                    }
                    progress?.send(
                        DeterminateFileOperationProgress(
                            currentEntryIndex, entriesCount
                        )
                    )
                }
            }
        }
    }

    fun copyStreamToFile(
        outputFile: File,
        size: Long,
        input: InputStream,
        progress: SendChannel<DeterminateFileOperationProgress>? = null
    ) {
        FileOutputStream(outputFile).use { fout ->
            allocateStorage(size, fout.fd, storageManager)
            copyStreamTo(input, fout, size, progress)
        }
    }

    private fun copyStreamTo(
        input: InputStream,
        out: OutputStream,
        length: Long? = null,
        progress: SendChannel<DeterminateFileOperationProgress>? = null,
        bufferSize: Int = DEFAULT_BUFFER_SIZE
    ): Long {
        var bytesCopied: Long = 0
        val buffer = ByteArray(bufferSize)
        var bytes = input.read(buffer)
        while (bytes >= 0) {
            out.write(buffer, 0, bytes)
            bytesCopied += bytes
            length?.let { l ->
                progress?.trySend(DeterminateFileOperationProgress(bytesCopied, l))
            }
            bytes = input.read(buffer)
        }
        return bytesCopied
    }

    private fun copyAsset(
        asset: String, ctx: Context, target: File
    ) {
        ctx.assets.open(asset).use { assetInputStream ->
            target.deleteOnExit()
            target.outputStream().use { assetOutStream ->
                allocateStorage(
                    bytes = assetInputStream.available().toLong(),
                    fd = assetOutStream.fd,
                    storageMgr = storageManager
                )
                assetInputStream.copyTo(assetOutStream)
            }
        }
    }

    private fun allocateStorage(bytes: Long, fd: FileDescriptor, storageMgr: StorageManager) {
        if (storageMgr.isAllocationSupported(fd) && bytes > 0) {
            try {
                storageMgr.allocateBytes(fd, bytes)
            } catch (e: IOException) {
                warn { "Tried to allocate $bytes but failed." }
            }
        }
    }

    private fun getSaveDirectoryContents(root: File): Array<out File> {
        if (root.exists()) {
            return root.listFiles { f -> f.isFile && f.extension.lowercase() == SAVE_GAME_EXTENSION }
                ?: arrayOf()
        }
        return arrayOf()
    }

    companion object {
        private const val ENGINE_ZIP_FILE = "game.zip"
        const val SAVE_GAME_EXTENSION = "sav"
        const val SAVE_GAME_FILE_SUFFIX = ".$SAVE_GAME_EXTENSION"
    }
}
