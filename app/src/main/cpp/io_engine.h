//
// Created by liujia on 16/8/20.
//

#ifndef CLOUDWOOD_ANDROID_NEW_IO_ENGINE_H
#define CLOUDWOOD_ANDROID_NEW_IO_ENGINE_H

#include "net_engine.h"
#include "mutex.h"
#include <set>
#include <map>
#include <vector>
#include <mutex>
#include <atomic>

/*
#include <netinet/in.h>
#include <sys/select.h>
#include <fcntl.h>
#include <errno.h>
*/

struct TimerItem {
	SOCKET	socket;
	int		id;
	int		interval;
	time_t	last;

	TimerItem() = delete;

	TimerItem(SOCKET sock_, int id_, int interval_)
	:socket(sock_),
	 id(id_),
	 interval(interval_)
	{
	    ::time(&last);
	}

    TimerItem(SOCKET sock_, int id_)
            :TimerItem(sock_, id_, 0)
    {

    }

	bool operator == (const TimerItem& item){
		return this->socket == item.socket && this->id == item.id;
	}
};

struct IConn;
class IoEngine
{
public:
    static IoEngine* Instance();
	static void Release();

private:
    static IoEngine*    m_pInstance;

private:
    IoEngine();
    ~IoEngine();

public:
    bool    init();
    void    run(void*);
    void    stop();
    void    setEvent(IConn* pConn, SOCKET s, uint16_t opt, bool bAdd = true);
	void    addTimer(SOCKET sock, int id, int interval);
	void    removeTimer(SOCKET sock, int id);
	void    removeTimer(SOCKET sock);

private:
    void    _onRecv(SOCKET s);
    void    _onSend(SOCKET s);
	void	_onTimer(SOCKET sock, int id);
	IConn*  _findConnBySocket(SOCKET sock);

private:
    std::atomic<bool>       m_bStopped;
    SOCKET                  m_uMaxFd;
    std::set<SOCKET>        m_readSoSet;
    std::set<SOCKET>        m_writeSoSet;
    std::map<SOCKET, IConn*> m_connMap;
    std::mutex              m_connMapMutex; //protect m_readSoSet/m_writeSoSet/m_connMap
	unsigned int			m_uDelayCheck;

	//timer:
	std::mutex              m_timerMutex; //protect m_arrTimers/m_arrRemovedTimers
	std::vector<TimerItem>	m_arrTimers;
	std::vector<TimerItem>	m_arrRemovedTimers;
};

#endif //CLOUDWOOD_ANDROID_NEW_IO_ENGINE_H
