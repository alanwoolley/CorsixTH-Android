package uk.co.armedpineapple.cth

import androidx.preference.PreferenceManager
import androidx.room.Room
import org.jetbrains.anko.defaultSharedPreferences
import uk.co.armedpineapple.cth.files.FilesService
import uk.co.armedpineapple.cth.files.persistence.GameDatabase
import uk.co.armedpineapple.cth.localisation.LanguageService

class CTHApplication : android.app.Application() {

    lateinit var configuration: GameConfiguration
    val database: GameDatabase by lazy {
        Room.databaseBuilder(
            this, GameDatabase::class.java, "database"
        ).build()
    }

    val filesService: FilesService by lazy {
        FilesService(this)
    }

    val isFirstLaunchForVersion : Boolean by lazy {
        val preferences = defaultSharedPreferences

        val lastLaunchVersion = preferences.getInt("last_launch_version", -1)
        val currentVersion = BuildConfig.VERSION_CODE
        preferences.edit().putInt("last_launch_version", currentVersion).apply()

        currentVersion > lastLaunchVersion
    }

    override fun onCreate() {
        super.onCreate()

        initConfiguration()
    }

    private fun initConfiguration() {
        val preferences = defaultSharedPreferences

        val service = LanguageService(this)

        if (!preferences.getBoolean(
                PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false
            )
        ) {
            for (preferencesId in arrayOf(
                R.xml.advanced_preferences,
                R.xml.audio_preferences,
                R.xml.display_preferences,
                R.xml.gameplay_preferences,
                R.xml.input_preferences,
                R.xml.main_preferences,
                R.xml.video_preferences
            )) {
                // Because we use multiple preferences files, we need to set readAgain, otherwise
                // only the first preferences file will have default values set.
                PreferenceManager.setDefaultValues(this, preferencesId, true);
            }
            preferences.edit().putString(
                this.getString(R.string.prefs_language), service.getCthLanguageFromAppConfig()
            ).apply()
        }
        configuration = GameConfiguration(this, preferences)
    }
}
