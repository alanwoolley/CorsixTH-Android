/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import uk.co.armedpineapple.corsixth.gestures.LongPressGesture;
import uk.co.armedpineapple.corsixth.gestures.TwoFingerMoveGesture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.bugsense.trace.BugSenseHandler;

/**
 * SDLSurface. This is what we draw on, so we need to know when it's created in
 * order to do anything useful.
 * 
 * Because of this, that's where we set up the SDL thread
 */
public class SDLSurface extends SurfaceView implements SurfaceHolder.Callback,
		View.OnKeyListener, View.OnTouchListener, SensorEventListener {

	// This is what SDL runs in. It invokes SDL_main(), eventually
	private Thread mSDLThread;

	public int width;
	public int height;

	// EGL private objects
	// private EGLContext mEGLContext;
	private EGLSurface mEGLSurface;
	private EGLDisplay mEGLDisplay;

	// Sensors
	private static SensorManager mSensorManager;

	private GestureDetector longPressGestureDetector;
	private ScaleGestureDetector moveGestureDetector;

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
		moveGestureDetector = new ScaleGestureDetector(context,
				new TwoFingerMoveGesture());
		longPressGestureDetector = new GestureDetector(context,
				new LongPressGesture());
		longPressGestureDetector.setIsLongpressEnabled(true);

		mSensorManager = (SensorManager) context.getSystemService("sensor");

	}

	// Called when we have a valid drawing surface
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(getClass().getSimpleName(), "surfaceCreated()");

		enableSensor(Sensor.TYPE_ACCELEROMETER, true);
	}

	// Called when we lose the surface
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(getClass().getSimpleName(), "surfaceDestroyed()");

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
		}

		enableSensor(Sensor.TYPE_ACCELEROMETER, false);

	}

	// Called when the surface is resized
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(getClass().getSimpleName(), "surfaceChanged()");

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
		Log.v(getClass().getSimpleName(), "Starting up OpenGL ES "
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
				Log.e(getClass().getSimpleName(), "No EGL config available");
				return false;
			}
			EGLConfig config = configs[0];

			EGLContext ctx = egl.eglCreateContext(dpy, config,
					EGL10.EGL_NO_CONTEXT, null);
			if (ctx == EGL10.EGL_NO_CONTEXT) {
				Log.e(getClass().getSimpleName(), "Couldn't create context");
				return false;
			}

			EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, this,
					null);
			if (surface == EGL10.EGL_NO_SURFACE) {
				Log.e(getClass().getSimpleName(), "Couldn't create surface");
				return false;
			}

			if (!egl.eglMakeCurrent(dpy, surface, surface, ctx)) {
				Log.e(getClass().getSimpleName(),
						"Couldn't make context current");
				return false;
			}

			// mEGLContext = ctx;
			mEGLDisplay = dpy;
			mEGLSurface = surface;

		} catch (Exception e) {
			Log.v(getClass().getSimpleName(), e + "");
			BugSenseHandler.log("SDL", e);
			for (StackTraceElement s : e.getStackTrace()) {
				Log.v(getClass().getSimpleName(), s.toString());
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

			// TODO - ICS seems to fail at this point with invalid argument
			// (null).
			egl.eglSwapBuffers(mEGLDisplay, mEGLSurface);

		} catch (Exception e) {
			Log.v(getClass().getSimpleName(), "flipEGL(): " + e);
			BugSenseHandler.log("SDL", e);
			for (StackTraceElement s : e.getStackTrace()) {
				Log.v(getClass().getSimpleName(), s.toString());
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
			SDLActivity.onNativeKeyDown(keyCode);
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			SDLActivity.onNativeKeyUp(keyCode);
			return true;
		}
		return false;

	}

	/**
	 * Triggered when the screen is touched. We need to convert the location
	 * pressed on the display into a location on the surface. As this is not
	 * always 1:1 (when not using native resolution), we need to do a bit of
	 * maths.
	 */
	public boolean onTouch(View v, MotionEvent event) {
		// Forward event to the gesture detector.
		longPressGestureDetector.onTouchEvent(event);
		moveGestureDetector.onTouchEvent(event);

		int action = event.getAction();

		float[] coords = translateCoords(event.getX(), event.getY());
		float p = event.getPressure();
		int pc = event.getPointerCount();

		// TODO: Anything else we need to pass?
		SDLActivity.onNativeTouch(action, coords[0], coords[1], p, pc, 0);

		return true;
	}

	public float[] translateCoords(float x, float y) {
		float newX = ((float) this.width / getWidth()) * x;
		float newY = ((float) this.height / getHeight()) * y;
		return new float[] { newX, newY };
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