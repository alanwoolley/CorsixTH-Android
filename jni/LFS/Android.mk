LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := LFS

LOCAL_CFLAGS := -I$(LOCAL_PATH) -DHAVE_ALLOCA_H

# Note this simple makefile var substitution, you can find even simpler examples in different Android projects
LOCAL_SRC_FILES := lfs.c

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)


LOCAL_SHARED_LIBRARIES := libLUA
include $(BUILD_STATIC_LIBRARY)

