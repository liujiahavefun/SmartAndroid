//
// Created by liujia on 16/8/17.
//

#include "smartgo.h"
#include <android/log.h>
#include <cstdlib>
#include "net_engine.h"
#include "log.h"
#include "jni_util.h"
#include "jni_callback.h"
#include "util/string_util.h"

jint JNI_OnLoad(JavaVM* jvm, void* reserved)
{
    return JNI_OnLoad_Impl(jvm, reserved);
}

//jint JNI_OnUnload(JavaVM* vm, void* reserved)
//{
//    JavaVM* g_JavaVM = NULL;
//}

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    NetEngineStart
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_NetEngineStart
  (JNIEnv * env, jobject thiz)
{
    LOGD("NetEngineStart");
    g_JNICallbackObj = env->NewGlobalRef(thiz);
    return NetEngine::NetEngineStart();
}

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    NetEngineStop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_NetEngineStop
  (JNIEnv * env, jobject thiz)
{
    LOGD("NetEngineStop");
    NetEngine::NetEngineStop();

    env->DeleteGlobalRef(g_JNICallbackObj);
    g_JNICallbackObj = nullptr;

    return 0;
}

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnCreate
 * Signature: (Lcom/smart/android/smartandroid/jni/ConnAttrWrapper;)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnCreate
  (JNIEnv* env, jobject thiz, jobject attr)
{
    LOGD("ConnCreate");

    //liujia: attr这个object要不是com/smart/android/smartandroid/jni/ConnAttrWrapper，就把调用者拉出去砍了
    //jclass clazz =env->FindClass("com/smart/android/smartandroid/jni/ConnAttrWrapper");
    jclass clazz = env->GetObjectClass(attr);
    if(clazz == NULL){
        return -1;
    }

    int conn_type = 0;
    if(!get_obj_int_field(env, clazz, attr, "ConnType", conn_type)){
        return -1;
    }

    std::string remote_ip;
    if(!get_obj_string_field(env, clazz, attr, "RemoteIP", remote_ip)){
        return -1;
    }

    std::string remote_port;
    if(!get_obj_string_field(env, clazz, attr, "RemotePort", remote_port)){
        return -1;
    }

    std::string local_ip;
    if(!get_obj_string_field(env, clazz, attr, "LocalIP", local_ip)){
        return -1;
    }

    std::string local_port;
    if(!get_obj_string_field(env, clazz, attr, "LocalPort", local_port)){
        return -1;
    }

    LOGD("ConnCreate, type: %d, remote ip: %s, remote port: %s, local ip: %s, local port: %s", conn_type, remote_ip.c_str(), remote_port.c_str(), local_ip.c_str(), local_port.c_str());
    env->DeleteLocalRef(clazz);

    if(conn_type != NetEngine::ConnAttr::CONN_TCP && conn_type != NetEngine::ConnAttr::CONN_UDP){
        return -2;
    }

    if(conn_type == NetEngine::ConnAttr::CONN_UDP) {
        if(local_ip.empty() || local_port.empty()) {
            return -2;
        }
    }

    NetEngine::ConnAttr connAttr;
    connAttr.ConnType = conn_type;
    inet_pton(AF_INET, remote_ip.c_str(), &connAttr.RemoteIP);
    connAttr.RemotePort = htons(string_util::str_to_num<short>(remote_port.c_str()));
    inet_pton(AF_INET, local_ip.c_str(), &connAttr.LocalIP);
    connAttr.LocalPort = htons(string_util::str_to_num<short>(local_port.c_str()));

    return NetEngine::ConnCreate(&connAttr);
}

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnConnect
 * Signature: (IIS)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnConnect
  (JNIEnv *, jobject, jint conn_id, jint ip, jshort port)
{
    LOGD("ConnConnect");
    return NetEngine::ConnConnect(conn_id, ip, port);
}

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnClose
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnClose
  (JNIEnv * env, jobject thiz, jint conn_id)
{
    LOGD("ConnClose");
    return NetEngine::ConnClose(conn_id);
}

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnSend
 * Signature: (I[B)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnSend
  (JNIEnv * env, jobject thiz, jint conn_id, jint uri, jbyteArray jbarray, jint len)
{
    LOGD("ConnSend");

    if(len == 0) {
        return -1;
    }

    //这里用GetByteArrayElements，本质就是增加JVM堆内存的引用计数，防止老夫在native这里正用着这块内存呢，然后被GC了
    //因为这里就用一下，反正也要拷贝到Packet里，就不用GetByteArrayRegion()，这个函数会做一次内存拷贝，把JVM内存拷贝到native堆里，没必要
    const char* buffer = (const char*)env->GetByteArrayElements(jbarray, NULL);
    if(buffer == NULL) {
        return -1;
    }

    //发包
    NetEngine::Packet* pk = NetEngine::PacketAlloc(uri, buffer, len);
    NetEngine::ConnSend(conn_id, pk);
    NetEngine::PacketRelease(pk);

    //不用忘了减引用计数
    env->ReleaseByteArrayElements(jbarray, (jbyte*)buffer, 0);

    return 0;
}

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnSetNoDelay
 * Signature: (IZ)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnSetNoDelay
  (JNIEnv *, jobject, jint conn_id, jboolean flag)
{
    LOGD("ConnSetNoDelay");
    return NetEngine::ConnSetNoDelay(conn_id, flag);
}

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnAddTimer
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnAddTimer
  (JNIEnv *, jobject, jint conn_id, jint timer_id, jint interval)
{
    LOGD("ConnAddTimer");
    return NetEngine::ConnAddTimer(conn_id, timer_id, interval);
}

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    ConnRemoveTimer
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_smart_android_smartandroid_jni_JniManager_ConnRemoveTimer
  (JNIEnv *, jobject, jint conn_id, jint timer_id)
{
    LOGD("ConnRemoveTimer");
    return NetEngine::ConnRemoveTimer(conn_id, timer_id);
}

/*
 * Class:     com_smart_android_smartandroid_jni_JniManager
 * Method:    stringFromJNI
 * Signature: ()I
 */
JNIEXPORT jstring JNICALL Java_com_smart_android_smartandroid_jni_JniManager_stringFromJNI(
        JNIEnv* env,
        jobject /* this */)
{
#if defined(__arm__)
    #if defined(__ARM_ARCH_7A__)
    #if defined(__ARM_NEON__)
      #if defined(__ARM_PCS_VFP)
        #define ABI "armeabi-v7a/NEON (hard-float)"
      #else
        #define ABI "armeabi-v7a/NEON"
      #endif
    #else
      #if defined(__ARM_PCS_VFP)
        #define ABI "armeabi-v7a (hard-float)"
      #else
        #define ABI "armeabi-v7a"
      #endif
    #endif
  #else
   #define ABI "armeabi"
  #endif
#elif defined(__i386__)
#define ABI "x86"
#elif defined(__x86_64__)
#define ABI "x86_64"
#elif defined(__mips64)  /* mips64el-* toolchain defines __mips__ too */
#define ABI "mips64"
#elif defined(__mips__)
#define ABI "mips"
#elif defined(__aarch64__)
#define ABI "arm64-v8a"
#else
#define ABI "unknown"
#endif

    //std::string hello = "Hello from C++";
    std::string hello = std::string("客官你好, cpu is ") + ABI;
    return env->NewStringUTF(hello.c_str());
}