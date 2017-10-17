//
// Created by liujia on 16/8/27.
//

#include <jni.h>
#include <string>
#include "log.h"
#include "jni_util.h"
#include "jni_callback.h"

using namespace NetEngine;

/*
JNI官方规范： http://blog.csdn.net/a345017062/article/details/8068925
JNI方法参数签名
Z   boolean
B   byte
C   char
S   short
I   int
J   long
F   float
D   double
[B  byte[]
Ljava/lang/String;  String
L类名 object   例如 Lcom/cloudywood/ip/jni/ConnEventWrapper
[类型   Arrays  例如 byte[] 就是 [B
函数 (arg1arg2arg3)revVal   特例是 <init>表示构造函数，signature是(arg1arg2arg3)V，因为构造函数无返回值

1）FindClass / GetObjectClass(obj);
2）GetStaticMethodID(class, name, signature)/GetMethodID(class, name, signature) --这个必须是GetObjectClass返回的针对具体obj的class
   GetFieldID（class, name, signature)
3) GetStaticMethodID GetMethodID  GetFieldID GetObjectField
4) CallVoidMethod                   CallStaticVoidMethod
   CallIntMethod                     CallStaticVoidMethod
   CallBooleanMethod              CallStaticVoidMethod
   CallByteMethod                   CallStaticVoidMethod
*/

const char* JAVA_CALLBACK_CLASS = "com/smart/android/smartandroid/jni/JniEventHandler";

jni_callback& jni_callback::instance()
{
    static jni_callback instance;
    return instance;
}

jni_callback::jni_callback()
{}

jni_callback::~jni_callback()
{}

template<typename ... Args>
void jni_callback::call(const char* func_name, Args... args)
{
    return;
}

//liujia: TODO, 提前返回的话有资源泄露的可能，goto凑活一下可以解决
int jni_callback::log(int level, const char* format, ...)
{
    // get JNIEnv
    JNIEnv* env = getEnv();
    if(env == NULL) {
        return 1;
    }

    // find class
    jclass clazz = env->FindClass(JAVA_CALLBACK_CLASS);
    //jclass clazz = findClass(env, JAVA_CALLBACK_CLASS);
    if (clazz == NULL) {
        printf("failed to find class %s", JAVA_CALLBACK_CLASS);
        return 2;
    }

    // get class's static method
    //注意这个函数签名 (Ljava/lang/String;I)V 表面函数返回值V(void),两个参数Ljava/lang/String（String）， I(int)
    //注意Ljava/lang/String后面要加上";"，如果是（int，String）则写成ILjava/lang/String;
    jmethodID method_id = env->GetStaticMethodID(clazz, "NativeLog", "(ILjava/lang/String;)V");
    if (method_id == NULL) {
        printf("failed to find static method [NativeLog]");
        return 3;
    }

    char buf[1024];
    va_list args;
    va_start(args, format);
    vsnprintf(buf, sizeof(buf) - 1, format, args);
    va_end(args);

    jstring jstr_arg = env->NewStringUTF(buf);

    //call class method
    env->CallStaticVoidMethod(clazz, method_id, level, jstr_arg);

    // 删除局部引用
    env->DeleteLocalRef(clazz);
    env->DeleteLocalRef(jstr_arg);

    return 0;
}

int jni_callback::notify_conn_event(CNetEventConnState* state, NetEngine::Packet* pkt)
{
    if(state == NULL){
        return -1;
    }

    // get JNIEnv
    JNIEnv* env = getEnv();
    if(env == NULL){
        printf("failed to get JNIEnv");
        return 1;
    }

    // find class
    jclass clazz = env->FindClass(JAVA_CALLBACK_CLASS);
    //jclass clazz = findClass(env, JAVA_CALLBACK_CLASS);
    if (clazz == NULL) {
        printf("failed to find class %s", JAVA_CALLBACK_CLASS);
        return 2;
    }

    // get class's static method
    jmethodID method_id = env->GetStaticMethodID(clazz, "OnNetEvent", "(Lcom/smart/android/smartandroid/jni/ConnEventWrapper;[BI)V");
    if (method_id == NULL) {
        printf("failed to find static method [OnNetEvent]");
        return 3;
    }

    jclass clazz_event = env->FindClass("com/smart/android/smartandroid/jni/ConnEventWrapper");
    if (clazz_event == NULL) {
        printf("failed to find class com/smart/android/smartandroid/jni/ConnEventWrappers");
        return 4;
    }
    jmethodID method_id_cons_event = env->GetMethodID(clazz_event, "<init>", "()V");
    if (method_id_cons_event == NULL) {
        printf("failed to find ctor method for class com/smart/android/smartandroid/jni/ConnEventWrappers");
        return 5;
    }

    jobject obj_event = env->NewObject(clazz_event, method_id_cons_event);
    if (obj_event == NULL) {
        printf("failed to new object");
        return 6;
    }

    if(!set_obj_int_field(env, clazz_event, obj_event, "connId", state->ConnId)){
        printf("failed to set field value for connId");
        return 7;
    }
    if(!set_obj_int_field(env, clazz_event, obj_event, "eventType", state->EvtType)){
        printf("failed to set field value for eventType");
        return 8;
    }

    if(!set_obj_long_field(env, clazz_event, obj_event, "retVal", state->RetVal)){
        printf("failed to set field value for retVal");
        return 9;
    }

    if(!set_obj_int_field(env, clazz_event, obj_event, "connState", state->state)){
        printf("failed to set field value for connState");
        return 10;
    }

    if(!set_obj_long_field(env, clazz_event, obj_event, "timestamp", state->timestamp)){
        printf("failed to set field value for timestamp");
        return 11;
    }

    jbyteArray data = NULL;
    int data_len = 0;
    if(pkt != NULL){
        data_len = pkt->_dataLen;
        data = env->NewByteArray(data_len);
        //jbyte* pbuf = (jbyte*)malloc(data_len);
        //memcpy(pbuf, pkt->_data, data_len);
        env->SetByteArrayRegion(data, 0, data_len, (jbyte*)pkt->_data); //liujia: 升级NDK后，这里要强转一下才能编过
        //free(pbuf);
    }

    env->CallStaticVoidMethod(clazz, method_id, obj_event, data, data_len);

    env->DeleteLocalRef(clazz);
    env->DeleteLocalRef(clazz_event);
    if(data != NULL){
        env->DeleteLocalRef(data);
    }

    return 0;
}