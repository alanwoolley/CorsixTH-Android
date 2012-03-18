package uk.co.armedpineapple.corsixth.wizard;

import java.io.File;
import uk.co.armedpineapple.corsixth.Configuration;
import uk.co.armedpineapple.corsixth.Files;
import uk.co.armedpineapple.corsixth.R;
import uk.co.armedpineapple.corsixth.Files.DownloadFileTask;
import uk.co.armedpineapple.corsixth.Files.UnzipTask;
import android.app.AlertDialog;
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
			config.setOriginalFilesPath("/mnt/sdcard/th");
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
		editTextBox.setText("/mnt/sdcard/th");
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

		if (config.getOriginalFilesPath().equals("/mnt/sdcard/th")) {
			automaticRadio.setChecked(true);
		} else {
			manualRadio.setChecked(true);
		}

		downloadDemoRadio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
				DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == DialogInterface.BUTTON_POSITIVE) {
							doDemoDownload();
						}
					}

				};

				builder.setMessage(
						getResources().getString(R.string.download_demo_dialog))
						.setCancelable(true)
						.setNegativeButton("Cancel", alertListener)
						.setPositiveButton("OK", alertListener);

				AlertDialog alert = builder.create();
				alert.show();
			}

		});
	}

	void doDemoDownload() {

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

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
				protected void onPostExecute(String result) {
					super.onPostExecute(result);
					dialog.hide();
					if (result != null) {
						customLocation = result + "HOSP";
						Log.d(getClass().getSimpleName(), "Extracted TH demo: "
								+ customLocation);
					} else {
						Toast errorToast = Toast
								.makeText(ctx, R.string.download_demo_error,
										Toast.LENGTH_LONG);

						errorToast.show();
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
				protected void onPostExecute(File result) {
					super.onPostExecute(result);

					Toast errorToast = Toast.makeText(ctx,
							R.string.download_demo_error, Toast.LENGTH_LONG);

					if (result == null) {
						automaticRadio.setChecked(true);
						dialog.hide();
						errorToast.show();
					} else {
						uzt.execute(result);
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

		} else {
			Toast toast = Toast.makeText(ctx, R.string.no_external_storage,
					Toast.LENGTH_LONG);
			toast.show();
			automaticRadio.setChecked(true);
		}
	}

}
