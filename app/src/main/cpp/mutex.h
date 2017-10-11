//
// Created by liujia on 16/8/17.
//

#ifndef CLOUDWOOD_ANDROID_NEW_MUTEX_H
#define CLOUDWOOD_ANDROID_NEW_MUTEX_H

#include <pthread.h>

class MutexLock
{
public:
#ifdef _WIN32
    MutexLock(const wchar_t* name);
#else
    MutexLock();
#endif
    ~MutexLock();

public:
    void lock();
    void unlock();

private:
#ifdef _WIN32
    //HANDLE lock_;
    CRITICAL_SECTION lock_;
#else
    pthread_mutex_t lock_;
#endif
};

class AutoLock
{
public:
    AutoLock(MutexLock* lock);
    AutoLock(MutexLock& lock);
    ~AutoLock();
private:
    MutexLock& lock_;
};

#endif //CLOUDWOOD_ANDROID_NEW_MUTEX_H
