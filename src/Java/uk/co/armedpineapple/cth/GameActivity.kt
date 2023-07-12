package uk.co.armedpineapple.cth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.Keep
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.google.common.io.ByteStreams
import com.google.common.io.Closeables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoLogger
import org.libsdl.app.SDLActivity
import uk.co.armedpineapple.cth.files.FilesService
import uk.co.armedpineapple.cth.settings.SettingsActivity
import uk.co.armedpineapple.cth.setup.SetupActivity
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

class GameActivity : SDLActivity(), AnkoLogger {

    @Keep
    private external fun startLogger();

    private val configuration: GameConfiguration
        get() = (application as CTHApplication).configuration

    override fun onCreate(savedInstanceState: Bundle?) {
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
        if (!filesService.hasGameFiles(configuration)) {
            Toast.makeText(this, "Upgrading", Toast.LENGTH_SHORT).show()
            installJob = CoroutineScope(Dispatchers.IO).launch {
                filesService.installGameFiles(configuration)
            }
        }

        // Upgrading the game files will nuke the installation directory, so make sure that
        // the latest configuration exists in there.
        configuration.persist()

        super.onCreate(savedInstanceState)
        startLogger()
        singleton = this

        // Make sure the game file installation installation has completed before moving on.
        if (installJob != null) {
            runBlocking {
                installJob.join()
            }
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
        var singleton: GameActivity? = null

        @Keep
        @JvmStatic
        fun showSettings() {
            Log.i("GameActivity", "Showing settings")

            val intent = Intent(singleton, SettingsActivity::class.java)
            singleton?.startActivity(intent);
        }
    }

}