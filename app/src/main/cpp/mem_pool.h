//
// Created by liujia on 16/8/19.
//

#ifndef SMARTOGO_ANDROID_NEW_MEM_POOL_H
#define SMARTOGO_ANDROID_NEW_MEM_POOL_H

#include "net_engine.h"
#include <deque>
#include <map>
#include <mutex>

#define MAX_MEM_BLOCKS_NUM 10

enum MEM_BLOCK_SIZE
{
    MAX_MEM_SIZE = 1024,
    MID_MEM_SIZE = 512,
    MIN_MEM_SIZE = 256
};

using namespace NetEngine;

class MutexLock;

class MemPool
{
public:
    static MemPool* Instance();
    static void Release();

    Packet* create_packet(uint32_t uri, const char* data, uint32_t len);
    void    free_packet(Packet* pkt);

private:
    static MemPool* m_pInstance;

private:
    MemPool(uint32_t blockNum);
    ~MemPool();

private:
    typedef std::map<uint32_t, std::deque<NetEngine::Packet*> > mem_pool_t;
    mem_pool_t          m_pool;
    MutexLock*          m_pLock;
    std::mutex          m_Lock;
};

#endif //SMARTOGO_ANDROID_NEW_MEM_POOL_H
