/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.wizard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AlertDialog.Builder
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast


import java.io.File

import uk.co.armedpineapple.cth.AsyncTaskResult
import uk.co.armedpineapple.cth.Configuration
import uk.co.armedpineapple.cth.Configuration.ConfigurationException
import uk.co.armedpineapple.cth.Files
import uk.co.armedpineapple.cth.Files.DownloadFileTask
import uk.co.armedpineapple.cth.Files.FindFilesTask
import uk.co.armedpineapple.cth.Files.UnzipTask
import uk.co.armedpineapple.cth.Network
import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.Reporting
import uk.co.armedpineapple.cth.dialogs.DialogFactory

class OriginalFilesWizard : WizardView {

    private var originalFilesRadioGroup: RadioGroup? = null
    private var automaticRadio: RadioButton? = null
    private var manualRadio: RadioButton? = null
    private var downloadDemoRadio: RadioButton? = null

    private var customLocation: String? = null

    private val ctx: Context

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        ctx = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        ctx = context
    }

    constructor(context: Context) : super(context) {
        ctx = context
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        findViewById<View>(R.id.need_help_text).setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ctx
                    .resources.getString(R.string.full_version_url)))
            ctx.startActivity(browserIntent)
        }

    }

    @Throws(ConfigurationException::class)
    override fun saveConfiguration(config: Configuration) {
        if (customLocation == null || !Files.hasDataFiles(customLocation!!)) {

            // Check to see if there's a HOSP directory instead

            val hosp = File(config.originalFilesPath + "/HOSP")
            if (hosp.exists() && Files.hasDataFiles(config.originalFilesPath + "/HOSP")) {
                manualRadio!!.isChecked = true
                customLocation = config.originalFilesPath + "/HOSP"
                config.originalFilesPath = customLocation
                return
            }

            val builder = AlertDialog.Builder(ctx)

            builder.setMessage(ctx.getString(R.string.no_data_dialog))
                    .setCancelable(true)
                    .setNeutralButton(R.string.ok) { dialog, which -> dialog.dismiss() }

            val alert = builder.create()
            alert.show()

            throw ConfigurationException()
        }
        config.originalFilesPath = customLocation

    }

    override fun loadConfiguration(config: Configuration) {

        originalFilesRadioGroup = findViewById<View>(R.id.originalFilesRadioGroup) as RadioGroup
        automaticRadio = findViewById<View>(R.id.automaticRadio) as RadioButton
        manualRadio = findViewById<View>(R.id.manualRadio) as RadioButton
        downloadDemoRadio = findViewById<View>(R.id.downloadDemoRadio) as RadioButton

        val editTextBox = EditText(ctx)
        editTextBox.setText(Files.extStoragePath + "th")
        val builder = Builder(ctx)
        builder.setMessage(R.string.custom_location_message)
        builder.setNeutralButton(R.string.ok
        ) { dialog, which ->
            customLocation = editTextBox.text.toString()
            manualRadio!!.text = (ctx.getString(R.string.custom_files) + " ("
                    + customLocation + ")")
        }

        builder.setView(editTextBox)

        val d = builder.create()

        automaticRadio!!.setOnClickListener { doGameFilesSearch() }

        manualRadio!!.setOnClickListener { d.show() }

        automaticRadio!!.isChecked = false
        downloadDemoRadio!!.isChecked = false
        if (config.originalFilesPath != null && config.originalFilesPath.length > 0) {
            manualRadio!!.isChecked = true
        }

        downloadDemoRadio!!.setOnClickListener(OnClickListener {
            if (!Files.canAccessExternalStorage()) {
                // Show an error message or something here.
                return@OnClickListener
            }

            val f = File(ctx.getExternalFilesDir(null)!!.absolutePath + "/demo/HOSP")
            if (!f.exists()) {

                val dialogBuilder = AlertDialog.Builder(ctx)
                val alertListener = DialogInterface.OnClickListener { dialog, which ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {

                        doDemoDownload()

                    } else {
                        downloadDemoRadio!!.isChecked = false
                    }
                }

                dialogBuilder
                        .setMessage(
                                resources.getString(R.string.download_demo_dialog))
                        .setCancelable(true)
                        .setNegativeButton(R.string.cancel, alertListener)
                        .setPositiveButton(R.string.ok, alertListener)

                val alert = dialogBuilder.create()
                alert.show()
            } else {
                customLocation = ctx.getExternalFilesDir(null)!!.absolutePath + "/demo/HOSP"
            }
        })
    }

    private fun doDemoDownload() {
        // Check that the external storage is mounted.
        if (!Files.canAccessExternalStorage()) {
            // External storage error
            val toast = Toast.makeText(ctx, R.string.no_external_storage,
                    Toast.LENGTH_LONG)
            toast.show()
            return
        }

        // Check that there is an active network connection
        // TODO - warn if connecting over mobile internet

        if (!Network.hasNetworkConnection(ctx)) {
            // Connection error
            val connectionDialog = DialogFactory
                    .createNoNetworkConnectionDialog(ctx)
            connectionDialog.show()
            return
        }

        val extDir = ctx.getExternalFilesDir(null)
        val dialog = ProgressDialog(ctx)

        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog.setMessage(ctx.getString(R.string.downloading_demo))
        dialog.isIndeterminate = false
        dialog.setCancelable(false)

        val uzt = object : Files.UnzipTask(extDir!!.absolutePath + "/demo/", ctx) {

            override fun onPostExecute(result: AsyncTaskResult<String>) {
                super.onPostExecute(result)
                dialog.hide()

                if (result.result != null) {
                    customLocation = result.result + "HOSP"
                    Log.d("Extracted TH demo: " + customLocation!!)
                } else if (result.error != null) {
                    val e = result.error
                    Reporting.reportWithToast(ctx, ctx.getString(R.string.download_demo_error), e)
                    downloadDemoRadio!!.isChecked = false
                }
            }

            @SuppressLint("NewApi")
            override fun onPreExecute() {
                super.onPreExecute()
                dialog.setMessage(ctx.getString(R.string.extracting_demo))
                if (Build.VERSION.SDK_INT >= 14) {
                    dialog.setProgressNumberFormat(null)
                }
            }

            protected override fun onProgressUpdate(vararg values: Int?) {
                super.onProgressUpdate(*values)
                if (values != null && values.size >= 2) {
                    dialog.progress = values!![0] as Int
                    dialog.max = values!![1] as Int
                }
            }

        }

        val dft = object : Files.DownloadFileTask(
                extDir.absolutePath, ctx) {

            override fun onPostExecute(result: AsyncTaskResult<File>) {
                super.onPostExecute(result)

                if (result.error != null) {
                    automaticRadio!!.isChecked = true
                    dialog.hide()

                    DialogFactory.createFromException(result.error!!,
                            ctx.getString(R.string.download_demo_error), ctx, false).show()
                } else {
                    uzt.execute(result.result)
                }
            }

            @SuppressLint("NewApi")
            override fun onPreExecute() {
                super.onPreExecute()
                if (Build.VERSION.SDK_INT >= 14) {
                    dialog.setProgressNumberFormat(ctx
                            .getString(R.string.download_progress_dialog_text))
                }
                dialog.show()
            }

            protected override fun onProgressUpdate(vararg values: Int?) {
                super.onProgressUpdate(*values)
                if (values != null && values.size >= 2) {
                    dialog.progress = values[0]!! / 1000000
                    dialog.max = values[1]!! / 1000000
                }

            }

        }
        val mobileDialog = DialogFactory.createMobileNetworkWarningDialog(ctx,
                DialogInterface.OnClickListener { dialog, which -> dft.execute(ctx.getString(R.string.demo_url)) })

        if (Network.isMobileNetwork(ctx)) {
            mobileDialog.show()
        } else {
            dft.execute(ctx.getString(R.string.demo_url))
        }

    }

    private fun doGameFilesSearch() {
        val fft = object : FindFilesTask() {

            internal var progressDialog: ProgressDialog? = null

            override fun onCancelled(result: AsyncTaskResult<String>) {
                progressDialog?.hide()
                automaticRadio!!.isChecked = false
                Toast.makeText(ctx, R.string.search_cancelled, Toast.LENGTH_LONG)
                        .show()
            }

            override fun onPostExecute(result: AsyncTaskResult<String>) {
                super.onPostExecute(result)

                progressDialog?.hide()
                if (result.result != null) {
                    customLocation = result.result
                    Toast.makeText(ctx,
                            ctx.getString(R.string.found_files) + customLocation!!,
                            Toast.LENGTH_LONG).show()
                } else {
                    automaticRadio!!.isChecked = false
                    Toast.makeText(ctx, R.string.couldnt_find_game_files,
                            Toast.LENGTH_LONG).show()
                }
            }

            override fun onPreExecute() {
                super.onPreExecute()

                progressDialog = ProgressDialog(ctx)
                progressDialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                progressDialog?.isIndeterminate = true
                progressDialog?.setMessage(context.getString(R.string.searching))
                progressDialog?.setCancelable(true)
                progressDialog?.show()

            }

        }

        fft.execute()
    }

    companion object {
        private val Log = Reporting.getLogger("OriginalFilesWizard")
    }
}
