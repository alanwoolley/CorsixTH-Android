package uk.co.armedpineapple.cth.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import uk.co.armedpineapple.cth.CTHApplication
import uk.co.armedpineapple.cth.GameActivity
import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.defaultSharedPreferences
import uk.co.armedpineapple.cth.setup.SetupActivity


class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private val preferenceListener = { preferences: SharedPreferences, preference: String? ->
        onPreferenceUpdated(
            preferences,
            preference
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.settings, MainFragment())
                .commit()
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    private fun onPreferenceUpdated(preferences: SharedPreferences, preference: String?){
        Log.i("SettingsActivity", "Setting updated: $preference")

        GameActivity.singleton.updateGameConfig()
    }

    override fun onPause() {
        super.onPause()
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat, pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader, pref.fragment!!
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction().replace(R.id.settings, fragment)
            .addToBackStack(null).commit()
        title = pref.title
        return true
    }


    class MainFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.main_preferences, rootKey)

            val application = (requireContext().applicationContext as CTHApplication)
            val filesService = application.filesService

            findPreference<Preference>(getString(R.string.prefs_upgrade))?.isVisible =
                filesService.isDemoVersion(application.configuration)
        }
    }

    class AdvancedFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.advanced_preferences, rootKey)
        }
    }

    class AudioFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.audio_preferences, rootKey)
        }
    }

    class DisplayFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.display_preferences, rootKey)
        }
    }

    class GameplayFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.gameplay_preferences, rootKey)
        }
    }

    class InputFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.input_preferences, rootKey)
        }
    }

    class VideoFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.video_preferences, rootKey)
        }
    }

    class AnalyticsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.analytics_preferences, rootKey)

            val context = requireContext()
            val application = (context.applicationContext as CTHApplication)
            findPreference<Preference>(context.getString(R.string.prefs_policy))?.setOnPreferenceClickListener {
                application.reporting.openPrivacyPolicy()
                true
            }
        }
    }

    class UpgradeFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.upgrade_preferences, rootKey)

            findPreference<Preference>(getString(R.string.prefs_reinstall))?.setOnPreferenceClickListener {
                val currentActivity = requireActivity()
                val intent = Intent(currentActivity, SetupActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                currentActivity.finish()
                GameActivity.singleton.finish()

                true
            }
        }
    }
}