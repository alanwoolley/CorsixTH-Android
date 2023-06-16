/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.jetbrains.anko.defaultSharedPreferences
import java.util.*
import java.util.prefs.Preferences

class CTHApplication : android.app.Application() {
    lateinit var configuration: GameConfiguration

    override fun onCreate() {
        super.onCreate()

        val preferences = defaultSharedPreferences

        if (!defaultSharedPreferences.getBoolean(
                    PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES,
                    false
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
        }
        configuration = GameConfiguration(this, preferences)
    }
}
