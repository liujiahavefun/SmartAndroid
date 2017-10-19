//
// Created by liujia on 16/8/19.
//

#include "mem_pool.h"
#include "mutex.h"
#include "util/bytes_swap.h"

MemPool* MemPool::m_pInstance = nullptr;

MemPool* MemPool::Instance()
{
    if ( nullptr == m_pInstance ) {
        m_pInstance = new MemPool(MAX_MEM_BLOCKS_NUM);
    }
    return m_pInstance;
}

void MemPool::Release()
{
	if( m_pInstance ) {
		delete m_pInstance;
		m_pInstance = nullptr;
	}
}

MemPool::MemPool(uint32_t blockNum)
{
    m_pLock = new MutexLock();
    for (uint32_t i = 0; i < blockNum; ++i) {
        char* buf = new char[MAX_MEM_SIZE];
        Packet* pkt = new Packet(buf, MAX_MEM_SIZE);
        pkt->_type = Packet::MEM_POOL_MAX_T;
        m_pool[MAX_MEM_SIZE].push_back(pkt);

        buf = new char[MID_MEM_SIZE];
        pkt = new Packet(buf, MID_MEM_SIZE);
        pkt->_type = Packet::MEM_POOL_MID_T;
        m_pool[MID_MEM_SIZE].push_back(pkt);

        buf = new char[MIN_MEM_SIZE];
        pkt = new Packet(buf, MIN_MEM_SIZE);
        pkt->_type = Packet::MEM_POOL_MIN_T;
        m_pool[MIN_MEM_SIZE].push_back(pkt);
    }
}

MemPool::~MemPool()
{
    m_pLock->lock();
    for (mem_pool_t::iterator iter = m_pool.begin(); iter != m_pool.end(); ++iter) {
        for (std::deque<Packet*>::iterator iter2 = iter->second.begin(); iter2 != iter->second.end(); ++iter2) {
            if (*iter2) {
                delete *iter2;
            }
        }
    }
    m_pool.clear();
    m_pLock->unlock();
    delete m_pLock;
}

Packet* MemPool::create_packet(uint32_t uri, const char* data, uint32_t len)
{
    if(data == nullptr || len == 0) {
        return nullptr;
    }

    //增加8字节包头，其中4个字节的包长，4个字节的uri
    len += 2*sizeof(uint32_t);

    Packet* pkt = nullptr;
    m_pLock->lock();
    if ( len <= MIN_MEM_SIZE && !m_pool[MIN_MEM_SIZE].empty() ) {
        pkt = *m_pool[MIN_MEM_SIZE].begin();
        m_pool[MIN_MEM_SIZE].pop_front();
    }
    else if ( len <= MID_MEM_SIZE && !m_pool[MID_MEM_SIZE].empty() ) {
        pkt = *m_pool[MID_MEM_SIZE].begin();
        m_pool[MID_MEM_SIZE].pop_front();
    }
    else if ( len <= MAX_MEM_SIZE && !m_pool[MAX_MEM_SIZE].empty() ) {
        pkt = *m_pool[MAX_MEM_SIZE].begin();
        m_pool[MAX_MEM_SIZE].pop_front();
    }
    else {
        pkt = new Packet();
        pkt->_data = new char[len];
        pkt->_bufLen = len;
        pkt->_type = Packet::MEM_NEW_T;
    }
    m_pLock->unlock();

    //copy header uri and data to buffer
    uint32_t header_len = to_little_endian<>(len);
    uint32_t header_uri = to_little_endian<>(uri);
    memcpy(pkt->_data, &header_len, sizeof(uint32_t));
    memcpy(pkt->_data + sizeof(uint32_t), &header_uri, sizeof(uint32_t));
    memcpy(pkt->_data + 2*sizeof(uint32_t), data, len - 2*sizeof(uint32_t));
    pkt->_dataLen = len;
    pkt->_uri = uri;

    return pkt;
}

void MemPool::free_packet(Packet* pkt)
{
    if (!pkt) {
        return;
    }

    m_pLock->lock();
    if (pkt->_type == Packet::MEM_POOL_MIN_T) {
        pkt->reset();
        m_pool[MIN_MEM_SIZE].push_back(pkt);
    }
    else if (pkt->_type == Packet::MEM_POOL_MID_T) {
        pkt->reset();
        m_pool[MID_MEM_SIZE].push_back(pkt);
    }
    else if (pkt->_type == Packet::MEM_POOL_MAX_T) {
        pkt->reset();
        m_pool[MAX_MEM_SIZE].push_back(pkt);
    }
    else {
        delete pkt;
    }
    m_pLock->unlock();
}