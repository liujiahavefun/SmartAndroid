//
// Created by liujia on 16/8/21.
//

#ifndef CLOUDWOOD_ANDROID_NEW_TIME_UTIL_H
#define CLOUDWOOD_ANDROID_NEW_TIME_UTIL_H

#include <time.h>

/*
* return tick count in millisecond
*
*/
inline unsigned long GetTickCount()
{
    unsigned long tick = 0;
    struct timespec ts;
    if(clock_gettime(CLOCK_MONOTONIC, &ts) == 0) {
        tick = (ts.tv_sec * 1000 + ts.tv_nsec/1000000);
    }
    return tick;
}

/*
* return current time stamp in millisecond
*
*/
inline unsigned long GetTimestamp()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (tv.tv_sec * 1000) + (tv.tv_usec / 1000);
}

#endif //CLOUDWOOD_ANDROID_NEW_TIME_UTIL_H
