# Use a more recent and supported toolchain version
NDK_TOOLCHAIN_VERSION := clang
APP_PLATFORM := android-29 # Use the latest Android platform
APP_STL := c++_shared # Use the shared C++ library for better performance

# Specify the desired ABIs for your app
APP_ABI := armeabi-v7a arm64-v8a x86 x86_64
