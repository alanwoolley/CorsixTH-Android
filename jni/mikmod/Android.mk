LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := mikmod

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include

LOCAL_CFLAGS := -DMIKMOD_H=$(LOCAL_PATH)/include/mikmod.h

LOCAL_SRC_FILES := \
	drivers/drv_AF.c \
	drivers/drv_aix.c \
	drivers/drv_alsa.c \
	drivers/drv_esd.c \
	drivers/drv_hp.c \
	drivers/drv_nos.c \
	drivers/drv_pipe.c \
	drivers/drv_oss.c \
	drivers/drv_sam9407.c \
	drivers/drv_sgi.c \
	drivers/drv_stdout.c \
	drivers/drv_sun.c \
	drivers/drv_ultra.c \
	drivers/drv_wav.c \
	loaders/load_669.c \
	loaders/load_amf.c \
	loaders/load_dsm.c \
	loaders/load_far.c \
	loaders/load_gdm.c \
	loaders/load_it.c \
	loaders/load_imf.c \
	loaders/load_m15.c \
	loaders/load_med.c \
	loaders/load_mod.c \
	loaders/load_mtm.c \
	loaders/load_okt.c \
	loaders/load_s3m.c \
	loaders/load_stm.c \
	loaders/load_stx.c \
	loaders/load_ult.c \
	loaders/load_uni.c \
	loaders/load_xm.c \
	mmio/mmalloc.c \
	mmio/mmerror.c \
	mmio/mmio.c \
	playercode/mdriver.c \
	playercode/mdreg.c \
	playercode/mdulaw.c \
	playercode/mloader.c \
	playercode/mlreg.c \
	playercode/mlutil.c \
	playercode/mplayer.c \
	playercode/munitrk.c \
	playercode/mwav.c \
	playercode/npertab.c \
	playercode/sloader.c \
	playercode/virtch.c \
	playercode/virtch2.c \
	playercode/virtch_common.c \
	posix/strcasecmp.c \
	posix/strdup.c \
	posix/strstr.c

include $(BUILD_SHARED_LIBRARY)
