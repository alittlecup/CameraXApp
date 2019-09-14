//
// Created by baole.huang on 2019-09-12.
//
#include <jni.h>
#ifndef CAMERA_X_APP_NATIVE_COLOR_H
#define CAMERA_X_APP_NATIVE_COLOR_H
#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT void JNICALL surfaceCreated(JNIEnv *, jobject, jint);

JNIEXPORT void JNICALL surfaceChanged(JNIEnv *, jobject, jint, jint);

JNIEXPORT void JNICALL onDrawFrame(JNIEnv *, jobject);
#ifdef __cplusplus
}
#endif
#endif //CAMERA_X_APP_NATIVE_COLOR_H
