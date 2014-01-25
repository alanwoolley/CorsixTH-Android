/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth;

import com.samsung.spen.lib.input.SPenEventLibrary;
import uk.co.armedpineapple.cth.gestures.LongPressGesture;
import uk.co.armedpineapple.cth.gestures.TwoFingerGestureDetector;
import uk.co.armedpineapple.cth.gestures.TwoFingerMoveGesture;
import uk.co.armedpineapple.cth.spen.SamsungSPenUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
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

    public static final String LOG_TAG = "SDLSurface";

    private static final int GESTURE_LONGPRESS = 1;
    private static final int GESTURE_MOVE      = 2;

    private boolean scrolling        = false;
    private boolean inMiddleOfScroll = false;
    public final int width;
    public final int height;

    // Sensors
    private static SensorManager mSensorManager;

    private final GestureDetector          longPressGestureDetector;
    private final TwoFingerGestureDetector moveGestureDetector;

    private final SDLActivity context;

    // Startup
    @SuppressLint("NewApi")
    public SDLSurface(final SDLActivity context, int width, int height) {
        super(context);
        SPenEventLibrary sPenEventLibrary = new SPenEventLibrary();
        this.context = context;
        getHolder().addCallback(this);
        this.width = width;
        this.height = height;
        // setFocusable(true);
        // setFocusableInTouchMode(true);
        // requestFocus();
        setOnKeyListener(this);
        setOnTouchListener(this);
        moveGestureDetector = new TwoFingerGestureDetector(context,
                new TwoFingerMoveGesture(context));
        longPressGestureDetector = new GestureDetector(context,
                new LongPressGesture(context));
        longPressGestureDetector.setIsLongpressEnabled(true);

        if (Build.VERSION.SDK_INT >= 14) {
            setOnGenericMotionListener(new OnGenericMotionListener() {

                @Override
                public boolean onGenericMotion(View v, MotionEvent event) {
                    Log.d(LOG_TAG, event.toString());
                    if (context.app.configuration.getControlsMode() == Configuration.CONTROLS_DESKTOP) {
                        int actionPointerIndex = event.getActionIndex();
                        float[] coords = translateCoords(event.getX(actionPointerIndex),
                                event.getY(actionPointerIndex));
                        SDLActivity.onNativeHover(coords[0], coords[1]);

                        return true;
                    }

                    return false;

                }

            });
        }

	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {

		super.onWindowFocusChanged(hasWindowFocus);
		Log.d(LOG_TAG, "focus changed");
		context.hideSystemUi();
	}

	// Called when we have a valid drawing surface
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(LOG_TAG, "surfaceCreated()");

		SDLActivity.createEGLSurface();

		// enableSensor(Sensor.TYPE_ACCELEROMETER, true);
		if (context.app.configuration.getSpen()) {
			Log.d(LOG_TAG, "S Pen support enabled");
			SamsungSPenUtils.registerListeners();
		}
	}

	// Called when we lose the surface
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(LOG_TAG, "surfaceDestroyed()");

		// Send a quit message to the application
		// SDLActivity.nativePause();
		SDLActivity.nativeQuit();

		// enableSensor(Sensor.TYPE_ACCELEROMETER, false);

	}

	// Called when the surface is resized
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(LOG_TAG, "surfaceChanged()");

		int sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565 by default
		switch (format) {
			case PixelFormat.A_8:
				Log.v(LOG_TAG, "pixel format A_8");
				break;
			case PixelFormat.L_8:
				Log.v(LOG_TAG, "pixel format L_8");
				break;
			case PixelFormat.RGBA_8888:
				Log.v(LOG_TAG, "pixel format RGBA_8888");
				sdlFormat = 0x86462004; // SDL_PIXELFORMAT_RGBA8888
				break;
			case PixelFormat.RGBX_8888:
				Log.v(LOG_TAG, "pixel format RGBX_8888");
				sdlFormat = 0x86262004; // SDL_PIXELFORMAT_RGBX8888
				break;
			case PixelFormat.RGB_565:
				Log.v(LOG_TAG, "pixel format RGB_565");
				sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565
				break;
			case PixelFormat.RGB_888:
				Log.v(LOG_TAG, "pixel format RGB_888");
				// Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
				sdlFormat = 0x86161804; // SDL_PIXELFORMAT_RGB888
				break;
			default:
				Log.v(LOG_TAG, "pixel format unknown " + format);
				break;
		}

		SDLActivity.onNativeResize(width, height, 0x86462004);

		context.startApp();

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
	@SuppressLint("NewApi")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int controlsMode = context.app.configuration.getControlsMode();
		boolean spenEnabled = context.app.configuration.getSpen();
		int actionPointerIndex = event.getActionIndex();
		float[] coords = translateCoords(event.getX(actionPointerIndex),
				event.getY(actionPointerIndex));
		int action = event.getAction();

		// S Pen Stuff
		if (scrolling && spenEnabled) {
			if (action == MotionEvent.ACTION_DOWN) {
				SDLActivity.onNativeTouch(0, 0, MotionEvent.ACTION_DOWN, coords[0],
						coords[1], 0, 2, GESTURE_MOVE,
						context.app.configuration.getControlsMode());
				inMiddleOfScroll = true;
			} else if (action == MotionEvent.ACTION_MOVE) {
				SDLActivity.onNativeTouch(0, 0, MotionEvent.ACTION_MOVE, coords[0],
						coords[1], 0, 2, GESTURE_MOVE,
						context.app.configuration.getControlsMode());
			} else if (action == MotionEvent.ACTION_UP) {
				SDLActivity.onNativeTouch(0, 0, MotionEvent.ACTION_UP, coords[0],
						coords[1], 0, 2, GESTURE_MOVE,
						context.app.configuration.getControlsMode());
				inMiddleOfScroll = false;
			}
			return true;
		}

		if (controlsMode == Configuration.CONTROLS_NORMAL) {
			// Forward event to the gesture detector.
			longPressGestureDetector.onTouchEvent(event);

			moveGestureDetector.onTouchEvent(event);
		}

		if (controlsMode == Configuration.CONTROLS_DESKTOP
				&& Build.VERSION.SDK_INT >= 14
				&& event.getToolType(actionPointerIndex) != MotionEvent.TOOL_TYPE_FINGER) {

			coords = translateCoords(event.getX(actionPointerIndex),
					event.getY(actionPointerIndex));

			SDLActivity.onNativeTouch(event.getDeviceId(), event.getButtonState(),
					event.getAction(), coords[0], coords[1], 0, 0, 0,
					context.app.configuration.getControlsMode());
			return true;
		}

		final int pointerCount = event.getPointerCount();

		if (!moveGestureDetector.isInProgress() && pointerCount < 2) {
			final int touchDevId = event.getDeviceId();
			int pointerFingerId = event.getPointerId(actionPointerIndex);
			float p = event.getPressure(actionPointerIndex);

			if (action == MotionEvent.ACTION_MOVE && pointerCount == 1) {
				// TODO send motion to every pointer if its position has
				// changed since prev event.
				for (int i = 0; i < pointerCount; i++) {
					pointerFingerId = event.getPointerId(i);
					coords = translateCoords(event.getX(actionPointerIndex),
							event.getY(actionPointerIndex));
					p = event.getPressure(i);
					SDLActivity.onNativeTouch(touchDevId, pointerFingerId, action,
							coords[0], coords[1], p, pointerCount, 0,
							Configuration.CONTROLS_NORMAL);
				}
			} else {
				SDLActivity.onNativeTouch(touchDevId, pointerFingerId, action,
						coords[0], coords[1], p, pointerCount, 0,
						Configuration.CONTROLS_NORMAL);
			}
		}
		return true;
	}

	/**
	 * Translates an x,y pair of coordinates according to the scaling factor of
	 * the surface
	 * 
	 * @return an array containing the translated coordinates
	 */
	public float[] translateCoords(float x, float y) {
		float newX = ((float) this.width / getWidth()) * x;
		float newY = ((float) this.height / getHeight()) * y;
		return new float[] { newX, newY };
	}

	// Sensor events
	public void enableSensor(int sensortype, boolean enabled) {
		// TODO: This uses getDefaultSensor - what if we have >1 accels?

		if (enabled) {
			if (mSensorManager == null) {
				mSensorManager = (SensorManager) context
						.getSystemService(Context.SENSOR_SERVICE);
			}

			mSensorManager.registerListener(this,
					mSensorManager.getDefaultSensor(sensortype),
					SensorManager.SENSOR_DELAY_GAME, null);
		} else {
			if (mSensorManager != null) {
				mSensorManager.unregisterListener(this,
						mSensorManager.getDefaultSensor(sensortype));
			}
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			SDLActivity.onNativeAccel(event.values[0] / SensorManager.GRAVITY_EARTH,
					event.values[1] / SensorManager.GRAVITY_EARTH, event.values[2]
							/ SensorManager.GRAVITY_EARTH);
		}
	}

	public void setScrolling(boolean scrolling) {
		if (!scrolling && inMiddleOfScroll) {
			SDLActivity.onNativeTouch(0, 0, MotionEvent.ACTION_UP, -1, -1, 0, 2, 2,
					context.app.configuration.getControlsMode());
			inMiddleOfScroll = false;
		}
		this.scrolling = scrolling;
	}

}