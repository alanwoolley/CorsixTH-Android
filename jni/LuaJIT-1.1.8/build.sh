NDK=/cygdrive/d/Android/android-ndk-r7
NDKABI=8
NDKVER=$NDK/toolchains/arm-linux-androideabi-4.4.3
NDKP=$NDKVER/prebuilt/linux-x86/bin/arm-linux-androideabi-
NDKF="--sysroot $NDK/platforms/android-$NDKABI/arch-arm"
make linux HOST_CC="gcc -m32" CROSS=$NDKP TARGET_FLAGS="$NDKF" TARGET=arm
