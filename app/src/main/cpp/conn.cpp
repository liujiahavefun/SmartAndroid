//
// Created by liujia on 16/8/20.
//

#include "conn.h"
#include "config.h"
#include "io_engine.h"
#include "mem_pool.h"
#include "jni_callback.h"
#include "util/time_util.h"
//#include "link_layer_enc.h"
//#include "link_layer_proxy.h"
//#include "link_layer_compress.h"
//#include "link_layer_direct.h"

#if defined(__i386__) || defined(__x86_64__) || defined(_WIN32) //big start
inline uint16_t XHTONS(uint16_t i16)
{
	return((i16 << 8) | (i16 >> 8));
}
inline uint32_t XHTONL(uint32_t i32)
{
	return((uint32_t(XHTONS(i32)) << 16) | XHTONS(i32>>16));
}
inline uint64_t XHTONLL(uint64_t i64)
{
	return((uint64_t(XHTONL((uint32_t)i64)) << 32) |XHTONL((uint32_t(i64>>32))));
}
#else /* big end */
#define XHTONS
#define XHTONL
#define XHTONLL
#endif /* __i386__ */


static const int FIXED_HEADER_LENGTH = sizeof(uint32_t);
static const int FIXED_URI_LENGTH = sizeof(uint32_t);

static uint64_t g_uLastTime = 0;

CConn::CConn()
: m_sockfd(INVALID_SOCKET)
, m_sockType(SOCK_STREAM)
, m_status(CONN_INIT)
, m_pEvH(NULL)
, m_pFirstLayer(NULL)
, m_pLastLayer(NULL)
{
    m_connId = ATOMIC_ADD(&g_curConnId);

    m_localAddr.sin_family		= AF_INET;
    m_localAddr.sin_addr.s_addr	= htonl(INADDR_ANY);
    m_localAddr.sin_port		= htons(0);

    m_remoteAddr.sin_family		= AF_INET;
    m_remoteAddr.sin_addr.s_addr = htonl(INADDR_ANY);
    m_remoteAddr.sin_port		= htons(0);
}

CConn::~CConn()
{
    ILinkLayer * layer = m_pFirstLayer;
    while (layer)
    {
        m_pFirstLayer = m_pFirstLayer->m_pNextLayer;
        delete layer;
        layer = m_pFirstLayer;
    }
}

int CConn::init(ConnAttr* attr)
{
    if ( ConnAttr::CONN_TCP == attr->ConnType ) {
        m_sockType = SOCK_STREAM;
    } else if ( ConnAttr::CONN_UDP == attr->ConnType ) {
        m_sockType = SOCK_DGRAM;
    } else {
        //unknown protocol
        return -1;
    }

    m_sockfd = ::socket(AF_INET, m_sockType, 0);
    if ( INVALID_SOCKET == m_sockfd ) {
        return -1;
    }

    //forbid reuse port!!! cause reuse last port may trigger onSend event(but connection closed last time)
    int on = 0;
    ::setsockopt(m_sockfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on));

    //set this socket non-block
    setNonBlock();

    //the byte order is already translated into network order....
    if (attr->LocalIP != INADDR_ANY) {
        m_localAddr.sin_addr.s_addr = attr->LocalIP;
    }

    if (attr->LocalPort != 0) {
        m_localAddr.sin_port = attr->LocalPort;
    }

    if (attr->RemoteIP != INADDR_ANY) {
        m_remoteAddr.sin_addr.s_addr = attr->RemoteIP;
    }

    if (attr->RemotePort != 0) {
        m_remoteAddr.sin_port = attr->RemotePort;
    }

    if (attr->evHandler) {
        m_pEvH = attr->evHandler;
    }

    m_input.reserve(16*1024);
    m_output.reserve(16*1024);

    // create layers
    /*
    ILinkLayer* layer = NULL;
    Extension** ext = attr->exts;
    for (int i = 0; ext[i] != NULL; i++)
    {
        // Create Each Layer and link them together
        layer = createLayer(ext[i]);
        if (layer)
        {
            layer->m_pOwner = this;
            if (m_pFirstLayer == NULL) {
                layer->m_pNextLayer = layer->m_pPreLayer = NULL;
                m_pFirstLayer = m_pLastLayer = layer;
            }
            else {
                layer->m_pPreLayer = m_pLastLayer;
                m_pLastLayer->m_pNextLayer = layer;
                m_pLastLayer = layer;
            }
        }
    }
    */

    return m_connId;
}

int CConn::setNoDelay(bool flag)
{
    if(::setsockopt(m_sockfd, IPPROTO_TCP, TCP_NODELAY, (char*)&flag, sizeof(flag)) != 0) {
        return -1;
    }
    return 0;
}

int CConn::connect(uint32_t ip, uint16_t port)
{
    /*
	if ( INADDR_ANY != ip ) {
        m_remoteAddr.sin_addr.s_addr = ip;
    }
	if ( 0 != port) {
        m_remoteAddr.sin_port = htons(port);
    }
     */

    /*
    LOGE("CConn::connect, connId/ip/port=%d/%d/%d", m_connId, m_remoteAddr.sin_addr.s_addr, m_remoteAddr.sin_port);
    if (m_pFirstLayer) {
        return m_pFirstLayer->connect(m_remoteAddr.sin_addr.s_addr, m_remoteAddr.sin_port, m_sockType);
    }else {
        return _connect(m_remoteAddr.sin_addr.s_addr, m_remoteAddr.sin_port, m_sockType);
    }
    */

    int ret = _connect(m_remoteAddr.sin_addr.s_addr, m_remoteAddr.sin_port, m_sockType);
    if(ret == 0) {
        LOGE("CConn::connect, connect success!!!");
        this->onConnected();
    }else {
        this->onError();
        this->close();
    }
    return ret;
}

int CConn::_connect(int ip, short port, int sockType)
{
    //是否应该等connect好了，再打开读写？
    //IoEngine::Instance()->setEvent(this, m_sockfd, FD_OP_READ | FD_OP_WRITE, true);

    sockaddr_in destAddr;
    bzero(&destAddr, sizeof(destAddr));
    destAddr.sin_family = AF_INET;
    destAddr.sin_addr.s_addr = ip;
    destAddr.sin_port = port;

    LOGE("CConn::_connect, ip/port/type=%d/%d/%d", destAddr.sin_addr.s_addr, destAddr.sin_port, sockType);

    m_status = CONN_CONNECTING;

    notifyEvent(CNetEvent::EV_CONNECTING, 0);
    notifyConnState(CNetEventConnState::CS_TRANSPTLAYER_CONNECTING);

    if ( SOCK_STREAM == sockType )
    {
        if ( ::connect(m_sockfd, (struct sockaddr*)&destAddr, sizeof(destAddr) ) == -1 )
        {
            //if connect fail, treat EINPROGRESS as success, treat other errno as failed
            uint32_t uLastErrorNo = errno;
            if( EINPROGRESS == uLastErrorNo ) {
                //to judge it's connected
                fd_set fdr, fdw;
                struct timeval timeout;
                FD_ZERO(&fdr);
                FD_ZERO(&fdw);
                FD_SET(m_sockfd, &fdr);
                FD_SET(m_sockfd, &fdw);
                timeout.tv_sec = 5;
                timeout.tv_usec = 0;
                int rc = ::select(m_sockfd + 1, &fdr, &fdw, NULL, &timeout);
                LOGE("CConn::_connect, EINPROGRESS, select return %d", rc);
                //select调用失败
                if (rc < 0) {
                    LOGE("CConn::_connect, EINPROGRESS, select failed, err: %s", strerror(errno));
                    return -1;
                }

                if (rc == 0) {
                    //连接超时
                    LOGE("CConn::_connect, EINPROGRESS, select connect timeout");
                    return -1;
                }else if (rc == 1 && FD_ISSET(m_sockfd, &fdw)) {
                    //[1] 当连接成功建立时，描述符变成可写,rc=1
                    LOGE("CConn::_connect, EINPROGRESS, connect success");
                    return 0;
                }else if (rc == 2) {
                    //[2] 当连接建立遇到错误时，描述符变为即可读，也可写，rc=2 遇到这种情况，可调用getsockopt函数
                    int so_error;
                    socklen_t len = sizeof(so_error);
                    if (getsockopt(m_sockfd, SOL_SOCKET, SO_ERROR, &so_error, &len) == -1) {
                        LOGE("CConn::_connect, EINPROGRESS, getsockopt(SO_ERROR): %s", strerror(errno));
                        return -1;
                    }

                    if (so_error) {
                        errno = so_error;
                        LOGE("CConn::_connect, EINPROGRESS, connect error:%s", strerror(errno));
                        return -1;
                    }else {
                        LOGE("CConn::_connect, EINPROGRESS, but connected");
                        return 0;
                    }
                }else {
                    LOGE("CConn::_connect, EINPROGRESS, but NOT connected, rc: %d", rc);
                    return -1;
                }
            }else {
                LOGE("CConn::_connect, Error: connect failed, lastError=%u", uLastErrorNo);
                return -1;
            }
            return -1;
        }
        return 0;
    }
    else if ( SOCK_DGRAM == sockType )
    {
        int iError = bind(m_sockfd, (sockaddr*)&m_localAddr, sizeof(m_localAddr));
        if ( iError < 0 ) {
            return -1;
        }

        // 8MB buffer is enough
        int sock_bufsize = 8*1024*1024;
        if (setsockopt(m_sockfd, SOL_SOCKET, SO_RCVBUF, (char*)&sock_bufsize, sizeof(int)) != 0) {
            return -1;
        }
        if (setsockopt(m_sockfd, SOL_SOCKET, SO_SNDBUF, (char*)&sock_bufsize, sizeof(int)) != 0) {
            return -1;
        }
        return 0;
    }else {
        LOGE("CConn::_connect, Error: invalid type, only support TCP/UDP");
        return -1;
    }

    return 0;
}

int CConn::send(char* data, size_t len)
{
	if( m_status == CONN_CLOSE || m_sockfd == INVALID_SOCKET)
		return -1;

    //if (m_pFirstLayer)
    //    return m_pFirstLayer->send(data ,len);
    //else
        return _send(data, len);
}

int CConn::_send(char* data, size_t len)
{
    int ret = -1;
    if( !data || len == 0){
        return ret;
    }

    sockaddr_in* pAddr = NULL;
    if ( SOCK_DGRAM == m_sockType ) {
        pAddr = &m_remoteAddr;
    }

    ret = m_output.write(m_sockfd, data, len, pAddr, m_sockType);
    //if means that socket can't send more data just now(maybe block),notify select to tell us when to send another data still in buffer
    if (!m_output.empty())
        IoEngine::Instance()->setEvent(this, m_sockfd, FD_OP_WRITE);
    return ret;
}

int CConn::close()
{
	int ret = -1;
	if( m_sockfd != INVALID_SOCKET ){
		LOGE("CConn::close, m_connId/socket=", m_connId, m_sockfd);
	}

    //if (m_pFirstLayer)
    //    ret = m_pFirstLayer->close();
    //else
        ret = _close();

	return ret;
}

int CConn::_close()
{
    m_status = CONN_CLOSE;
	if( m_sockfd != INVALID_SOCKET ){
		IoEngine::Instance()->setEvent(this, m_sockfd, FD_OP_CLR);
	}

	m_pEvH = NULL;

	if( m_sockfd != INVALID_SOCKET )
	{
        LOGE("CConn::_close, real close socket");
        ::close(m_sockfd);
		m_sockfd = INVALID_SOCKET;
	}
    return 0;
}

int	CConn::addTimer(int id, int interval)
{
	IoEngine::Instance()->addTimer(m_sockfd, id, interval);
	return 0;
}

int	CConn::removeTimer(int id)
{
	IoEngine::Instance()->removeTimer(m_sockfd, id);
	return 0;
}

int CConn::onRecv()
{
    if (CONN_CLOSE == m_status || m_sockfd == INVALID_SOCKET ) {
        return -1;
    }

    // FD_OP_READ is set when connection established
    if ( CONN_CONNECTING == m_status )
    {
        onConnected();
        return 0;
    }

    sockaddr_in  srvAddr;
    sockaddr_in* pAddr = NULL;
    if ( SOCK_DGRAM == m_sockType ) {
        pAddr = &srvAddr;
    }

    int nrecv = m_input.read(m_sockfd, pAddr, m_sockType); //in UDP, pAddr point to where data from, maybe we should compare it with m_remoteAddr.anyway, let it go now
    if ( nrecv > 0)
    {
        if ( m_sockType == SOCK_DGRAM )
        {
            if ( pAddr->sin_addr.s_addr != m_remoteAddr.sin_addr.s_addr )
            {
                LOGE("Exp: udp packet not from server!!! from/server=", pAddr->sin_addr.s_addr, m_remoteAddr.sin_addr.s_addr);
            }
        }

        //if (m_pLastLayer)
        //    m_pLastLayer->onData( m_input, nrecv ); //handle the data that received just now, not all data in buffer
        //else
            _onData();
        return 0;
    }
    else
    {
        //connection maybe broken!
        LOGE("call recv(recvfrom) but read nothing, connection maybe reset by peer, connId/sockType/nrecv=%d/%d/%d", m_connId, m_sockType, nrecv);
        return onError();
    }
    return -1;
}

int CConn::_onData()
{
    if ( SOCK_STREAM == m_sockType)
    {
        //assemble packet
        while (!m_input.empty())
        {
            //length < header, not enough data, tcp wait
            if (m_input.size() < FIXED_HEADER_LENGTH) {
                break;
            }

            //let's see how long the packet is
            uint32_t length = peekLength(m_input.data());
            if (length <= FIXED_HEADER_LENGTH + FIXED_URI_LENGTH) {
                LOGE("TCP Fxxk!!!wrong length of a packet!!!len/buf_size=",length, m_input.size());
                onError();
				break;
            }

            //current data in buffer can't assemble a packet, not enough data
            if (m_input.size() < length) {
                break;
            }

            uint32_t uri = *((uint32_t*)(m_input.data() + FIXED_HEADER_LENGTH));
            const char* data = m_input.data() + FIXED_HEADER_LENGTH + FIXED_URI_LENGTH;
            LOGD("recv TCP packet, id:%u, len:%d, data:%s", uri, length, data);

            //the length only contains the body length, not contain the header itself
            Packet* pkt = MemPool::Instance()->create_packet(uri, m_input.data() + FIXED_HEADER_LENGTH + FIXED_URI_LENGTH, length - FIXED_HEADER_LENGTH - FIXED_URI_LENGTH);
            pkt->_timestamp = getCurTime();

            CNetEvent evtTcp;
            evtTcp.EvtType = CNetEvent::EV_IN;
            evtTcp.RetVal = 0;

            //notify the one who cares of this conn
            notifyInData(&evtTcp, pkt);

            m_input.erase(0, length);
        }
    }
    else if ( SOCK_DGRAM == m_sockType )
    {
        //length < header, not enough data, udp drop this unnormal packet
        if ( m_input.size() <= FIXED_HEADER_LENGTH + FIXED_URI_LENGTH) {
            LOGE("UDP Error: udp packet size < 4+4, drop it. size=", m_input.size());
            m_input.free();
            return 0;
        }

        uint32_t length = peekLength(m_input.data()); //let's see how long the packet is
        if ( length != m_input.size() ) {
            //an udp packet in input buffer
            LOGE("UDP Fxxk!!!wrong length of a packet!!!len/buf_size=",length,m_input.size());
            m_input.free();
            return 0;
        }

        uint32_t uri = *((uint32_t*)(m_input.data() + FIXED_HEADER_LENGTH));
        const char* data = m_input.data() + FIXED_HEADER_LENGTH + FIXED_URI_LENGTH;
        LOGD("recv UDP packet, id:%u, len:%d, data:%s", uri, length, data);

        Packet* pkt = MemPool::Instance()->create_packet(uri, m_input.data() + FIXED_HEADER_LENGTH + FIXED_URI_LENGTH, length - FIXED_HEADER_LENGTH - FIXED_URI_LENGTH);
        pkt->_timestamp = getCurTime();

        CNetEvent evtUdp;
        evtUdp.EvtType = CNetEvent::EV_IN;
        evtUdp.RetVal = 0;

        //notify the one who cares of this conn
        notifyInData(&evtUdp, pkt);

        m_input.erase(0, length);
    }

    return 0;
}

int CConn::onSend()
{
    if ( CONN_CLOSE == m_status || m_sockfd == INVALID_SOCKET )
        return -1;

    if ( CONN_CONNECTING == m_status )
    {
        onConnected();
        return 0;
    }

    //if (m_pLastLayer)
    //    return m_pLastLayer->onSend();
    //else
        return _onSend();
}

int CConn::_onSend()
{
	if ( CONN_CLOSE == m_status || m_sockfd == INVALID_SOCKET ) {
        return -1;
    }

    notifyEvent(CNetEvent::EV_SENT, 0);

    sockaddr_in* pAddr = NULL;
    if (SOCK_DGRAM == m_sockType) {
        pAddr = &m_remoteAddr;
    }

    m_output.flush(m_sockfd, pAddr, m_sockType);
    if (m_output.empty()) {
        //if data in buffer send over, unregister FD_OP_WRITE event from select (or onSend event will be given every time!)
        IoEngine::Instance()->setEvent(this, m_sockfd, FD_OP_WRITE, false); //unregister onWrite event
    }

    return 0;
}

bool CConn::isActive()
{
    return (INVALID_SOCKET != m_sockfd);
}

void CConn::setNonBlock()
{
    int fflags = fcntl(m_sockfd, F_GETFL);
    if (-1 == fflags){
        LOGE("set NonBlock error, socket id:%d", m_sockfd);
        return ;
    }
    fflags |= O_NONBLOCK;
    fcntl(m_sockfd, F_SETFL, fflags);
}

uint64_t CConn::getCurTime()
{
    uint64_t t = GetTimestamp();

    if ( t == 0 )
        t = g_uLastTime;
    else{
        g_uLastTime = t;
    }

    return t;
}

uint32_t CConn::peekLength(const void* d)
{
    uint32_t l = XHTONL( *((uint32_t*)d) );
    uint32_t len = 0;
    if ( (l & 0x80000000) == 0 )
    {
        len = l;
    }

    return len;
}

int CConn::onConnected()
{
	LOGE("CConn::onConnected, m_connId/socket/status=%d/%d/%d", m_connId, m_sockfd, m_status);

    m_status = CONN_CONNECTED;

    //这里只设置READ事件，WRITE事件在发送时设置，发送完事就unregister
    IoEngine::Instance()->setEvent(this, m_sockfd, FD_OP_READ, true);
    IoEngine::Instance()->setEvent(this, m_sockfd, FD_OP_WRITE, false); //unregister onWrite event from select (or onSend event will be given every time!)

    //if (m_pLastLayer)
    //    return m_pLastLayer->onConnected();
    //else
        return _onConnected();
}

int CConn::_onConnected()
{
    /*
    uint32_t id = 8899;
    const char* p = "smallball";
    const char* data = new char[sizeof(uint32_t) + strlen(p)];
    memcpy((void*)data, &id, sizeof(uint32_t));
    memcpy((void*)(data+4), p, strlen(p));

    NetEngine::Packet* pkt = NetEngine::PacketAlloc(id, data, strlen(p));
    this->send(pkt->_data, pkt->_dataLen);
    NetEngine::PacketRelease(pkt);
    */

    notifyConnState(CNetEventConnState::CS_CONNECTED);
    notifyEvent(CNetEvent::EV_CONNECTED, 0);
    return 0;
}

int CConn::onError()
{
	LOGE("CConn::onError, m_connId/socket/status=%d/%d/%d", m_connId, m_sockfd, m_status);

    IoEngine::Instance()->setEvent(this, m_sockfd, FD_OP_CLR);
    //if (m_pLastLayer)
    //    return m_pLastLayer->onError();
    //else
        return _onError();
}

int CConn::_onError()
{
    notifyEvent(CNetEvent::EV_ERROR, 0);
    return 0;
}

int CConn::onTimer(int id)
{
	notifyEvent(CNetEvent::EV_TIMER, id);
	return 0;
}

void CConn::notifyConnState(int state)
{
    /*
    CNetEventConnState evt;
    evt.EvtType = CNetEvent::EV_CONNSTATE;
    evt.ConnId = m_connId;
    evt.RetVal = 0;
    evt.state = state;
    evt.timestamp = GetTimestamp();
    */

    //if(m_pEvH)
    //   m_pEvH->OnEvent(&evt, NULL);
    jni_callback::instance().on_event(m_connId, CNetEvent::EV_CONNSTATE, state);
}

void CConn::notifyEvent(int eventType, unsigned long retVal)
{
    /*
    CNetEventConnState evt;
    evt.EvtType = eventType;
    evt.ConnId = m_connId;
    evt.RetVal = retVal;
    evt.state = CNetEventConnState::CS_STATE_UNKNOWN;
    evt.timestamp = GetTimestamp();
    */

    //if(m_pEvH)
    //   m_pEvH->OnEvent(&evt, NULL);
    jni_callback::instance().on_event(m_connId, eventType, retVal);
}

void CConn::notifyInData(CNetEvent* evt, Packet* pkt)
{
    if(evt == NULL || pkt == NULL){
        return;
    }

    /*
    CNetEventConnState event;
    event.ConnId = m_connId;
    event.EvtType = evt->EvtType;
    event.RetVal = evt->RetVal;
    event.state = CNetEventConnState::CS_CONNECTED;
    event.timestamp = GetTimestamp();
    */

    //if(m_pEvH)
    //   m_pEvH->OnEvent(&event, pkt);
    jni_callback::instance().on_data(m_connId, pkt->_uri, pkt->_data+2*sizeof(uint32_t), pkt->_dataLen-2*sizeof(uint32_t));
}

ILinkLayer* CConn::createLayer(Extension* ext)
{
/*
    ILinkLayer* layer = NULL;
    switch (ext->extID)
    {
    case ExtEncryption::EXTID:
        {
            layer = new LinkLayerEnc();
            layer->init(ext);
            break;
        }
    case ExtProxy::EXTID:
        {
            layer = new LinkLayerProxy();
            layer->init(ext);
            break;
        }
    case ExtCompress::EXTID:
        {
            layer = new LinkLayerCompress();
            layer->init(ext);
            break;
        }

    case ExtDirect::EXTID:
        {
            layer = new LinkLayerDirect();
            layer->init(ext);
            break;
        }
    default:
        return NULL;
    }
    return layer;
*/
    return nullptr;
}

uint32_t CConn::tryPartitionPkt()
{
    if ( SOCK_STREAM == m_sockType && m_input.size() >= 4)
    {
        uint32_t length = peekLength(m_input.data()); //let's see how long the packet is
        if (length <= 4)
        {
            LOGE("tryPartitionPkt: wrong length of a packet!!!len/buf_size=",length, m_input.size());
            return -1;
        }
        else if (length < m_input.size())//current data in buffer can't assemble a packet, not enough data
        {
            return 0;
        }
        else
        {
            return length;
        }
    }

    return 0;
}

int CConn::_onMsgOOB(char* data, size_t len)    //MSG_OOB, urgent channel
{
    if(m_sockType != SOCK_STREAM || len < 4 || data == NULL) {
        return -1;
    }

    uint32_t length = peekLength(data);
    if(length <= 8 || len < length) {
        LOGE("_onMsgOOB!!!wrong length of a MSG_OOB packet!!!len/buf_size=",length, len);
        return -1;
    }

    uint32_t uri = peekLength(data+4);

    CNetEvent evt;
    Packet* pkt = MemPool::Instance()->create_packet(uri, data, length);
    pkt->_timestamp = getCurTime();

    if ( m_pEvH )
    {
        evt.EvtType = CNetEvent::EV_IN;
        evt.RetVal = 0;
        m_pEvH->OnEvent(&evt, pkt);
    }

    return 0;
}

int CConn::_onDataDirect()       //not partition.
{
    CNetEvent evt;
    if ( SOCK_DGRAM == m_sockType )
    {
        if ( m_input.size() < 4 ) //length < header, not enough data, udp drop this unnormal packet
        {
            LOGE("UDP Error: udp packet size < 4, drop it. size=", m_input.size());
            m_input.free();
            return 0;
        }
    }

    if ( SOCK_STREAM == m_sockType || SOCK_DGRAM == m_sockType)
    {
        //assemble packet
        if(!m_input.empty())
        {
            uint32_t length = (uint32_t)(m_input.size()); //let's see how long the packet is
            Packet* pkt = MemPool::Instance()->create_packet(0, m_input.data(), length);
            pkt->_timestamp = getCurTime();

            if ( m_pEvH )
            {
                evt.EvtType = CNetEvent::EV_INSTREAM;
                evt.RetVal = 0;
                m_pEvH->OnEvent(&evt, pkt);
            }
            m_input.erase(0, length);
        }
    }

    return 0;
}