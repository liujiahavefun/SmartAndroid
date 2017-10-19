//
// Created by liujia on 16/8/16.
//


#include <pthread.h>
#include "net_engine.h"
#include "mutex.h"
#include "io_engine.h"
#include "conn_mgr.h"
#include "mem_pool.h"

void* _io_engine_loop_func_linux(void* ptr)
{
    IoEngine::Instance()->init();
    IoEngine::Instance()->run(ptr);

	IoEngine::Release();
	CConnMgr::Release();
	MemPool::Release();

    return NULL;
}

namespace NetEngine
{
static pthread_t s_pthreadId = 0;

NETENGINE_API int NetEngineStart()
{
	if(s_pthreadId > 0){
	    LOGE("NetEngineStart, s_pthreadId is %d", s_pthreadId);
	    return 0;
	}

    CConnMgr::Instance();
    IoEngine::Instance();
    MemPool::Instance();

    return ::pthread_create(&s_pthreadId, NULL, &_io_engine_loop_func_linux, NULL);
}

NETENGINE_API int NetEngineStop()
{
	LOGE("NetEngineStop");

	if( s_pthreadId == 0 )
	{
		LOGE("NetEngineStop, s_pthreadId == 0");
		return 0;
	}

	IoEngine::Instance()->stop();
    pthread_join(s_pthreadId, NULL);
    s_pthreadId = 0;

    return 0;
}

NETENGINE_API Packet* PacketAlloc(uint32_t uri, const char* data, size_t len)
{
    return MemPool::Instance()->create_packet(uri, data, len);
}

NETENGINE_API void PacketRelease(Packet* pkt)
{
    MemPool::Instance()->free_packet(pkt);
}

NETENGINE_API int ConnCreate(ConnAttr* attr)
{
    return CConnMgr::Instance()->newConn(attr);
}

NETENGINE_API int ConnConnect(int connid, uint32_t ip, uint16_t port)
{
    if( connid == 0 ) {
        return -1;
    }

    return CConnMgr::Instance()->connect(connid, ip, port);
}

NETENGINE_API int ConnSetNoDelay(int connid, bool flag)
{
    if( connid == 0 ) {
        return -1;
    }

    return CConnMgr::Instance()->setNoDelay(connid, flag);
}

NETENGINE_API int ConnSend(int connid, Packet* pkt)
{
    if( connid == 0 ) {
        return -1;
    }

    return CConnMgr::Instance()->send(connid, pkt->_data, pkt->_dataLen);
}

NETENGINE_API int ConnClose(int connid)
{
	if( connid == 0 ) {
		return -1;
    }

    CConnMgr::Instance()->close(connid);
    CConnMgr::Instance()->delayRemove(connid);
    return 0;
}

NETENGINE_API int ConnAddTimer(int connid, int id, int interval)
{
	if (connid == 0) {
		return -1;
	}

	CConnMgr::Instance()->addTimer(connid, id, interval);
	return 0;
}

NETENGINE_API int ConnRemoveTimer(int connid, int id)
{
	if (connid == 0) {
		return -1;
	}

	CConnMgr::Instance()->removeTimer(connid, id);
	return 0;
}

} //namespace NetEngine