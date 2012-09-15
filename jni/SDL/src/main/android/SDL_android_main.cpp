
/* Include the SDL main definition header */
#include "SDL_main.h"

/*******************************************************************************
                 Functions called by JNI
*******************************************************************************/
#include <jni.h>

// Called before SDL_main() to initialize JNI bindings in SDL library
extern "C" void SDL_Android_Init(JNIEnv* env, jclass cls);

// Library init
extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    return JNI_VERSION_1_4;
}

// Start up the SDL app
extern "C" void Java_uk_co_armedpineapple_corsixth_SDLActivity_nativeInit(JNIEnv* env, jclass cls, jstring path)
{
	const char *nativeString = env->GetStringUTFChars(path, 0);

	char* p = (char*)malloc(sizeof(nativeString));
	strcpy(p, nativeString);
	env->ReleaseStringUTFChars(path, nativeString);

    /* This interface could expand with ABI negotiation, calbacks, etc. */
    SDL_Android_Init(env, cls);

    /* Run the application code! */
    int status;
    char *argv[2];
    argv[0] = strdup("SDL_app");
    argv[1] = p;
    status = SDL_main(2, argv, env);

    /* We exit here for consistency with other platforms. */
    exit(status);
}

/* vi: set ts=4 sw=4 expandtab: */
