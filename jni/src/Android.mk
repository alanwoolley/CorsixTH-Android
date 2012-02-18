LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := appmain

SDL_PATH := ../SDL
LUA_PATH := ../LUA
SDL_MIXER_PATH :=../SDL_mixer
AGG_PATH := ../AGG
CORSIX_TH_SRC := CorsixTH/Src
LFS_SRC := LFS
LPEG_SRC := LPEG
FREETYPE_PATH := ../freetype

LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(SDL_PATH)/include \
					$(LOCAL_PATH)/$(AGG_PATH)/include \
					$(LOCAL_PATH)/$(FREETYPE_PATH)/include \
					$(LOCAL_PATH)/$(FREETYPE_PATH)/include/freetype \
					$(LOCAL_PATH)/$(FREETYPE_PATH)/include/config \
					$(LOCAL_PATH)/$(FREETYPE_PATH)/include/internal \
					$(LOCAL_PATH)/$(FREETYPE_PATH)/include/internal/services \
					$(LOCAL_PATH)/$(LUA_PATH) \
					$(LOCAL_PATH)/$(SDL_MIXER_PATH) \
					$(CORSIX_TH_SRC) \
					$(LFS_SRC) \
					$(LPEG)
					
LOCAL_CFLAGS := -DPLAY_MOD					

# Add your application source files here...
LOCAL_SRC_FILES := $(SDL_PATH)/src/main/android/SDL_android_main.cpp \
			appmain.cpp \
			$(CORSIX_TH_SRC)/main.cpp \
			$(CORSIX_TH_SRC)/bootstrap.cpp \
			$(CORSIX_TH_SRC)/th_lua.cpp \
			$(CORSIX_TH_SRC)/th.cpp \
			$(CORSIX_TH_SRC)/th_lua_map.cpp \
			$(CORSIX_TH_SRC)/random.c \
			$(CORSIX_TH_SRC)/th_pathfind.cpp\
			$(CORSIX_TH_SRC)/th_lua_gfx.cpp \
			$(CORSIX_TH_SRC)/th_map.cpp \
			$(CORSIX_TH_SRC)/th_lua_anims.cpp \
			$(CORSIX_TH_SRC)/th_gfx.cpp \
			$(CORSIX_TH_SRC)/th_lua_sound.cpp \
			$(CORSIX_TH_SRC)/th_gfx_sdl.cpp \
			$(CORSIX_TH_SRC)/th_lua_strings.cpp \
			$(CORSIX_TH_SRC)/run_length_encoder.cpp \
			$(CORSIX_TH_SRC)/th_lua_ui.cpp \
			$(CORSIX_TH_SRC)/th_sound.cpp \
			$(CORSIX_TH_SRC)/th_gfx_font.cpp \
			$(CORSIX_TH_SRC)/rnc.cpp \
			$(CORSIX_TH_SRC)/iso_fs.cpp \
			$(CORSIX_TH_SRC)/persist_lua.cpp \
			$(CORSIX_TH_SRC)/sdl_core.cpp \
			$(CORSIX_TH_SRC)/sdl_audio.cpp \
			$(CORSIX_TH_SRC)/sdl_wm.cpp \
			$(CORSIX_TH_SRC)/xmi2mid.cpp \
			$(LFS_SRC)/lfs.c \
			$(LFS_SRC)/lfs_ext.c \
			$(LPEG_SRC)/lpeg.c
			

LOCAL_SHARED_LIBRARIES := libLUA libAGG libSDL libSDL_image libSDL_mixer 
LOCAL_STATIC_LIBRARIES := libfreetype

LOCAL_LDLIBS := -lGLESv1_CM -llog 
LOCAL_LDLIBS += -L$(LOCAL_PATH)/libs/

include $(BUILD_SHARED_LIBRARY)
