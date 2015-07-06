/*
    SDL_android_main.c, placed in the public domain by Sam Lantinga  3/13/14
*/
#include "../../SDL_internal.h"

#ifdef __ANDROID__

/* Include the SDL main definition header */
#include "SDL_main.h"

/*******************************************************************************
                 Functions called by JNI
*******************************************************************************/
#include <jni.h>

/* Called before SDL_main() to initialize JNI bindings in SDL library */
extern "C" void SDL_Android_Init(JNIEnv* env, jclass cls);

static JavaVM* jvm;

// Library init
extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	jvm = vm;
	return JNI_VERSION_1_4;
}

/* Start up the SDL app */
extern "C" int Java_uk_co_armedpineapple_cth_SDLActivity_nativeInit(JNIEnv* env, jclass cls, jobjectArray array, jobject configuration)
{
    int i;
    int argc;
    int status;

    /* This interface could expand with ABI negotiation, callbacks, etc. */
    SDL_Android_Init(env, cls);

    SDL_SetMainReady();

    /* Prepare the arguments. */

    int len = env->GetArrayLength(array);
    char* argv[1 + len + 1];
    argc = 0;
    /* Use the name "app_process" so PHYSFS_platformCalcBaseDir() works.
       https://bitbucket.org/MartinFelis/love-android-sdl2/issue/23/release-build-crash-on-start
     */
    argv[argc++] = SDL_strdup("app_process");
    for (i = 0; i < len; ++i) {
        const char* utf;
        char* arg = NULL;
        jstring string = (jstring)  env->GetObjectArrayElement(array, i);
        if (string) {
            utf = env->GetStringUTFChars(string, 0);
            if (utf) {
                arg = SDL_strdup(utf);
                env->ReleaseStringUTFChars(string, utf);
            }
            env->DeleteLocalRef(string);
        }
        if (!arg) {
            arg = SDL_strdup("");
        }
        argv[argc++] = arg;
    }
    argv[argc] = NULL;


    /* Run the application. */

    status = SDL_main(argc, argv, jvm, configuration);

    /* Release the arguments. */

    for (i = 0; i < argc; ++i) {
        SDL_free(argv[i]);
    }

    /* Do not issue an exit or the whole application will terminate instead of just the SDL thread */
    /* exit(status); */

    return status;
}

#endif /* __ANDROID__ */

/* vi: set ts=4 sw=4 expandtab: */
