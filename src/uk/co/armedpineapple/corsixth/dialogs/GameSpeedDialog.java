/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth.dialogs;

import uk.co.armedpineapple.corsixth.R;
import uk.co.armedpineapple.corsixth.SDLActivity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class GameSpeedDialog extends Dialog {

	@SuppressWarnings("nls")
	private String[] speeds = { "Paused", "Slowest", "Slower", "Normal",
			"Max speed", "And then some more" };
	private SeekBar seekbar;
	private TextView gameSpeedText;
	private Button gameSpeedButton;
	private SDLActivity ctx;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		SDLActivity.cthGameSpeed(ctx.config.getGameSpeed());
	}

	public GameSpeedDialog(SDLActivity context) {
		super(context);
		this.ctx = context;
		setTitle(R.string.gamespeed_dialog_header);
		setContentView(R.layout.gamespeed_dialog);
		getWindow().setLayout(600, LayoutParams.WRAP_CONTENT);
		seekbar = (SeekBar) findViewById(R.id.gamespeed_seek);
		gameSpeedText = (TextView) findViewById(R.id.gamespeed_text);
		gameSpeedButton = (Button) findViewById(R.id.gamespeed_ok);

		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// Update the text with the speed's textual value
				gameSpeedText.setText(speeds[progress]);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

		});

		gameSpeedButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SDLActivity.cthGameSpeed(seekbar.getProgress());
				hide();
			}

		});

	}

	public void show(int speed) {
		seekbar.setProgress(speed);
		show();
	}

}
