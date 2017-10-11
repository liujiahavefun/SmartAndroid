/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_smart_android_smartandroid_jni_JniManager */

#ifndef _INCLUDE_SMART_GO_
#define _INCLUDE_SMART_GO_
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    NetEngineStart
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_NetEngineStart
  (JNIEnv *, jobject);

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    NetEngineStop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_NetEngineStop
  (JNIEnv *, jobject);

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnCreate
 * Signature: (Lcom/smart/android/smartandroid/jni/ConnAttrWrapper;)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnCreate
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnConnect
 * Signature: (IIS)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnConnect
  (JNIEnv *, jobject, jint, jint, jshort);

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnClose
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnClose
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnSend
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnSend
  (JNIEnv *, jobject, jint, jbyteArray, jint);

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnSetNoDelay
 * Signature: (IZ)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnSetNoDelay
  (JNIEnv *, jobject, jint, jboolean);

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnAddTimer
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnAddTimer
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnRemoveTimer
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnRemoveTimer
  (JNIEnv *, jobject, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
