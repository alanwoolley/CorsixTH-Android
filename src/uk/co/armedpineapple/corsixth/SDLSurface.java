/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.corsixth;

import uk.co.armedpineapple.corsixth.SDLActivity.Command;
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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * SDLSurface. This is what we draw on, so we need to know when it's created in
 * order to do anything useful.
 * 
 * Because of this, that's where we set up the SDL thread
 */
public class SDLSurface extends SurfaceView implements SurfaceHolder.Callback,
		View.OnKeyListener, View.OnTouchListener, SensorEventListener {

	public int width;
	public int height;

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

		holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		SDLActivity.createEGLSurface();

		enableSensor(Sensor.TYPE_ACCELEROMETER, true);
	}

	// Called when we lose the surface
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(getClass().getSimpleName(), "surfaceDestroyed()");

		// Send a quit message to the application
		// SDLActivity.nativePause();
		SDLActivity.nativeQuit();

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

		SDLActivity.startApp();
	}

	// unused
	public void onDraw(Canvas canvas) {
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
			SDLActivity.sendCommand(Command.SHOW_MENU, null);
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
		final int pointerCount = event.getPointerCount();

		if (!moveGestureDetector.isInProgress() && pointerCount < 2) {
			final int touchDevId = event.getDeviceId();
			int actionPointerIndex = event.getActionIndex();
			int pointerFingerId = event.getPointerId(actionPointerIndex);

			int action = event.getAction();

			float[] coords = translateCoords(event.getX(actionPointerIndex),
					event.getY(actionPointerIndex));
			float p = event.getPressure(actionPointerIndex);

			if (action == MotionEvent.ACTION_MOVE && pointerCount == 1) {
				// TODO send motion to every pointer if its position has
				// changed since prev event.
				for (int i = 0; i < pointerCount; i++) {
					pointerFingerId = event.getPointerId(i);
					coords = translateCoords(event.getX(actionPointerIndex),
							event.getY(actionPointerIndex));
					p = event.getPressure(i);
					SDLActivity.onNativeTouch(touchDevId, pointerFingerId,
							action, coords[0], coords[1], p, pointerCount, 0);
				}
			} else {
				SDLActivity.onNativeTouch(touchDevId, pointerFingerId, action,
						coords[0], coords[1], p, pointerCount, 0);
			}
		}
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
			SDLActivity.onNativeAccel(event.values[0]
					/ SensorManager.GRAVITY_EARTH, event.values[1]
					/ SensorManager.GRAVITY_EARTH, event.values[2]
					/ SensorManager.GRAVITY_EARTH);
		}
	}
}