/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.TextView
import uk.co.armedpineapple.cth.Files
import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.SDLActivity
import java.io.IOException

object DialogFactory {

    fun createTestingWarningDialog(ctx: Context, positive: OnClickListener): Dialog {
        val builder = AlertDialog.Builder(ctx)

        builder.setMessage(
                ctx.resources.getString(R.string.preferences_alpha_warning_text))
                .setCancelable(true)
        builder.setNegativeButton(R.string.cancel, OnClickListener { dialog, which -> return@OnClickListener })
        builder.setPositiveButton(R.string.ok, positive)
        builder.setTitle(R.string.warning)
        return builder.create()

    }

    fun createAboutDialog(ctx: SDLActivity): Dialog {
        val d = Dialog(ctx)

        d.setContentView(R.layout.about)

        d.window!!
                .setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        d.setTitle(R.string.about_dialog_header)

        val button = d.findViewById<View>(R.id.dismissDialogButton) as Button
        button.setOnClickListener { d.dismiss() }

        d.setOnShowListener {
            val aboutText = d.findViewById<View>(R.id.aboutTextView) as TextView
            val text: String = try {
                Files.readTextFromResource(ctx, R.raw.about)
            } catch (e: IOException) {
                ""
            }

            aboutText.text = text
        }

        return d
    }

    fun createNoNetworkConnectionDialog(ctx: Context): Dialog {
        val builder = AlertDialog.Builder(ctx)

        builder.setMessage(
                ctx.resources.getString(R.string.no_connection_error))
                .setCancelable(false)

        builder.setNeutralButton(R.string.ok
        ) { dialog, which -> }

        return builder.create()
    }

    fun createErrorDialog(ctx: Context): Dialog {
        val builder = AlertDialog.Builder(ctx)
        builder.setMessage(ctx.resources.getString(R.string.load_error))
                .setCancelable(false)
        builder.setNeutralButton(R.string.ok
        ) { dialog, which -> SDLActivity.nativeQuit() }

        return builder.create()

    }

    fun createMobileNetworkWarningDialog(ctx: Context,
                                         positive: OnClickListener): Dialog {
        val builder = AlertDialog.Builder(ctx)

        builder.setMessage(
                ctx.resources.getString(R.string.mobile_network_warning))
                .setCancelable(true)
        builder.setNegativeButton(R.string.cancel, OnClickListener { dialog, which -> return@OnClickListener })
        builder.setPositiveButton(R.string.ok, positive)
        builder.setTitle(R.string.warning)
        return builder.create()

    }

    fun createExternalStorageWarningDialog(ctx: Context,
                                           finish: Boolean): Dialog {

        val builder = AlertDialog.Builder(ctx)

        builder.setMessage(
                ctx.resources.getString(R.string.no_external_storage))
                .setCancelable(false)
        if (finish) {

            builder.setNeutralButton(R.string.ok
            ) { dialog, which -> (ctx as Activity).finish() }

        } else {
            builder.setNeutralButton(R.string.ok
            ) { dialog, which -> }
        }

        return builder.create()
    }

    fun createFromException(e: Exception, ctx: Context,
                            finish: Boolean): Dialog {
        return createFromException(e, null, ctx, finish)
    }

    fun createFromException(e: Exception, title: String?,
                            ctx: Context, finish: Boolean): Dialog {
        val builder = AlertDialog.Builder(ctx)

        builder.setCancelable(false)
        if (title == null) {
            builder.setTitle(R.string.dialog_error_title)
        } else {
            builder.setTitle(title)
        }
        builder.setMessage(e.localizedMessage)

        if (finish) {
            builder.setNeutralButton(R.string.ok
            ) { dialog, which -> (ctx as Activity).finish() }
        } else {
            builder.setNeutralButton(R.string.ok
            ) { dialog, which -> }
        }

        return builder.create()
    }
}
