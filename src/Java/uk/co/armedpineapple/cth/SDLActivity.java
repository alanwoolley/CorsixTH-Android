/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
// $codepro.audit.disable disallowNativeMethods
package uk.co.armedpineapple.cth;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.*;
import android.content.SharedPreferences.*;
import android.content.pm.PackageManager.*;
import android.hardware.*;
import android.media.*;
import android.os.*;
import android.os.PowerManager.*;
import android.support.v4.widget.*;
import android.support.v4.widget.DrawerLayout.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import uk.co.armedpineapple.cth.CommandHandler.*;
import uk.co.armedpineapple.cth.Files.*;
import uk.co.armedpineapple.cth.dialogs.*;

import javax.microedition.khronos.egl.*;
import java.io.*;
import java.util.*;

public class SDLActivity extends CTHActivity {

    public static final Reporting.Logger Log = Reporting.INSTANCE
            .getLogger("SDLActivity");

    private static final String ENGINE_ZIP_FILE = "game.zip";

    // Keep track of the paused state
    public static boolean mIsPaused, mIsSurfaceReady, mHasFocus;
    public static boolean mExitCalledFromJava;

    /**
     * If shared libraries (e.g. SDL or the native application) could not be loaded.
     */
    public static boolean mBrokenLibraries;

    // If we want to separate mouse and touch events.
    //  This is only toggled in native code when a hint is set!
    public static boolean mSeparateMouseAndTouch = true;

    // Main components
    public static SDLActivity mSingleton;
    public static SDLSurface mSurface;
    protected static View mTextEdit;
    protected static ViewGroup mLayout;
    protected static SDLJoystickHandler mJoystickHandler;

    // This is what SDL runs in. It invokes SDL_main(), eventually
    protected static Thread mSDLThread;
    // EGL private objects
    private static EGLContext mEGLContext;
    private static EGLSurface mEGLSurface;
    private static EGLDisplay mEGLDisplay;
    private static EGLConfig mEGLConfig;
    private static int mGLMajor, mGLMinor;
    // Audio
    private static Thread mAudioThread;
    private static AudioTrack mAudioTrack;
    private static Object audioBuffer;
    // Handler for the messages
    public CommandHandler commandHandler;
    // Menu Drawer
    DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private int currentVersion;
    private WakeLock wake;
    private boolean hasGameLoaded = false;

    // Vibration
    private Vibrator mVibratorService;
    private PowerManager pm;

    /**
     * This method is called by SDL before starting the native application thread.
     * It can be overridden to provide the arguments after the application name.
     * The default implementation returns an empty array. It never returns null.
     *
     * @return arguments for the native application.
     */
    protected String[] getArguments() {
        return new String[0];
    }

    // C functions we call
    public static native void nativeInit(Object arguments,
            Configuration config);

    public static native void nativeLowMemory();

    public static native void nativeQuit();

    public static native void nativePause();

    public static native void nativeResume();

    public static native void nativeFlipBuffers();

    public static native void onNativeResize(int x, int y, int format);

    public static native void onNativeKeyDown(int keycode);

    public static native void onNativeKeyUp(int keycode);

    public static native void onNativeKeyboardFocusLost();

    public static native void onNativeMouse(int button, int action, float x,
            float y);

    public static native void onNativeTouch(int touchDevId, int pointerFingerId,
            int action, float x, float y, float p);

    public static native int nativeAddJoystick(int device_id, String name,
            int is_accelerometer, int nbuttons, int naxes, int nhats,
            int nballs);

    public static native int nativeRemoveJoystick(int device_id);

    public static native int onNativePadDown(int device_id, int keycode);

    public static native int onNativePadUp(int device_id, int keycode);

    public static native void onNativeJoy(int device_id, int axis, float value);

    public static native void onNativeHat(int device_id, int hat_id, int x,
            int y);

    public static native void onNativeAccel(float x, float y, float z);

    //public static native void onNativeHover(float x, float y);

    public static native void onNativeSurfaceChanged();

    public static native void onNativeSurfaceDestroyed();

    public static native String nativeGetHint(String name);

    public static native void nativeRunAudioThread();

    public static native void cthRestartGame();

    public static native void cthSaveGame(String path);

    public static native void cthLoadGame(String path);

    public static native void cthGameSpeed(int speed);

    public static native void cthTryAutoSave(String filename);

    public static native void cthUpdateConfiguration(Configuration config);

    public static native void cthShowCheats();

    public static native void cthShowJukebox();

    public static void initialize() {
        // The static nature of the singleton and Android quirkyness force us to initialize everything here
        // Otherwise, when exiting the app and returning to it, these variables *keep* their pre exit values
        mSingleton = null;
        mSurface = null;
        mTextEdit = null;
        mLayout = null;
        mJoystickHandler = null;
        mSDLThread = null;
        mAudioTrack = null;
        mExitCalledFromJava = false;
        mIsPaused = false;
        mIsSurfaceReady = false;
        mHasFocus = true;
    }

    public static String nativeGetGamePath() {
        return mSingleton.getApp().getConfiguration().getCthPath()
                + "/scripts/";
    }

    // EGL functions
    public static boolean initEGL(int majorVersion, int minorVersion) {
        if (SDLActivity.mEGLDisplay == null) {
            Log.v("Starting up OpenGL ES " + majorVersion + "." + minorVersion);

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
                    Log.e("No EGL config available");
                    return false;
                }
                EGLConfig config = configs[0];

                SDLActivity.mEGLDisplay = dpy;
                SDLActivity.mEGLConfig = config;
                SDLActivity.mGLMajor = majorVersion;
                SDLActivity.mGLMinor = minorVersion;

                SDLActivity.createEGLSurface();
            } catch (Exception e) {
                Reporting.INSTANCE.report(e);
            }
        } else
            SDLActivity.createEGLSurface();

        return true;
    }

    @Override protected void onDestroy() {
        Log.v("onDestroy()");

        if (SDLActivity.mBrokenLibraries) {
            super.onDestroy();
            // Reset everything in case the user re opens the app
            SDLActivity.initialize();
            return;
        }

        if (hasGameLoaded) {
            // Send a quit message to the application
            SDLActivity.mExitCalledFromJava = true;
            SDLActivity.nativeQuit();

            // Now wait for the SDL thread to quit
            if (SDLActivity.mSDLThread != null) {
                try {
                    SDLActivity.mSDLThread.join();
                } catch (Exception e) {
                    Log.w("Problem stopping thread");
                    Reporting.INSTANCE.report(e);
                }
                SDLActivity.mSDLThread = null;

                Log.v("Finished waiting for SDL thread");
            }

        }
        super.onDestroy();
        // Reset everything in case the user re opens the app
        SDLActivity.initialize();
    }

    public static boolean createEGLContext() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        int contextAttrs[] = new int[] { EGL_CONTEXT_CLIENT_VERSION,
                SDLActivity.mGLMajor, EGL10.EGL_NONE };
        SDLActivity.mEGLContext = egl.eglCreateContext(SDLActivity.mEGLDisplay,
                SDLActivity.mEGLConfig, EGL10.EGL_NO_CONTEXT, contextAttrs);
        if (SDLActivity.mEGLContext == EGL10.EGL_NO_CONTEXT) {
            Log.e("Couldn't create activityContext");
            return false;
        }
        return true;
    }

    public static boolean createEGLSurface() {
        if (SDLActivity.mEGLDisplay != null && SDLActivity.mEGLConfig != null) {
            EGL10 egl = (EGL10) EGLContext.getEGL();
            if (SDLActivity.mEGLContext == null)
                createEGLContext();

            Log.v("Creating new EGL Surface");
            EGLSurface surface = egl
                    .eglCreateWindowSurface(SDLActivity.mEGLDisplay,
                            SDLActivity.mEGLConfig, SDLActivity.mSurface, null);
            if (surface == EGL10.EGL_NO_SURFACE) {
                Log.e("Couldn't create surface");
                return false;
            }

            if (!egl.eglMakeCurrent(SDLActivity.mEGLDisplay, surface, surface,
                    SDLActivity.mEGLContext)) {
                Log.e("Old EGL Context doesnt work, trying with a new one");
                createEGLContext();
                if (!egl.eglMakeCurrent(SDLActivity.mEGLDisplay, surface,
                        surface, SDLActivity.mEGLContext)) {
                    Log.e("Failed making EGL Context current");
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

        SDLActivity.nativeFlipBuffers();
    }

    // EGL buffer flip
    public static void flipEGL() {
        try {
            EGL10 egl = (EGL10) EGLContext.getEGL();

            egl.eglWaitNative(EGL10.EGL_CORE_NATIVE_ENGINE, null);

            // drawing here

            egl.eglWaitGL();

            egl.eglSwapBuffers(SDLActivity.mEGLDisplay,
                    SDLActivity.mEGLSurface);

        } catch (Exception e) {
            Reporting.INSTANCE.report(e);
        }
    }

    public static boolean setActivityTitle(String title) {
        return true;
    }

    public static void sendCommandObject(int cmd, Object data) {
        mSingleton.sendCommand(cmd, data);
    }

    public static void sendCommand(Command command, Object data) {
        mSingleton.sendCommand(command.ordinal(), data);
    }

    public static void sendCommand(int cmd, int data) {
        mSingleton.sendCommand(cmd, Integer.valueOf(data));
    }

    public static void sendCommand(int cmd) {
        mSingleton.sendCommand(cmd, null);
    }

    boolean sendCommand(int cmd, Object data) {
        Message msg = mSingleton.commandHandler.obtainMessage();
        msg.arg1 = cmd;
        msg.obj = data;
        return commandHandler.sendMessage(msg);
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static boolean sendMessage(int command, int param) {
        return mSingleton.sendCommand(command, Integer.valueOf(param));
    }

    // Audio
    public static int audioInit(int sampleRate, boolean is16Bit,
            boolean isStereo, int desiredFrames) {
        int channelConfig = isStereo ?
                AudioFormat.CHANNEL_CONFIGURATION_STEREO :
                AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioFormat = is16Bit ?
                AudioFormat.ENCODING_PCM_16BIT :
                AudioFormat.ENCODING_PCM_8BIT;
        int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

        Log.v("SDL audio: wanted " + (isStereo ? "stereo" : "mono") + " " + (
                is16Bit ?
                        "16-bit" :
                        "8-bit") + " " + (sampleRate / 1000f) + "kHz, "
                + desiredFrames + " frames buffer");

        // Let the user pick a larger buffer if they really want -- but ye
        // gods they probably shouldn't, the minimums are horrifyingly high
        // latency already
        desiredFrames = Math.max(desiredFrames, (AudioTrack
                .getMinBufferSize(sampleRate, channelConfig, audioFormat)
                + frameSize - 1) / frameSize);

        if (mAudioTrack == null) {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                    channelConfig, audioFormat, desiredFrames * frameSize,
                    AudioTrack.MODE_STREAM);

            // Instantiating AudioTrack can "succeed" without an exception and the track may still be invalid
            // Ref: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/media/java/android/media/AudioTrack.java
            // Ref: http://developer.android.com/reference/android/media/AudioTrack.html#getState()

            if (mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                Log.e("Failed during initialization of Audio Track");
                mAudioTrack = null;
                return -1;
            }

            mAudioTrack.play();
        }

        Log.v("SDL audio: got " + ((mAudioTrack.getChannelCount() >= 2) ?
                "stereo" :
                "mono") + " " + ((mAudioTrack.getAudioFormat()
                == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit" : "8-bit") + " "
                + (mAudioTrack.getSampleRate() / 1000f) + "kHz, "
                + desiredFrames + " frames buffer");

        return 0;
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
        if (mAudioTrack == null) {
            Log.w("Tried to write to audio buffer without initialising first");
            return;
        }
        for (int i = 0; i < buffer.length; ) {
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
                Log.w("SDL audio: error return from write(short)");
                return;
            }
        }
    }

    // Java functions called from C

    public static void audioWriteByteBuffer(byte[] buffer) {
        if (mAudioTrack == null) {
            Log.w("Tried to write to audio buffer without initialising first");
            return;
        }
        for (int i = 0; i < buffer.length; ) {
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
                Log.w("SDL audio: error return from write(byte)");
                return;
            }
        }
    }

    // Send a message from the SDLMain thread

    public static void audioQuit() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack = null;
        }
    }



    /*public static void toggleScrolling(boolean scrolling) {
        Log.d("Scrolling Java call: "
                + scrolling);
        mSurface.setScrolling(scrolling);
    }*/

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("Device: " + android.os.Build.DEVICE);
        Log.v("Model: " + android.os.Build.MODEL);
        Log.v("onCreate(): " + mSingleton);

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        SDLActivity.initialize();
        // So we can call stuff from static callbacks
        mSingleton = this;

        commandHandler = new CommandHandler(this);
        // The volume buttons should change the media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Make sure that external media is mounted.
        if (Files.Companion.canAccessExternalStorage()) {

            final SharedPreferences preferences = getApp().getPreferences();

            if (getApp().getConfiguration() == null) {
                try {
                    getApp().setConfiguration(Configuration
                            .loadFromPreferences(this, preferences));
                } catch (IOException | StorageUnavailableException e) {
                    Log.e("Can't get storage.");

                    // Create an alert dialog warning that external storage isn't
                    // mounted. The application will have to exit at this point.

                    DialogFactory.INSTANCE
                            .createExternalStorageWarningDialog(this, true)
                            .show();
                }
            }

            currentVersion = preferences.getInt("last_version", 0) - 1;

            try {
                currentVersion = (getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionCode);

            } catch (NameNotFoundException e) {
                Reporting.INSTANCE.report(e);
            }

            // Check to see if the game files have been copied yet, or whether the
            // application has been updated
            if (!preferences.getBoolean("scripts_copied", false)
                    || preferences.getInt("last_version", 0) < currentVersion) {

                Log.d("This is a new installation");
                Reporting.INSTANCE.setBool("new_installation", true);

                // Show the recent changes dialog
                Dialog recentChangesDialog = DialogFactory.INSTANCE
                        .createRecentChangesDialog(this);
                recentChangesDialog
                        .setOnDismissListener(new OnDismissListener() {

                            @Override public void onDismiss(
                                    DialogInterface arg0) {
                                installFiles(preferences);
                            }

                        });
                recentChangesDialog.show();

            } else if (BuildConfig.ALWAYS_UPGRADE) {
                // For the debug variants, we always want to copy new files
                installFiles(preferences);
            } else {

                // Continue to load the application otherwise
                loadApplication();
            }

        } else {
            Log.e("Can't get storage.");

            // Create an alert dialog warning that external storage isn't
            // mounted. The application will have to exit at this point.

            DialogFactory.INSTANCE
                    .createExternalStorageWarningDialog(this, true).show();
        }
    }

    private void installFiles(final SharedPreferences preferences) {
        Log.d("Installing files");
        final ProgressDialog dialog = new ProgressDialog(this);
        final UnzipTask unzipTask = new UnzipTask(
                getApp().getConfiguration().getCthPath() + "/scripts/", pm) {

            @Override protected void onPreExecute() {
                super.onPreExecute();
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setMessage(
                        getString(R.string.preparing_game_files_dialog));
                dialog.setIndeterminate(false);
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                dialog.setProgress(values[0]);
                dialog.setMax(values[1]);
            }

            @Override protected void onPostExecute(
                    AsyncTaskResult<String> result) {
                super.onPostExecute(result);
                Exception error;
                if ((error = result.getError()) != null) {
                    Reporting.INSTANCE.reportWithToast(SDLActivity.this,
                            "Error copying files", error);
                }

                Editor edit = preferences.edit();
                edit.putBoolean("scripts_copied", true);
                edit.putInt("last_version", currentVersion);
                edit.apply();
                dialog.cancel();
                loadApplication();
            }

        };

        AsyncTask<String, Void, AsyncTaskResult<File>> fontCopyTask = new AsyncTask<String, Void, AsyncTaskResult<File>>() {

            @Override protected AsyncTaskResult<File> doInBackground(
                    String... params) {

                try {
                    Files.Companion
                            .copyAsset(SDLActivity.this, params[0], params[1]);
                } catch (IOException e) {
                    return new AsyncTaskResult<File>(e);
                }
                return new AsyncTaskResult<File>(
                        new File(params[1] + "/" + params[0]));
            }

            @Override protected void onPostExecute(
                    AsyncTaskResult<File> result) {
                super.onPostExecute(result);
                File f;
                if ((f = result.getResult()) == null) {
                    // TODO

                }
            }

        };

        AsyncTask<String, Void, AsyncTaskResult<File>> copyTask = new

                AsyncTask<String, Void, AsyncTaskResult<File>>() {

                    @Override protected AsyncTaskResult<File> doInBackground(
                            String... params) {

                        try {
                            Files.Companion
                                    .copyAsset(SDLActivity.this, params[0],
                                            params[1]);
                        } catch (IOException e) {

                            return new AsyncTaskResult<File>(e);
                        }
                        return new AsyncTaskResult<File>(
                                new File(params[1] + "/" + params[0]));
                    }

                    @Override protected void onPostExecute(
                            AsyncTaskResult<File> result) {
                        super.onPostExecute(result);
                        File f;
                        if ((f = result.getResult()) != null) {
                            unzipTask.execute(f);
                        } else {
                            Reporting.INSTANCE.reportWithToast(SDLActivity.this,
                                    "Unable to copy files", result.getError());
                        }
                    }

                };

        if (Files.Companion.canAccessExternalStorage()) {
            Log.d("Starting copy task");
            try {
                copyTask.execute(ENGINE_ZIP_FILE,
                        getExternalCacheDir().getCanonicalPath());

                // Copy fallback font asset
                fontCopyTask.execute("DroidSansFallbackFull.ttf",
                        getFilesDir().getCanonicalPath());
            } catch (IOException e) {
                Reporting.INSTANCE.report(e);
                DialogFactory.INSTANCE
                        .createExternalStorageWarningDialog(this, true).show();
            }

        } else {
            Log.w("Wasn't able to access external storage when copying files");
            DialogFactory.INSTANCE
                    .createExternalStorageWarningDialog(this, true).show();
        }
    }

    public static Surface getNativeSurface() {
        return mSurface.getHolder().getSurface();
    }

    void loadApplication() {

        // Load shared libraries
        String errorMsgBrokenLib = "";
        try {
            // Load the libraries
            System.loadLibrary("SDL2");
            System.loadLibrary("LUA");
            System.loadLibrary("SDL2_mixer");
            //System.loadLibrary("ffmpeg");
            System.loadLibrary("appmain");
            //            System.loadLibrary("avcodec");
            //            System.loadLibrary("avfilter");
            //            System.loadLibrary("avformat");
            //            System.loadLibrary("avutil");
            //            System.loadLibrary("swresample");
            //            System.loadLibrary("swscale");
        } catch (UnsatisfiedLinkError e) {
            System.err.println(e.getMessage());
            mBrokenLibraries = true;
            errorMsgBrokenLib = e.getMessage();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            mBrokenLibraries = true;
            errorMsgBrokenLib = e.getMessage();
        }

        if (mBrokenLibraries) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage(
                    "An error occurred while trying to start the application. Please try again and/or reinstall."
                            + System.getProperty("line.separator") + System
                            .getProperty("line.separator") + "Error: "
                            + errorMsgBrokenLib);
            dlgAlert.setTitle("SDL Error");
            dlgAlert.setPositiveButton("Exit",
                    new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog,
                                int id) {
                            // if this button is clicked, close current activity
                            SDLActivity.mSingleton.finish();
                        }
                    });
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();

            return;
        }

        try {
            getApp().getConfiguration().writeToFile();
        } catch (IOException e) {
            Reporting.INSTANCE.reportWithToast(SDLActivity.this,
                    "Could not write to configuration file", e);
        }

        File f = new File(getApp().getConfiguration().getSaveGamesPath());

        if (!f.isDirectory()) {
            f.mkdirs();
        }

        hideSystemUi();

        mSurface = new SDLSurface(this,
                getApp().getConfiguration().getDisplayWidth(),
                getApp().getConfiguration().getDisplayHeight());
        mSurface.setZOrderOnTop(false);

        mLayout = (DrawerLayout) getLayoutInflater()
                .inflate(R.layout.game, null);
        FrameLayout gameFrame = ((FrameLayout) mLayout
                .findViewById(R.id.game_frame));

        gameFrame.addView(mSurface);
        setContentView(mLayout);

        if (getApp().getConfiguration().getHaptic()) {
            mVibratorService = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_layout);
        mDrawerList = (ListView) findViewById(R.id.menu_drawer);
        mDrawerList.setAdapter(new NavDrawerAdapter(this,
                uk.co.armedpineapple.cth.MenuItems.Companion.getItems(
                        BuildConfig.DEBUG || getApp().getConfiguration()
                                .getDebug())));
        mDrawerList.setOnItemClickListener(new NavDrawerListListener(this));
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.setDrawerListener(new DrawerListener() {

            @Override public void onDrawerClosed(View arg0) {
                // Restore game speed
                cthGameSpeed(getApp().getConfiguration().getGameSpeed());
                mDrawerLayout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override public void onDrawerOpened(View arg0) {
                // Pause the game
                cthGameSpeed(0);
            }

            @Override public void onDrawerSlide(View arg0, float arg1) {
                arg0.bringToFront();
                mDrawerLayout.bringChildToFront(arg0);
                mDrawerLayout.requestLayout();

            }

            @Override public void onDrawerStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }

        });

        mJoystickHandler = new SDLJoystickHandler_API12();

        SurfaceHolder holder = mSurface.getHolder();
        holder.setFixedSize(getApp().getConfiguration().getDisplayWidth(),
                getApp().getConfiguration().getDisplayHeight());

        gameFrame.setVisibility(View.VISIBLE);

        hasGameLoaded = true;

    }

    @SuppressLint("NewApi") public static void hideSystemUi() {
        if (!isActivityAvailable() || mSingleton.getWindow() == null) {
            Log.i("Tried to hide system UI with no window. Ignoring.");
            return;
        }

        // Hide the navigation buttons
        mSingleton.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

    }

    public void startApp() {
        // Start up the C app thread

        //mSDLThread = new Thread(new SDLMain(app.configuration, ""), "SDLThread");
        // mSDLThread.start();

    }

    // Events
    protected void onPause() {
        super.onPause();
        Log.v("onPause()");

        // Attempt to autosave.
        if (hasGameLoaded) {
            // Reset the game speed back to normal
            cthGameSpeed(getApp().getConfiguration().getGameSpeed());

            cthTryAutoSave("cthAndroidAutoSave.sav");
        }

        if (wake != null && wake.isHeld()) {
            Log.d("Releasing wakelock");
            wake.release();
        }

        stopVibration();
    }

    protected void onResume() {
        super.onResume();
        Log.v("onResume()");

        if (getApp().getConfiguration() != null && getApp().getConfiguration()
                .getKeepScreenOn()) {
            Log.d("Getting wakelock");
            PowerManager powerManager = (PowerManager) getSystemService(
                    Context.POWER_SERVICE);
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

    @Override public void onLowMemory() {
        super.onLowMemory();
        Log.w("Low memory detected. Going to try and tighten our belt!");

        if (hasGameLoaded) {
            // Attempt to save first
            cthTryAutoSave(getString(R.string.autosave_name));
        }

        // Remove references to some stuff that can just be regenerated later, so
        // that the GC can get rid of them.
        commandHandler.cleanUp();

        // Can't do anything in this case
        if (SDLActivity.mBrokenLibraries || !isGameLoaded()) {
            return;
        }

        SDLActivity.nativeLowMemory();

    }

    public void playVibration(int vibrationCode) {
        if (getApp().getConfiguration().getHaptic() && getApp()
                .getHasVibration()) {

            if (mVibratorService == null)
                mVibratorService = (Vibrator) getSystemService(
                        VIBRATOR_SERVICE);
            if (getApp().getHasVibration()) {

                switch (vibrationCode) {
                    case CommandHandler.VIBRATION_SHORT_CLICK:
                    case CommandHandler.VIBRATION_LONG_CLICK:
                        mVibratorService.vibrate(100);
                        break;
                    case CommandHandler.VIBRATION_EXPLOSION:
                        mVibratorService.vibrate(3000);
                        break;

                    default:
                        break;
                }
            }
        }
    }

    public void stopVibration() {
        if (mVibratorService != null && getApp().getHasVibration() && getApp()
                .getConfiguration().getHaptic()) {
            mVibratorService.cancel();
            commandHandler.setPlayingEarthquake(false);
        }
    }

    // Input

    @Override public boolean dispatchKeyEvent(KeyEvent event) {

        if (SDLActivity.mBrokenLibraries) {
            return false;
        }

        int keyCode = event.getKeyCode();
        // Ignore certain special keys so they're handled by Android
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == 168 || /* API 11: KeyEvent.KEYCODE_ZOOM_IN */
                keyCode == 169 /* API 11: KeyEvent.KEYCODE_ZOOM_OUT */) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * @return an array which may be empty but is never null.
     */
    public static int[] inputGetInputDeviceIds(int sources) {
        int[] ids = InputDevice.getDeviceIds();
        int[] filtered = new int[ids.length];
        int used = 0;
        for (int i = 0; i < ids.length; ++i) {
            InputDevice device = InputDevice.getDevice(ids[i]);
            if ((device != null) && ((device.getSources() & sources) != 0)) {
                filtered[used++] = device.getId();
            }
        }
        return Arrays.copyOf(filtered, used);
    }

    // Joystick glue code, just a series of stubs that redirect to the SDLJoystickHandler instance
    public static boolean handleJoystickMotionEvent(MotionEvent event) {
        return mJoystickHandler.handleMotionEvent(event);
    }

    public static void pollInputDevices() {
        if (SDLActivity.mSDLThread != null) {
            mJoystickHandler.pollInputDevices();
        }
    }

    /**
     * Called by onPause or surfaceDestroyed. Even if surfaceDestroyed
     * is the first to be called, mIsSurfaceReady should still be set
     * to 'true' during the call to onPause (in a usual scenario).
     */
    public static void handlePause() {
        if (!SDLActivity.mIsPaused && SDLActivity.mIsSurfaceReady) {
            SDLActivity.mIsPaused = true;
            SDLActivity.nativePause();
            mSurface.enableSensor(Sensor.TYPE_ACCELEROMETER, false);
        }
    }

    /**
     * Called by onResume or surfaceCreated. An actual resume should be done only when the surface is ready.
     * Note: Some Android variants may send multiple surfaceChanged events, so we don't need to resume
     * every time we get one of those events, only if it comes after surfaceDestroyed
     */
    public static void handleResume() {
        if (SDLActivity.mIsPaused && SDLActivity.mIsSurfaceReady
                && SDLActivity.mHasFocus) {
            SDLActivity.mIsPaused = false;
            SDLActivity.nativeResume();
            mSurface.enableSensor(Sensor.TYPE_ACCELEROMETER, true);
        }
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.v("onWindowFocusChanged(): " + hasFocus);

        if (SDLActivity.mBrokenLibraries) {
            return;
        }

        SDLActivity.mHasFocus = hasFocus;
        if (hasFocus) {
            SDLActivity.handleResume();
        }
    }

    public static boolean isActivityAvailable() {
        return mSingleton != null && !mSingleton.isDestroyed() && !mSingleton
                .isFinishing();
    }

    /* The native thread has finished */
    public static void handleNativeExit() {
        SDLActivity.mSDLThread = null;

        // The activity could already be in the process of closing down
        if (isActivityAvailable()) {
            mSingleton.finish();
        }
    }

    public void hideTextEdit() {
        if (mTextEdit != null) {
            mTextEdit.setVisibility(View.GONE);

            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mTextEdit.getWindowToken(), 0);
        }
    }

    public boolean isGameLoaded() {
        return hasGameLoaded;
    }

    public void setScreenOn(boolean on) {
        Window window = getWindow();
        if (window != null) {
            if (on) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                window.clearFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    public static Context getContext() {
        return mSingleton;
    }

    public static boolean showTextInput(int x, int y, int w, int h) {
        // Transfer the task to the main thread as a Runnable
        return mSingleton.commandHandler
                .post(new ShowTextInputTask(x, y, w, h));
    }

    static class ShowTextInputTask implements Runnable {
        /*
         * This is used to regulate the pan&scan method to have some offset from
        * the bottom edge of the input region and the top edge of an input
         * method (soft keyboard)
        */
        static final int HEIGHT_PADDING = 15;

        public int x, y, w, h;

        public ShowTextInputTask(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override public void run() {
            DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams(w,
                    h);

            if (mTextEdit == null) {
                mTextEdit = new DummyEdit(getContext());

                mLayout.addView(mTextEdit, params);
            } else {
                mTextEdit.setLayoutParams(params);
            }

            mTextEdit.setVisibility(View.VISIBLE);
            mTextEdit.requestFocus();

            InputMethodManager imm = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mTextEdit, 0);
        }
    }

}

/**
 * Simple nativeInit() runnable
 */
class SDLMain implements Runnable {

    private static final Reporting.Logger Log = Reporting.INSTANCE
            .getLogger("SDLMain");

    private final Configuration config;
    private final String toLoad;

    public SDLMain(Configuration config, String toLoad) {
        this.config = config;
        this.toLoad = toLoad;

    }

    public void run() {
        // Runs SDL_main()
        SDLActivity.nativeInit(new String[] { "--load", toLoad }, config);

        Log.d("SDL thread terminated");
    }
}

/* A null joystick handler for API level < 12 devices (the accelerometer is handled separately) */
class SDLJoystickHandler {

    public boolean handleMotionEvent(MotionEvent event) {
        return false;
    }

    public void pollInputDevices() {
    }
}

/* Actual joystick functionality available for API >= 12 devices */
class SDLJoystickHandler_API12 extends SDLJoystickHandler {

    class SDLJoystick {
        public int device_id;
        public String name;
        public ArrayList<InputDevice.MotionRange> axes;
        public ArrayList<InputDevice.MotionRange> hats;
    }

    class RangeComparator implements Comparator<InputDevice.MotionRange> {
        @Override public int compare(InputDevice.MotionRange arg0,
                InputDevice.MotionRange arg1) {
            return arg0.getAxis() - arg1.getAxis();
        }
    }

    private ArrayList<SDLJoystick> mJoysticks;

    public SDLJoystickHandler_API12() {

        mJoysticks = new ArrayList<SDLJoystick>();
    }

    @Override public void pollInputDevices() {
        int[] deviceIds = InputDevice.getDeviceIds();
        // It helps processing the device ids in reverse order
        // For example, in the case of the XBox 360 wireless dongle,
        // so the first controller seen by SDL matches what the receiver
        // considers to be the first controller

        for (int i = deviceIds.length - 1; i > -1; i--) {
            SDLJoystick joystick = getJoystick(deviceIds[i]);
            if (joystick == null) {
                joystick = new SDLJoystick();
                InputDevice joystickDevice = InputDevice
                        .getDevice(deviceIds[i]);
                if ((joystickDevice.getSources()
                        & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
                    joystick.device_id = deviceIds[i];
                    joystick.name = joystickDevice.getName();
                    joystick.axes = new ArrayList<InputDevice.MotionRange>();
                    joystick.hats = new ArrayList<InputDevice.MotionRange>();

                    List<InputDevice.MotionRange> ranges = joystickDevice
                            .getMotionRanges();
                    Collections.sort(ranges, new RangeComparator());
                    for (InputDevice.MotionRange range : ranges) {
                        if ((range.getSource()
                                & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
                            if (range.getAxis() == MotionEvent.AXIS_HAT_X
                                    || range.getAxis()
                                    == MotionEvent.AXIS_HAT_Y) {
                                joystick.hats.add(range);
                            } else {
                                joystick.axes.add(range);
                            }
                        }
                    }

                    mJoysticks.add(joystick);
                    SDLActivity.nativeAddJoystick(joystick.device_id,
                            joystick.name, 0, -1, joystick.axes.size(),
                            joystick.hats.size() / 2, 0);
                }
            }
        }

        /* Check removed devices */
        ArrayList<Integer> removedDevices = new ArrayList<Integer>();
        for (int i = 0; i < mJoysticks.size(); i++) {
            int device_id = mJoysticks.get(i).device_id;
            int j;
            for (j = 0; j < deviceIds.length; j++) {
                if (device_id == deviceIds[j])
                    break;
            }
            if (j == deviceIds.length) {
                removedDevices.add(device_id);
            }
        }

        for (int i = 0; i < removedDevices.size(); i++) {
            int device_id = removedDevices.get(i);
            SDLActivity.nativeRemoveJoystick(device_id);
            for (int j = 0; j < mJoysticks.size(); j++) {
                if (mJoysticks.get(j).device_id == device_id) {
                    mJoysticks.remove(j);
                    break;
                }
            }
        }
    }

    protected SDLJoystick getJoystick(int device_id) {
        for (int i = 0; i < mJoysticks.size(); i++) {
            if (mJoysticks.get(i).device_id == device_id) {
                return mJoysticks.get(i);
            }
        }
        return null;
    }

    @Override public boolean handleMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) != 0) {
            int actionPointerIndex = event.getActionIndex();
            int action = event.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    SDLJoystick joystick = getJoystick(event.getDeviceId());
                    if (joystick != null) {
                        for (int i = 0; i < joystick.axes.size(); i++) {
                            InputDevice.MotionRange range = joystick.axes
                                    .get(i);
                            /* Normalize the value to -1...1 */
                            float value = (event.getAxisValue(range.getAxis(),
                                    actionPointerIndex) - range.getMin())
                                    / range.getRange() * 2.0f - 1.0f;
                            SDLActivity
                                    .onNativeJoy(joystick.device_id, i, value);
                        }
                        for (int i = 0; i < joystick.hats.size(); i += 2) {
                            int hatX = Math.round(event.getAxisValue(
                                    joystick.hats.get(i).getAxis(),
                                    actionPointerIndex));
                            int hatY = Math.round(event.getAxisValue(
                                    joystick.hats.get(i + 1).getAxis(),
                                    actionPointerIndex));
                            SDLActivity.onNativeHat(joystick.device_id, i / 2,
                                    hatX, hatY);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }

}

class SDLGenericMotionListener_API12 implements View.OnGenericMotionListener {
    // Generic Motion (mouse hover, joystick...) events go here
    // We only have joysticks yet
    @Override public boolean onGenericMotion(View v, MotionEvent event) {
        return SDLActivity.handleJoystickMotionEvent(event);
    }
}

/* This is a fake invisible editor view that receives the input and defines the
          * pan&scan region
          */
class DummyEdit extends View implements View.OnKeyListener {
    InputConnection ic;

    public DummyEdit(Context context) {
        super(context);
        setFocusableInTouchMode(true);
        setFocusable(true);
        setOnKeyListener(this);
    }

    @Override public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override public boolean onKey(View v, int keyCode, KeyEvent event) {

        // This handles the hardware keyboard input
        if (event.isPrintingKey()) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                ic.commitText(String.valueOf((char) event.getUnicodeChar()), 1);
            }
            return true;
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

    //
    @Override public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        // As seen on StackOverflow: http://stackoverflow.com/questions/7634346/keyboard-hide-event
        // FIXME: Discussion at http://bugzilla.libsdl.org/show_bug.cgi?id=1639
        // FIXME: This is not a 100% effective solution to the problem of detecting if the keyboard is showing or not
        // FIXME: A more effective solution would be to change our Layout from AbsoluteLayout to Relative or Linear
        // FIXME: And determine the keyboard presence doing this: http://stackoverflow.com/questions/2150078/how-to-check-visibility-of-software-keyboard-in-android
        // FIXME: An even more effective way would be if Android provided this out of the box, but where would the fun be in that :)
        if (event.getAction() == KeyEvent.ACTION_UP
                && keyCode == KeyEvent.KEYCODE_BACK) {
            if (SDLActivity.mTextEdit != null
                    && SDLActivity.mTextEdit.getVisibility() == View.VISIBLE) {
                SDLActivity.onNativeKeyboardFocusLost();
                SDLActivity.hideSystemUi();
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override public InputConnection onCreateInputConnection(
            EditorInfo outAttrs) {
        ic = new SDLInputConnection(this, true);

        outAttrs.inputType = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
                | 33554432 /* API 11: EditorInfo.IME_FLAG_NO_FULLSCREEN */;

        return ic;
    }
}

class SDLInputConnection extends BaseInputConnection {

    public SDLInputConnection(View targetView, boolean fullEditor) {
        super(targetView, fullEditor);

    }

    @Override public boolean sendKeyEvent(KeyEvent event) {

                 /*
            * This handles the keycodes from soft keyboard (and IME-translated
            * input from hardkeyboard)
            */
        int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.isPrintingKey()) {
                commitText(String.valueOf((char) event.getUnicodeChar()), 1);
            }
            SDLActivity.onNativeKeyDown(keyCode);
            return true;
        } else if (event.getAction() == KeyEvent.ACTION_UP) {

            SDLActivity.onNativeKeyUp(keyCode);
            return true;
        }
        return super.sendKeyEvent(event);
    }

    @Override public boolean commitText(CharSequence text,
            int newCursorPosition) {

        nativeCommitText(text.toString(), newCursorPosition);

        return super.commitText(text, newCursorPosition);
    }

    @Override public boolean setComposingText(CharSequence text,
            int newCursorPosition) {

        nativeSetComposingText(text.toString(), newCursorPosition);

        return super.setComposingText(text, newCursorPosition);
    }

    public native void nativeCommitText(String text, int newCursorPosition);

    public native void nativeSetComposingText(String text,
            int newCursorPosition);

    @Override public boolean deleteSurroundingText(int beforeLength,
            int afterLength) {
        // Workaround to capture backspace key. Ref: http://stackoverflow.com/questions/14560344/android-backspace-in-webview-baseinputconnection
        if (beforeLength == 1 && afterLength == 0) {
            // backspace
            return super.sendKeyEvent(
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                    && super.sendKeyEvent(
                    new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
        }

        return super.deleteSurroundingText(beforeLength, afterLength);
    }
}