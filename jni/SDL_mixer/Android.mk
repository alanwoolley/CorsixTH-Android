LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := SDL_mixer

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/.. \
	$(LOCAL_PATH)/../SDL/include \
	$(LOCAL_PATH)/../mikmod/include \
	$(LOCAL_PATH)/timidity



LOCAL_CFLAGS := -DMID_MUSIC -DWAV_MUSIC -DOGG_MUSIC -DOGG_USE_TREMOR -DMOD_MUSIC -DUSE_TIMIDITY_MIDI

LOCAL_SRC_FILES := $(notdir $(filter-out %/playmus.c %/playwave.c, $(wildcard $(LOCAL_PATH)/*.c))) \
			$(addprefix timidity/, $(notdir $(wildcard $(LOCAL_PATH)/timidity/*.c)))
			
LOCAL_SHARED_LIBRARIES := SDL mikmod
LOCAL_STATIC_LIBRARIES := tremor
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
