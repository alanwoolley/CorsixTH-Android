package uk.co.armedpineapple.corsixth;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import javax.microedition.khronos.egl.*;

import com.bugsense.trace.BugSenseHandler;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.graphics.*;
import android.media.*;
import android.hardware.*;

/**
 * SDL Activity
 */
public class SDLActivity extends Activity {

	private String scriptsPath = "";

	private Properties properties;

	private final static int SURFACE_WIDTH = 640;
	private final static int SURFACE_HEIGHT = 480;

	// Main components
	private static SDLActivity mSingleton;
	private static SDLSurface mSurface;

	// Audio
	private static Thread mAudioThread;
	private static AudioTrack mAudioTrack;

	// Load the .so
	static {
		System.loadLibrary("SDL");
		System.loadLibrary("SDL_image");
		System.loadLibrary("mikmod");
		System.loadLibrary("LUA");
		System.loadLibrary("AGG");
		System.loadLibrary("SDL_mixer");
		// System.loadLibrary("SDL_ttf");
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

		} else {
			Log.e(getClass().getSimpleName(), "Can't get storage.");
		}

		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		/*
		 * if (preferences.getAll().size() == 0) {
		 * setDefaultPreferences(preferences); }
		 */

		if (preferences.getBoolean("customgamescripts_pref", false)) {
			scriptsPath = preferences.getString("gamescripts_pref",
					getExternalFilesDir(null).getAbsolutePath());
		} else {
			scriptsPath = getExternalFilesDir(null).getAbsolutePath();
		}

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
								scriptsPath);
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

	}

	void continueLoad() {

		updateConfigFile(PreferenceManager
				.getDefaultSharedPreferences(getBaseContext()));

		setGamePath(scriptsPath + "/scripts/");

		// So we can call stuff from static callbacks
		mSingleton = this;

		// Set up the surface
		mSurface = new SDLSurface(getApplication(), SURFACE_WIDTH,
				SURFACE_HEIGHT);

		setContentView(mSurface);
		SurfaceHolder holder = mSurface.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		holder.setFixedSize(SURFACE_WIDTH, SURFACE_HEIGHT);

	}

	void updateConfigFile(SharedPreferences preferences) {
		StringBuilder sbuilder = new StringBuilder();
		sbuilder.append("theme_hospital_install = [["
				+ preferences.getString("originalfiles_pref", "") + "]]\n");
		sbuilder.append("prevent_edge_scrolling = true\n");

		sbuilder.append("audio = "
				+ String.valueOf(preferences.getBoolean("audio_pref", true))
				+ "\n");
		sbuilder.append("audio_frequency = 22050\n");
		sbuilder.append("audio_channels = 2\n");
		sbuilder.append("audio_buffer_size = 2048\n");

		sbuilder.append("play_music = "
				+ String.valueOf(preferences.getBoolean("music_pref", true))
				+ "\n");
		sbuilder.append("music_volume = 0."
				+ preferences.getString("musicvolume_pref", "5") + "\n");

		sbuilder.append("play_announcements = "
				+ String.valueOf(preferences.getBoolean("announcer_pref", true))
				+ "\n");
		sbuilder.append("announcement_volume = 0."
				+ preferences.getString("announcervolume_pref", "5") + "\n");

		sbuilder.append("play_sounds = "
				+ String.valueOf(preferences.getBoolean("fx_pref", true))
				+ "\n");
		sbuilder.append("sound_volume = 0."
				+ preferences.getString("fxvolume_pref", "5") + "\n");

		sbuilder.append("language = [["
				+ preferences.getString("language_pref", "en") + "]]\n");

		sbuilder.append("width = 640\n");
		sbuilder.append("height = 480\n");
		sbuilder.append("fullscreen = true\n");

		sbuilder.append("debug = "
				+ String.valueOf(preferences.getBoolean("debug_pref", false))
				+ "\n");
		sbuilder.append("track_fps = false\n");

		String configFileName = scriptsPath + "/scripts/" + "config.txt";
		try {
			FileWriter writer = new FileWriter(configFileName, false);
			writer.write(sbuilder.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(getClass().getSimpleName(), "Couldn't write config file");
		}

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

/**
 * SDLSurface. This is what we draw on, so we need to know when it's created in
 * order to do anything useful.
 * 
 * Because of this, that's where we set up the SDL thread
 */
class SDLSurface extends SurfaceView implements SurfaceHolder.Callback,
		View.OnKeyListener, View.OnTouchListener, SensorEventListener {

	// This is what SDL runs in. It invokes SDL_main(), eventually
	private Thread mSDLThread;

	private int width;
	private int height;

	// EGL private objects
	private EGLContext mEGLContext;
	private EGLSurface mEGLSurface;
	private EGLDisplay mEGLDisplay;

	// Sensors
	private static SensorManager mSensorManager;

	// Startup
	public SDLSurface(Context context, int width, int height) {
		super(context);
		getHolder().addCallback(this);
		this.width = width;
		this.height = height;
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		setOnKeyListener(this);
		setOnTouchListener(this);

		mSensorManager = (SensorManager) context.getSystemService("sensor");
	}

	// Called when we have a valid drawing surface
	public void surfaceCreated(SurfaceHolder holder) {
		// Log.v("SDL", "surfaceCreated()");

		enableSensor(Sensor.TYPE_ACCELEROMETER, true);
	}

	// Called when we lose the surface
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Log.v("SDL", "surfaceDestroyed()");

		// Send a quit message to the application
		SDLActivity.nativeQuit();

		// Now wait for the SDL thread to quit
		if (mSDLThread != null) {
			try {
				mSDLThread.join();
			} catch (Exception e) {
				BugSenseHandler.log("SDL", e);
				Log.v(getClass().getSimpleName(), "Problem stopping thread: "
						+ e);
			}
			mSDLThread = null;

			// Log.v("SDL", "Finished waiting for SDL thread");
		}

		enableSensor(Sensor.TYPE_ACCELEROMETER, false);
	}

	// Called when the surface is resized
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// Log.v("SDL", "surfaceChanged()");

		int sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565 by default
		switch (format) {
		case PixelFormat.A_8:
			Log.v(SDLActivity.class.getSimpleName(), "pixel format A_8");
			break;
		case PixelFormat.LA_88:
			Log.v(SDLActivity.class.getSimpleName(), "pixel format LA_88");
			break;
		case PixelFormat.L_8:
			Log.v(SDLActivity.class.getSimpleName(), "pixel format L_8");
			break;
		case PixelFormat.RGBA_4444:
			Log.v(SDLActivity.class.getSimpleName(), "pixel format RGBA_4444");
			sdlFormat = 0x85421002; // SDL_PIXELFORMAT_RGBA4444
			break;
		case PixelFormat.RGBA_5551:
			Log.v(SDLActivity.class.getSimpleName(), "pixel format RGBA_5551");
			sdlFormat = 0x85441002; // SDL_PIXELFORMAT_RGBA5551
			break;
		case PixelFormat.RGBA_8888:
			Log.v(SDLActivity.class.getSimpleName(), "pixel format RGBA_8888");
			sdlFormat = 0x86462004; // SDL_PIXELFORMAT_RGBA8888
			break;
		case PixelFormat.RGBX_8888:
			Log.v(SDLActivity.class.getSimpleName(), "pixel format RGBX_8888");
			sdlFormat = 0x86262004; // SDL_PIXELFORMAT_RGBX8888
			break;
		case PixelFormat.RGB_332:
			Log.v(SDLActivity.class.getSimpleName(), "pixel format RGB_332");
			sdlFormat = 0x84110801; // SDL_PIXELFORMAT_RGB332
			break;
		case PixelFormat.RGB_565:
			Log.v(SDLActivity.class.getSimpleName(), "pixel format RGB_565");
			sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565
			break;
		case PixelFormat.RGB_888:
			Log.v(SDLActivity.class.getSimpleName(), "pixel format RGB_888");
			// Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
			sdlFormat = 0x86161804; // SDL_PIXELFORMAT_RGB888
			break;
		default:
			Log.v(SDLActivity.class.getSimpleName(), "pixel format unknown "
					+ format);
			break;
		}
		SDLActivity.onNativeResize(width, height, sdlFormat);

		// Now start up the C app thread
		if (mSDLThread == null) {
			mSDLThread = new Thread(new SDLMain(), "SDLThread");
			mSDLThread.start();
		}
	}

	// unused
	public void onDraw(Canvas canvas) {
	}

	// EGL functions
	public boolean initEGL(int majorVersion, int minorVersion) {
		Log.v("SDL", "Starting up OpenGL ES " + majorVersion + "."
				+ minorVersion);

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
				Log.e("SDL", "No EGL config available");
				return false;
			}
			EGLConfig config = configs[0];

			EGLContext ctx = egl.eglCreateContext(dpy, config,
					EGL10.EGL_NO_CONTEXT, null);
			if (ctx == EGL10.EGL_NO_CONTEXT) {
				Log.e("SDL", "Couldn't create context");
				return false;
			}

			EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, this,
					null);
			if (surface == EGL10.EGL_NO_SURFACE) {
				Log.e("SDL", "Couldn't create surface");
				return false;
			}

			if (!egl.eglMakeCurrent(dpy, surface, surface, ctx)) {
				Log.e("SDL", "Couldn't make context current");
				return false;
			}

			mEGLContext = ctx;
			mEGLDisplay = dpy;
			mEGLSurface = surface;

		} catch (Exception e) {
			Log.v("SDL", e + "");
			BugSenseHandler.log("SDL", e);
			for (StackTraceElement s : e.getStackTrace()) {
				Log.v("SDL", s.toString());
			}
		}

		return true;
	}

	// EGL buffer flip
	public void flipEGL() {
		try {
			EGL10 egl = (EGL10) EGLContext.getEGL();

			egl.eglWaitNative(EGL10.EGL_NATIVE_RENDERABLE, null);

			// drawing here

			egl.eglWaitGL();

			egl.eglSwapBuffers(mEGLDisplay, mEGLSurface);

		} catch (Exception e) {
			Log.v("SDL", "flipEGL(): " + e);
			BugSenseHandler.log("SDL", e);
			for (StackTraceElement s : e.getStackTrace()) {
				Log.v("SDL", s.toString());
			}
		}
	}

	// Key events
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			return false;
		case KeyEvent.KEYCODE_VOLUME_UP:
			return false;
		case KeyEvent.KEYCODE_VOLUME_MUTE:
			return false;
		case KeyEvent.KEYCODE_MENU:
			return false;
		default:
			break;
		}
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			Log.v("SDL", "key down: " + keyCode);
			SDLActivity.onNativeKeyDown(keyCode);
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			Log.v("SDL", "key up: " + keyCode);
			SDLActivity.onNativeKeyUp(keyCode);
			return true;
		}
		return false;

	}

	// Touch events
	public boolean onTouch(View v, MotionEvent event) {

		int action = event.getAction();
		Log.d(getClass().getSimpleName(), "Surface dimensions: " + this.width
				+ " x " + this.height);
		Log.d(getClass().getSimpleName(), "View dimensions: " + v.getWidth()
				+ " x " + v.getHeight());
		float x = ((float) this.width / v.getWidth()) * event.getX();
		float y = ((float) this.height / v.getHeight()) * event.getY();
		Log.d(getClass().getSimpleName(), "Touching at: " + x + " x " + y);
		float p = event.getPressure();
		int pc = event.getPointerCount();
		// Log.d(getClass().getSimpleName(), "Sending action: " + action +
		// ", x: " + x + ", y: " + y + ", p: " + p + ", pc: " + pc + " to SDL");
		// TODO: Anything else we need to pass?
		SDLActivity.onNativeTouch(action, x, y, p, pc);

		return true;
	}

	// Sensor events
	public void enableSensor(int sensortype, boolean enabled) {
		// TODO: This uses getDefaultSensor - what if we have >1 accels?
		if (enabled) {
			mSensorManager.registerListener(this,
					mSensorManager.getDefaultSensor(sensortype),
					SensorManager.SENSOR_DELAY_GAME, null);
		} else {
			mSensorManager.unregisterListener(this,
					mSensorManager.getDefaultSensor(sensortype));
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			SDLActivity.onNativeAccel(event.values[0], event.values[1],
					event.values[2]);
		}
	}

}
