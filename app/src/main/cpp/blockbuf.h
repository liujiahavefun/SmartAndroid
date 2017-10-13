//
// Created by liujia on 16/8/17.
//

#ifndef CLOUDWOOD_ANDROID_NEW_BLOCKBUF_H
#define CLOUDWOOD_ANDROID_NEW_BLOCKBUF_H

#include <stdlib.h> //linux need it to identify free function!!!
#include <string.h> //memcpy, memmov
#include "log.h"
#include <iostream>

#ifndef _WIN32
#include <sys/socket.h>
#include <errno.h>
#ifndef SOCKET
#define SOCKET int
#endif
#else
#include "config.h"
#endif

template<unsigned BlockSize>
struct Allocator_malloc_free
{
    enum{
        BLOCKSIZE = BlockSize
    };

    static char* ordered_malloc(size_t block)
    {
        return (char*)::malloc(BLOCKSIZE * block);
    }
    static void ordered_free(char* block)
    {
        ::free(block);
    }
};

template<unsigned BlockSize>
struct Allocator_new_delete
{
    enum{
        BLOCKSIZE = BlockSize
    };

    static char* ordered_malloc(size_t block)
    {
        return new char[BLOCKSIZE * block];
    }
    static void ordered_free(char* block)
    {
        delete[] block;
    }
};

#ifdef _USE_NEW_DELETE_ALLOCATOR_
    typedef Allocator_new_delete<1 * 1024>  Allocator_Block_1k;
    typedef Allocator_new_delete<2 * 1024>  Allocator_Block_2k;
    typedef Allocator_new_delete<4 * 1024>  Allocator_Block_4k;
    typedef Allocator_new_delete<8 * 1024>  Allocator_Block_8k;
    typedef Allocator_new_delete<16 * 1024> Allocator_Block_16k;
    typedef Allocator_new_delete<32 * 1024> Allocator_Block_32k;
    typedef Allocator_new_delete<64 * 1024> Allocator_Block_64k;
    typedef Allocator_new_delete<128* 1024> Allocator_Block_128k;
#else
    typedef Allocator_malloc_free<1 * 1024> Allocator_Block_1k;
    typedef Allocator_malloc_free<2 * 1024> Allocator_Block_2k;
    typedef Allocator_malloc_free<4 * 1024> Allocator_Block_4k;
    typedef Allocator_malloc_free<8 * 1024> Allocator_Block_8k;
    typedef Allocator_malloc_free<16 * 1024> Allocator_Block_16k;
    typedef Allocator_malloc_free<32 * 1024> Allocator_Block_32k;
    typedef Allocator_malloc_free<64 * 1024> Allocator_Block_64k;
    typedef Allocator_malloc_free<128* 1024> Allocator_Block_128k;
#endif

template<typename TAllocator = Allocator_Block_4k, unsigned MaxBlockNum = 2>
class BlockBuf
{
    enum{
        MAXBLOCKNUM = MaxBlockNum
    };
    enum{
        mPos = size_t(-1)  //4294967295L
    };
public:
    BlockBuf(): m_size(0), m_blockNum(0), m_data(NULL){}
    virtual ~BlockBuf(){ this->free(); }

public:
    inline char*    data()      { return m_data; }
    inline char*    tail()      { return m_data + m_size; }
    inline size_t   size()      { return m_size; }
    inline size_t   blocksize() { return TAllocator::BLOCKSIZE; }
    inline size_t   blocknum()  { return m_blockNum; }
    inline size_t   capacity()  { return m_blockNum * TAllocator::BLOCKSIZE; }
    inline size_t   freespace() { return capacity() - size(); }
    inline bool     empty()     { return size() == 0; }
    inline void     setsize(size_t n)   { m_size = n < capacity() ? n : capacity(); }
    inline void     free()      { TAllocator::ordered_free(m_data); m_data = NULL; m_blockNum = 0; m_size = 0; }

    bool     reserve(size_t n);
    bool     append(const char* data, size_t len);
    void     erase(size_t pos, size_t n, bool hold = false);

    int      read(SOCKET s, sockaddr_in* pAddr=NULL, int soType=SOCK_STREAM);
    int      write(SOCKET s, const char* data, size_t len, sockaddr_in* pAddr=NULL, int soType=SOCK_STREAM);
    int      flush(SOCKET s, sockaddr_in* pAddr=NULL, int soType=SOCK_STREAM); //flush cached data when onSend event triggered

protected:
    bool     increase_capacity(size_t increase_size);

private:
    size_t m_blockNum;
    size_t m_size;
    char*  m_data;
};

template<typename TAllocator, unsigned MaxBlockNum>
bool BlockBuf<TAllocator,MaxBlockNum>::increase_capacity(size_t increase_size)
{
    if ( increase_size == 0 || increase_size <= freespace() )
        return true;

    increase_size -= freespace();
    size_t newBlockNum = m_blockNum + ((increase_size + TAllocator::BLOCKSIZE - 1) / TAllocator::BLOCKSIZE);

    if (newBlockNum > MAXBLOCKNUM) {
        LOGE("increase capacity failed!!! newBlockNum/MAXBLOCKNUM=%d/%d",newBlockNum, MAXBLOCKNUM);
        return false;
    }

    char* newData = TAllocator::ordered_malloc(newBlockNum);
    if (newData == NULL){
        LOGE("allocate new space failed!!! newBlockNum/MAXBLOCKNUM=%d/%d",newBlockNum, MAXBLOCKNUM);
        return false; //log here
    }

    if ( !empty() )
    {
        //copy old data and free old block
        memcpy(newData, m_data, m_size);
        TAllocator::ordered_free(m_data);
    }

    m_data = newData;
    m_blockNum = newBlockNum;

    return true;
}

template<typename TAllocator, unsigned MaxBlockNum>
bool BlockBuf<TAllocator, MaxBlockNum>::append(const char* data, size_t len)
{
    if(data == nullptr)
        return false;

    if (len == 0)
        return true; // no data

    if (increase_capacity(len))
    {
        memmove(tail(), data, len); // append
        m_size += len;
        return true;
    }
    else
    {
        LOGE("append failed!!! not enough buffer and increase failed, len = %d", len);
        return false;
    }
}

template<typename TAllocator, unsigned MaxBlockNum>
int BlockBuf<TAllocator, MaxBlockNum>::read(SOCKET s, sockaddr_in* pAddr/*=NULL*/, int soType/*=SOCK_STREAM*/)
{
    if (freespace() < (blocksize() >> 1) && blocknum() < MAXBLOCKNUM) {
        // ignore increase_capacity result.
        increase_capacity(blocksize());
    }

    size_t nrecv = freespace() < mPos ? freespace() : mPos;  // min(mPos, freespace());
    if (nrecv == 0) {
        return -1;
    }

    int ret = 0;

    if ( SOCK_STREAM == soType ) {
        ret = ::recv(s, (char*)tail(), (int)nrecv, 0);
    }
    else if ( SOCK_DGRAM == soType ) {
#ifdef _WIN32
        int addr_len = sizeof(struct sockaddr);
#else
        socklen_t addr_len = sizeof(struct sockaddr);
#endif
        ret = ::recvfrom(s, (char*)tail(), (int)nrecv, 0, (struct sockaddr*)pAddr, &addr_len );
    }

    if (ret > 0) {
        m_size += ret;
    } else {
#ifdef _WIN32
        uint32_t uLastErr = WSAGetLastError();
        LOGE("Blockbuf::read, read nothing, lastErrCode=%d",uLastErr);
#else
        //if(errno != EAGAIN && errno == EINTR) //算了，都打出来吧
        LOGE("Blockbuf::read, read error, ret=%d, lastErrCode=%d", ret, errno);
#endif
    }
    return ret;
}

template<typename TAllocator, unsigned MaxBlockNum>
int BlockBuf<TAllocator, MaxBlockNum>::write(SOCKET s, const char* data, size_t len, sockaddr_in* pAddr/*=NULL*/, int soType/*=SOCK_STREAM*/)
{
    if (len == 0)
        return -1;

    if(blocknum() > MAXBLOCKNUM)
        return -1;

    int nsent = 0;
    if (empty()) //call send as no data cached in buffer,otherwise,socket can't send anything and we should cache the data into buffer until onSend event was given
    {
        if ( SOCK_STREAM == soType ) {
            nsent = ::send(s , data, (int)len, 0);
        } else if ( SOCK_DGRAM == soType ) {
            nsent = ::sendto(s, data, (int)len, 0, (struct sockaddr*)pAddr, sizeof(struct sockaddr));
        }
    }

    if(nsent < 0)
    {
#ifdef _WIN32
        uint32_t uLastError = WSAGetLastError();
        //NET_LOG("socket was blocked, Async send data, LastErrCode=%d",uLastError);
        if (uLastError == WSAEWOULDBLOCK || uLastError == WSAEINTR || uLastError == WSAEINPROGRESS)
            nsent = 0;
		else
        {
            nsent = 0;
			NET_LOG("WSAGetLastError != WSAEWOULDBLOCK, link maybe broken! lastErrCode=%d",uLastError);
        }
#else
        if(errno == EAGAIN || errno == EINTR || errno == EINPROGRESS)
            nsent = 0;
        //else
        //throw buffer_overflow("send error");
#endif
    }
    int restLen = len - nsent;
    if (restLen > 0) {
        if (!append(data + nsent, restLen)) {
            LOGE("write, append failed!!! send data len=%d", len);
        }
    }
    return (int)nsent;
}

template<typename TAllocator, unsigned MaxBlockNum>
int BlockBuf<TAllocator, MaxBlockNum>::flush(SOCKET s, sockaddr_in* pAddr/*=NULL*/, int soType/*=SOCK_STREAM*/)
{
    if (empty())
    {
        return 0;
    }

    int ret = 0;
    if ( SOCK_STREAM == soType )
        ret = ::send(s, (const char*)data(), (int)size(), 0);
    else if ( SOCK_DGRAM == soType )
        ret = ::sendto(s, (const char*)data(), (int)size(), 0, (struct sockaddr*)pAddr, sizeof(struct sockaddr) );

    erase( 0, ret );
    //NET_LOG("flush, sent bytes=%d",ret);
    return ret;
}

template<typename TAllocator, unsigned MaxBlockNum>
bool BlockBuf<TAllocator, MaxBlockNum>::reserve(size_t n)
{
    return (n <= capacity() || increase_capacity(n - capacity()));
}

template<typename TAllocator, unsigned MaxBlockNum>
void BlockBuf<TAllocator, MaxBlockNum>::erase(size_t pos, size_t n, bool hold)
{
    if (pos > size())
        pos = size();

    size_t m = size() - pos; // can erase
    if (n >= m)
        m_size = pos; // all clear after pos
    else
    {
        m_size -= n;
        memmove(m_data + pos, m_data + pos + n, m - n);
    }
}

typedef BlockBuf<Allocator_Block_8k, 8>     Buffer8x8k;  //64k
typedef BlockBuf<Allocator_Block_8k, 16>    Buffer8x16k; //128k
typedef BlockBuf<Allocator_Block_8k, 32>    Buffer8x32k; //256k
typedef BlockBuf<Allocator_Block_32k, 16>   Buffer32x16k; //512k
typedef BlockBuf<Allocator_Block_32k, 32>   Buffer32x32k; //1M Wow!!!
typedef BlockBuf<Allocator_Block_64k, 64>   Buffer64x64k; //4M Wow!!!
typedef BlockBuf<Allocator_Block_128k, 64>  Buffer128x64k; //8M Holy!!!

typedef Buffer64x64k  inputbuf_t;
typedef Buffer128x64k outputbuf_t; //8M, increase output buffer max size limit, in case buffer overflow in swarming (send data)

#endif //CLOUDWOOD_ANDROID_NEW_BLOCKBUF_H
