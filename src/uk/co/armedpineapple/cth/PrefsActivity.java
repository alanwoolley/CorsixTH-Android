/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth;

import java.io.File;

import com.bugsense.trace.BugSenseHandler;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.Files.DownloadFileTask;
import uk.co.armedpineapple.cth.Files.UnzipTask;
import uk.co.armedpineapple.cth.dialogs.DialogFactory;

public class PrefsActivity extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {

	private CTHApplication		application;
	private SharedPreferences	preferences;

	/** Preferences that require the game to be restarted before they take effect **/
	private String[]					requireRestart	= new String[] { "language_pref",
			"debug_pref", "movies_pref", "intromovie_pref", "resolution_pref",
			"reswidth_pref", "resheight_pref", "music_pref" };

	private boolean						displayRestartMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Make sure we're using the correct preferences
		PreferenceManager prefMgr = getPreferenceManager();

		prefMgr.setSharedPreferencesName(CTHApplication.PREFERENCES_KEY);
		prefMgr.setSharedPreferencesMode(MODE_PRIVATE);

		// Save the configuration to the preferences to make sure that everything is
		// up-to-date

		application = (CTHApplication) getApplication();
		preferences = application.getPreferences();
		application.configuration.saveToPreferences();

		addPreferencesFromResource(R.xml.prefs);

		// Custom Preference Listeners

		updateResolutionPrefsDisplay(preferences.getString("resolution_pref", "1"));

		findPreference("music_pref").setOnPreferenceChangeListener(
				new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						if (!(Boolean) newValue) {
							return true;
						}

						return onMusicEnabled();
					}

				});
		findPreference("resolution_pref").setOnPreferenceChangeListener(
				new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						Log.d(getClass().getSimpleName(), "Res mode: " + newValue);
						updateResolutionPrefsDisplay((String) newValue);
						return true;
					}
				});

		findPreference("setupwizard_pref").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PrefsActivity.this);
						DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

								Editor editor = preferences.edit();
								editor.putBoolean("wizard_run", false);
								editor.commit();

							}

						};
						builder
								.setMessage(
										PrefsActivity.this.getResources().getString(
												R.string.setup_wizard_dialog)).setCancelable(false)
								.setNeutralButton(R.string.ok, alertListener);

						AlertDialog alert = builder.create();
						alert.show();
						return true;
					}

				});

		for (String s : requireRestart) {
			findPreference(s).setOnPreferenceClickListener(
					new OnPreferenceClickListener() {

						@Override
						public boolean onPreferenceClick(Preference preference) {
							displayRestartMessage = true;
							return true;
						}

					});
		}

		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);

		Log.d(getClass().getSimpleName(), "Refreshing configuration");

		if (displayRestartMessage) {
			Log.d(getClass().getSimpleName(), "app requires restarting");
			Toast.makeText(this, R.string.dialog_require_restart, Toast.LENGTH_LONG)
					.show();
		}

		application.configuration.refresh();

		SDLActivity.cthUpdateConfiguration(application.configuration);
		SDLActivity.cthGameSpeed(application.configuration.getGameSpeed());
	}

	@Override
	protected void onResume() {
		super.onResume();
		displayRestartMessage = false;
		Log.d(getClass().getSimpleName(), "onResume()");
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
	}

	private void updateResolutionPrefsDisplay(String newValue) {
		if (newValue.equals("3")) {
			findPreference("reswidth_pref").setEnabled(true);
			findPreference("resheight_pref").setEnabled(true);
		} else {
			findPreference("reswidth_pref").setEnabled(false);
			findPreference("resheight_pref").setEnabled(false);
		}
	}

	boolean onMusicEnabled() {

		// Check if the original files has music, and show a dialog if not

		if (!Files.hasMusicFiles(application.configuration.getOriginalFilesPath())) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setMessage(getString(R.string.no_music_dialog))
					.setCancelable(true)
					.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}

					});

			AlertDialog alert = builder.create();
			alert.show();
			return false;
		}

		File timidityConfig = new File(Files.getExtStoragePath() + "timidity"
				+ File.separator + "timidity.cfg");

		if (!(timidityConfig.isFile() && timidityConfig.canRead())) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == DialogInterface.BUTTON_POSITIVE) {
						doTimidityDownload();
					}
				}

			};

			builder.setMessage(getString(R.string.music_download_dialog))
					.setCancelable(true)
					.setNegativeButton(R.string.cancel, alertListener)
					.setPositiveButton(R.string.ok, alertListener);

			AlertDialog alert = builder.create();
			alert.show();

		} else {
			// Music files are installed and ready
			return true;
		}

		return false;

	}

	void doTimidityDownload() {
		// Check for external storage
		if (!Files.canAccessExternalStorage()) {
			// No external storage
			Toast toast = Toast.makeText(this, R.string.no_external_storage,
					Toast.LENGTH_LONG);
			toast.show();
			return;
		}

		// Check for network connection
		if (!Network.hasNetworkConnection(this)) {
			// Connection error
			Dialog connectionDialog = DialogFactory
					.createNoNetworkConnectionDialog(this);
			connectionDialog.show();
			return;

		}

		final File extDir = getExternalFilesDir(null);
		final ProgressDialog dialog = new ProgressDialog(this);

		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMessage(getString(R.string.downloading_timidity));
		dialog.setIndeterminate(false);
		dialog.setCancelable(false);

		final UnzipTask uzt = new Files.UnzipTask(Files.getExtStoragePath()
				+ File.separator + "timidity" + File.separator, this) {

			@Override
			protected void onPostExecute(AsyncTaskResult<String> result) {
				super.onPostExecute(result);
				dialog.hide();

				if (result.getResult() != null) {
					Log.d(getClass().getSimpleName(),
							"Downloaded and extracted Timidity successfully");

					Editor editor = preferences.edit();
					editor.putBoolean("music_pref", true);
					editor.commit();

					((CheckBoxPreference) findPreference("music_pref")).setChecked(true);

				} else if (result.getError() != null) {
					Exception e = result.getError();
					BugSenseHandler.sendException(e);
					Toast errorToast = Toast.makeText(PrefsActivity.this,
							R.string.download_timidity_error, Toast.LENGTH_LONG);

					errorToast.show();
				}
			}

			@SuppressLint("NewApi")
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				dialog.setMessage(getString(R.string.extracting_timidity));
				if (Build.VERSION.SDK_INT >= 14) {
					dialog.setProgressNumberFormat(null);
				}

			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
				dialog.setProgress(values[0]);
				dialog.setMax(values[1]);
			}

		};

		final DownloadFileTask dft = new Files.DownloadFileTask(
				extDir.getAbsolutePath(), this) {

			@Override
			protected void onPostExecute(AsyncTaskResult<File> result) {
				super.onPostExecute(result);

				Toast errorToast = Toast.makeText(PrefsActivity.this,
						R.string.download_timidity_error, Toast.LENGTH_LONG);

				if (result.getError() != null) {
					BugSenseHandler.sendException(result.getError());
					dialog.hide();
					errorToast.show();
				} else {
					uzt.execute(result.getResult());
				}
			}

			@SuppressLint("NewApi")
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				dialog.show();
				if (Build.VERSION.SDK_INT >= 14) {
					dialog
							.setProgressNumberFormat(getString(R.string.download_progress_dialog_text));
				}
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
				dialog.setProgress(values[0] / 1000000);
				dialog.setMax(values[1] / 1000000);
			}

		};

		Dialog mobileDialog = DialogFactory.createMobileNetworkWarningDialog(this,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dft.execute(getString(R.string.timidity_url));

					}

				});

		if (Network.isMobileNetwork(this)) {
			mobileDialog.show();
		} else {
			dft.execute(getString(R.string.timidity_url));
		}

	}

}
