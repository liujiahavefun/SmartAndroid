//
// Created by liujia on 16/8/27.
//

#ifndef CLOUDWOOD_ANDROID_NEW_JNI_CALLBACK_H
#define CLOUDWOOD_ANDROID_NEW_JNI_CALLBACK_H

#include "net_engine.h"

class jni_callback
{
public:
    static jni_callback& instance();

public:
    int log(int level, const char* format, ...);
    int notify_conn_event(NetEngine::CNetEventConnState* state, NetEngine::Packet* pkt);

    template<typename ... Args>
    void call(const char* name, Args... args);

private:
    jni_callback();
    ~jni_callback();

    //noncopyable
    jni_callback(const jni_callback&) = delete;
    const jni_callback& operator=(const jni_callback&) = delete;
};

#endif //CLOUDWOOD_ANDROID_NEW_JNI_CALLBACK_H
