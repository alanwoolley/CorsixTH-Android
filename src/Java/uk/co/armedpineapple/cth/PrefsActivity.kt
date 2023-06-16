///*
// *   Copyright (C) 2012 Alan Woolley
// *
// *   See LICENSE.TXT for full license
// */
//package uk.co.armedpineapple.cth
//
//import android.annotation.SuppressLint
//import android.app.AlertDialog
//import android.app.ProgressDialog
//import android.content.Context
//import android.content.DialogInterface
//import android.content.DialogInterface.OnClickListener
//import android.content.Intent
//import android.content.SharedPreferences
//import android.net.Uri
//import android.os.Bundle
//import android.os.PowerManager
//import android.preference.CheckBoxPreference
//import android.preference.Preference.OnPreferenceChangeListener
//import android.preference.Preference.OnPreferenceClickListener
//import android.preference.PreferenceActivity
//import android.view.ViewGroup.LayoutParams
//import android.widget.Toast
//import uk.co.armedpineapple.cth.dialogs.DialogFactory
//import java.io.File
//import java.util.*
//
//class PrefsActivity : PreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
//
//    private val application: CTHApplication get() { return getApplication() as CTHApplication
//    }
//    private val preferences: SharedPreferences? get() { return application.preferences }
//
//    private val pm: PowerManager
//        get() {
//            return this.getSystemService(POWER_SERVICE) as PowerManager
//        }
//
//    /**
//     * Preferences that require the game to be restarted before they take effect *
//     */
//    private val requireRestart = arrayOf("language_pref", "debug_pref", "movies_pref", "intromovie_pref", "resolution_pref", "reswidth_pref", "resheight_pref", "music_pref", "autowage_pref", "usage_pref")
//
//    private var displayRestartMessage: Boolean = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//
//        super.onCreate(savedInstanceState)
//
//        // Make sure we're using the correct preferences
//        val prefMgr = preferenceManager
//
//        prefMgr.sharedPreferencesName = CTHApplication.PREFERENCES_KEY
//        prefMgr.sharedPreferencesMode = Context.MODE_PRIVATE
//
//        // Save the configuration to the preferences to make sure that everything is
//        // up-to-date
//
//        application.configuration!!.saveToPreferences()
//
//        addPreferencesFromResource(R.xml.prefs)
//
//        // Custom Preference Listeners
//
//        updateResolutionPrefsDisplay(preferences!!.getString("resolution_pref", "1"))
//
//        findPreference("music_pref").onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
//            if (!(newValue as Boolean)) {
//                true
//            } else onMusicEnabled()
//        }
//        findPreference("resolution_pref").onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
//            updateResolutionPrefsDisplay(newValue as String)
//            Log.d("Res mode: " + newValue)
//            true
//        }
//
//        findPreference("reswidth_pref").onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
//            val s = (newValue as String).trim { it <= ' ' }
//
//            try {
//                val i = Integer.parseInt(s)
//
//                if (i < 640) {
//                    return@OnPreferenceChangeListener false
//                }
//
//            } catch (e: NumberFormatException) {
//                return@OnPreferenceChangeListener false
//            }
//
//            true
//        }
//
//        findPreference("resheight_pref").onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
//            val s = (newValue as String).trim { it <= ' ' }
//            try {
//                val i = Integer.parseInt(s)
//
//                if (i < 480) {
//                    return@OnPreferenceChangeListener false
//                }
//
//            } catch (e: NumberFormatException) {
//                return@OnPreferenceChangeListener false
//            }
//
//            true
//        }
//
//        findPreference("setupwizard_pref").onPreferenceClickListener = OnPreferenceClickListener {
//            val builder = AlertDialog.Builder(
//                    this@PrefsActivity)
//            val alertListener = OnClickListener { dialog, which ->
//                val editor = preferences!!.edit()
//                editor.putBoolean("wizard_run", false)
//                editor.apply()
//            }
//            builder
//                    .setMessage(
//                            this@PrefsActivity.resources.getString(
//                                    R.string.setup_wizard_dialog)).setCancelable(false)
//                    .setNeutralButton(R.string.ok, alertListener)
//
//            val alert = builder.create()
//            alert.show()
//            true
//        }
//
//
//        if (CTHApplication.isTestingVersion) {
//            findPreference("alpha_pref").setTitle(R.string.preferences_alpha_leave)
//            findPreference("alpha_pref").setSummary(R.string.preferences_alpha_off)
//
//            findPreference("alpha_pref").onPreferenceClickListener = OnPreferenceClickListener {
//                openTestingPage()
//                true
//            }
//
//        } else {
//            findPreference("alpha_pref").setTitle(R.string.preferences_alpha_join)
//            findPreference("alpha_pref").setSummary(R.string.preferences_alpha_on)
//
//            findPreference("alpha_pref").onPreferenceClickListener = OnPreferenceClickListener {
//                DialogFactory
//                        .createTestingWarningDialog(this@PrefsActivity, OnClickListener { dialog, which -> openTestingPage() }).show()
//
//                true
//            }
//        }
//
//        findPreference("bug_pref").onPreferenceClickListener = OnPreferenceClickListener {
//            doBugReport()
//            true
//        }
//
//        for (s in requireRestart) {
//            findPreference(s).onPreferenceClickListener = OnPreferenceClickListener {
//                displayRestartMessage = true
//                true
//            }
//        }
//
//        window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        preferenceManager.sharedPreferences
//                .unregisterOnSharedPreferenceChangeListener(this)
//
//        Log.d("Refreshing configuration")
//
//        if (displayRestartMessage) {
//            Log.d("app requires restarting")
//            Toast.makeText(this, R.string.dialog_require_restart, Toast.LENGTH_LONG)
//                    .show()
//        }
//        if (Files.canAccessExternalStorage()) {
//            application!!.configuration!!.refresh()
//            if (SDLActivity.isActivityAvailable()) {
//                SDLActivity.cthUpdateConfiguration(
//                        application!!.configuration)
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        displayRestartMessage = false
//        Log.d("onResume()")
//        preferenceManager.sharedPreferences
//                .registerOnSharedPreferenceChangeListener(this)
//    }
//
//    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
//                                           key: String) {
//    }
//
//    private fun updateResolutionPrefsDisplay(newValue: String?) {
//        if (newValue == "3") {
//            findPreference("reswidth_pref").isEnabled = true
//            findPreference("resheight_pref").isEnabled = true
//        } else {
//            findPreference("reswidth_pref").isEnabled = false
//            findPreference("resheight_pref").isEnabled = false
//        }
//    }
//
//    private fun onMusicEnabled(): Boolean {
//
//        // Check if the original files has music, and show a dialog if not
//
//        if (!Files.hasMusicFiles(application!!.configuration!!.originalFilesPath)) {
//
//            val builder = AlertDialog.Builder(this)
//
//            builder.setMessage(getString(R.string.no_music_dialog))
//                    .setCancelable(true)
//                    .setNeutralButton(R.string.ok) { dialog, which -> dialog.dismiss() }
//
//            val alert = builder.create()
//            alert.show()
//            return false
//        }
//
//        val timidityConfig = File(Files.extStoragePath + "timidity"
//                + File.separator + "timidity.cfg")
//
//        if (!(timidityConfig.isFile && timidityConfig.canRead())) {
//
//            val builder = AlertDialog.Builder(this)
//            val alertListener = OnClickListener { dialog, which ->
//                if (which == DialogInterface.BUTTON_POSITIVE) {
//                    doTimidityDownload()
//                }
//            }
//
//            builder.setMessage(getString(R.string.music_download_dialog))
//                    .setCancelable(true)
//                    .setNegativeButton(R.string.cancel, alertListener)
//                    .setPositiveButton(R.string.ok, alertListener)
//
//            val alert = builder.create()
//            alert.show()
//
//        } else {
//            // Music files are installed and ready
//            return true
//        }
//
//        return false
//
//    }
//
//    private fun doTimidityDownload() {
//        // Check for external storage
//        if (!Files.canAccessExternalStorage()) {
//            // No external storage
//            val toast = Toast.makeText(this, R.string.no_external_storage,
//                    Toast.LENGTH_LONG)
//            toast.show()
//            return
//        }
//
//        // Check for network connection
//        if (!Network.hasNetworkConnection(this)) {
//            // Connection error
//            val connectionDialog = DialogFactory
//                    .createNoNetworkConnectionDialog(this)
//            connectionDialog.show()
//            return
//
//        }
//
//        val extDir = getExternalFilesDir(null)
//        val dialog = ProgressDialog(this)
//
//        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
//        dialog.setMessage(getString(R.string.downloading_timidity))
//        dialog.isIndeterminate = false
//        dialog.setCancelable(false)
//
//        val uzt = object : Files.UnzipTask(Files.extStoragePath
//                + File.separator + "timidity" + File.separator, pm) {
//
//            override fun onPostExecute(result: AsyncTaskResult<String>) {
//                super.onPostExecute(result)
//                dialog.hide()
//
//                if (result.result != null) {
//                    Log.d("Downloaded and extracted Timidity successfully")
//
//                    val editor = preferences!!.edit()
//                    editor.putBoolean("music_pref", true)
//                    editor.commit()
//
//                    (findPreference("music_pref") as CheckBoxPreference).isChecked = true
//
//                } else if (result.error != null) {
//                    val e = result.error
//                    Reporting
//                            .reportWithToast(this@PrefsActivity, getString(R.string.download_timidity_error), e)
//                }
//            }
//
//            @SuppressLint("NewApi")
//            override fun onPreExecute() {
//                super.onPreExecute()
//                dialog.setMessage(getString(R.string.extracting_timidity))
//
//                dialog.setProgressNumberFormat(null)
//
//
//            }
//
//            override fun onProgressUpdate(vararg values: Int?) {
//                super.onProgressUpdate(*values)
//                dialog.progress = values[0] as Int
//                dialog.max = values[1] as Int
//            }
//
//        }
//
//        val dft = object : Files.DownloadFileTask(
//                extDir!!.canonicalPath, pm) {
//
//            override fun onPostExecute(result: AsyncTaskResult<File>) {
//                super.onPostExecute(result)
//
//                val errorToast = Toast.makeText(this@PrefsActivity,
//                        R.string.download_timidity_error, Toast.LENGTH_LONG)
//
//                if (result.error != null) {
//                    dialog.hide()
//                    errorToast.show()
//                } else {
//                    uzt.execute(result.result)
//                }
//            }
//
//            override fun onPreExecute() {
//                super.onPreExecute()
//                dialog.show()
//                    dialog
//                            .setProgressNumberFormat(getString(R.string.download_progress_dialog_text))
//            }
//
//            override fun onProgressUpdate(vararg values: Int?) {
//                super.onProgressUpdate(*values)
//                dialog.progress = values[0]!! / 1000000
//                dialog.max = values[1]!! / 1000000
//            }
//
//        }
//
//        val mobileDialog = DialogFactory.createMobileNetworkWarningDialog(this,
//                OnClickListener { dialog, which -> dft.execute(getString(R.string.timidity_url)) })
//
//        if (Network.isMobileNetwork(this)) {
//            mobileDialog.show()
//        } else {
//            dft.execute(getString(R.string.timidity_url))
//        }
//
//    }
//
//    private fun openTestingPage() {
//        val url = resources.getString(R.string.testing_url)
//        val i = Intent(Intent.ACTION_VIEW)
//        i.data = Uri.parse(url)
//        startActivity(i)
//    }
//
//    private fun doBugReport() {
//
//        val messageIntent = Intent(Intent.ACTION_SEND_MULTIPLE, Uri.fromParts("mailto", BUG_FEEDBACK_DEST, null))
//
//        val aEmailList = arrayOf(BUG_FEEDBACK_DEST)
//        messageIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList)
//
//        messageIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, BUG_FEEDBACK_SUBJECT)
//        messageIntent.type = "message/rfc822"
//
//        val cthLog = File(application.configuration!!.cthPath + "/cthlog.txt")
//        val cthErrLog = File(application.configuration!!.cthPath + "/ctherrlog.txt")
//
//        val uris = ArrayList<Uri>()
//
//        if (cthLog.canRead() && cthLog.length() > 0) {
//            uris.add(Uri.fromFile(cthLog))
//        }
//        if (cthErrLog.canRead() && cthErrLog.length() > 0) {
//            uris.add(Uri.fromFile(cthErrLog))
//        }
//
//        if (uris.size > 0) {
//            messageIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
//        }
//
//        startActivity(Intent.createChooser(messageIntent, resources.getString(R.string.send_feedback_email_chooser)))
//    }
//
//    companion object {
//
//        private val Log = Reporting.getLogger("Settings")
//
//        private const val BUG_FEEDBACK_DEST = "alan@armedpineapple.co.uk"
//        private const val BUG_FEEDBACK_SUBJECT = "CorsixTH for Android Bug/Feedback"
//    }
//
//}
