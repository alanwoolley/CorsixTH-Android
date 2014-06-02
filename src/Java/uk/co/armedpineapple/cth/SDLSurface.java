/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth;

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
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.samsung.spen.lib.input.SPenEventLibrary;

import uk.co.armedpineapple.cth.gestures.LongPressGesture;
import uk.co.armedpineapple.cth.gestures.TwoFingerGestureDetector;
import uk.co.armedpineapple.cth.gestures.TwoFingerMoveGesture;
import uk.co.armedpineapple.cth.spen.SamsungSPenUtils;

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
    protected static SensorManager mSensorManager;
    protected static Display       mDisplay;

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
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(this);
        setOnTouchListener(this);

        mDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);


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
        holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);

		// enableSensor(Sensor.TYPE_ACCELEROMETER, true);
		if (context.app.configuration.getSpen()) {
			Log.d(LOG_TAG, "S Pen support enabled");
			SamsungSPenUtils.registerListeners();
		}
	}

	// Called when we lose the surface
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(LOG_TAG, "surfaceDestroyed()");

        // Call this *before* setting mIsSurfaceReady to 'false'
        SDLActivity.handlePause();
        SDLActivity.mIsSurfaceReady = false;
        SDLActivity.onNativeSurfaceDestroyed();

	}

    // Called when the surface is resized
    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height) {
        Log.v("SDL", "surfaceChanged()");

        int sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565 by default
        switch (format) {
            case PixelFormat.A_8:
                Log.v("SDL", "pixel format A_8");
                break;
            case PixelFormat.LA_88:
                Log.v("SDL", "pixel format LA_88");
                break;
            case PixelFormat.L_8:
                Log.v("SDL", "pixel format L_8");
                break;
            case PixelFormat.RGBA_4444:
                Log.v("SDL", "pixel format RGBA_4444");
                sdlFormat = 0x15421002; // SDL_PIXELFORMAT_RGBA4444
                break;
            case PixelFormat.RGBA_5551:
                Log.v("SDL", "pixel format RGBA_5551");
                sdlFormat = 0x15441002; // SDL_PIXELFORMAT_RGBA5551
                break;
            case PixelFormat.RGBA_8888:
                Log.v("SDL", "pixel format RGBA_8888");
                sdlFormat = 0x16462004; // SDL_PIXELFORMAT_RGBA8888
                break;
            case PixelFormat.RGBX_8888:
                Log.v("SDL", "pixel format RGBX_8888");
                sdlFormat = 0x16261804; // SDL_PIXELFORMAT_RGBX8888
                break;
            case PixelFormat.RGB_332:
                Log.v("SDL", "pixel format RGB_332");
                sdlFormat = 0x14110801; // SDL_PIXELFORMAT_RGB332
                break;
            case PixelFormat.RGB_565:
                Log.v("SDL", "pixel format RGB_565");
                sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565
                break;
            case PixelFormat.RGB_888:
                Log.v("SDL", "pixel format RGB_888");
                // Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
                sdlFormat = 0x16161804; // SDL_PIXELFORMAT_RGB888
                break;
            default:
                Log.v("SDL", "pixel format unknown " + format);
                break;
        }

       // this.width = width;
        //this.height = height;
        SDLActivity.onNativeResize(width, height, sdlFormat);
        Log.v("SDL", "Window size:" + width + "x"+height);

        // Set mIsSurfaceReady to 'true' *before* making a call to handleResume
        SDLActivity.mIsSurfaceReady = true;
        SDLActivity.onNativeSurfaceChanged();


        if (SDLActivity.mSDLThread == null) {
            // This is the entry point to the C app.
            // Start up the C app thread and enable sensor input for the first time
            Log.d(LOG_TAG, "Starting up SDLThread");
            SDLActivity.mSDLThread = new Thread(new SDLMain(context.app.configuration, ""), "SDLThread");
            enableSensor(Sensor.TYPE_ACCELEROMETER, true);
            SDLActivity.mSDLThread.start();

            // Set up a listener thread to catch when the native thread ends
            new Thread(new Runnable(){
                @Override
                public void run(){
                    try {
                        SDLActivity.mSDLThread.join();
                    }
                    catch(Exception e){}
                    finally{
                        // Native thread has finished
                        if (! SDLActivity.mExitCalledFromJava) {
                            SDLActivity.handleNativeExit();
                        }
                    }
                }
            }).start();
        }
    }

	// unused
	public void onDraw(Canvas canvas) {
	}

	// Key events
	public boolean onKey(View v, int keyCode, KeyEvent event) {

        // TODO - handle pad input here. Fallback to keyboard otherwise.
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
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(sensortype),
                    SensorManager.SENSOR_DELAY_GAME, null);
        } else {
            mSensorManager.unregisterListener(this,
                    mSensorManager.getDefaultSensor(sensortype));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x, y;
            switch (mDisplay.getRotation()) {
                case Surface.ROTATION_90:
                    x = -event.values[1];
                    y = event.values[0];
                    break;
                case Surface.ROTATION_270:
                    x = event.values[1];
                    y = -event.values[0];
                    break;
                case Surface.ROTATION_180:
                    x = -event.values[1];
                    y = -event.values[0];
                    break;
                default:
                    x = event.values[0];
                    y = event.values[1];
                    break;
            }
            SDLActivity.onNativeAccel(-x / SensorManager.GRAVITY_EARTH,
                    y / SensorManager.GRAVITY_EARTH,
                    event.values[2] / SensorManager.GRAVITY_EARTH - 1);
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