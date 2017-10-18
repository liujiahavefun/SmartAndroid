//
// Created by liujia on 16/8/27.
//

/*
 * 老夫在这里说几句
 * 首先notify_conn_event这个函数已经注释掉了，因为这个函数有个问题，就是从java线程里从jni回调java是ok的
 * 但在native线程里，如IOEngine线程里，FindClass就找不到类了，怀疑是JNIEnv里的classloader有问题。。。
 * 但是我保存了全局的classloader，参考jni_util::findClass，但NewStringUTF会崩溃，不知道啥原因。。。
 * 所以这个问题，老夫细细从头梳理，应该这样做
 * 1）对于native线程，启动时(或者getenv时)，调用AttachCurrentThread去将JNIEnv attach到当前线程(顺便也获取了当前线程的JNIEnv，这个对象是与线程绑定的)
 * 2）java通过jni调用native函数时，必须通过JniManager对象调用，并传递了这个对象作为第二个参数。见NetEngineStart函数
 *    我们要做的就是保存这个对象的一个全局引用，即g_JNICallbackObj
 * 3）回调时，显示百分百无法通过env->FindClass("com/smart/android/smartandroid/jni/JniEventHandler")找打回调类
 *    我们这样，通过GetObjectClass，获得这个回调对象的类
 * 4）然后拿到jclass了，就好办了，就拿各种method，再回调就好了
 */

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
void jni_callback::log(int level, const char* format, ...)
{
    //get JNIEnv
    JNIEnv* env = getEnv();
    if(env == NULL) {
        ANDROID_LOGE("failed to get JNIEnv");
        return;
    }

    //find class
    if(g_JNICallbackObj == nullptr) {

    }
    jclass clazz = env->GetObjectClass(g_JNICallbackObj);
    if (clazz == NULL) {
        ANDROID_LOGE("failed to find callback class");
        return;
    }

    //get class's static method
    //注意这个函数签名 (Ljava/lang/String;I)V 表面函数返回值V(void),两个参数Ljava/lang/String（String）， I(int)
    //注意Ljava/lang/String后面要加上";"，如果是（int，String）则写成ILjava/lang/String;
    jmethodID method_id = env->GetStaticMethodID(clazz, "OnLog", "(ILjava/lang/String;)V");
    if (method_id == NULL) {
        ANDROID_LOGE("failed to find static method [NativeLog]");
        return ;
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

    return;
}

void jni_callback::on_event(int conn_id, int event_id, long val)
{
    //get JNIEnv
    JNIEnv* env = getEnv();
    if(env == NULL){
        LOGE("failed to get JNIEnv");
        return;
    }

    //find class
    jclass clazz = env->GetObjectClass(g_JNICallbackObj);
    if (clazz == NULL) {
        LOGE("failed to find callback class");
        return;
    }

    //get method
    jmethodID method_id = env->GetStaticMethodID(clazz, "OnEvent", "(IIJ)V");
    if (method_id == NULL) {
        LOGE("failed to find static method [OnEvent]");
        return;
    }

    //call callback fucntion
    env->CallStaticVoidMethod(clazz, method_id, conn_id, event_id, val);

    //release ref
    env->DeleteLocalRef(clazz);
}

void jni_callback::on_data(int conn_id, const char* data, int len) {
    //get JNIEnv
    JNIEnv* env = getEnv();
    if(env == NULL){
        LOGE("failed to get JNIEnv");
        return;
    }

    //find class
    jclass clazz = env->GetObjectClass(g_JNICallbackObj);
    if (clazz == NULL) {
        LOGE("failed to find callback class");
        return;
    }

    //get method
    jmethodID method_id = env->GetStaticMethodID(clazz, "OnData", "(I[BI)V");
    if (method_id == NULL) {
        LOGE("failed to find static method [OnEvent]");
        return;
    }

    //prepare data buffer
    jbyteArray jbarray = env->NewByteArray(len);
    env->SetByteArrayRegion(jbarray, 0, len, (jbyte*)data);


    //call callback fucntion
    env->CallStaticVoidMethod(clazz, method_id, conn_id, jbarray, len);

    //release ref
    env->DeleteLocalRef(clazz);
    env->DeleteLocalRef(jbarray);
}

/*
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
*/