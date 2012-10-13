/*
 SDL - Simple DirectMedia Layer
 Copyright (C) 1997-2011 Sam Lantinga

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 Sam Lantinga
 slouken@libsdl.org
 */
#include "SDL_config.h"
#include "SDL_stdinc.h"

#include "SDL_android.h"

extern "C" {
#include "../../events/SDL_events_c.h"
#include "../../video/android/SDL_androidkeyboard.h"
#include "../../video/android/SDL_androidtouch.h"
#include "../../video/android/SDL_androidvideo.h"

#include <android/log.h>
#include <pthread.h>
#define LOG_TAG "SDL_android"
//#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
//#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGI(...) do {} while (false)
#define LOGE(...) do {} while (false)


/* Impelemented in audio/android/SDL_androidaudio.c */
extern void Android_RunAudioThread();
} // C

/*******************************************************************************
 This file links the Java side of Android with libsdl
 *******************************************************************************/
#include <jni.h>
#include <android/log.h>
#include <pthread.h>

/*******************************************************************************
 Globals
 *******************************************************************************/
static pthread_key_t mThreadKey;
static JavaVM* mJavaVM;

// Main activity
static jclass mActivityClass;

// method signatures
static jmethodID midCreateGLContext;
static jmethodID midFlipBuffers;
static jmethodID midAudioInit;static
jmethodID midAudioWriteShortBuffer;
static jmethodID midAudioWriteByteBuffer;static
jmethodID midAudioQuit;

// Accelerometer data storage
static float fLastAccelerometer[3];

/*******************************************************************************
 Functions called by JNI
 *******************************************************************************/

// Library init
extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv *env;
    mJavaVM = vm;
    LOGI("JNI_OnLoad called");
    if (mJavaVM->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("Failed to get the environment using GetEnv()");
        return -1;
    }
    /*
     * Create mThreadKey so we can keep track of the JNIEnv assigned to each thread
     * Refer to http://developer.android.com/guide/practices/design/jni.html for the rationale behind this
     */
    if (pthread_key_create(&mThreadKey, Android_JNI_ThreadDestroyed)) {
        __android_log_print(ANDROID_LOG_ERROR, "SDL", "Error initializing pthread key");
    }
    else {
        Android_JNI_SetupThread();
    }
    
	return JNI_VERSION_1_4;
}

// Called before SDL_main() to initialize JNI bindings
extern "C" void SDL_Android_Init(JNIEnv* mEnv, jclass cls) {
	__android_log_print(ANDROID_LOG_INFO, "SDL", "SDL_Android_Init()");

    Android_JNI_SetupThread();
    
    mActivityClass = (jclass)mEnv->NewGlobalRef(cls);
    
	midCreateGLContext = mEnv->GetStaticMethodID(mActivityClass,
			"createGLContext", "(II)Z");
	midFlipBuffers = mEnv->GetStaticMethodID(mActivityClass, "flipBuffers",
			"()V");
	midAudioInit = mEnv->GetStaticMethodID(mActivityClass, "audioInit",
			"(IZZI)Ljava/lang/Object;");
	midAudioWriteShortBuffer = mEnv->GetStaticMethodID(mActivityClass,
			"audioWriteShortBuffer", "([S)V");
	midAudioWriteByteBuffer = mEnv->GetStaticMethodID(mActivityClass,
			"audioWriteByteBuffer", "([B)V");
	midAudioQuit = mEnv->GetStaticMethodID(mActivityClass, "audioQuit", "()V");

	if (!midCreateGLContext || !midFlipBuffers || !midAudioInit
			|| !midAudioWriteShortBuffer || !midAudioWriteByteBuffer
			|| !midAudioQuit) {
		__android_log_print(ANDROID_LOG_WARN, "SDL",
				"SDL: Couldn't locate Java callbacks, check that they're named and typed correctly");
	}
    __android_log_print(ANDROID_LOG_INFO, "SDL", "SDL_Android_Init() finished!");
}

// Resize
extern "C" void Java_uk_co_armedpineapple_corsixth_SDLActivity_onNativeResize(
		JNIEnv* env, jclass jcls, jint width, jint height, jint format) {
	Android_SetScreenResolution(width, height, format);
}

// Keydown
extern "C" void Java_uk_co_armedpineapple_corsixth_SDLActivity_onNativeKeyDown(
		JNIEnv* env, jclass jcls, jint keycode) {
	Android_OnKeyDown(keycode);
}

// Keyup
extern "C" void Java_uk_co_armedpineapple_corsixth_SDLActivity_onNativeKeyUp(
		JNIEnv* env, jclass jcls, jint keycode) {
	Android_OnKeyUp(keycode);
}

// Touch
extern "C" void Java_uk_co_armedpineapple_corsixth_SDLActivity_onNativeTouch(
		JNIEnv* env, jclass jcls, jint touch_device_id_in,
		jint pointer_finger_id_in, jint action, jfloat x, jfloat y, jfloat p,
		jint pc, jint gestureTriggered) {
	Android_OnTouch(touch_device_id_in, pointer_finger_id_in, action, x, y, p,
			pc, gestureTriggered);
}

// Accelerometer
extern "C" void Java_uk_co_armedpineapple_corsixth_SDLActivity_onNativeAccel(
		JNIEnv* env, jclass jcls, jfloat x, jfloat y, jfloat z) {
	fLastAccelerometer[0] = x;
	fLastAccelerometer[1] = y;
	fLastAccelerometer[2] = z;
}

// Quit
extern "C" void Java_uk_co_armedpineapple_corsixth_SDLActivity_nativeQuit(
		JNIEnv* env, jclass cls) {
	// Inject a SDL_QUIT event
	SDL_SendQuit();
}

extern "C" void Java_uk_co_armedpineapple_corsixth_SDLActivity_nativeRunAudioThread(
		JNIEnv* env, jclass cls) {
	/* This is the audio thread, with a different environment */
	Android_JNI_SetupThread();

	Android_RunAudioThread();
}

/*******************************************************************************
 Functions called by SDL into Java
 *******************************************************************************/
extern "C" SDL_bool Android_JNI_CreateContext(int majorVersion,
		int minorVersion) 
{
    JNIEnv *mEnv = Android_JNI_GetEnv();
	if (mEnv->CallStaticBooleanMethod(mActivityClass, midCreateGLContext,
			majorVersion, minorVersion)) {
		return SDL_TRUE;
	} else {
		return SDL_FALSE;
	}
}

extern "C" void Android_JNI_SwapWindow() {
    JNIEnv *mEnv = Android_JNI_GetEnv();
	mEnv->CallStaticVoidMethod(mActivityClass, midFlipBuffers);
}

extern "C" void Android_JNI_SetActivityTitle(const char *title) {
	jmethodID mid;

    JNIEnv *mEnv = Android_JNI_GetEnv();
	mid = mEnv->GetStaticMethodID(mActivityClass, "setActivityTitle",
			"(Ljava/lang/String;)V");
	if (mid) {
		mEnv->CallStaticVoidMethod(mActivityClass, mid,
				mEnv->NewStringUTF(title));
	}
}

extern "C" void Android_JNI_GetAccelerometerValues(float values[3]) {
	int i;
	for (i = 0; i < 3; ++i) {
		values[i] = fLastAccelerometer[i];
	}
}

static void Android_JNI_ThreadDestroyed(void* value) {
    /* The thread is being destroyed, detach it from the Java VM and set the mThreadKey value to NULL as required */
    JNIEnv *env = (JNIEnv*) value;
    if (env != NULL) {
        mJavaVM->DetachCurrentThread();
        pthread_setspecific(mThreadKey, NULL);
    }
}

JNIEnv* Android_JNI_GetEnv(void) {
    /* From http://developer.android.com/guide/practices/jni.html
     * All threads are Linux threads, scheduled by the kernel.
     * They're usually started from managed code (using Thread.start), but they can also be created elsewhere and then
     * attached to the JavaVM. For example, a thread started with pthread_create can be attached with the
     * JNI AttachCurrentThread or AttachCurrentThreadAsDaemon functions. Until a thread is attached, it has no JNIEnv,
     * and cannot make JNI calls.
     * Attaching a natively-created thread causes a java.lang.Thread object to be constructed and added to the "main"
     * ThreadGroup, making it visible to the debugger. Calling AttachCurrentThread on an already-attached thread
     * is a no-op.
     * Note: You can call this function any number of times for the same thread, there's no harm in it
     */

    JNIEnv *env;
    int status = mJavaVM->AttachCurrentThread(&env, NULL);
    if(status < 0) {
        LOGE("failed to attach current thread");
        return 0;
    }

    return env;
}

int Android_JNI_SetupThread(void) {
    /* From http://developer.android.com/guide/practices/jni.html
     * Threads attached through JNI must call DetachCurrentThread before they exit. If coding this directly is awkward,
     * in Android 2.0 (Eclair) and higher you can use pthread_key_create to define a destructor function that will be
     * called before the thread exits, and call DetachCurrentThread from there. (Use that key with pthread_setspecific
     * to store the JNIEnv in thread-local-storage; that way it'll be passed into your destructor as the argument.)
     * Note: The destructor is not called unless the stored value is != NULL
     * Note: You can call this function any number of times for the same thread, there's no harm in it
     *       (except for some lost CPU cycles)
     */
    JNIEnv *env = Android_JNI_GetEnv();
    pthread_setspecific(mThreadKey, (void*) env);
    return 1;
}

   
//
// Audio support
//
static jboolean audioBuffer16Bit = JNI_FALSE;
static jboolean audioBufferStereo = JNI_FALSE;
static jobject audioBuffer = NULL;
static void* audioBufferPinned = NULL;

extern "C" int Android_JNI_OpenAudioDevice(int sampleRate, int is16Bit,
		int channelCount, int desiredBufferFrames) {
	int audioBufferFrames;
    JNIEnv *env = Android_JNI_GetEnv();

    if (!env) {
        LOGE("callback_handler: failed to attach current thread");
    }
    Android_JNI_SetupThread();
    
	__android_log_print(ANDROID_LOG_VERBOSE, "SDL",
			"SDL audio: opening device");
	audioBuffer16Bit = is16Bit;
	audioBufferStereo = channelCount > 1;

	audioBuffer = env->CallStaticObjectMethod(mActivityClass, midAudioInit, sampleRate, audioBuffer16Bit, audioBufferStereo, desiredBufferFrames);

	if (audioBuffer == NULL) {
		__android_log_print(ANDROID_LOG_WARN, "SDL",
				"SDL audio: didn't get back a good audio buffer!");
		return 0;
	}
	audioBuffer = env->NewGlobalRef(audioBuffer);

	jboolean isCopy = JNI_FALSE;
	if (audioBuffer16Bit) {
        audioBufferPinned = env->GetShortArrayElements((jshortArray)audioBuffer, &isCopy);
        audioBufferFrames = env->GetArrayLength((jshortArray)audioBuffer);
    } else {
        audioBufferPinned = env->GetByteArrayElements((jbyteArray)audioBuffer, &isCopy);
        audioBufferFrames = env->GetArrayLength((jbyteArray)audioBuffer);
	}
	if (audioBufferStereo) {
		audioBufferFrames /= 2;
	}
	

	return audioBufferFrames;
}

extern "C" void * Android_JNI_GetAudioBuffer() {
	return audioBufferPinned;
}

extern "C" void Android_JNI_WriteAudioBuffer() {
	JNIEnv *mAudioEnv = Android_JNI_GetEnv();
	if (audioBuffer16Bit) {
		mAudioEnv->ReleaseShortArrayElements((jshortArray) audioBuffer,
				(jshort *) audioBufferPinned, JNI_COMMIT);
		mAudioEnv->CallStaticVoidMethod(mActivityClass,
				midAudioWriteShortBuffer, (jshortArray) audioBuffer);
	} else {
		mAudioEnv->ReleaseByteArrayElements((jbyteArray) audioBuffer,
				(jbyte *) audioBufferPinned, JNI_COMMIT);
		mAudioEnv->CallStaticVoidMethod(mActivityClass, midAudioWriteByteBuffer,
				(jbyteArray) audioBuffer);
	}

	/* JNI_COMMIT means the changes are committed to the VM but the buffer remains pinned */
}

extern "C" void Android_JNI_CloseAudioDevice() {
    int status;
	JNIEnv *env = Android_JNI_GetEnv();

    env->CallStaticVoidMethod(mActivityClass, midAudioQuit); 

	if (audioBuffer) {
		env->DeleteGlobalRef(audioBuffer);
		audioBuffer = NULL;
		audioBufferPinned = NULL;
	}
	

}

/* vi: set ts=4 sw=4 expandtab: */
