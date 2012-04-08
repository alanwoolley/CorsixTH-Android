package uk.co.armedpineapple.corsixth.wizard;

import java.io.File;

import com.bugsense.trace.BugSenseHandler;

import uk.co.armedpineapple.corsixth.AsyncTaskResult;
import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.Files;
import uk.co.armedpineapple.corsixth.R;
import uk.co.armedpineapple.corsixth.Files.DownloadFileTask;
import uk.co.armedpineapple.corsixth.Files.UnzipTask;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AudioWizard extends WizardView {

	CheckBox audioCheck;
	CheckBox fxCheck;
	CheckBox announcerCheck;
	CheckBox musicCheck;

	Context ctx;

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
	void saveConfiguration(Configuration config) {

		config.setPlayAnnouncements(announcerCheck.isChecked());
		config.setPlayMusic(musicCheck.isChecked());
		config.setPlaySoundFx(fxCheck.isChecked());

	}

	@Override
	void loadConfiguration(Configuration config) {

		audioCheck = ((CheckBox) findViewById(R.id.audioCheck));
		fxCheck = ((CheckBox) findViewById(R.id.fxCheck));
		announcerCheck = ((CheckBox) findViewById(R.id.announcerCheck));
		musicCheck = ((CheckBox) findViewById(R.id.musicCheck));

		audioCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				fxCheck.setEnabled(isChecked);
				announcerCheck.setEnabled(isChecked);
				musicCheck.setEnabled(isChecked);
			}

		});

		musicCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(final CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					File timidityConfig = new File(
							"/sdcard/timidity/timidity.cfg");
					
					if (!(timidityConfig.isFile() && timidityConfig.canRead())) {

						AlertDialog.Builder builder = new AlertDialog.Builder(
								ctx);
						DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == DialogInterface.BUTTON_POSITIVE) {
									doTimidityDownload();
								} else {
									buttonView.setChecked(false);
								}
							}

						};

						builder.setMessage(
								"Music requires an additional download. Download file (14.1MB)?")
								.setCancelable(true)
								.setNegativeButton("Cancel", alertListener)
								.setPositiveButton("OK", alertListener);

						AlertDialog alert = builder.create();
						alert.show();

					}
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
		

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			
			final File extDir = ctx.getExternalFilesDir(null);
			final ProgressDialog dialog = new ProgressDialog(ctx);

			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setMessage(ctx.getString(R.string.downloading_timidity));
			dialog.setIndeterminate(false);
			dialog.setMax(100);
			dialog.setCancelable(false);
			
			final UnzipTask uzt = new Files.UnzipTask("/mnt/sdcard/timidity/") {

				@Override
				protected void onPostExecute(AsyncTaskResult<String> result) {
					super.onPostExecute(result);
					dialog.hide();

					if (result.getResult() != null) {
					
					} else if (result.getError() != null) {
						Exception e = result.getError();
						BugSenseHandler.log("Extract", e);
						Toast errorToast = Toast
								.makeText(ctx, R.string.download_timidity_error,
										Toast.LENGTH_LONG);

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
						BugSenseHandler.log("Download", result.getError());
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
			
		} else {
			Toast toast = Toast.makeText(ctx, R.string.no_external_storage,
					Toast.LENGTH_LONG);
			toast.show();
			musicCheck.setChecked(false);
		}
	}
}
