package uk.co.armedpineapple.cth.setup

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.storage.StorageManager
import androidx.lifecycle.AndroidViewModel
import com.lazygeniouz.dfc.file.DocumentFileCompat
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import uk.co.armedpineapple.cth.CTHApplication
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SetupViewModel(application: Application) : AndroidViewModel(application), AnkoLogger {

    private val storageManager: StorageManager =
        application.getSystemService(StorageManager::class.java)

    fun onGameSourceAccessGranted(uri: Uri) {
        val application = getApplication<CTHApplication>()
        val filesRoot = application.configuration.thFiles
        if (filesRoot.exists()) filesRoot.deleteRecursively()

        val documentRoot = DocumentFileCompat.fromTreeUri(application, uri)
        documentRoot?.let { copyDirectoryTree(application, it, filesRoot) }
    }

    private fun copyDirectoryTree(
        context: Context, root: DocumentFileCompat, destinationDirectory: File
    ) {
        // Create the destination directory if it doesn't exist
        destinationDirectory.mkdirs()

        if (root.isDirectory()) {
            root.listFiles().forEach { file ->

                if (file.isDirectory()) {
                    val newDestination = File(destinationDirectory, file.name)
                    // If child is a directory, recursively copy its contents to the new destination
                    copyDirectoryTree(context, file, newDestination)
                } else {
                    // If child is a file, directly copy it to the destination directory
                    copyFile(context, file, destinationDirectory)
                }
            }
        } else {
            // If the root itself is a file, copy it to the destination directory
            copyFile(context, root, destinationDirectory)
        }
    }

    private fun copyFile(
        context: Context, file: DocumentFileCompat, destinationDirectory: File
    ) {
        val sourceUri = file.uri
        val inputStream = context.contentResolver.openInputStream(sourceUri)
        inputStream?.use { input ->
            val outputFile = File(destinationDirectory, file.name)
            FileOutputStream(outputFile).use { output ->
                val inputSize = input.available()

                debug { "Allocating $inputSize for ${output.fd}" }
                if (inputSize > 0 && storageManager.isAllocationSupported(output.fd)) {
                    try {
                        storageManager.allocateBytes(output.fd, inputSize.toLong())
                    } catch (e: IOException) {
                        debug { "Unable to allocate storage. We'll copy anyway but it may fail." }
                    }
                }

                input.copyTo(output)
                info { "Copied $sourceUri to $outputFile" }
            }
        }
    }

}