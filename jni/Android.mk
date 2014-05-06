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
	smac/visualise.c

LOCAL_CFLAGS := -I$(LOCAL_PATH)/smac/ $(TARGET_GLOBAL_CFLAGS) $(PRIVATE_ARM_CFLAGS)
LOCAL_MODULE := smac
LOCAL_LDFLAGS := -Wl,--no-gc-sections
include $(BUILD_EXECUTABLE)
	
include $(CLEAR_VARS)
LOCAL_SRC_FILES := \
	smac/extract_instance.c

LOCAL_CFLAGS := -I$(LOCAL_PATH)/smac/ $(TARGET_GLOBAL_CFLAGS) $(PRIVATE_ARM_CFLAGS)
LOCAL_MODULE := extract_instance
LOCAL_LDFLAGS := -Wl,--no-gc-sections
include $(BUILD_EXECUTABLE)
