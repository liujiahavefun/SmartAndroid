//
// Created by liujia on 16/8/17.
//

#include "mutex.h"
#include <string>

/*
* Lock implementation for Wi32 and Linux
* or include <mutex> to use c++11 locks
*/
#ifdef _WIN32
MutexLock::MutexLock(const wchar_t* name)
#else
MutexLock::MutexLock()
#endif
{

#ifdef _WIN32
    /*
    std::wstring mutexName = std::wstring(name);
    mutexName += "_";
    mutextName += std::to_wstring(::GetCurrentProcessId());
    mutexName += "_";
    mutexName += std::to_wstring(::GetTickCount());
    m_lk = ::CreateMutex(NULL, FALSE, mutexName.c_str());
    */
    InitializeCriticalSection(&lock_);
#else
    pthread_mutex_init(&lock_, NULL);
#endif

}

MutexLock::~MutexLock()
{
#ifdef _WIN32
    //::CloseHandle(lock_);
    DeleteCriticalSection(&lock_);
#else
    pthread_mutex_destroy(&lock_);
#endif
}

void MutexLock::lock()
{
#ifdef _WIN32
    /*
    switch(::WaitForSingleObject(lock_, INFINITE) )
    {
    case WAIT_OBJECT_0:
        break;
    case WAIT_ABANDONED:
        break;
    }
    */
    EnterCriticalSection(&lock_);
#else
    ::pthread_mutex_lock(&lock_);
#endif
}

void MutexLock::unlock()
{
#ifdef _WIN32
    //ReleaseMutex(lock_);
    LeaveCriticalSection(&lock_);
#else
    ::pthread_mutex_unlock(&lock_);
#endif
}

/*
* AutoLock : A RAII lock
* or include <mutex> to use c++11 LockGuard
*/
AutoLock::AutoLock(MutexLock* lock) : lock_(*lock)
{
    lock_.lock();
}

AutoLock::AutoLock(MutexLock& lock) : lock_(lock)
{
    lock_.lock();
}

AutoLock::~AutoLock()
{
    lock_.unlock();
}