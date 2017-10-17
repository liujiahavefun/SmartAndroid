//
// Created by liujia on 16/8/27.
//

#include "jni_util.h"
#include <stdio.h>
#include <android/log.h>
#include "jni_callback.h"

JavaVM* g_JavaVM = NULL;
static jobject gClassLoader;
static jmethodID gFindClassMethod;

static pthread_key_t s_JniThreadKey;
//static pthread_once_t key_once;


static void JNI_OnThreadDestroyed(void* value)
{
    JNIEnv *env = (JNIEnv*)value;
    if (env != NULL && g_JavaVM != NULL) {
        g_JavaVM->DetachCurrentThread();
        pthread_setspecific(s_JniThreadKey, NULL);
    }
}

JNIEnv* getEnv()
{
    JNIEnv* env = (JNIEnv*)pthread_getspecific(s_JniThreadKey);
    if(env != NULL){
        return env;
    }

    if (g_JavaVM->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK)
    {
        int result = g_JavaVM->AttachCurrentThread(&env, NULL);
        if (result != JNI_OK){
            printf("failed to attach current native thread %lu \n", pthread_self());
        }
        else{
            printf("successfully attached current native thread %lu \n", pthread_self());
        }

        result = pthread_setspecific(s_JniThreadKey, (void *)env);
        if (result != 0){
            printf("failed to register for detach \n");
        }
        else{
            printf("successfully to register for detach \n");
        }

    }

    return env;
}

jint JNI_OnLoad_Impl(JavaVM* jvm, void* reserved) {
    g_JavaVM = jvm;
    JNIEnv *env = NULL;;
    if (g_JavaVM->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    if (pthread_key_create(&s_JniThreadKey, JNI_OnThreadDestroyed) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "NATIVE", "failed initializing pthread key");
    }

    //liujia: cache something....
    if (env != nullptr) {
        auto randomClass = env->FindClass(JAVA_CALLBACK_CLASS);
        jclass classClass = env->GetObjectClass(randomClass);
        auto classLoaderClass = env->FindClass("java/lang/ClassLoader");
        auto getClassLoaderMethod = env->GetMethodID(classClass, "getClassLoader", "()Ljava/lang/ClassLoader;");
        gClassLoader = env->NewGlobalRef(env->CallObjectMethod(randomClass, getClassLoaderMethod));
        gFindClassMethod = env->GetMethodID(classLoaderClass, "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    }

    return JNI_VERSION_1_6;
}

jclass findClass(JNIEnv* env, const char* name) {
    if(env == nullptr || name == NULL) {
        return nullptr;
    }

    jclass clazz = env->FindClass(name);
    if (clazz != nullptr) {
        return clazz;
    }

    return static_cast<jclass>(env->CallObjectMethod(gClassLoader, gFindClassMethod, env->NewStringUTF(name)));
}

bool get_obj_int_field(JNIEnv* env, jclass clazz, jobject obj, const char* field, int& val)
{
    if(env == NULL || clazz == NULL || field == NULL){
        return false;
    }
    jfieldID field_id = env->GetFieldID(clazz, field, "I");
    if (field_id == NULL){
        return false;
    }

    val = env->GetIntField(obj, field_id);
    return true;
}

bool get_obj_string_field(JNIEnv* env, jclass clazz, jobject obj, const char* field, std::string& val)
{
    if(env == NULL || clazz == NULL || obj == NULL || field == NULL){
        return false;
    }
    jfieldID field_id = env->GetFieldID(clazz, field, "Ljava/lang/String;");
    if (field_id == NULL){
        return false;
    }

    //liujia: 升级NDK后，这里要强转为jstring
    jstring jval = (jstring)env->GetObjectField(obj, field_id);
    if(jval == NULL){
        return false;
    }

    val = env->GetStringUTFChars(jval, NULL);
    env->DeleteLocalRef(jval);
    return true;
}

bool set_obj_int_field(JNIEnv* env, jclass clazz, jobject obj, const char* field, int val)
{
    if(env == NULL || clazz == NULL || obj == NULL || field == NULL){
        return false;
    }

    jfieldID field_id = env->GetFieldID(clazz, field, "I");
    if (field_id == NULL){
        return false;
    }

    env->SetIntField(obj, field_id, val);
    return true;
}

bool set_obj_long_field(JNIEnv* env, jclass clazz, jobject obj, const char* field, long val)
{
    if(env == NULL || clazz == NULL || field == NULL){
        return false;
    }

    jfieldID field_id = env->GetFieldID(clazz, field, "J");
    if (field_id == NULL){
        return false;
    }

    env->SetLongField(obj, field_id, val);
    return true;
}

bool set_obj_string_field(JNIEnv* env, jclass clazz, jobject obj, const char* field, const char* val)
{
    if(env == NULL || clazz == NULL || field == NULL || val == NULL){
        return false;
    }

    jfieldID field_id = env->GetFieldID(clazz, field, "Ljava/lang/String;");
    if (field_id == NULL){
        return false;
    }

    /*
    jclass strClass = env->FindClass("Ljava/lang/String;");
    jmethodID ctorID = env->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = env->NewByteArray(strlen(val));
    env->SetByteArrayRegion(bytes, 0, strlen(val), (jbyte*)val);
    jstring encoding = env->NewStringUTF("utf-8");
    jstring valData = (jstring)env->NewObject(strClass, ctorID, bytes, encoding);
    env->ReleaseStringUTF(encoding);
    */

    jstring jstr = env->NewStringUTF(val);
    env->SetObjectField(obj, field_id, jstr);
    env->DeleteLocalRef(jstr);
    return true;
}