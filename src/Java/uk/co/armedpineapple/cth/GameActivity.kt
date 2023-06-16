package uk.co.armedpineapple.cth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.Keep
import com.google.common.io.ByteStreams
import com.google.common.io.Closeables
import org.jetbrains.anko.AnkoLogger
import org.libsdl.app.SDLActivity
import uk.co.armedpineapple.cth.settings.SettingsActivity
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

class GameActivity : SDLActivity(), AnkoLogger {

    private val ENGINE_ZIP_FILE = "game.zip"

    @Keep
    private external fun startLogger();

    private val configuration: GameConfiguration
        get() = (application as CTHApplication).configuration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        singleton = this

        startLogger();

        var assetIn = application.assets.open(ENGINE_ZIP_FILE)
        var assetOut = File(application.cacheDir, ENGINE_ZIP_FILE)
        assetIn.copyTo(assetOut.outputStream())

        var zipFile = ZipFile(assetOut)
        var entries = zipFile.entries()
        var target = configuration.cthFiles
        while (entries.hasMoreElements()) {
            val ze = entries.nextElement()

            val f = File(target, ze.name)
            if (!f.parentFile.exists()) {
                f.parentFile.mkdirs()
            }

            if (ze.isDirectory) {

                if (!f.isDirectory) {
                    f.mkdirs()
                }
            } else {

                var zin: InputStream? = null
                var fout: FileOutputStream?
                try {
                    zin = zipFile.getInputStream(ze)
                    fout = FileOutputStream(f)
                    ByteStreams.copy(zin!!, fout)
                } finally {
                    Closeables.closeQuietly(zin)
                }

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
            "--interpreter=" + File(
                application.noBackupFilesDir, "cth/CorsixTH.lua"
            ).absolutePath, "--config-file=" + configuration.gameConfigFile.absolutePath
        )
    }

    companion object {
        var singleton: GameActivity? = null

        @Keep
        @JvmStatic
        fun showSettings() {
            Log.i("GameActivity", "Showing settings")

            var intent = Intent(singleton, SettingsActivity::class.java)
            singleton?.startActivity(intent);
        }
    }

}