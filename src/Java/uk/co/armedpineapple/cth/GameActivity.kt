package uk.co.armedpineapple.cth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.Keep
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.libsdl.app.SDLActivity
import uk.co.armedpineapple.cth.files.FilesService
import uk.co.armedpineapple.cth.files.SaveGameContract
import uk.co.armedpineapple.cth.persistence.saves.SaveData
import uk.co.armedpineapple.cth.settings.SettingsActivity
import uk.co.armedpineapple.cth.setup.SetupActivity
import uk.co.armedpineapple.cth.stats.StatisticsService
import java.io.File

class GameActivity : SDLActivity(), Loggable {
    @Keep
    private external fun startLogger()

    @Keep
    private external fun nativeSave(saveName: String)

    @Keep
    private external fun nativeLoad(saveName: String)

    @Keep
    private external fun nativeUpdateConfig(config: GameConfiguration)

    private val configuration: GameConfiguration
        get() = (application as CTHApplication).configuration


    private val filesService: FilesService get() = (application as CTHApplication).filesService

    @get:Keep
    val gameEventHandler by lazy {
        GameEventHandler(StatisticsService((application as CTHApplication).statsDatabase))
    }

    private val saveDao get() = (application as CTHApplication).gameDatabase.saveDao()

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
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            super.onCreate(savedInstanceState)
            return;
        }

        // Install the latest CTH game files in the background.
        var installJob: Job? = null
        if ((application as CTHApplication).isFirstLaunchForVersion || !filesService.hasGameFiles(
                configuration
            ) || BuildConfig.ALWAYS_UPGRADE
        ) {
            Toast.makeText(this, "Upgrading", Toast.LENGTH_SHORT).show()

            val target = configuration.cthFiles
            if (target.exists()) target.deleteRecursively()

            installJob = CoroutineScope(Dispatchers.IO).launch {
                filesService.installGameFiles(configuration)
            }
        }

        // Install the music library in the background
        var musicInstallJob: Job? = null
        if (!filesService.hasMusicLibrary(configuration)) {
            val target = configuration.musicLib
            if (target.exists()) target.deleteRecursively()

            musicInstallJob = CoroutineScope(Dispatchers.IO).launch {
                filesService.installMusicLibrary(configuration)
            }
        }

        var fontInstallJob: Job? = null
        if (!configuration.unicodeFont.exists()) {
            fontInstallJob = CoroutineScope(Dispatchers.IO).launch {
                configuration.unicodeFont.parentFile?.mkdirs()
                filesService.copyAsset(
                    "DroidSansFallbackFull.ttf", singleton, configuration.unicodeFont
                )
            }
        }

        // Upgrading the game files will nuke the installation directory, so make sure that
        // the latest configuration exists in there.
        configuration.persist()

        super.onCreate(savedInstanceState)
        nativeSetenv("TIMIDITY_CFG", File(configuration.musicLib, "timidity.cfg").absolutePath);

        startLogger()

        // Make sure the game file installation installation has completed before moving on.
        if (installJob != null || musicInstallJob != null || fontInstallJob != null) {
            runBlocking {
                installJob?.join()
                musicInstallJob?.join()
                fontInstallJob?.join()
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

    fun updateGameConfig() {
        Log.i("GameActivity", "Updating game configuration")

        nativeUpdateConfig(configuration)

        Log.i("GameActivity", "Updated game config")
    }

    companion object {
        @JvmStatic
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
                filePath = fileName.toUtf8String(),
                rep = rep,
                money = money,
                level = level.toUtf8String(),
                screenshot = screenshot.toUtf8String()
            )
        }

        @Keep
        @JvmStatic
        fun onGameError(handler: ByteArray?, stack: ByteArray?) {
            Firebase.crashlytics.recordException(
                if (handler != null) {
                    NativeLuaHandlerException(handler, stack)
                } else {
                    NativeLuaException(stack, "Game Error")
                }
            )
        }
    }
}