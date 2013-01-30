LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libSDL_gfx

LOCAL_CFLAGS := -I$(LOCAL_PATH) -I$(LOCAL_PATH)/.. \
                -I$(LOCAL_PATH)/../SDL/include -O3

LOCAL_CPP_EXTENSION := .cpp

# Note this simple makefile var substitution, you can find even simpler examples in different Android projects
LOCAL_SRC_FILES := $(notdir $(wildcard $(LOCAL_PATH)/*.c))

LOCAL_SHARED_LIBRARIES := SDL

include $(BUILD_STATIC_LIBRARY)
