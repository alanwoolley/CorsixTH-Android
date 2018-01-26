/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import uk.co.armedpineapple.cth.spen.SamsungSPenUtils;

/**
 * SDLSurface. This is what we draw on, so we need to know when it's created in
 * order to do anything useful.
 * 
 * Because of this, that's where we set up the SDL thread
 */
public class SDLSurface extends SurfaceView implements SurfaceHolder.Callback,
		View.OnKeyListener, View.OnTouchListener, SensorEventListener {

    public static final Reporting.Logger Log = Reporting.getLogger("SDLSurface");

    private static final int GESTURE_LONGPRESS = 1;
    private static final int GESTURE_MOVE      = 2;

    private boolean scrolling        = false;
    private boolean inMiddleOfScroll = false;
    public final int width;
    public final int height;

    // Sensors
    protected static SensorManager mSensorManager;
    protected static Display       mDisplay;

    private final SDLActivity context;

    // Startup
    @SuppressLint("NewApi")
    public SDLSurface(final SDLActivity context, int width, int height) {
        super(context);
        this.context = context;
        this.width = width;
        this.height = height;
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        getHolder().addCallback(this);

        setOnKeyListener(this);
        setOnTouchListener(this);

        mDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        setOnGenericMotionListener(new OnGenericMotionListener() {

            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                if (context.getApp().getConfiguration().getControlsMode() == Configuration.CONTROLS_DESKTOP) {
                    int actionPointerIndex = event.getActionIndex();
                    float[] coords = translateCoords(event.getX(actionPointerIndex),
                            event.getY(actionPointerIndex));
                    SDLActivity.onNativeMouse(0, MotionEvent.ACTION_HOVER_MOVE, coords[0], coords[1]);
                    return true;
                }

                return false;
            }
        });
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {

		super.onWindowFocusChanged(hasWindowFocus);
		Log.d("focus changed");
		context.hideSystemUi();
	}

	// Called when we have a valid drawing surface
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v("surfaceCreated()");
        holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);

		// enableSensor(Sensor.TYPE_ACCELEROMETER, true);
		if (context.getApp().getConfiguration().getSpen()) {
			Log.d("S Pen support enabled");
            Reporting.setBool("spen", true);
			SamsungSPenUtils.registerListeners();
		}
	}

	// Called when we lose the surface
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v("surfaceDestroyed()");

        // Call this *before* setting mIsSurfaceReady to 'false'
        SDLActivity.handlePause();
        SDLActivity.mIsSurfaceReady = false;
        SDLActivity.onNativeSurfaceDestroyed();

	}

    // Called when the surface is resized
    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height) {
        Log.v("surfaceChanged()");

        int sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565 by default
        switch (format) {
            case PixelFormat.A_8:
                Log.v("pixel format A_8");
                break;
            case PixelFormat.LA_88:
                Log.v("pixel format LA_88");
                break;
            case PixelFormat.L_8:
                Log.v("pixel format L_8");
                break;
            case PixelFormat.RGBA_4444:
                Log.v("pixel format RGBA_4444");
                sdlFormat = 0x15421002; // SDL_PIXELFORMAT_RGBA4444
                break;
            case PixelFormat.RGBA_5551:
                Log.v("pixel format RGBA_5551");
                sdlFormat = 0x15441002; // SDL_PIXELFORMAT_RGBA5551
                break;
            case PixelFormat.RGBA_8888:
                Log.v("pixel format RGBA_8888");
                sdlFormat = 0x16462004; // SDL_PIXELFORMAT_RGBA8888
                break;
            case PixelFormat.RGBX_8888:
                Log.v("pixel format RGBX_8888");
                sdlFormat = 0x16261804; // SDL_PIXELFORMAT_RGBX8888
                break;
            case PixelFormat.RGB_332:
                Log.v("pixel format RGB_332");
                sdlFormat = 0x14110801; // SDL_PIXELFORMAT_RGB332
                break;
            case PixelFormat.RGB_565:
                Log.v("pixel format RGB_565");
                sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565
                break;
            case PixelFormat.RGB_888:
                Log.v("pixel format RGB_888");
                // Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
                sdlFormat = 0x16161804; // SDL_PIXELFORMAT_RGB888
                break;
            default:
                Log.v("pixel format unknown " + format);
                break;
        }

       // this.width = width;
        //this.height = height;
        SDLActivity.onNativeResize(width, height, sdlFormat);
        Log.v("Window size:" + width + "x"+height);

        // Set mIsSurfaceReady to 'true' *before* making a call to handleResume
        SDLActivity.mIsSurfaceReady = true;
        SDLActivity.onNativeSurfaceChanged();


        if (SDLActivity.mSDLThread == null) {
            // This is the entry point to the C app.
            // Start up the C app thread and enable sensor input for the first time
            Log.d("Starting up SDLThread");
            SDLActivity.mSDLThread = new Thread(new SDLMain(
                    context.getApp().getConfiguration(), ""), "SDLThread");
            enableSensor(Sensor.TYPE_ACCELEROMETER, true);
            SDLActivity.mSDLThread.start();

            // Set up a listener thread to catch when the native thread ends
            new Thread(new Runnable(){
                @Override
                public void run(){
                    try {
                        SDLActivity.mSDLThread.join();
                    }
                    catch(Exception e){
                        Reporting.report(e);
                    }
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

    // Dispatch the different events depending on where they come from
        // Some SOURCE_DPAD or SOURCE_GAMEPAD are also SOURCE_KEYBOARD
        // So, we try to process them as DPAD or GAMEPAD events first, if that fails we try them as KEYBOARD

        if ( (event.getSource() & 0x00000401) != 0 || /* API 12: SOURCE_GAMEPAD */
                (event.getSource() & InputDevice.SOURCE_DPAD) != 0 ) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (SDLActivity.onNativePadDown(event.getDeviceId(), keyCode) == 0) {
                    return true;
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                if (SDLActivity.onNativePadUp(event.getDeviceId(), keyCode) == 0) {
                    return true;
                }
            }
        }

        if( (event.getSource() & InputDevice.SOURCE_KEYBOARD) != 0) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                Log.v("key down: " + keyCode);
                SDLActivity.onNativeKeyDown(keyCode);
                return true;
            }
            else if (event.getAction() == KeyEvent.ACTION_UP) {
                Log.v("key up: " + keyCode);
                SDLActivity.onNativeKeyUp(keyCode);
                return true;
            }
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
        /* Ref: http://developer.android.com/training/gestures/multi.html */
        final int touchDevId = event.getDeviceId();
        final int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();
        int pointerFingerId;
        int mouseButton;
        int i = -1;
        float x,y,p;
        float[] coords;
        // !!! FIXME: dump this SDK check after 2.0.4 ships and require API14.
        if (event.getSource() == InputDevice.SOURCE_MOUSE && SDLActivity.mSeparateMouseAndTouch) {
            if (Build.VERSION.SDK_INT < 14) {
                mouseButton = 1;    // For Android==12 all mouse buttons are the left button
            } else {
                try {
                    mouseButton = (Integer) event.getClass().getMethod("getButtonState").invoke(event);
                } catch(Exception e) {
                    mouseButton = 1;    // oh well.
                }
            }
            Log.v("Mouse event. Action: " + action + ", button: " + mouseButton);
            SDLActivity.onNativeMouse(mouseButton, action, event.getX(0), event.getY(0));
        } else {
            switch(action) {
                case MotionEvent.ACTION_MOVE:
                    for (i = 0; i < pointerCount; i++) {
                        pointerFingerId = event.getPointerId(i);

                        //x = event.getX(i) / width;
                        //y = event.getY(i) / height;

                        coords = translateCoords(event.getX(i), event.getY(i));
                        x = coords[0];
                        y = coords[1];

                        p = event.getPressure(i);
                        if (p > 1.0f) {
                            // may be larger than 1.0f on some devices
                            // see the documentation of getPressure(i)
                            p = 1.0f;
                        }
                        SDLActivity.onNativeTouch(touchDevId, pointerFingerId, action, x, y, p);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_DOWN:
                    // Primary pointer up/down, the index is always zero
                    i = 0;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_POINTER_DOWN:
                    // Non primary pointer up/down
                    if (i == -1) {
                        i = event.getActionIndex();
                    }

                    pointerFingerId = event.getPointerId(i);
                    //x = event.getX(i) / width;
                    //y = event.getY(i) / height;

                    coords = translateCoords(event.getX(i), event.getY(i));
                    x = coords[0];
                    y = coords[1];

                    p = event.getPressure(i);
                    if (p > 1.0f) {
                        // may be larger than 1.0f on some devices
                        // see the documentation of getPressure(i)
                        p = 1.0f;
                    }
                    SDLActivity.onNativeTouch(touchDevId, pointerFingerId, action, x, y, p);
                    break;

                case MotionEvent.ACTION_CANCEL:
                    for (i = 0; i < pointerCount; i++) {
                        pointerFingerId = event.getPointerId(i);

                        //x = event.getX(i) / width;
                        //y = event.getY(i) / height;

                        coords = translateCoords(event.getX(i), event.getY(i));
                        x = coords[0];
                        y = coords[1];

                        p = event.getPressure(i);
                        if (p > 1.0f) {
                            // may be larger than 1.0f on some devices
                            // see the documentation of getPressure(i)
                            p = 1.0f;
                        }
                        SDLActivity.onNativeTouch(touchDevId, pointerFingerId, MotionEvent.ACTION_UP, x, y, p);
                    }
                    break;

                default:
                    break;
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

    /*public void setScrolling(boolean scrolling) {
		if (!scrolling && inMiddleOfScroll) {
			SDLActivity.onNativeTouch(0, 0, MotionEvent.ACTION_UP, -1, -1, 0, 2, 2,
					context.app.configuration.getControlsMode());
			inMiddleOfScroll = false;
		}
		this.scrolling = scrolling;
	}*/


}