LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := lodepng

LOCAL_CFLAGS := -I$(LOCAL_PATH)

LOCAL_SRC_FILES := lodepng.cpp

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)

include $(BUILD_STATIC_LIBRARY)

