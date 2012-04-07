// $codepro.audit.disable disallowNativeMethods
package uk.co.armedpineapple.corsixth;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import uk.co.armedpineapple.corsixth.Files.CopyAssetsTask;
import uk.co.armedpineapple.corsixth.Files.DiscoverAssetsTask;
import uk.co.armedpineapple.corsixth.Files.UnzipTask;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.apps.analytics.easytracking.TrackedActivity;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.media.*;

public class SDLActivity extends TrackedActivity {

	private String dataRoot = "";
	int currentVersion;
	private Properties properties;
	private Configuration config;

	// Main components
	private static SDLActivity mSingleton;
	private static SDLSurface mSurface;

	// Audio
	private static Thread mAudioThread;
	private static AudioTrack mAudioTrack;

	// Load the libraries
	static {
		System.loadLibrary("SDL");
		System.loadLibrary("SDL_image");
		System.loadLibrary("mikmod");
		System.loadLibrary("LUA");
		System.loadLibrary("AGG");
		System.loadLibrary("SDL_mixer");
		System.loadLibrary("appmain");
	}

	// Setup
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// The volume buttons should change the media volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Make sure that external media is mounted.
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			final SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());

			PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

			config = Configuration.loadFromPreferences(this, preferences);

			dataRoot = config.getCthPath();

			currentVersion = preferences.getInt("last_version", 0) - 1;

			try {
				currentVersion = (getPackageManager().getPackageInfo(
						getPackageName(), 0).versionCode);

			} catch (NameNotFoundException e) {
				BugSenseHandler.log("Scripts", e);
			}

			if (!preferences.getBoolean("scripts_copied", false)
					|| preferences.getInt("last_version", 0) < currentVersion) {

				Log.d(getClass().getSimpleName(), "This is a new installation");

				final ProgressDialog dialog = new ProgressDialog(this);
				final UnzipTask unzipTask = new UnzipTask(dataRoot + "/scripts/") {

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						dialog.setMessage(getString(R.string.preparing_game_files_dialog));
						dialog.setIndeterminate(false);
						dialog.setCancelable(false);
						dialog.show();
					}

					@Override
					protected void onProgressUpdate(Integer... values) {
						super.onProgressUpdate(values);
						dialog.setProgress(values[0]);
					}

					@Override
					protected void onPostExecute(AsyncTaskResult<String> result) {
						super.onPostExecute(result);
						Exception error;
						if ((error = result.getError()) != null) {
							Log.d(getClass().getSimpleName(),
									"Error copying files.");
							BugSenseHandler.log("File", error);
						}

						Editor edit = preferences.edit();
						edit.putBoolean("scripts_copied", true);
						edit.putInt("last_version", currentVersion);
						edit.commit();
						dialog.hide();
						continueLoad();
					}

				};

				AsyncTask<String, Void, AsyncTaskResult<File>> copyTask = new AsyncTask<String, Void, AsyncTaskResult<File>>() {

					@Override
					protected AsyncTaskResult<File> doInBackground(String... params) {

						try {
							Files.copyAsset(SDLActivity.this, params[0],
									params[1]);
						} catch (IOException e) {

							return new AsyncTaskResult<File>(e);
						}
						return new AsyncTaskResult<File>(new File(params[1] + "/"
								+ params[0]));
					}

					@Override
					protected void onPostExecute(AsyncTaskResult<File> result) {
						super.onPostExecute(result);
						File f;
						if ((f = result.getResult()) != null) {
							unzipTask.execute(result.getResult());
						} else {
							BugSenseHandler.log("File", result.getError());

						}
					};

				};

				copyTask.execute("game.zip", getExternalCacheDir().getAbsolutePath());

			} else {
				continueLoad();
			}

		} else {
			Log.e(getClass().getSimpleName(), "Can't get storage.");

			// Create an alert dialog warning that external storage isn't
			// mounted. The application will have to exit at this point.

			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setMessage(
					getResources().getString(R.string.no_external_storage))
					.setCancelable(false)
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}

							});

			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	void continueLoad() {

		try {
			config.writeToFile();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(getClass().getSimpleName(),
					"Couldn't write to configuration file");
			BugSenseHandler.log("Config", e);
		}

		setGamePath(dataRoot + "/scripts/");

		File f = new File(config.getSaveGamesPath());

		if (!f.isDirectory()) {
			f.mkdirs();
		}

		// So we can call stuff from static callbacks
		mSingleton = this;

		mSurface = new SDLSurface(getApplication(), config.getDisplayWidth(),
				config.getDisplayHeight());

		setContentView(mSurface);
		SurfaceHolder holder = mSurface.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		holder.setFixedSize(config.getDisplayWidth(), config.getDisplayHeight());

	}

	// Events
	protected void onPause() {
		super.onPause();
		Log.d(getClass().getSimpleName(), "onPause()");

	}

	protected void onResume() {
		super.onResume();
		Log.d(getClass().getSimpleName(), "onResume()");

	}

	private void restartActivity() {
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_settings:
			// finish();
			startActivity(new Intent(this, PrefsActivity.class));
			break;
		case R.id.menuitem_help:
			break;

		case R.id.menuitem_wizard:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences preferences = PreferenceManager
							.getDefaultSharedPreferences(getBaseContext());
					Editor editor = preferences.edit();
					editor.putBoolean("wizard_run", false);
					editor.commit();
				}

			};

			builder.setMessage(getResources().getString(R.string.setup_wizard))
					.setCancelable(false).setNeutralButton("OK", alertListener);

			AlertDialog alert = builder.create();
			alert.show();

			break;

		}
		return true;

	}

	// Messages from the SDLMain thread
	static int COMMAND_CHANGE_TITLE = 1;

	// Handler for the messages
	Handler commandHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.arg1 == COMMAND_CHANGE_TITLE) {
				setTitle((String) msg.obj);
			}
		}
	};

	// Send a message from the SDLMain thread
	void sendCommand(int command, Object data) {
		Message msg = commandHandler.obtainMessage();
		msg.arg1 = command;
		msg.obj = data;
		commandHandler.sendMessage(msg);
	}

	// C functions we call
	public static native void nativeInit();

	public static native void nativeQuit();

	public static native void onNativeResize(int x, int y, int format);

	public static native void onNativeKeyDown(int keycode);

	public static native void onNativeKeyUp(int keycode);

	public static native void onNativeTouch(int action, float x, float y,
			float p, int pc);

	public static native void onNativeAccel(float x, float y, float z);

	public static native void nativeRunAudioThread();

	public static native void setGamePath(String path);

	// Java functions called from C

	/**
	 * Shows the virtual keyboard. This will be called from the native LUA when
	 * a text box is pressed.
	 * 
	 * TODO - check whether the phone has a hardware keyboard. I've no idea how
	 * it behaves in this case.
	 */
	public static void showSoftKeyboard() {
		Log.d(SDLActivity.class.getSimpleName(), "Showing keyboard");
		InputMethodManager mgr = (InputMethodManager) mSingleton
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.showSoftInput(mSurface, InputMethodManager.SHOW_FORCED);

	}

	/**
	 * Hides the virtual keyboard.
	 * 
	 * TODO - implement this
	 */
	public static void hideSoftKeyboard() {
		Log.d(SDLActivity.class.getSimpleName(), "Hiding keyboard");

	}

	public static boolean createGLContext(int majorVersion, int minorVersion) {
		return mSurface.initEGL(majorVersion, minorVersion);
	}

	public static void flipBuffers() {
		mSurface.flipEGL();
	}

	public static void setActivityTitle(String title) {
		// Called from SDLMain() thread and can't directly affect the view
		// mSingleton.sendCommand(COMMAND_CHANGE_TITLE, title);
	}

	// Audio
	private static Object audioBuffer;

	public static Object audioInit(int sampleRate, boolean is16Bit,
			boolean isStereo, int desiredFrames) {
		int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO
				: AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT
				: AudioFormat.ENCODING_PCM_8BIT;
		int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

		Log.v(SDLActivity.class.getSimpleName(), "SDL audio: wanted "
				+ (isStereo ? "stereo" : "mono") + " "
				+ (is16Bit ? "16-bit" : "8-bit") + " "
				+ ((float) sampleRate / 1000f) + "kHz, " + desiredFrames
				+ " frames buffer");

		// Let the user pick a larger buffer if they really want -- but ye
		// gods they probably shouldn't, the minimums are horrifyingly high
		// latency already
		desiredFrames = Math.max(
				desiredFrames,
				(AudioTrack.getMinBufferSize(sampleRate, channelConfig,
						audioFormat) + frameSize - 1)
						/ frameSize);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				channelConfig, audioFormat, desiredFrames * frameSize,
				AudioTrack.MODE_STREAM);

		audioStartThread();

		Log.v(SDLActivity.class.getSimpleName(),
				"SDL audio: got "
						+ ((mAudioTrack.getChannelCount() >= 2) ? "stereo"
								: "mono")
						+ " "
						+ ((mAudioTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit"
								: "8-bit") + " "
						+ ((float) mAudioTrack.getSampleRate() / 1000f)
						+ "kHz, " + desiredFrames + " frames buffer");

		if (is16Bit) {
			audioBuffer = new short[desiredFrames * (isStereo ? 2 : 1)];
		} else {
			audioBuffer = new byte[desiredFrames * (isStereo ? 2 : 1)];
		}
		return audioBuffer;
	}

	public static void audioStartThread() {
		mAudioThread = new Thread(new Runnable() {
			public void run() {
				mAudioTrack.play();
				nativeRunAudioThread();
			}
		}, "Audio Thread");

		// I'd take REALTIME if I could get it!
		mAudioThread.setPriority(Thread.MAX_PRIORITY);
		mAudioThread.start();
	}

	public static void audioWriteShortBuffer(short[] buffer) {
		for (int i = 0; i < buffer.length;) {
			int result = mAudioTrack.write(buffer, i, buffer.length - i);
			if (result > 0) {
				i += result;
			} else if (result == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// Nom nom
				}
			} else {
				Log.w(SDLActivity.class.getSimpleName(),
						"SDL audio: error return from write(short)");
				return;
			}
		}
	}

	public static void audioWriteByteBuffer(byte[] buffer) {
		for (int i = 0; i < buffer.length;) {
			int result = mAudioTrack.write(buffer, i, buffer.length - i);
			if (result > 0) {
				i += result;
			} else if (result == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// Nom nom
				}
			} else {
				Log.w(SDLActivity.class.getSimpleName(),
						"SDL audio: error return from write(short)");
				return;
			}
		}
	}

	public static void audioQuit() {
		if (mAudioThread != null) {
			try {
				mAudioThread.join();
			} catch (Exception e) {
				Log.v(SDLActivity.class.getSimpleName(),
						"Problem stopping audio thread: " + e);
				BugSenseHandler.log("SDL Audio", e);
			}
			mAudioThread = null;

		}

		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack = null;
		}
	}
}

/**
 * Simple nativeInit() runnable
 */
class SDLMain implements Runnable {
	public void run() {
		// Runs SDL_main()
		SDLActivity.nativeInit();

		Log.v(getClass().getSimpleName(), "SDL thread terminated");
	}
}
