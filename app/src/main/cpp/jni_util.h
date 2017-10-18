//
// Created by liujia on 16/8/27.
//

#ifndef CLOUDWOOD_ANDROID_NEW_JVMUTIL_H
#define CLOUDWOOD_ANDROID_NEW_JVMUTIL_H

#include <jni.h>
#include <pthread.h>
#include <string>

//global JavaVM pointer
extern JavaVM* g_JavaVM;

//global JAVA callback object
extern jobject g_JNICallbackObj;

//get JNIEnv in any thread context
JNIEnv* getEnv();

//implementation of JNI_OnLoad when our .so lib is loaded by JVM
jint JNI_OnLoad_Impl(JavaVM* vm, void* reserved);

//find class
//jclass findClass(JNIEnv* env, const char* name);

//get/set object field
bool get_obj_int_field(JNIEnv* env, jclass clazz, jobject obj, const char* field, int& val);
bool get_obj_string_field(JNIEnv* env, jclass clazz, jobject obj, const char* field, std::string& val);

bool set_obj_int_field(JNIEnv* env, jclass clazz, jobject obj, const char* field, int val);
bool set_obj_long_field(JNIEnv* env, jclass clazz, jobject obj, const char* field, long val);
bool set_obj_string_field(JNIEnv* env, jclass clazz, jobject obj, const char* field, const char* val);

#endif //CLOUDWOOD_ANDROID_NEW_JVMUTIL_H
