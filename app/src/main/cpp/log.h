//
// Created by liujia on 16/8/17.
//

#ifndef CLOUDWOOD_ANDROID_NEW_LOG_H
#define CLOUDWOOD_ANDROID_NEW_LOG_H

//#include <iostream>
//#include <sstream>
#include <stdio.h>
#include <ctime>
#include <string>
#include <jni.h>
#include <android/log.h>
#include <string.h>

#define   LOG_TAG    "NATIVE"
#define   ANDROID_LOGI(FORMAT,...)  __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, FORMAT, VA_ARGS__)
#define   ANDROID_LOGE(FORMAT,...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, FORMAT, VA_ARGS__)
#define   PRINTF(FORMAT,...)          printf(FORMAT,__VA_ARGS__);

inline std::string formatedStamp()
{
    time_t now_time;
    now_time = time(NULL);
    char stamp[64];
    strftime(stamp, sizeof(stamp), "%Y-%m-%d %H:%M:%S ", localtime(&now_time));
    return stamp;
}

inline std::string format_to_string(const char *format, ...)
{
    char buf[1024];
    va_list args;
    va_start(args, format);
    vsnprintf(buf, sizeof(buf) - 1, format, args);
    va_end(args);
    return std::string(buf);
}

inline void LOG(android_LogPriority level, const char* format, va_list args)
{
    __android_log_vprint(level, LOG_TAG, format, args);
}

inline void LOGD(const char *format, ...)
{
    va_list args;
    va_start(args, format);
    LOG(ANDROID_LOG_DEBUG, format, args);
    va_end(args);
}

inline void LOGI(const char *format, ...)
{
    va_list args;
    va_start(args, format);
    LOG(ANDROID_LOG_INFO, format, args);
    va_end(args);
}

inline void LOGE(const char *format, ...)
{
    va_list args;
    va_start(args, format);
    LOG(ANDROID_LOG_ERROR, format, args);
    va_end(args);
}

inline void LOGF(const char *format, ...)
{
    va_list args;
    va_start(args, format);
    LOG(ANDROID_LOG_FATAL, format, args);
    va_end(args);
}

/*
inline void LOG(android_LogPriority level, const char *format, ...)
{
    char buf[1024];
    char* p = buf;
    va_list args;
    va_start(args, format);
    p += vsnprintf(p, sizeof(buf) - 5, format, args);
    va_end(args);
    *p++ = '\r';
    *p++ = '\n';
    *p++ = '\0';
    __android_log_print(level, LOG_TAG, "%s", buf);
    //printf("%s", buf);
}
*/
#endif //CLOUDWOOD_ANDROID_NEW_LOG_H
