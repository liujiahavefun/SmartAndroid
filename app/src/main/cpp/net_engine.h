//
// Created by liujia on 16/8/16.
//

#ifndef SMARTGO_ANDROID_NEW_NET_ENGINE_H
#define SMARTGO_ANDROID_NEW_NET_ENGINE_H

#include <sys/socket.h>
#include <sys/select.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <stdint.h>
#include <unistd.h>
#include <syslog.h>
#include <errno.h>
#include <pthread.h>
#include <semaphore.h>
#include <string.h>
#include "extension.h"

#ifndef SOCKET
#define SOCKET  int
#endif

#define INVALID_CONNID  (~0)
#define INVALID_SOCKET  (-1)
#define NETENGINE_API

namespace NetEngine
{

#define FD_OP_READ  (0x0001)
#define FD_OP_WRITE (0x0010)
#define FD_OP_EXP   (0x0100) //not used currently
#define FD_OP_CLR   (0x1000) //can't used with other opt like FD_SO_READ, or it will clear anyway

enum CONNSTATUS
{
    CONN_INIT = 0,
    CONN_CONNECTING = 1,
    CONN_CONNECTED = 2,
    CONN_CLOSE = 3
};

//liujia: use c++11 atomic instead in future
static volatile long g_curConnId = 0;

#ifdef _WIN32
#define ATOMIC_ADD(p) InterlockedIncrement(p)
#define ATOMIC_GET(p) InterlockedExchangeAdd(p,0)
#else
#define ATOMIC_ADD(p) __sync_add_and_fetch(p, 1);
#endif

/*
* network packet received or to sent
* a packet thru network is composed of 4bytes header(length of the packet), 4bytes uri, then data
*/
struct NETENGINE_API Packet
{
    enum MemType
    {
        MEM_POOL_MAX_T = 0, //1024 bytes
        MEM_POOL_MID_T = 1, //512 bytes
        MEM_POOL_MIN_T = 2, //256 bytes
        MEM_NEW_T = 3       //malloc/new buffer, size on demand...
    };
    char*		_data;
    size_t      _bufLen;    //length of malloc buffer
    size_t		_dataLen;   //length of real data
    MemType     _type;
    uint64_t	_timestamp;
    uint32_t    _uri;

    Packet() : _data(NULL), _bufLen(0), _dataLen(0), _type(MEM_NEW_T), _timestamp(0), _uri(0){}
    Packet(char* data, size_t bufLen) : _data(data), _bufLen(bufLen), _dataLen(0), _type(MEM_NEW_T), _timestamp(0), _uri(0){} //pre malloc buffer but not insert data
    ~Packet(){ if(_data) delete _data; _data = NULL; _dataLen = _bufLen = 0; }

    inline void reset(){ memset(_data, 0, _dataLen); _dataLen = 0; }
};

NETENGINE_API Packet* PacketAlloc(uint32_t uri, const char* data, size_t len);
NETENGINE_API void PacketRelease(Packet* pkt);

/*
* for compatible, CNetEvent defined like NetIo did.
*/
struct NETENGINE_API CNetEvent
{
    //Event types
    enum
    {
        EV_UNKNOWN   = 0,
        EV_CONNECTING = 1,          //连接中
        EV_CONNECTED = 2,           //已连接
        EV_CLOSED = 3,              //已关闭
        EV_ERROR = 4,               //错误
        EV_IN = 5,                  //收到完整数据包
        EV_INSTREAM = 6,            //数据流
        EV_SENT = 7,                //数据包已发送
        EV_CONNSTATE = 8,           //连接状态，具体见下面的CNetEventConnState定义
        EV_TIMER = 9,               //定时器
        EV_ALIVE = 10,              //收到ping回复
    };
    int			ConnId;		// actually the value is socket id(SOCKET)
    int			EvtType;	// Event Type
    uint64_t	RetVal;		// more detailed return value
};

struct NETENGINE_API CNetEventConnState : public CNetEvent
{
    //enum connstate
    enum
    {
        CS_STATE_UNKNOWN = -1,
        CS_TRANSPTLAYER_CONNECTING = 0,
        CS_TRANSPTLAYER_CONNECTED = 1,
        CS_PROXYLAYER_CONNECTING = 2,
        CS_PROXYLAYER_CONNECTED = 3,
        CS_ENCLAYER_START = 4,
        CS_ENCLAYER_FINISH = 5,
        CS_CONNECTED = 10,
    };

    int state;
    uint64_t timestamp;
};


class NETENGINE_API IEventHandler
{
public:
    virtual ~IEventHandler() {};

    /**
    * Notification event handlers
    *
    * when a event happens on a connection, the OnEvent callback will be invoked
    * @param	evt, specify what's happening on which connection
    * @param	pkt, pointer to the Packet to be released
    * @return	0, on success
    *			ENIO_BUSY, on you are too busy to handle more events
    * NOTE:	1). the OnEvent() implementation should be very lightweight, NEVER call
    *			any blocking operation in OnEvent(), or, it MAY block all network
    *			processing
    *			2). Do NOT FORGET call PacketRelease(pkt) in some place.
    */
    virtual int	OnEvent(CNetEvent* evt, Packet* pkt) = 0;
};

struct NETENGINE_API ConnAttr{
    enum
    {
        CONN_UDP = SOCK_DGRAM,	// a UDP connection
        CONN_TCP = SOCK_STREAM,	// a TCP connection
    };

    // CONN_UDP or CONN_TCP
    int		ConnType;

    // The local address, if the ConnType is UDP, LocalAddr must be specified
    int		LocalIP;
    short	LocalPort;

    // The remote address, if the ConnType is TCP, RemoteAddr must be specified
    int		RemoteIP;
    short	RemotePort;

    // Event Handler, the reference to the EventHandler, on event happens, the
    // evHandler->OnEvent() will be invorked
    IEventHandler*		evHandler;

    // The extension layers want to be added to the connection, end with a NULL
    // Set the exts in order, if you want a multiport, compressed, encrypted conn using a
    // proxy, exts[0] = ExtMultiport, exts[1] = ExtCompress, exts[2] = ExtEncryption,
    // exts[3] = ExtProxy, exts[4] = NULL;
    const static int MAX_EXTENSIONS = 16;
    Extension*	exts[MAX_EXTENSIONS];
};

/*
* start NetMod, create a thread to run select engine
*/
NETENGINE_API int NetEngineStart();

/*
* stop NetMod, remove all connections
*/
NETENGINE_API int NetEngineStop();

/*
* create a async connection that unconnected yet, need to call ConnConnect to do connect
* @param attr:      use this struct to specify what kinds of connection with specified parameters you want to create
* @return int:      connection ID, INVALID_SOCKET(-1) will be returned if failed
*/
NETENGINE_API int ConnCreate(ConnAttr* attr);

/*
* do connect
* @param connid : the connection id that ConnCreate returned, it's a connId to get associated connection in ConnMgr
* @param ip     : target server ip, in network order
* @param port   : target server port, in network order
* @return       : int, return 0 if success, else -1 will be given
*/
NETENGINE_API int ConnConnect(int connid, uint32_t ip = 0, uint16_t port = 0);

/*
* send data
* @param connid : the socket id that ConnCreate returned, it's a connId to get associated connection in ConnMgr
* @param pkt    : wrapper of sent data
*/
NETENGINE_API int ConnSend(int connid, Packet* pkt);

/*
* close a connection
* @param connid : the connection ID id that ConnCreate returned, it's a connId to get associated connection in ConnMgr
*/
NETENGINE_API int ConnClose(int connid);

/*
* enable/disable tcp link NO_DELAY mode
* @param    connid: the connection ID that ConnSetNodelay affact
*           flag:   true for enable NO_DELAY, false for disable NO_DELAY
*/
NETENGINE_API int ConnSetNoDelay(int connid, bool flag);

/*
* add a timer
*
*/
NETENGINE_API int ConnAddTimer(int connid, int id, int interval);

/*
* remove a timer.
*
*/
NETENGINE_API int ConnRemoveTimer(int connid, int id);
}

#endif //SMARTGO_ANDROID_NEW_NET_ENGINE_H
