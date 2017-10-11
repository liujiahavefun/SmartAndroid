//
// Created by liujia on 16/8/20.
//

#include "conn_mgr.h"
//#include "link_layer_enc.h"

/*
* 对每个操作都用锁来保护，即锁保护的的不仅仅是connMap，而且还有对取到的CConn*的操作
* 这样对所有连接的所有操作都顺序化了，解决可能几个线程对同一个的CConn同时做不同的操作，例如一个关闭连接一个要发数据
* 这样是影响了效率，但客户端这么点连接这么点吞吐，需要这点效率么？
*/

CConnMgr* CConnMgr::m_pInstance = NULL;

CConnMgr* CConnMgr::Instance()
{
    if ( NULL == m_pInstance )
    {
        //no locker here coz NetEngineStart will call this function to make sure m_pInstance is initialized first!!!
        m_pInstance = new CConnMgr();
        //LinkLayerEnc::genRSAKey();
    }
    return m_pInstance;
}

void CConnMgr::Release()
{
	if( m_pInstance )
	{
		delete m_pInstance;
		m_pInstance = NULL;
	}
}

CConnMgr::CConnMgr()
{
}

CConnMgr::~CConnMgr()
{
	removeAll();
}

int CConnMgr::newConn(ConnAttr* attr)
{
    CConn * pConn = new CConn();
    int connid = pConn->init(attr);
    if (connid == -1)
    {
        delete pConn;
        return -1;
    }

    do{
        std::lock_guard<std::mutex> lock_(m_connMapMutex);
        m_connMap.insert(std::make_pair(connid, pConn));
    }while(false);

    return connid;
}

void CConnMgr::removeConn(int connid)
{
    LOGE("CConnMgr::removeConn, connId=%d", connid);
    std::lock_guard<std::mutex> lock_(m_connMapMutex);

    connmap_t::iterator iter = m_connMap.find(connid);
    if ( iter != m_connMap.end() )
    {
		LOGE("CConnMgr::removeConn, real remove conn, connId=%d", connid);
		if(iter->second){
            delete iter->second;
        }
        m_connMap.erase(iter);
    }
	else
	{
		LOGE("CConnMgr::removeConn, conn not found, connid=%d", connid);
	}
}

void CConnMgr::removeAll()
{
	LOGE("CConnMgr::removeAll");

	do{
	    std::lock_guard<std::mutex> lock_(m_connMapMutex);

        for ( connmap_t::iterator iter = m_connMap.begin(); iter != m_connMap.end(); ++iter )
        {
            if (iter->second)
                delete iter->second;
        }
        m_connMap.clear();
	}while(false);

    do{
        std::lock_guard<std::mutex> lock_(m_delayConnMutex);
        m_delayConnSet.clear();
    }while(false);

}

int CConnMgr::connect(int connid, uint32_t ip, uint16_t port)
{
	int ret = -1;
    std::lock_guard<std::mutex> lock_(m_connMapMutex);
    CConn* conn = _findConn(connid);
    if(conn){
        ret = conn->connect(ip, port);
    }

    return ret;
}

int CConnMgr::setNoDelay(int connid, bool flag)
{
	int ret = -1;
	std::lock_guard<std::mutex> lock_(m_connMapMutex);
	CConn* conn = _findConn(connid);
    if(conn){
        ret = conn->setNoDelay(flag);
    }

	return ret;
}

int CConnMgr::send(int connid, char* data, size_t len)
{
	int ret = -1;
	std::lock_guard<std::mutex> lock_(m_connMapMutex);
    CConn* conn = _findConn(connid);
    if(conn){
        ret = conn->send(data, len);
    }

    return ret;
}

int CConnMgr::close(int connid)
{
	int ret = -1;
	std::lock_guard<std::mutex> lock_(m_connMapMutex);
    CConn* conn = _findConn(connid);
    if(conn){
        ret = conn->close();
    }

    return ret;
}

void CConnMgr::delayRemove(int connid)
{
    std::lock_guard<std::mutex> lock_(m_delayConnMutex);
	m_delayConnSet.insert(connid);
}

void CConnMgr::checkDelayRemove()
{
    std::lock_guard<std::mutex> lock_(m_delayConnMutex);
	if( m_delayConnSet.empty() )
		return;

	for( connset_t::const_iterator iter = m_delayConnSet.begin(); iter != m_delayConnSet.end(); ++iter )
	{
		LOGE("CConnMgr::checkDelayRemove, connid=%d", *iter);
		removeConn(*iter);
	}
	m_delayConnSet.clear();
}

int	CConnMgr::addTimer(int connid, int id, int interval)
{
	int ret = -1;
	std::lock_guard<std::mutex> lock_(m_connMapMutex);
    CConn* conn = _findConn(connid);
    if(conn){
        ret = conn->addTimer(id, interval);
    }

	return ret;
}

int	CConnMgr::removeTimer(int connid, int id)
{
	int ret = -1;
	std::lock_guard<std::mutex> lock_(m_connMapMutex);
    CConn* conn = _findConn(connid);
    if(conn){
        ret = conn->removeTimer(id);
    }

	return ret;
}

CConn* CConnMgr::_findConn(int connid)
{
    connmap_t::const_iterator iter = m_connMap.find(connid);
    if (iter != m_connMap.end()) {
        return iter->second;
    }
    return nullptr;
}

/******************************************
int CConnMgr::onRecv(SOCKET s)
{
    AutoLock autoLock(m_lock_map);
    std::map<SOCKET, CConn*>::const_iterator iFind = m_connMap.find(s);
    if ( iFind != m_connMap.end() )
        return iFind->second->onRecv();

    return -1;
}

int CConnMgr::onSend(SOCKET s)
{
    AutoLock autoLock(m_lock_map);
    std::map<SOCKET, CConn*>::const_iterator iFind = m_connMap.find(s);
    if ( iFind != m_connMap.end() )
        return iFind->second->onSend();

    return -1;
}

int CConnMgr::UnregEvHandler(IEventHandler* ev)
{
    int cnt = 0;
    //AutoLock autoLock(m_lock_map);
	m_lock_map.lock();
#ifdef _WIN32
    for ( connmap_t::iterator io = m_connMap.begin(); io != m_connMap.end();)
    {
        if (io->second)
        {
            if (io->second->getEvHandler() == ev)
            {
                //std::cout << "CConnMgr::UnregEvHandler, delete conn, connid=" << io->first << std::endl;
				NET_LOG("CConnMgr::UnregEvHandler, delete connId=", io->first);

                //delete io->second;
                io = m_connMap.erase(io);
                cnt++;
            }
            else
                ++io;
        }
        else
            ++io;
    }
#else
    std::set<int> eraseSet;
    for ( connmap_t::iterator io = m_connMap.begin(); io != m_connMap.end(); ++io )
    {
        if (io->second)
        {
            if (io->second->getEvHandler() == ev)
            {
                std::cout << "CConnMgr::UnregEvHandler, delete conn, connid=" << io->first << std::endl;
                delete io->second;
                eraseSet.insert(io->first);
                cnt++;
            }
        }
    }
    for (std::set<int>::iterator it = eraseSet.begin(); it != eraseSet.end(); ++it)
    {
        m_connMap.erase(*it);
    }
#endif
	m_lock_map.unlock();

    return cnt;
}
*/