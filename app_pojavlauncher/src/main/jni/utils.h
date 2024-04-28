#pragma once

#include <stdbool.h>
#include <jni.h>
#include <stdlib.h>
#include <string.h>

char** convert_to_char_array(JNIEnv *env, jobjectArray jstringArray);
jobjectArray convert_from_char_array(JNIEnv *env, char **charArray, jsize num_rows);
void free_char_array(JNIEnv *env, jobjectArray jstringArray, const char **charArray);
jstring convertStringJVM(JNIEnv* srcEnv, JNIEnv* dstEnv, jstring srcStr);

void hookExec();
void installLinkerBugMitigation();
void installEMUIIteratorMititgation();

JNIEXPORT jstring JNICALL
Java_org_lwjgl_glfw_CallbackBridge_nativeClipboard(JNIEnv* env, jclass clazz, jint action, jbyteArray copySrc) {
    // implementation here
}

char** convert_to_char_array(JNIEnv *env, jobjectArray jstringArray) {
    // implementation here
}

jobjectArray convert_from_char_array(JNIEnv *env, char **charArray, jsize num_rows) {
    // implementation here
}

void free_char_array(JNIEnv *env, jobjectArray jstringArray, const char **charArray) {
    // implementation here
}

jstring convertStringJVM(JNIEnv* srcEnv, JNIEnv* dstEnv, jstring srcStr) {
    // implementation here
}

void hookExec() {
    // implementation here
}

void installLinkerBugMitigation() {
    // implementation here
}

void installEMUIIteratorMititgation() {
    // implementation here
}
