/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth.wizard;

import java.io.File;

import com.bugsense.trace.BugSenseHandler;

import uk.co.armedpineapple.corsixth.AsyncTaskResult;
import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.ConfigurationException;
import uk.co.armedpineapple.corsixth.Files;
import uk.co.armedpineapple.corsixth.Network;
import uk.co.armedpineapple.corsixth.R;
import uk.co.armedpineapple.corsixth.Files.DownloadFileTask;
import uk.co.armedpineapple.corsixth.Files.UnzipTask;
import uk.co.armedpineapple.corsixth.dialogs.DialogFactory;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AudioWizard extends WizardView {

	CheckBox	audioCheck;
	CheckBox	fxCheck;
	CheckBox	announcerCheck;
	CheckBox	musicCheck;

	Context		ctx;

	public AudioWizard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.ctx = context;
	}

	public AudioWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
	}

	public AudioWizard(Context context) {
		super(context);
		this.ctx = context;
	}

	@Override
	void saveConfiguration(Configuration config) throws ConfigurationException {

		config.setPlayAnnouncements(announcerCheck.isChecked());
		config.setPlayMusic(musicCheck.isChecked());
		config.setPlaySoundFx(fxCheck.isChecked());

	}

	@Override
	void loadConfiguration(final Configuration config) {

		audioCheck = ((CheckBox) findViewById(R.id.audioCheck));
		fxCheck = ((CheckBox) findViewById(R.id.fxCheck));
		announcerCheck = ((CheckBox) findViewById(R.id.announcerCheck));
		musicCheck = ((CheckBox) findViewById(R.id.musicCheck));

		audioCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				fxCheck.setEnabled(isChecked);
				announcerCheck.setEnabled(isChecked);
				musicCheck.setEnabled(isChecked);
			}

		});

		musicCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(final CompoundButton buttonView,
					boolean isChecked) {

				if (!isChecked) {
					return;
				}
				// Check if the original files has music, and show a dialog if not

				if (!Files.hasMusicFiles(config.getOriginalFilesPath())) {
					musicCheck.setChecked(false);
					AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

					builder
							.setMessage(ctx.getString(R.string.no_music_dialog))
							.setCancelable(true)
							.setNeutralButton(R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();
										}

									});

					AlertDialog alert = builder.create();
					alert.show();
					return;
				}

				File timidityConfig = new File(Files.getExtStoragePath() + "timidity"
						+ File.separator + "timidity.cfg");

				if (!(timidityConfig.isFile() && timidityConfig.canRead())) {

					AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
					DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == DialogInterface.BUTTON_POSITIVE) {
								doTimidityDownload();
							} else {
								buttonView.setChecked(false);
							}
						}

					};

					builder.setMessage(ctx.getString(R.string.music_download_dialog))
							.setCancelable(true)
							.setNegativeButton(R.string.cancel, alertListener)
							.setPositiveButton(R.string.ok, alertListener);

					AlertDialog alert = builder.create();
					alert.show();

				}

			}

		});

		fxCheck.setChecked(config.getPlaySoundFx());
		announcerCheck.setChecked(config.getPlayAnnouncements());
		musicCheck.setChecked(config.getPlayMusic());
		audioCheck.setChecked(config.getPlaySoundFx()
				|| config.getPlayAnnouncements() || config.getPlayMusic());

	}

	public void doTimidityDownload() {

		// Check for external storage
		if (!Files.canAccessExternalStorage()) {
			// No external storage
			Toast toast = Toast.makeText(ctx, R.string.no_external_storage,
					Toast.LENGTH_LONG);
			toast.show();
			musicCheck.setChecked(false);
			return;
		}

		// Check for network connection
		if (!Network.HasNetworkConnection(ctx)) {
			// Connection error
			Dialog connectionDialog = DialogFactory.createNetworkDialog(ctx);
			connectionDialog.show();
			musicCheck.setChecked(false);
			return;

		}

		final File extDir = ctx.getExternalFilesDir(null);
		final ProgressDialog dialog = new ProgressDialog(ctx);

		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMessage(ctx.getString(R.string.downloading_timidity));
		dialog.setIndeterminate(false);
		dialog.setMax(100);
		dialog.setCancelable(false);

		final UnzipTask uzt = new Files.UnzipTask(Files.getExtStoragePath()
				+ File.separator + "timidity" + File.separator) {

			@Override
			protected void onPostExecute(AsyncTaskResult<String> result) {
				super.onPostExecute(result);
				dialog.hide();

				if (result.getResult() != null) {

				} else if (result.getError() != null) {
					Exception e = result.getError();
					BugSenseHandler.sendException(e);
					Toast errorToast = Toast.makeText(ctx,
							R.string.download_timidity_error, Toast.LENGTH_LONG);

					errorToast.show();
					musicCheck.setChecked(false);
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				dialog.setMessage(ctx.getString(R.string.extracting_timidity));

			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
				dialog.setProgress(values[0]);
			}

		};

		final DownloadFileTask dft = new Files.DownloadFileTask(
				extDir.getAbsolutePath()) {

			@Override
			protected void onPostExecute(AsyncTaskResult<File> result) {
				super.onPostExecute(result);

				Toast errorToast = Toast.makeText(ctx,
						R.string.download_timidity_error, Toast.LENGTH_LONG);

				if (result.getError() != null) {
					BugSenseHandler.sendException(result.getError());
					musicCheck.setChecked(false);
					dialog.hide();
					errorToast.show();
				} else {
					uzt.execute(result.getResult());
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				dialog.show();
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
				dialog.setProgress(values[0]);
			}

		};

		dft.execute(ctx.getString(R.string.timidity_url));

	}
}
