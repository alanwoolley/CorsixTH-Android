#!/bin/bash

NDK=/usr/lib/android-sdk/ndk/25.2.9519653
PREBUILT=$NDK/toolchains/llvm/prebuilt/linux-x86_64
SYSROOT=$PREBUILT/sysroot
PKG_CONFIG=$(which pkg-config)
BASE_DIR="$( cd "$( dirname "$0" )" && pwd )"
BUILD_DIR=${BASE_DIR}/build
OUTPUT_DIR=${BASE_DIR}/output


function build
{
./configure --target-os=android \
    --prefix=$PREFIX \
    --enable-cross-compile \
    --arch=$ARCH \
    --cc=$PREBUILT/bin/$ABI-linux-$ANDROIDVER-clang \
    --cxx=$PREBUILT/bin/$ABI-linux-$ANDROIDVER-clang++ \
    --ld=$PREBUILT/bin/$ABI-linux-$ANDROIDVER-clang \
    --cross-prefix=$PREBUILT/bin/$ABI-linux-$ANDROIDVER- \
    --nm=$PREBUILT/bin/llvm-nm \
    --ar=$PREBUILT/bin/llvm-ar \
    --as=$PREBUILT/bin/$ABI-linux-$ANDROIDVER-clang \
    --ranlib=$PREBUILT/bin/llvm-ranlib \
    --strip=$PREBUILT/bin/llvm-strip \
    --sysroot=$SYSROOT \
    --extra-cflags="-O3 -fpic $OPTIMIZE_CFLAGS" \
    --disable-shared \
    --enable-static \
	--disable-asm \
	--disable-everything \
    --disable-ffplay \
	--enable-version3 \
    --enable-avformat \
	--enable-swresample \
	--enable-protocol=file \
	--enable-protocol=pipe \
	--enable-protocol=cache \
	--enable-decoder=bink \
	--enable-demuxer=bink \
	--enable-demuxer=smacker \
	--enable-decoder=binkaudio_dct \
	--enable-decoder=binkaudio_rdft \
	--enable-decoder=smacker \
	--enable-decoder=smackaud \
    --disable-network \
    --disable-avdevice \
	--disable-postproc \
	--disable-avfilter \
    --disable-vulkan \
    --disable-ffprobe \
    --pkg-config=$PKG_CONFIG \
    $ADDITIONAL_CONFIGURE_FLAG

make clean
make -j install
#$PREBUILT/bin/$ABI-linux-$ANDROIDVER-ar d libavcodec/libavcodec.a inverse.o
#$PREBUILT/bin/$ABI-linux-$ANDROIDVER-ld -rpath-link=$SYSROOT/usr/lib -L$SYSROOT/usr/lib  -soname libffmpeg.so -shared -nostdlib  -z,noexecstack -Bsymbolic --whole-archive --no-undefined -o $PREFIX/libffmpeg.so libavcodec/libavcodec.a libavformat/libavformat.a libavutil/libavutil.a libswscale/libswscale.a libswresample/libswresample.a -lc -lm -lz -ldl -llog  --warn-once
}


#arm v7
ABI=armv7a
ARCH=arm
OPTIMIZE_CFLAGS=
PREFIX=./android/armeabi-v7a
ANDROIDVER=androideabi27
ADDITIONAL_CONFIGURE_FLAG=
build

#arm64-v8a
ARCH=aarch64
ABI=aarch64
OPTIMIZE_CFLAGS=
PREFIX=./android/arm64-v8a
ANDROIDVER=android27
ADDITIONAL_CONFIGURE_FLAG=
build

#x86
ARCH=i686
ABI=i686
OPTIMIZE_CFLAGS=
PREFIX=./android/x86
ANDROIDVER=android27
ADDITIONAL_CONFIGURE_FLAG=
build

#x86_64
ARCH=x86_64
ABI=x86_64
OPTIMIZE_CFLAGS=
PREFIX=./android/x86_64
ANDROIDVER=android27
ADDITIONAL_CONFIGURE_FLAG=
build

