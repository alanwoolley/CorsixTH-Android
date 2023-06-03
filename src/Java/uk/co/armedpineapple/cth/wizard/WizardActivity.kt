/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.wizard

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.ViewFlipper
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import uk.co.armedpineapple.cth.*
import uk.co.armedpineapple.cth.Configuration.ConfigurationException
import uk.co.armedpineapple.cth.Files.StorageUnavailableException
import uk.co.armedpineapple.cth.databinding.WizardBinding
import uk.co.armedpineapple.cth.dialogs.DialogFactory

class WizardActivity : CTHActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: WizardBinding

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        bootstrap()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Log.d("User permenantly denied us permission!")
            AppSettingsDialog.Builder(this).build().show()
        } else {
            Log.d("User just denied us permission!")
            bootstrap()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            bootstrap()
        }
    }

    @AfterPermissionGranted(REQUIRED_PERMISSIONS)
    fun bootstrap() {
        val perms = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            hasPermissions()
        } else {
            Log.d("Do not have permissions - requesting")
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "This application requires access to storage to run.",
                    REQUIRED_PERMISSIONS, *perms)
        }
    }

    private fun hasPermissions() {
        if (!Files.canAccessExternalStorage()) {
            Log.e("Can't get storage.")
            // Show dialog and end
            DialogFactory.createExternalStorageWarningDialog(this, true).show()
            return

        }

        val preferences = app.preferences
        val alreadyRun = preferences!!.getBoolean("wizard_run", false)
        if (alreadyRun) {
            if (Files
                            .hasDataFiles(preferences.getString("originalfiles_pref", "")!!)) {
                Log.d("Wizard isn't going to run.")
                finish()
                startActivity(Intent(this, SDLActivity::class.java))
                return
            } else {
                Log.w("Configured but cannot find data files")
            }
        }

        Log.d("Wizard is going to run.")
        binding = WizardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setContentView(R.layout.wizard)

        if (app.configuration == null) {
            try {
                app.configuration = Configuration.loadFromPreferences(this, preferences)
            } catch (e: StorageUnavailableException) {
                Log.e("Can't get storage.")

                // Show dialog and end
                DialogFactory
                        .createExternalStorageWarningDialog(this, true).show()
                return
            }

        }

        // Add all the wizard views

        val inflater = layoutInflater
        loadAndAdd(inflater, binding.flipper,
                inflater.inflate(R.layout.wizard_welcome, binding.flipper, false) as WizardView)
        loadAndAdd(inflater, binding.flipper,
                inflater.inflate(R.layout.wizard_language, binding.flipper, false) as LanguageWizard)
        loadAndAdd(inflater, binding.flipper, inflater.inflate(
                R.layout.wizard_originalfiles, binding.flipper, false) as OriginalFilesWizard)

        // Setup Buttons
        binding.leftButton.visibility = View.GONE
        val buttonClickListener = WizardButtonClickListener()

        binding.leftButton.setOnClickListener(buttonClickListener)
        binding.rightButton.setOnClickListener(buttonClickListener)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bootstrap()
    }

    private fun loadAndAdd(inflater: LayoutInflater, flipper: ViewFlipper?,
                           wv: WizardView): WizardView {

        flipper!!.addView(wv)
        wv.loadConfiguration(app.configuration!!)
        return wv
    }

    private inner class WizardButtonClickListener : OnClickListener {

        override fun onClick(v: View) {
            if (v == binding.leftButton) {
                binding.flipper.setInAnimation(this@WizardActivity,
                        R.animator.wizard_anim_slideinright)
                binding.flipper.setOutAnimation(this@WizardActivity,
                        R.animator.wizard_anim_slideoutright)
                binding.flipper!!.showPrevious()
            } else if (v == binding.rightButton) {
                try {
                    (binding.flipper.currentView as WizardView)
                            .saveConfiguration(app.configuration!!)

                    if (!hasNext(binding.flipper)) {
                        app.configuration!!.saveToPreferences()

                        finish()
                        this@WizardActivity.startActivity(Intent(this@WizardActivity,
                                SDLActivity::class.java))
                    } else {
                        binding.flipper.setInAnimation(this@WizardActivity,
                                R.animator.wizard_anim_slideinleft)
                        binding.flipper.setOutAnimation(this@WizardActivity,
                                R.animator.wizard_anim_slideoutleft)
                        binding.flipper.showNext()
                    }

                } catch (e: ConfigurationException) {
                    // Couldn't save the configuration. Don't change the view.
                    Log.w("Couldn't save configuration")
                    return
                }

            }

            if (hasNext(binding.flipper)) {
                binding.rightButton.setText(R.string.nextButton)
            } else {
                binding.rightButton.setText(R.string.play_button)
            }

            if (hasPrevious(binding.flipper)) {
                binding.leftButton.visibility = View.VISIBLE
            } else {
                binding.leftButton.visibility = View.GONE
            }

        }

        internal fun hasNext(flipper: ViewFlipper): Boolean {
            return flipper.indexOfChild(flipper.currentView) != flipper
                    .childCount - 1
        }

        internal fun hasPrevious(flipper: ViewFlipper): Boolean {
            return flipper.indexOfChild(flipper.currentView) != 0
        }

    }

    companion object {

        private val Log = Reporting.getLogger("Wizard")
        private const val REQUIRED_PERMISSIONS = 1
    }

}
