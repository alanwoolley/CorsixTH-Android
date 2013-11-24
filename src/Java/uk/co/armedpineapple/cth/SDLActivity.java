/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
// $codepro.audit.disable disallowNativeMethods
package uk.co.armedpineapple.cth;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import uk.co.armedpineapple.cth.CommandHandler.Command;
import uk.co.armedpineapple.cth.Files.StorageUnavailableException;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.Files.FileDetails;
import uk.co.armedpineapple.cth.Files.UnzipTask;
import uk.co.armedpineapple.cth.dialogs.DialogFactory;

import com.bugsense.trace.BugSenseHandler;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.media.*;

public class SDLActivity extends CTHActivity {

	public static final String	LOG_TAG					= "SDLActivity";

	private int									currentVersion;
	private Properties					properties;
	private WakeLock						wake;
	private boolean							hasGameLoaded		= false;

	// This is what SDL runs in. It invokes SDL_main(), eventually
	private static Thread				mSDLThread;

	// EGL private objects
	private static EGLContext		mEGLContext;
	private static EGLSurface		mEGLSurface;
	private static EGLDisplay		mEGLDisplay;
	private static EGLConfig		mEGLConfig;
	private static int					mGLMajor, mGLMinor;

	// Main components
	public static SDLActivity		mSingleton;
	public static SDLSurface		mSurface;

	// Audio
	private static Thread				mAudioThread;
	private static AudioTrack		mAudioTrack;
	private static Object				audioBuffer;

	// Menu Drawer
	DrawerLayout								mDrawerLayout;
	private ListView						mDrawerList;

	private static final String	ENGINE_ZIP_FILE	= "game.zip";

	// Handler for the messages
	public CommandHandler				commandHandler	= new CommandHandler(this);

	// C functions we call
	public static native void nativeInit(Configuration config, String toLoad);

	public static native void nativeQuit();

	public static native void onNativeResize(int x, int y, int format);

	public static native void onNativeKeyDown(int keycode);

	public static native void onNativeKeyUp(int keycode);

	public static native void onNativeTouch(int touchDevId, int pointerFingerId,
			int action, float x, float y, float p, int pc, int gestureTriggered,
			int controlsMode);

	public static native void onNativeAccel(float x, float y, float z);

	public static native void onNativeHover(float x, float y);

	public static native void onSpenButton();

	public static native void onNativeLowMemory();

	public static native void nativeRunAudioThread();

	public static native void cthRestartGame();

	public static native void cthSaveGame(String path);

	public static native void cthLoadGame(String path);

	public static native void cthGameSpeed(int speed);

	public static native void cthTryAutoSave(String filename);

	public static native void cthUpdateConfiguration(Configuration config);

	public static String nativeGetGamePath() {
		return mSingleton.app.configuration.getCthPath() + "/scripts/";
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// The volume buttons should change the media volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Make sure that external media is mounted.
		if (Files.canAccessExternalStorage()) {

			final SharedPreferences preferences = app.getPreferences();

			if (app.configuration == null) {
				try {
					app.configuration = Configuration.loadFromPreferences(this,
							preferences);
				} catch (StorageUnavailableException e) {
					Log.e(LOG_TAG, "Can't get storage.");

					// Create an alert dialog warning that external storage isn't
					// mounted. The application will have to exit at this point.

					DialogFactory.createExternalStorageWarningDialog(this, true).show();
				}
			}

			currentVersion = preferences.getInt("last_version", 0) - 1;

			try {
				currentVersion = (getPackageManager().getPackageInfo(getPackageName(),
						0).versionCode);

			} catch (NameNotFoundException e) {
				BugSenseHandler.sendException(e);
			}

			// Check to see if the game files have been copied yet, or whether the
			// application has been updated
			if (!preferences.getBoolean("scripts_copied", false)
					|| preferences.getInt("last_version", 0) < currentVersion) {

				Log.d(LOG_TAG, "This is a new installation");

				// Show the recent changes dialog
				Dialog recentChangesDialog = DialogFactory
						.createRecentChangesDialog(this);
				recentChangesDialog.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface arg0) {
						installFiles(preferences);
					}

				});
				recentChangesDialog.show();

			} else {

				// Continue to load the application otherwise
				loadApplication();
			}

		} else {
			Log.e(LOG_TAG, "Can't get storage.");

			// Create an alert dialog warning that external storage isn't
			// mounted. The application will have to exit at this point.

			DialogFactory.createExternalStorageWarningDialog(this, true).show();
		}
	}

	private void installFiles(final SharedPreferences preferences) {
		final ProgressDialog dialog = new ProgressDialog(this);
		final UnzipTask unzipTask = new UnzipTask(app.configuration.getCthPath()
				+ "/scripts/", this) {

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
				dialog.setMax(values[1]);
			}

			@Override
			protected void onPostExecute(AsyncTaskResult<String> result) {
				super.onPostExecute(result);
				Exception error;
				if ((error = result.getError()) != null) {
					Log.d(LOG_TAG, "Error copying files.");
					BugSenseHandler.sendException(error);
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
				return new AsyncTaskResult<File>(new File(params[1] + "/" + params[0]));
			}

			@Override
			protected void onPostExecute(AsyncTaskResult<File> result) {
				super.onPostExecute(result);
				File f;
				if ((f = result.getResult()) != null) {
					unzipTask.execute(f);
				} else {
					BugSenseHandler.sendException(result.getError());

				}
			}

		};

		if (Files.canAccessExternalStorage()) {

			copyTask
					.execute(ENGINE_ZIP_FILE, getExternalCacheDir().getAbsolutePath());
		} else {
			DialogFactory.createExternalStorageWarningDialog(this, true).show();
		}
	}

	void loadApplication() {

		// Load the libraries
		System.loadLibrary("SDL");
		System.loadLibrary("LUA");
		System.loadLibrary("SDL_mixer");
		System.loadLibrary("ffmpeg");
		System.loadLibrary("appmain");

		try {
			app.configuration.writeToFile();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Couldn't write to configuration file");
			BugSenseHandler.sendException(e);
		}

		File f = new File(app.configuration.getSaveGamesPath());

		if (!f.isDirectory()) {
			f.mkdirs();
		}

		// So we can call stuff from static callbacks
		mSingleton = this;

		hideSystemUi();

		mSurface = new SDLSurface(this, app.configuration.getDisplayWidth(),
				app.configuration.getDisplayHeight());
		mSurface.setZOrderOnTop(false);

		DrawerLayout mainLayout = (DrawerLayout) getLayoutInflater().inflate(
				R.layout.game, null);
		FrameLayout gameFrame = ((FrameLayout) mainLayout
				.findViewById(R.id.game_frame));

		gameFrame.addView(mSurface);
		setContentView(mainLayout);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.main_layout);
		mDrawerList = (ListView) findViewById(R.id.menu_drawer);
		mDrawerList.setAdapter(new NavDrawerAdapter(this,
				uk.co.armedpineapple.cth.MenuItems.getItems()));
		mDrawerList.setOnItemClickListener(new NavDrawerListListener(this));
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		mDrawerLayout.setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerClosed(View arg0) {
				// Restore game speed
				cthGameSpeed(app.configuration.getGameSpeed());
				mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			}

			@Override
			public void onDrawerOpened(View arg0) {
				// Pause the game
				cthGameSpeed(0);
			}

			@Override
			public void onDrawerSlide(View arg0, float arg1) {
				arg0.bringToFront();
				mDrawerLayout.bringChildToFront(arg0);
				mDrawerLayout.requestLayout();

			}

			@Override
			public void onDrawerStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}

		});
		SurfaceHolder holder = mSurface.getHolder();
		holder.setFixedSize(app.configuration.getDisplayWidth(),
				app.configuration.getDisplayHeight());

		gameFrame.setVisibility(View.VISIBLE);

		hasGameLoaded = true;

	}

	@SuppressLint("NewApi")
	public void hideSystemUi() {
		if (Build.VERSION.SDK_INT >= 19) {

			// Hide the navigation buttons if supported
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
							| View.SYSTEM_UI_FLAG_FULLSCREEN);
		} else if (Build.VERSION.SDK_INT >= 11) {

			// Use low profile mode if supported
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LOW_PROFILE);

		}
	}

	public void startApp() {
		// Start up the C app thread

		if (mSDLThread == null) {

			List<FileDetails> saves = null;
			try {
				saves = Files.listFilesInDirectory(
						app.configuration.getSaveGamesPath(), new FilenameFilter() {

							@Override
							public boolean accept(File dir, String filename) {
								return filename.toLowerCase(Locale.US).endsWith(".sav");
							}
						});
			} catch (IOException e) {}

			if (saves != null && saves.size() > 0) {
				Collections.sort(saves, Collections.reverseOrder());

				final String loadPath = saves.get(0).getFileName();

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.load_last_save);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.yes, new Dialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						mSDLThread = new Thread(new SDLMain(app.configuration, loadPath),
								"SDLThread");
						mSDLThread.start();
					}

				});
				builder.setNegativeButton(R.string.no, new Dialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mSDLThread = new Thread(new SDLMain(app.configuration, ""),
								"SDLThread");
						mSDLThread.start();
					}

				});
				builder.create().show();

			} else {

				mSDLThread = new Thread(new SDLMain(app.configuration, ""), "SDLThread");
				mSDLThread.start();
			}

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
						EGL10.EGL_RENDERABLE_TYPE, renderableType, EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];
				int[] num_config = new int[1];
				if (!egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config)
						|| num_config[0] == 0) {
					Log.e(SDLActivity.class.getSimpleName(), "No EGL config available");
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
			EGLSurface surface = egl.eglCreateWindowSurface(SDLActivity.mEGLDisplay,
					SDLActivity.mEGLConfig, SDLActivity.mSurface, null);
			if (surface == EGL10.EGL_NO_SURFACE) {
				Log.e("SDL", "Couldn't create surface");
				return false;
			}

			if (!egl.eglMakeCurrent(SDLActivity.mEGLDisplay, surface, surface,
					SDLActivity.mEGLContext)) {
				Log.e("SDL", "Old EGL Context doesnt work, trying with a new one");
				createEGLContext();
				if (!egl.eglMakeCurrent(SDLActivity.mEGLDisplay, surface, surface,
						SDLActivity.mEGLContext)) {
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
		Log.d(LOG_TAG, "onPause()");

		// Attempt to autosave.
		if (hasGameLoaded) {
			// Reset the game speed back to normal
			cthGameSpeed(app.configuration.getGameSpeed());

			cthTryAutoSave("cthAndroidAutoSave.sav");
		}

		if (wake != null && wake.isHeld()) {
			Log.d(LOG_TAG, "Releasing wakelock");
			wake.release();
		}

	}

	protected void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume()");

		if (app.configuration != null && app.configuration.getKeepScreenOn()) {
			Log.d(LOG_TAG, "Getting wakelock");
			PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wake = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
					"Keep Screen On Wakelock");
			wake.acquire();
		}

		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawers();
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
		int channelConfig = isStereo ? AudioFormat.CHANNEL_OUT_STEREO
				: AudioFormat.CHANNEL_OUT_MONO;
		int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT
				: AudioFormat.ENCODING_PCM_8BIT;
		int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

		Log.v("SDL", "SDL audio: wanted " + (isStereo ? "stereo" : "mono") + " "
				+ (is16Bit ? "16-bit" : "8-bit") + " " + (sampleRate / 1000f) + "kHz, "
				+ desiredFrames + " frames buffer");

		// Let the user pick a larger buffer if they really want -- but ye
		// gods they probably shouldn't, the minimums are horrifyingly high
		// latency already
		desiredFrames = Math.max(desiredFrames,
				(AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
						+ frameSize - 1)
						/ frameSize);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				channelConfig, audioFormat, desiredFrames * frameSize,
				AudioTrack.MODE_STREAM);

		audioStartThread();

		Log.v(
				"SDL",
				"SDL audio: got "
						+ ((mAudioTrack.getChannelCount() >= 2) ? "stereo" : "mono")
						+ " "
						+ ((mAudioTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit"
								: "8-bit") + " " + (mAudioTrack.getSampleRate() / 1000f)
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
				BugSenseHandler.sendException(e);
			}
			mAudioThread = null;

		}

		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack = null;
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.w(LOG_TAG, "Low memory detected. Going to try and tighten our belt!");

		if (hasGameLoaded) {
			// Attempt to save first
			cthTryAutoSave(getString(R.string.autosave_name));
		}

		// Remove references to some stuff that can just be regenerated later, so
		// that the GC can get rid of them.
		commandHandler.cleanUp();

		// Call LUA GC
		// TODO - this is buggy.
		// onNativeLowMemory();

	}

	public static void toggleScrolling(boolean scrolling) {
		Log.d(SDLActivity.class.getSimpleName(), "Scrolling Java call: "
				+ scrolling);
		mSurface.setScrolling(scrolling);
	}
}

/**
 * Simple nativeInit() runnable
 */
class SDLMain implements Runnable {
	public static final String	LOG_TAG	= "SDLMain";

	private Configuration				config;
	private String							toLoad;

	public SDLMain(Configuration config, String toLoad) {
		this.config = config;
		this.toLoad = toLoad;

	}

	public void run() {
		// Runs SDL_main()
		SDLActivity.nativeInit(config, toLoad);

		Log.v(LOG_TAG, "SDL thread terminated");
	}
}
