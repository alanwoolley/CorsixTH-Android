/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
// $codepro.audit.disable disallowNativeMethods
package uk.co.armedpineapple.corsixth;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import uk.co.armedpineapple.corsixth.Files.UnzipTask;
import uk.co.armedpineapple.corsixth.dialogs.DialogFactory;
import uk.co.armedpineapple.corsixth.dialogs.LoadDialog;
import uk.co.armedpineapple.corsixth.dialogs.MenuDialog;
import uk.co.armedpineapple.corsixth.dialogs.SaveDialog;

import com.bugsense.trace.BugSenseHandler;

import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.media.*;

public class SDLActivity extends CTHActivity {

	private int currentVersion;
	private Properties properties;
	public Configuration config;
	private WakeLock wake;
	private MenuDialog mainMenu;

	// Commands that can be sent from the game
	public enum Command {
		SHOW_MENU, SHOW_LOAD_DIALOG, SHOW_SAVE_DIALOG, RESTART_GAME, QUICK_LOAD, QUICK_SAVE, SHOW_KEYBOARD, HIDE_KEYBOARD, SHOW_ABOUT_DIALOG, PAUSE_GAME, SHOW_SETTINGS_DIALOG, GAME_SPEED_UPDATED
	}

	// This is what SDL runs in. It invokes SDL_main(), eventually
	private static Thread mSDLThread;

	// EGL private objects
	private static EGLContext mEGLContext;
	private static EGLSurface mEGLSurface;
	private static EGLDisplay mEGLDisplay;
	private static EGLConfig mEGLConfig;
	private static int mGLMajor, mGLMinor;

	// Main components
	public static SDLActivity mSingleton;
	public static SDLSurface mSurface;

	// Audio
	private static Thread mAudioThread;
	private static AudioTrack mAudioTrack;
	private static Object audioBuffer;

	// Handler for the messages
	Handler commandHandler = new CommandHandler(this);

	// C functions we call
	public static native void nativeInit(String logPath);

	public static native void nativeQuit();

	public static native void onNativeResize(int x, int y, int format);

	public static native void onNativeKeyDown(int keycode);

	public static native void onNativeKeyUp(int keycode);

	public static native void onNativeTouch(int touchDevId,
			int pointerFingerId, int action, float x, float y, float p, int pc,
			int gestureTriggered);

	public static native void onNativeAccel(float x, float y, float z);

	public static native void nativeRunAudioThread();

	public static native void cthRestartGame();

	public static native void cthSaveGame(String path);

	public static native void cthLoadGame(String path);

	public static native void cthGameSpeed(int speed);

	public static String nativeGetGamePath() {
		return mSingleton.config.getCthPath() + "/scripts/";
	}

	// Setup
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// The volume buttons should change the media volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Make sure that external media is mounted.
		if (Files.canAccessExternalStorage()) {

			final SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());

			PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

			config = Configuration.loadFromPreferences(this, preferences);

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
				Dialog recentChangesDialog = DialogFactory
						.createRecentChangesDialog(this);
				recentChangesDialog
						.setOnDismissListener(new OnDismissListener() {

							@Override
							public void onDismiss(DialogInterface arg0) {
								installFiles(preferences);
							}

						});
				recentChangesDialog.show();

			} else {
				loadApplication();
			}

		} else {
			Log.e(getClass().getSimpleName(), "Can't get storage.");

			// Create an alert dialog warning that external storage isn't
			// mounted. The application will have to exit at this point.

			DialogFactory.createExternalStorageDialog(this, true).show();
		}
	}

	private void installFiles(final SharedPreferences preferences) {
		final ProgressDialog dialog = new ProgressDialog(this);
		final UnzipTask unzipTask = new UnzipTask(config.getCthPath()
				+ "/scripts/") {

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
					Log.d(getClass().getSimpleName(), "Error copying files.");
					BugSenseHandler.log("File", error);
				}

				Editor edit = preferences.edit();
				edit.putBoolean("scripts_copied", true);
				edit.putInt("last_version", currentVersion);
				edit.commit();
				dialog.hide();
				loadApplication();
			}

		};

		AsyncTask<String, Void, AsyncTaskResult<File>> copyTask = new AsyncTask<String, Void, AsyncTaskResult<File>>() {

			@Override
			protected AsyncTaskResult<File> doInBackground(String... params) {

				try {
					Files.copyAsset(SDLActivity.this, params[0], params[1]);
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
					unzipTask.execute(f);
				} else {
					BugSenseHandler.log("File", result.getError());

				}
			};

		};

		copyTask.execute("game.zip", getExternalCacheDir().getAbsolutePath());

	}

	void loadApplication() {

		// Load the libraries
		System.loadLibrary("stlport_shared");
		System.loadLibrary("SDL");
		System.loadLibrary("SDL_gfx");
		System.loadLibrary("mikmod");
		System.loadLibrary("LUA");
		System.loadLibrary("AGG");
		System.loadLibrary("SDL_mixer");
		System.loadLibrary("appmain");

		try {
			config.writeToFile();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(getClass().getSimpleName(),
					"Couldn't write to configuration file");
			BugSenseHandler.log("Config", e);
		}

		File f = new File(config.getSaveGamesPath());

		if (!f.isDirectory()) {
			f.mkdirs();
		}

		// So we can call stuff from static callbacks
		mSingleton = this;

		mSurface = new SDLSurface(getApplication(), config.getDisplayWidth(),
				config.getDisplayHeight());

		FrameLayout mainLayout = (FrameLayout) getLayoutInflater().inflate(
				R.layout.game, null);

		((FrameLayout) mainLayout.findViewById(R.id.game_frame))
				.addView(mSurface);

		setContentView(mainLayout);

		SurfaceHolder holder = mSurface.getHolder();
		holder.setFixedSize(config.getDisplayWidth(), config.getDisplayHeight());

		// Use low profile mode if supported
		if (Build.VERSION.SDK_INT >= 11) {
			hideSystemUi();
		}

	}

	@TargetApi(11)
	private void hideSystemUi() {
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LOW_PROFILE);
	}

	public static void startApp() {
		// Start up the C app thread
		if (mSDLThread == null) {
			mSDLThread = new Thread(
					new SDLMain(mSingleton.config.getCthPath()), "SDLThread");
			mSDLThread.start();
		} else {
			// SDLActivity.nativeResume();
		}
	}

	// EGL functions
	public static boolean initEGL(int majorVersion, int minorVersion) {
		if (SDLActivity.mEGLDisplay == null) {
			Log.v(SDLActivity.class.getSimpleName(), "Starting up OpenGL ES "
					+ majorVersion + "." + minorVersion);

			try {
				EGL10 egl = (EGL10) EGLContext.getEGL();

				EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

				int[] version = new int[2];
				egl.eglInitialize(dpy, version);

				int EGL_OPENGL_ES_BIT = 1;
				int EGL_OPENGL_ES2_BIT = 4;
				int renderableType = 0;
				if (majorVersion == 2) {
					renderableType = EGL_OPENGL_ES2_BIT;
				} else if (majorVersion == 1) {
					renderableType = EGL_OPENGL_ES_BIT;
				}

				int[] configSpec = {
						// EGL10.EGL_DEPTH_SIZE, 16,
						EGL10.EGL_RENDERABLE_TYPE, renderableType,
						EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];
				int[] num_config = new int[1];
				if (!egl.eglChooseConfig(dpy, configSpec, configs, 1,
						num_config) || num_config[0] == 0) {
					Log.e(SDLActivity.class.getSimpleName(),
							"No EGL config available");
					return false;
				}
				EGLConfig config = configs[0];

				SDLActivity.mEGLDisplay = dpy;
				SDLActivity.mEGLConfig = config;
				SDLActivity.mGLMajor = majorVersion;
				SDLActivity.mGLMinor = minorVersion;

				SDLActivity.createEGLSurface();
			} catch (Exception e) {
				Log.v("SDL", e + "");
				for (StackTraceElement s : e.getStackTrace()) {
					Log.v("SDL", s.toString());
				}
			}
		} else
			SDLActivity.createEGLSurface();

		return true;
	}

	public static boolean createEGLContext() {
		EGL10 egl = (EGL10) EGLContext.getEGL();
		int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
		int contextAttrs[] = new int[] { EGL_CONTEXT_CLIENT_VERSION,
				SDLActivity.mGLMajor, EGL10.EGL_NONE };
		SDLActivity.mEGLContext = egl.eglCreateContext(SDLActivity.mEGLDisplay,
				SDLActivity.mEGLConfig, EGL10.EGL_NO_CONTEXT, contextAttrs);
		if (SDLActivity.mEGLContext == EGL10.EGL_NO_CONTEXT) {
			Log.e("SDL", "Couldn't create context");
			return false;
		}
		return true;
	}

	public static boolean createEGLSurface() {
		if (SDLActivity.mEGLDisplay != null && SDLActivity.mEGLConfig != null) {
			EGL10 egl = (EGL10) EGLContext.getEGL();
			if (SDLActivity.mEGLContext == null)
				createEGLContext();

			Log.v("SDL", "Creating new EGL Surface");
			EGLSurface surface = egl.eglCreateWindowSurface(
					SDLActivity.mEGLDisplay, SDLActivity.mEGLConfig,
					SDLActivity.mSurface, null);
			if (surface == EGL10.EGL_NO_SURFACE) {
				Log.e("SDL", "Couldn't create surface");
				return false;
			}

			if (!egl.eglMakeCurrent(SDLActivity.mEGLDisplay, surface, surface,
					SDLActivity.mEGLContext)) {
				Log.e("SDL",
						"Old EGL Context doesnt work, trying with a new one");
				createEGLContext();
				if (!egl.eglMakeCurrent(SDLActivity.mEGLDisplay, surface,
						surface, SDLActivity.mEGLContext)) {
					Log.e("SDL", "Failed making EGL Context current");
					return false;
				}
			}
			SDLActivity.mEGLSurface = surface;
			return true;
		}
		return false;
	}

	public static boolean createGLContext(int majorVersion, int minorVersion) {
		return initEGL(majorVersion, minorVersion);
	}

	public static void flipBuffers() {
		flipEGL();
	}

	// EGL buffer flip
	public static void flipEGL() {
		try {
			EGL10 egl = (EGL10) EGLContext.getEGL();

			egl.eglWaitNative(EGL10.EGL_CORE_NATIVE_ENGINE, null);

			// drawing here

			egl.eglWaitGL();

			egl.eglSwapBuffers(SDLActivity.mEGLDisplay, SDLActivity.mEGLSurface);

		} catch (Exception e) {
			Log.v("SDL", "flipEGL(): " + e);
			for (StackTraceElement s : e.getStackTrace()) {
				Log.v("SDL", s.toString());
			}
		}
	}

	// Events
	protected void onPause() {
		super.onPause();
		Log.d(getClass().getSimpleName(), "onPause()");
		if (wake != null && wake.isHeld()) {
			Log.d(getClass().getSimpleName(), "Releasing wakelock");
			wake.release();
		}

	}

	protected void onResume() {
		super.onResume();
		Log.d(getClass().getSimpleName(), "onResume()");

		if (config.getKeepScreenOn()) {
			Log.d(getClass().getSimpleName(), "Getting wakelock");
			PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wake = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
					"Keep Screen On Wakelock");
			wake.acquire();
		}

	}

	private void restartActivity() {
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	// Java functions called from C

	public static void setActivityTitle(String title) {
	}

	// Send a message from the SDLMain thread

	public static void sendCommand(Command command, Object data) {
		sendCommand(command.ordinal(), data);
	}

	public static void sendCommand(int cmd, int data) {
		sendCommand(cmd, Integer.valueOf(data));
	}

	public static void sendCommand(int cmd) {
		sendCommand(cmd, null);
	}

	public static void sendCommand(int cmd, Object data) {
		Message msg = mSingleton.commandHandler.obtainMessage();
		msg.arg1 = cmd;
		msg.obj = data;
		mSingleton.commandHandler.sendMessage(msg);
	}

	public static Object audioInit(int sampleRate, boolean is16Bit,
			boolean isStereo, int desiredFrames) {
		int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO
				: AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT
				: AudioFormat.ENCODING_PCM_8BIT;
		int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

		Log.v("SDL", "SDL audio: wanted " + (isStereo ? "stereo" : "mono")
				+ " " + (is16Bit ? "16-bit" : "8-bit") + " "
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

		Log.v("SDL",
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
						"SDL audio: error return from write(byte)");
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

	static class CommandHandler extends Handler {

		// Dialogs
		private SaveDialog saveDialog;
		private LoadDialog loadDialog;
		private Dialog aboutDialog;
		private MenuDialog mainMenuDialog;

		SDLActivity context;

		public CommandHandler(SDLActivity context) {
			super();
			this.context = context;

		}

		public void handleMessage(Message msg) {
			InputMethodManager mgr;
			switch (Command.values()[msg.arg1]) {
			case SHOW_ABOUT_DIALOG:
				if (aboutDialog == null) {
					aboutDialog = DialogFactory.createAboutDialog(context);
				}
				aboutDialog.show();
				break;

			case HIDE_KEYBOARD:
				mgr = (InputMethodManager) context
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(mSurface.getWindowToken(), 0);
				break;
			case SHOW_KEYBOARD:
				mgr = (InputMethodManager) context
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.showSoftInput(mSurface, InputMethodManager.SHOW_FORCED);
				break;
			case QUICK_LOAD:
				if (Files.doesFileExist(context.config.getSaveGamesPath()
						+ "/quicksave.sav")) {
					cthLoadGame("quicksave.sav");
				} else {
					Toast.makeText(context, "No quicksave to load!",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case QUICK_SAVE:
				cthSaveGame("quicksave.sav");
				break;
			case RESTART_GAME:
				cthRestartGame();
				break;

			case SHOW_LOAD_DIALOG:
				if (loadDialog == null) {
					loadDialog = new LoadDialog(context,
							context.config.getSaveGamesPath());
				}
				try {
					loadDialog.updateSaves(context);
					loadDialog.show();
				} catch (IOException e) {
					BugSenseHandler.log("Files", e);

					Toast.makeText(context, "Problem loading load dialog",
							Toast.LENGTH_SHORT).show();

				}
				break;

			case SHOW_SAVE_DIALOG:
				if (saveDialog == null) {
					saveDialog = new SaveDialog(context,
							context.config.getSaveGamesPath());
				}
				try {
					saveDialog.updateSaves(context);
					saveDialog.show();
				} catch (IOException e) {
					BugSenseHandler.log("Files", e);
					Toast.makeText(context, "Problem loading save dialog",
							Toast.LENGTH_SHORT).show();
				}

				break;
			case SHOW_MENU:
				if (mainMenuDialog == null) {
					mainMenuDialog = new MenuDialog(context);
				}

				// Pause the game
				cthGameSpeed(0);
				mainMenuDialog.show();

				break;
			case PAUSE_GAME:
				cthGameSpeed(0);
				break;
			case SHOW_SETTINGS_DIALOG:
				context.startActivity(new Intent(context, PrefsActivity.class));
				break;
			case GAME_SPEED_UPDATED:
				context.config.setGameSpeed((Integer) msg.obj);
				break;
			default:
				break;
			}
		}

	}

}

/**
 * Simple nativeInit() runnable
 */
class SDLMain implements Runnable {
	private String logPath;

	public SDLMain(String logPath) {
		this.logPath = logPath;

	}

	public void run() {
		// Runs SDL_main()
		SDLActivity.nativeInit(logPath);

		Log.v(getClass().getSimpleName(), "SDL thread terminated");
	}
}
