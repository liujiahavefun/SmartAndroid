//
// Created by liujia on 16/8/20.
//

#include "io_engine.h"
#include "conn.h"
#include "mem_pool.h"
#include "conn_mgr.h"
#include <time.h>
#include <algorithm>

IoEngine* IoEngine::m_pInstance = NULL;

IoEngine* IoEngine::Instance()
{
    if ( NULL == m_pInstance ) //no need locker here coz NetModStart will call this function to make sure m_pInstance is inited first!!!
    {
        m_pInstance = new IoEngine();
    }
    return m_pInstance;
}

void IoEngine::Release()
{
	LOGE("IoEngine::Release enter.");
	if( m_pInstance )
	{
		delete m_pInstance;
		m_pInstance = NULL;
	}
	LOGE("IoEngine::Release exit.");
}

IoEngine::IoEngine()
: m_bStopped(false)
, m_uMaxFd(0)
, m_uDelayCheck(0)
{
    m_readSoSet.clear();
    m_writeSoSet.clear();
    m_connMap.clear();
}

IoEngine::~IoEngine()
{
}

bool IoEngine::init()
{
    m_bStopped = false;
    return true;
}

void IoEngine::stop()
{
    // just set the flag and the IO thread would be stopping slowly...
	m_bStopped = true;
}

void IoEngine::addTimer(SOCKET sock, int id, int interval)
{
	LOGE("IoEngine.addTimer, sock/id/interval=%d/%d/%d", sock, id, interval);

    /*
	TimerItem item;
	item.socket = sock;
	item.id = id;
	item.interval = interval;
	time(&item.last);
    */

	TimerItem item(sock, id, interval);

	do{
	    std::lock_guard<std::mutex> lock_(m_timerMutex);
	    m_arrTimers.push_back(item);
	}while(false);
}

void IoEngine::removeTimer(SOCKET sock, int id)
{
    /*
	TimerItem t1;
	t1.socket = sock;
	t1.id = id;
	*/

    TimerItem t1(sock, id);

	do{
        std::lock_guard<std::mutex> lock_(m_timerMutex);
        m_arrTimers.erase(std::remove(m_arrTimers.begin(), m_arrTimers.end(), t1), m_arrTimers.end());
    }while(false);
}

void IoEngine::removeTimer(SOCKET sock)
{
	removeTimer(sock, 0);
}

void IoEngine::setEvent(IConn* pConn, SOCKET s, uint16_t opt, bool bAdd)
{
    //linkLayer will setEvent while in NETENGINE thread, while the connection is closed
    //in BIZ main thread, so check active of connection to do double check.
    std::lock_guard<std::mutex> lock_(m_connMapMutex);

    if(!pConn || !pConn->isActive()){
        return;
    }

    if (bAdd)
    {
        if ( opt & FD_OP_READ )
            m_readSoSet.insert(s);

        if ( opt & FD_OP_WRITE )
            m_writeSoSet.insert(s);

        m_connMap.insert(std::make_pair(s, pConn));
    }
    else
    {
        if ( opt & FD_OP_READ && !m_readSoSet.empty() )
            m_readSoSet.erase(s);

        if ( opt & FD_OP_WRITE && !m_writeSoSet.empty() )
            m_writeSoSet.erase(s);
    }

    if ( opt & FD_OP_CLR )
    {
		if( !m_readSoSet.empty() )
			m_readSoSet.erase(s);

		if( !m_writeSoSet.empty() )
			m_writeSoSet.erase(s);

		m_connMap.erase(s);
    }
}

void IoEngine::run(void*)
{
    fd_set  FdSetRead;
    fd_set  FdSetWrite;

	time_t now;
    struct timeval mtimeout;
	uint32_t nErrorCount = 0;

    while (false == m_bStopped)
    {
		//check timer at first
		time(&now);
		for (size_t i = 0; i < m_arrTimers.size(); i++) {
			TimerItem& item = m_arrTimers[i];

			if (item.last == 0) {
				item.last = now;
			} else if (item.last + item.interval <= now) {
				item.last = now;
				_onTimer(item.socket, item.id);
			}
		}

		if( m_readSoSet.empty() && m_writeSoSet.empty() )
		{
		    ::usleep(20*1000);
			continue;
		}

		//LOGE("IoEngine::run, read/write/connMap=", m_readSoSet.size(), m_writeSoSet.size(), m_connMap.size());
        std::vector<SOCKET> ReadSoSet;  // = m_readSoSet;
        std::vector<SOCKET> WriteSoSet;	// = m_writeSoSet;

        do{
            std::lock_guard<std::mutex> lock_(m_connMapMutex);
            ReadSoSet.assign(m_readSoSet.begin(), m_readSoSet.end());
            WriteSoSet.assign(m_writeSoSet.begin(), m_writeSoSet.end());
        }while(false);

        m_uMaxFd = 0;
        mtimeout.tv_sec = 0;
        mtimeout.tv_usec = 50*1000; //50ms

        FD_ZERO(&FdSetRead);
        FD_ZERO(&FdSetWrite);

        for (std::vector<SOCKET>::const_iterator citer = ReadSoSet.begin(); citer != ReadSoSet.end(); ++citer)
        {
            FD_SET(*citer, &FdSetRead);
            m_uMaxFd = m_uMaxFd < *citer ? *citer : m_uMaxFd;
        }
        for (std::vector<SOCKET>::const_iterator citer = WriteSoSet.begin(); citer != WriteSoSet.end(); ++citer)
        {
            FD_SET(*citer, &FdSetWrite);
            m_uMaxFd = m_uMaxFd < *citer ? *citer : m_uMaxFd;
        }

        //in WIN32, the first param in select() is ignored, but in unix(linux),it's important and it's value is the max of readfd,writefd,expfd +1
        int ret = (int)::select( m_uMaxFd+1 , &FdSetRead, &FdSetWrite, NULL, &mtimeout );
        if ( 0 >= ret ) //error if ret > 0, ret == 0 is timeout
		{
			if (0 > ret)
			{
				nErrorCount++;
				if( nErrorCount >= 50 )
				{
				    LOGE("IoEngine::run, select failed, lasterror=", errno);
					nErrorCount = 0;
				}

				::usleep(20*1000);
			}
            continue;
		}

        //no listen socket fd because it's client process, not server
        for (std::vector<SOCKET>::const_iterator citer = ReadSoSet.begin(); citer != ReadSoSet.end(); ++citer)
        {
            SOCKET so = *citer;
            if ( FD_ISSET(so , &FdSetRead) )
            {
                _onRecv(so);
            }
        }
        for (std::vector<SOCKET>::const_iterator citer = WriteSoSet.begin(); citer != WriteSoSet.end(); ++citer)
        {
            SOCKET so = *citer;
            if ( FD_ISSET(so, &FdSetWrite) )
            {
                _onSend(so);
            }
        }

		m_uDelayCheck++;
		if( m_uDelayCheck >= 30 )
		{
			CConnMgr::Instance()->checkDelayRemove();
			m_uDelayCheck = 0;
		}
    }

	LOGE("IoEngine::run, exit.");
}

IConn* IoEngine::_findConnBySocket(SOCKET sock)
{
    IConn* pConn = NULL;
    std::lock_guard<std::mutex> lock_(m_connMapMutex);
    std::map<SOCKET, IConn*>::const_iterator io = m_connMap.find(sock);
    if (io != m_connMap.end()){
        pConn = io->second;
    }

    return pConn;
}

void IoEngine::_onRecv(SOCKET s)
{
    //NET_LOG("IoEngine::_onRecv, socket=",s);
    /*************************************************************
    * conn objects can be deleted only by netmod thread!!! so when u
    * find conn that u want, release the lock to make sure it won't
    * cause dead lock, and the conn won't be deleted in this callback
    **************************************************************/

    IConn* pConn = _findConnBySocket(s);
    if (pConn)
        pConn->onRecv();
}

void IoEngine::_onSend(SOCKET s)
{
    //NET_LOG("IoEngine::_onSend, socket=",s);
    /*************************************************************
    * conn objects can be deleted only by netmod thread!!! so when u
    * find conn that u want, release the lock to make sure it won't
    * cause dead lock, and the conn won't be deleted in this callback
    **************************************************************/
    IConn* pConn = _findConnBySocket(s);
    if ( pConn )
        pConn->onSend();
}

void IoEngine::_onTimer(SOCKET sock, int id) {
	IConn* pConn = _findConnBySocket(sock);
	if (pConn) {
		LOGE("IoEngine::_onTimer, sock/id=%d/%d", sock, id);
		pConn->onTimer(id);
	}
	else {
		LOGE("IoEngine::_onTimer, pConn==NULL for socket=%d", sock);
	}
}
