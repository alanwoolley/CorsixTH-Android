/* Include the SDL main definition header */
#include "SDL_main.h"

/*******************************************************************************
 Functions called by JNI
 *******************************************************************************/
#include <jni.h>

static JavaVM* jvm;

// Called before SDL_main() to initialize JNI bindings in SDL library
extern "C" void SDL_Android_Init(JNIEnv* env, jclass cls);

// Library init
extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	jvm = vm;
	return JNI_VERSION_1_4;
}

// Start up the SDL app
extern "C" void Java_uk_co_armedpineapple_corsixth_SDLActivity_nativeInit(
		JNIEnv* env, jclass cls, jstring jni_log_path) {

	const char *log_path = env->GetStringUTFChars(jni_log_path, 0);

	/* This interface could expand with ABI negotiation, calbacks, etc. */
	SDL_Android_Init(env, cls);

	/* Run the application code! */
	int status;
	char *argv[3];
	argv[0] = strdup("SDL_app");
	argv[1] = strdup(log_path);

	env->ReleaseStringUTFChars(jni_log_path, log_path);

	status = SDL_main(2, argv, jvm);

	/* We exit here for consistency with other platforms. */
	exit(status);
}

/* vi: set ts=4 sw=4 expandtab: */
