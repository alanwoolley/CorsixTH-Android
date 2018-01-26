/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.dialogs

import android.app.Dialog
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView

import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.SDLActivity

class GameSpeedDialog(private val ctx: SDLActivity) : Dialog(ctx) {

    private val speeds = arrayOf("Paused", "Slowest", "Slower", "Normal", "Max speed", "And then some more")
    private val seekbar: SeekBar
    private val gameSpeedText: TextView

    override fun onBackPressed() {
        super.onBackPressed()

        // Unpause the game
        SDLActivity.cthGameSpeed(ctx.app.configuration!!.gameSpeed)
    }

    init {
        setTitle(R.string.gamespeed_dialog_header)
        setContentView(R.layout.gamespeed_dialog)
        window!!.setLayout(800, LayoutParams.WRAP_CONTENT)
        seekbar = findViewById<View>(R.id.gamespeed_seek) as SeekBar
        gameSpeedText = findViewById<View>(R.id.gamespeed_text) as TextView
        val gameSpeedButton = findViewById<View>(R.id.gamespeed_ok) as Button

        seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                           fromUser: Boolean) {
                // Update the text with the speed's textual value
                gameSpeedText.text = speeds[progress]
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}

        })

        gameSpeedButton.setOnClickListener {
            // Update the game speed
            ctx.app.configuration?.gameSpeed = seekbar.progress
            hide()
        }

    }

    fun show(speed: Int) {
        seekbar.progress = speed
        show()
    }

}
