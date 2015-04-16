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
	smac/dexml.c \
        smac/crypto.c \
        smac/nacl/src/crypto_box_curve25519xsalsa20poly1305_ref/keypair.c \
        smac/nacl/src/crypto_box_curve25519xsalsa20poly1305_ref/before.c \
        smac/nacl/src/crypto_box_curve25519xsalsa20poly1305_ref/after.c \
        smac/nacl/src/crypto_box_curve25519xsalsa20poly1305_ref/box.c \
        smac/nacl/src/crypto_core_hsalsa20_ref/core.c \
        smac/nacl/src/crypto_scalarmult_curve25519_ref/base.c \
        smac/nacl/src/crypto_scalarmult_curve25519_ref/smult.c \
        smac/nacl/src/crypto_secretbox_xsalsa20poly1305_ref/box.c \
        smac/nacl/src/crypto_onetimeauth_poly1305_ref/auth.c \
        smac/nacl/src/crypto_onetimeauth_poly1305_ref/verify.c \
        smac/nacl/src/crypto_verify_16_ref/verify.c \
        smac/nacl/src/crypto_stream_xsalsa20_ref/xor.c \
        smac/nacl/src/crypto_stream_xsalsa20_ref/stream.c \
        smac/nacl/src/crypto_core_salsa20_ref/core.c \
        smac/nacl/src/crypto_stream_salsa20_ref/xor.c \
        smac/nacl/src/crypto_stream_salsa20_ref/stream.c \
	smac/xml2recipe.c \
	smac/xhtml2recipe.c \
	smac/xmlparse.c \
	smac/xmlrole.c \
	smac/xmltok.c \
	smac/xmltok_impl.c \
	smac/xmltok_ns.c

LOCAL_CFLAGS := -I$(LOCAL_PATH)/smac/ -I$(LOCAL_PATH)/smac/nacl/include $(TARGET_GLOBAL_CFLAGS) $(PRIVATE_ARM_CFLAGS) -std=c99 -DHAVE_BCOPY
LOCAL_LDFLAGS := -llog
LOCAL_MODULE := libsmac
include $(BUILD_SHARED_LIBRARY)
