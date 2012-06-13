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
import uk.co.armedpineapple.corsixth.Files;
import uk.co.armedpineapple.corsixth.Network;
import uk.co.armedpineapple.corsixth.R;
import uk.co.armedpineapple.corsixth.Files.DownloadFileTask;
import uk.co.armedpineapple.corsixth.Files.UnzipTask;
import uk.co.armedpineapple.corsixth.dialogs.DialogFactory;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class OriginalFilesWizard extends WizardView {

	RadioGroup originalFilesRadioGroup;
	RadioButton automaticRadio;
	RadioButton manualRadio;
	RadioButton downloadDemoRadio;

	String customLocation;

	Context ctx;

	public OriginalFilesWizard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ctx = context;
	}

	public OriginalFilesWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;
	}

	public OriginalFilesWizard(Context context) {
		super(context);
		ctx = context;
	}

	@Override
	void saveConfiguration(Configuration config) {
		if (automaticRadio.isChecked()) {
			config.setOriginalFilesPath("/sdcard/th");
		} else if (manualRadio.isChecked() || downloadDemoRadio.isChecked()) {
			config.setOriginalFilesPath(customLocation);
		}
	}

	@Override
	void loadConfiguration(Configuration config) {

		originalFilesRadioGroup = ((RadioGroup) findViewById(R.id.originalFilesRadioGroup));
		automaticRadio = ((RadioButton) findViewById(R.id.automaticRadio));
		manualRadio = ((RadioButton) findViewById(R.id.manualRadio));
		downloadDemoRadio = ((RadioButton) findViewById(R.id.downloadDemoRadio));

		final EditText editTextBox = new EditText(ctx);
		editTextBox.setText("/sdcard/th");
		Builder builder = new Builder(ctx);
		builder.setMessage("Theme Hospital Game Files location");
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				customLocation = editTextBox.getText().toString();
				manualRadio.setText("Custom (" + customLocation + ")");
			}
		});

		builder.setView(editTextBox);

		final AlertDialog d = builder.create();

		manualRadio.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				d.show();
			}
		});

		if (config.getOriginalFilesPath().equals("/sdcard/th")) {
			automaticRadio.setChecked(true);
		} else {
			manualRadio.setChecked(true);
		}

		downloadDemoRadio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Files.canAccessExternalStorage()) {
					File f = new File(ctx.getExternalFilesDir(null)
							.getAbsolutePath() + "/demo/HOSP");
					if (!f.exists()) {

						AlertDialog.Builder builder = new AlertDialog.Builder(
								ctx);
						DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == DialogInterface.BUTTON_POSITIVE) {

									doDemoDownload();

								}
							}

						};

						builder.setMessage(
								getResources().getString(
										R.string.download_demo_dialog))
								.setCancelable(true)
								.setNegativeButton("Cancel", alertListener)
								.setPositiveButton("OK", alertListener);

						AlertDialog alert = builder.create();
						alert.show();
					} else {
						customLocation = ctx.getExternalFilesDir(null)
								.getAbsolutePath() + "/demo/HOSP";
					}
				}
			}
		});
	}

	void doDemoDownload() {
		// Check that the external storage is mounted.
		if (!Files.canAccessExternalStorage()) {
			// External storage error
			Toast toast = Toast.makeText(ctx, R.string.no_external_storage,
					Toast.LENGTH_LONG);
			toast.show();
			automaticRadio.setChecked(true);
			return;
		}

		// Check that there is an active network connection
		if (!Network.HasNetworkConnection(ctx)) {
			// Connection error
			Dialog connectionDialog = DialogFactory.createNetworkDialog(ctx);
			connectionDialog.show();
			automaticRadio.setChecked(true);
			return;
		}

		final File extDir = ctx.getExternalFilesDir(null);
		final ProgressDialog dialog = new ProgressDialog(ctx);

		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMessage(ctx.getString(R.string.downloading_demo));
		dialog.setIndeterminate(false);
		dialog.setMax(100);
		dialog.setCancelable(false);

		final UnzipTask uzt = new Files.UnzipTask(extDir.getAbsolutePath()
				+ "/demo/") {

			@Override
			protected void onPostExecute(AsyncTaskResult<String> result) {
				super.onPostExecute(result);
				dialog.hide();

				if (result.getResult() != null) {
					customLocation = result.getResult() + "HOSP";
					Log.d(getClass().getSimpleName(), "Extracted TH demo: "
							+ customLocation);
				} else if (result.getError() != null) {
					Exception e = result.getError();
					BugSenseHandler.log("Extract", e);
					DialogFactory.createFromException(result.getError(),
							ctx.getString(R.string.download_demo_error), ctx,
							false).show();
					automaticRadio.setChecked(true);
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				dialog.setMessage(ctx.getString(R.string.extracting_demo));
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

				if (result.getError() != null) {
					BugSenseHandler.log("Download", result.getError());
					automaticRadio.setChecked(true);
					dialog.hide();

					DialogFactory.createFromException(result.getError(),
							ctx.getString(R.string.download_demo_error), ctx,
							false).show();
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

		dft.execute(ctx.getString(R.string.demo_url));

	}
}
