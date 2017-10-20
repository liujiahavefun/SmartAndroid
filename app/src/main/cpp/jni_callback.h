//
// Created by liujia on 16/8/27.
//

#ifndef CLOUDWOOD_ANDROID_NEW_JNI_CALLBACK_H
#define CLOUDWOOD_ANDROID_NEW_JNI_CALLBACK_H

#include "net_engine.h"

extern const char* JAVA_CALLBACK_CLASS;

class jni_callback
{
public:
    static jni_callback& instance();

public:
    void log(int level, const char* format, ...);
    void on_event(int conn_id, int event_id, long val);
    void on_data(int conn_id, int uri, const char* data, int len);

    template<typename ... Args>
    void call(const char* full_class_name, const char* name, Args... args);

    //int notify_conn_event(NetEngine::CNetEventConnState* state, NetEngine::Packet* pkt);

private:
    jni_callback();
    ~jni_callback();

    //noncopyable
    jni_callback(const jni_callback&) = delete;
    const jni_callback& operator=(const jni_callback&) = delete;
};

#endif //CLOUDWOOD_ANDROID_NEW_JNI_CALLBACK_H
