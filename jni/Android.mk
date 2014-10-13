LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := \
        smac/arithmetic.c \
	smac/case.c \
	smac/charset.c \
	smac/entropyutil.c \
	smac/gsinterpolative.c \
	smac/length.c \
	smac/lowercasealpha.c \
	smac/main.c \
	smac/nonalpha.c \
	smac/packed_stats.c \
	smac/packedascii.c \
	smac/recipe.c \
	smac/smac.c \
	smac/unicode.c \
	smac/visualise.c \
	smac/jni.c \
	smac/map.c \
	smac/md5.c \
	smac/dexml.c

LOCAL_CFLAGS := -I$(LOCAL_PATH)/smac/ $(TARGET_GLOBAL_CFLAGS) $(PRIVATE_ARM_CFLAGS)
LOCAL_LDFLAGS := -llog
LOCAL_MODULE := libsmac

include $(BUILD_SHARED_LIBRARY)
include $(CLEAR_VARS)
LOCAL_SRC_FILES := \
        smac/arithmetic.c \
	smac/case.c \
	smac/charset.c \
	smac/entropyutil.c \
	smac/gsinterpolative.c \
	smac/length.c \
	smac/lowercasealpha.c \
	smac/main.c \
	smac/nonalpha.c \
	smac/packed_stats.c \
	smac/packedascii.c \
	smac/recipe.c \
	smac/smac.c \
	smac/unicode.c \
	smac/visualise.c \
	smac/jni.c \
	smac/map.c \
	smac/md5.c \
	smac/dexml.c

LOCAL_CFLAGS := -I$(LOCAL_PATH)/smac/ $(TARGET_GLOBAL_CFLAGS) $(PRIVATE_ARM_CFLAGS) -fPIE
LOCAL_LDFLAGS := -llog -fPIE -pie
LOCAL_MODULE := smacbin
include $(BUILD_EXECUTABLE)

