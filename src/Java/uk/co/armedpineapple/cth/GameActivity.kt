package uk.co.armedpineapple.cth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.AnkoLogger
import org.libsdl.app.SDLActivity
import uk.co.armedpineapple.cth.files.FilesService
import uk.co.armedpineapple.cth.files.SaveGameContract
import uk.co.armedpineapple.cth.files.persistence.SaveData
import uk.co.armedpineapple.cth.settings.SettingsActivity
import uk.co.armedpineapple.cth.setup.SetupActivity
import java.io.File
import java.nio.charset.Charset

class GameActivity : SDLActivity(), AnkoLogger {
    @Keep
    private external fun startLogger()

    @Keep
    private external fun nativeSave(saveName: String)

    @Keep
    private external fun nativeLoad(saveName: String)


    private val configuration: GameConfiguration
        get() = (application as CTHApplication).configuration

    private val filesService: FilesService get() = (application as CTHApplication).filesService

    private val saveDao get() = (application as CTHApplication).database.saveDao()

    private val loadGameLauncher: ActivityResultLauncher<Boolean> =
        registerForActivityResult(SaveGameContract()) { o -> doLoad(o) }
    private val saveGameLauncher: ActivityResultLauncher<Boolean> =
        registerForActivityResult(SaveGameContract()) { o -> doSave(o) }

    override fun onCreate(savedInstanceState: Bundle?) {
        singleton = this
        val filesService = FilesService(this)

        // Check whether the setup installation has run and the original TH files are available.
        // If not, redirect over to the setup activity.
        if (!filesService.hasOriginalFiles(configuration)) {
            finishAndRemoveTask()
            val intent = Intent(this, SetupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
            startActivity(intent)

            super.onCreate(savedInstanceState)
            return;
        }

        // Install the latest CTH game files in the background.
        var installJob: Job? = null
        val alwaysUpgrade = true
        if (alwaysUpgrade || !filesService.hasGameFiles(configuration)) {
            Toast.makeText(this, "Upgrading", Toast.LENGTH_SHORT).show()

            val target = configuration.cthFiles
            if (target.exists()) target.deleteRecursively()

            installJob = CoroutineScope(Dispatchers.IO).launch {
                filesService.installGameFiles(configuration)
            }
        }

        // Upgrading the game files will nuke the installation directory, so make sure that
        // the latest configuration exists in there.
        configuration.persist()

        super.onCreate(savedInstanceState)
        startLogger()

        // Make sure the game file installation installation has completed before moving on.
        if (installJob != null) {
            runBlocking {
                installJob.join()
            }
        }
    }

    private fun launchLoadGamePicker() {
        loadGameLauncher.launch(true)
    }

    private fun launchSaveGamePicker() {
        saveGameLauncher.launch(false)
    }

    private fun doLoad(saveName: String?) {
        saveName?.let { save ->
            val savePath = filesService.getSaveFile(save, configuration)

            if (savePath.exists()) {
                nativeLoad(savePath.absolutePath)
            }
        }

    }

    private fun doSave(saveName: String?) {
        if (saveName != null) {
            val savePath = File(configuration.saveFiles, saveName)
            nativeSave(savePath.absolutePath)
        }
    }

    private fun updateSaveGameDatabase(
        filePath: String, rep: Int, money: Long, level: String, screenshot: String
    ) {
        val fileName = File(filePath).name
        CoroutineScope(Dispatchers.IO).launch {
            saveDao.upsert(
                SaveData(
                    saveName = fileName,
                    screenshotPath = screenshot,
                    rep = rep,
                    money = money,
                    levelName = level
                )
            )
        }
    }

    @Override
    override fun getMainSharedObject(): String {
        return getContext().applicationInfo.nativeLibraryDir + "/" + "libappmain.so"
    }

    @Override
    override fun getLibraries(): Array<String>? {
        return arrayOf(
            "SDL2", "SDL2_mixer", "appmain"
        )
    }

    @Override
    override fun getMainFunction(): String {
        return "SDL_main"
    }

    override fun getArguments(): Array<String> {
        return arrayOf(
            "--interpreter=${configuration.cthLaunchScript.absolutePath}",
            "--config-file=${configuration.gameConfigFile.absolutePath}"
        )
    }

    companion object {
        lateinit var singleton: GameActivity

        @Keep
        @JvmStatic
        fun showSettings() {
            Log.i("GameActivity", "Showing settings")

            val intent = Intent(singleton, SettingsActivity::class.java)
            singleton.startActivity(intent);
        }

        @Keep
        @JvmStatic
        fun showLoad() {
            singleton.launchLoadGamePicker()
        }

        @Keep
        @JvmStatic
        fun showSave() {
            singleton.launchSaveGamePicker()
        }

        @Keep
        @JvmStatic
        fun onSaveGameChanged(
            fileName: ByteArray, rep: Int, money: Long, level: ByteArray, screenshot: ByteArray
        ) {
            singleton.updateSaveGameDatabase(
                filePath = String(fileName, Charset.forName("UTF-8")),
                rep = rep,
                money = money,
                level = String(level, Charset.forName("UTF-8")),
                screenshot = String(screenshot, Charset.forName("UTF-8"))
            )
        }
    }
}