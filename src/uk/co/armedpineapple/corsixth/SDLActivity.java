package uk.co.armedpineapple.corsixth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import com.bugsense.trace.BugSenseHandler;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.media.*;

/**
 * SDL Activity
 */
public class SDLActivity extends Activity {

	private String dataRoot = "";

	private Properties properties;
	private Configuration config;

	// Main components
	private static SDLActivity mSingleton;
	private static SDLSurface mSurface;

	// Audio
	private static Thread mAudioThread;
	private static AudioTrack mAudioTrack;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_settings:
			startActivity(new Intent(this, PrefsActivity.class));
			break;
		case R.id.menuitem_help:
			break;

		}
		return true;

	}

	// Load the .so
	static {
		System.loadLibrary("SDL");
		System.loadLibrary("SDL_image");
		System.loadLibrary("mikmod");
		System.loadLibrary("LUA");
		System.loadLibrary("AGG");
		System.loadLibrary("SDL_mixer");
		System.loadLibrary("appmain");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	// Setup
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// The volume buttons should change the media volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		Properties properties = new Properties();
		try {
			InputStream inputStream = getAssets()
					.open("application.properties");
			Log.d(getClass().getSimpleName(), "Loading properties");
			properties.load(inputStream);
		} catch (IOException e) {
			Log.d(getClass().getSimpleName(), "No properties file found");
		}

		if (properties.containsKey("bugsense.key")) {
			Log.d(getClass().getSimpleName(), "Setting up bugsense");
			BugSenseHandler
					.setup(this, (String) properties.get("bugsense.key"));
		}

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			File extDir = getExternalFilesDir(null);
			Log.i(getClass().getSimpleName(),
					"Directory: " + extDir.getAbsolutePath());

			final SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			
			PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
			
			config = Configuration.loadFromPreferences(this, preferences);

			dataRoot = config.getCthPath();

			if (!preferences.getBoolean("scripts_copied", false)) {
				final AsyncTask<Void, Void, ArrayList<String>> discoverTask;
				final AsyncTask<ArrayList<String>, Integer, Void> copyTask;
				copyTask = new AsyncTask<ArrayList<String>, Integer, Void>() {
					ProgressDialog dialog;

					@Override
					protected void onPreExecute() {
						dialog = new ProgressDialog(SDLActivity.this);
						dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						dialog.setMessage("Preparing Game Files. This will only occur once, but may take a while.");
						dialog.setIndeterminate(false);
						dialog.show();

					}

					@Override
					protected Void doInBackground(ArrayList<String>... params) {
						int max = params[0].size();
						for (int i = 0; i < max; i++) {
							Files.copyAsset(SDLActivity.this, params[0].get(i),
									dataRoot);
							publishProgress(i + 1, max);
						}
						return null;
					}

					@Override
					protected void onProgressUpdate(Integer... values) {
						dialog.setMax(values[1]);
						dialog.setProgress(values[0]);
					}

					@Override
					protected void onPostExecute(Void result) {
						dialog.hide();
						continueLoad();
					}

				};

				discoverTask = new AsyncTask<Void, Void, ArrayList<String>>() {
					ProgressDialog dialog;
					ArrayList<String> paths;

					@Override
					protected void onPreExecute() {
						dialog = new ProgressDialog(SDLActivity.this);
						dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						dialog.setMessage("Preparing Game Files. This will only occur once, but may take a while.");
						dialog.setIndeterminate(true);
						dialog.show();
					}

					@Override
					protected ArrayList<String> doInBackground(Void... params) {
						paths = new ArrayList<String>();
						paths = Files.listAssets(SDLActivity.this, "scripts");
						return paths;
					}

					@Override
					protected void onPostExecute(ArrayList<String> result) {
						dialog.hide();
						copyTask.execute(result);
						Editor edit = preferences.edit();
						edit.putBoolean("scripts_copied", true);
						edit.commit();
					}

				};

				discoverTask.execute();

			} else {
				continueLoad();
			}
		} else {
			Log.e(getClass().getSimpleName(), "Can't get storage.");

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}

			};

			builder.setMessage(
					getResources().getString(R.string.no_external_storage))
					.setCancelable(false).setNeutralButton("OK", alertListener);

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

		// So we can call stuff from static callbacks
		mSingleton = this;

		// Set up the surface
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
	}

	protected void onResume() {
		super.onResume();
	}

	private void restartActivity() {
		Intent intent = getIntent();
		finish();
		startActivity(intent);
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

	public static void showSoftKeyboard() {
		Log.d(SDLActivity.class.getSimpleName(), "Showing keyboard");
		InputMethodManager mgr = (InputMethodManager) mSingleton
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.showSoftInput(mSurface, InputMethodManager.SHOW_FORCED);

	}

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
		mSingleton.sendCommand(COMMAND_CHANGE_TITLE, title);
	}

	// Audio
	private static Object buf;

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
			buf = new short[desiredFrames * (isStereo ? 2 : 1)];
		} else {
			buf = new byte[desiredFrames * (isStereo ? 2 : 1)];
		}
		return buf;
	}

	public static void audioStartThread() {
		mAudioThread = new Thread(new Runnable() {
			public void run() {
				mAudioTrack.play();
				nativeRunAudioThread();
			}
		});

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

			// Log.v("SDL", "Finished waiting for audio thread");
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

		// Log.v("SDL", "SDL thread terminated");
	}
}
