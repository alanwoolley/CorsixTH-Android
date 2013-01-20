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
extern "C" void Java_uk_co_armedpineapple_cth_SDLActivity_nativeInit(
		JNIEnv* env, jclass cls, jobject configuration,
		jstring jni_loadgame_path) {

	const char* loadgame_path = env->GetStringUTFChars(jni_loadgame_path, 0);

	/* This interface could expand with ABI negotiation, callbacks, etc. */
	SDL_Android_Init(env, cls);

	/* Run the application code! */
	int status;
	char *argv[3];
	int argc = 1;
	argv[0] = strdup("SDL_app");
	if (strlen(loadgame_path) > 1) {
		char* loadstr = (char*) malloc(
				(8 + strlen(loadgame_path)) * sizeof(char));
		strcpy(loadstr, "--load=");
		loadstr = strcat(loadstr, loadgame_path);
		argv[1] = loadstr;
		argc = 2;
	}

	status = SDL_main(argc, argv, jvm, configuration);

	/* We exit here for consistency with other platforms. */
	exit(status);
}

/* vi: set ts=4 sw=4 expandtab: */
