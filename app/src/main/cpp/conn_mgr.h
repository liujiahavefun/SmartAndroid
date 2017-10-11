//
// Created by liujia on 16/8/20.
//

#ifndef CLOUDWOOD_ANDROID_NEW_CONN_MGR_H
#define CLOUDWOOD_ANDROID_NEW_CONN_MGR_H

#include "conn.h"
#include "mutex.h"
#include <map>
#include <set>
#include <mutex>

class CConnMgr
{
public:
    static CConnMgr* Instance();
	static void Release();

private:
    static CConnMgr*    m_pInstance;

private:
    CConnMgr();
    ~CConnMgr();

public:
    int     newConn(ConnAttr* attr);
    void    removeConn(int connid);
    void    removeAll();
	int		addTimer(int connid, int id, int interval);
	int		removeTimer(int connid, int id);
	//int     UnregEvHandler(IEventHandler* ev);

public:
    int     connect(int connid, uint32_t ip, uint16_t port);
    int     send(int connid, char* data, size_t len);
    int     close(int connid);
	void	delayRemove(int connid);
	void	checkDelayRemove();
    int		setNoDelay(int connid, bool flag);

private:
    CConn*  _findConn(int connid);

private:
    typedef std::map<int, CConn*> connmap_t;
    connmap_t           m_connMap;
	std::mutex          m_connMapMutex; //protect m_connMap

	typedef std::set<int> connset_t;
	connset_t			m_delayConnSet;
	std::mutex          m_delayConnMutex; //protect m_delayConnSet
};

#endif //CLOUDWOOD_ANDROID_NEW_CONN_MGR_H
