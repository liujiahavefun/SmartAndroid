//
// Created by liujia on 16/8/20.
//

#include "link_layer_proxy.h"
#include "conn.h"

// 0x05 = SOCK5; 02 = two Auth methods, 0x00 = No Auth; 0x02 = user/pwd Auth
// I need a base64 encoder to encode the "username:passwd"
static const char cb64[]="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
inline void b64encodeblock(unsigned char *in, unsigned char *out, int len)
{
    out[0] = cb64[ in[0] >> 2 ];
    out[1] = cb64[ ((in[0] & 0x03) << 4) | ((in[1] & 0xf0) >> 4) ];
    out[2] = (unsigned char) (len > 1 ? cb64[ ((in[1] & 0x0f) << 2) | ((in[2] & 0xc0) >> 6) ] : '=');
    out[3] = (unsigned char) (len > 2 ? cb64[ in[2] & 0x3f ] : '=');
}


LinkLayerProxy::LinkLayerProxy()
: m_status(STATUS_NEW)
, m_uHostIp(0)
, m_uHostPort(0)
, m_uProxyIp(0)
, m_uProxyPort(0)
, m_proxyType(ExtProxy::SOCK5)
{
    memset(m_userName, 0, ARRAY_LEN);
    memset(m_passWd, 0, ARRAY_LEN);
}

LinkLayerProxy::~LinkLayerProxy()
{

}

int LinkLayerProxy::init(NetMod::Extension* e)
{
    m_uExtID = e->extID;
    ExtProxy* ext = (ExtProxy*)e;

    m_proxyType  = ext->Type;
    m_uProxyPort = htons(ext->Port);
    m_uProxyIp   = ext->Ip;
    strncpy(m_userName, ext->username, ARRAY_LEN);
    strncpy(m_passWd, ext->passwd, ARRAY_LEN);
    NET_LOG("LinkLayerProxy::init, proxy type/ip/port=",m_proxyType, ext->Ip, ext->Port);
    return 0;
}

int LinkLayerProxy::connect(uint32_t ip, uint16_t port, int sockType)
{
    if ( SOCK_DGRAM == sockType )
        return -1;

    m_uHostIp = ip;
    m_uHostPort = port;

    m_pOwner->notifyConnState(CNetEventConnState::CS_PROXYLAYER_CONNECTING);

    if ( m_pNextLayer)
        return m_pNextLayer->connect(m_uProxyIp, m_uProxyPort, sockType);
    else
        return m_pOwner->_connect(m_uProxyIp, m_uProxyPort, sockType);
}

int LinkLayerProxy::onConnected()
{
    char* sendBuf = NULL;
    int bufLen = 0;

    m_pOwner->notifyConnState(CNetEventConnState::CS_PROXYLAYER_CONNECTED);

    if ( m_proxyType == ExtProxy::SOCK5 )
    {
        if (m_userName[0] == '\0') {
            sendBuf = new char[3];
            bufLen = 3;
            sendBuf[0] = 5;
            sendBuf[1] = 1;
            sendBuf[2] = 0;
        }
        else {
            sendBuf = new char[4];
            bufLen = 4;
            sendBuf[0] = 5;
            sendBuf[1] = 2;
            sendBuf[2] = 0;
            sendBuf[3] = 2;
        }
        m_status = STATUS_INIT;
    }
    else
    {
        sendBuf = new char[2048];
		bufLen = 2048;
        unsigned char cb64in[1024];
        unsigned char cb64out[1024];
        int cbin_len = _snprintf((char*)cb64in, 1024, "%s:%s", m_userName, m_passWd);
        int cbout_len = 0;
        unsigned char * cb64in_ptr = cb64in;
        unsigned char * cb64out_ptr = cb64out;
        while (cbin_len > 0)
        {
            b64encodeblock(cb64in_ptr, cb64out_ptr, cbin_len);
            cbin_len -=3;
            cb64in_ptr += 3;
            cbout_len += 4;
            cb64out_ptr += 4;
        }
        cb64out[cbout_len] = 0;
        int http_req_len = 0;

        struct in_addr addr;
        memcpy(&addr, &m_uHostIp, 4);
#if (_MSC_VER <= 1310)
        http_req_len = _snprintf(sendBuf, bufLen,
            "CONNECT %s:%d HTTP/1.1\r\nHost %s:%d\r\nAuthorization: Basic %s\r\nProxy-Authorization: Basic %s\r\n\r\n",
            inet_ntoa(addr), ntohs(m_uHostPort),
            inet_ntoa(addr), ntohs(m_uHostPort),
            cb64out, cb64out);
#else
        http_req_len = _snprintf_s(sendBuf, bufLen, _TRUNCATE,
            "CONNECT %s:%d HTTP/1.1\r\nHost %s:%d\r\nAuthorization: Basic %s\r\nProxy-Authorization: Basic %s\r\n\r\n",
            inet_ntoa(addr), ntohs(m_uHostPort),
            inet_ntoa(addr), ntohs(m_uHostPort),
            cb64out, cb64out);
#endif
        bufLen = http_req_len;
        m_status = STATUS_CONN;
    }

    if (m_pNextLayer)
        m_pNextLayer->send(sendBuf, bufLen);
    else
        m_pOwner->_send(sendBuf, bufLen);

    delete[] sendBuf;
    return 0;
}

int LinkLayerProxy::onData(inputbuf_t& input, size_t nrecv)
{
    //char** recvData = &(input.tail() - nrecv);
    if ( m_proxyType == ExtProxy::SOCK5 )
    {
        if ( m_status == STATUS_NEW )
        {
            input.erase( input.size()-nrecv, nrecv);
            return -1;
        }
        else if ( m_status == STATUS_INIT )
        {
            if ( nrecv < 2 || (input.tail() - nrecv)[0] != 5 )
            {
                // error response
                input.erase( input.size()-nrecv, nrecv);
                NET_LOG("LinkLayerProxy::onData, Error: socks5 error response, connid=",m_pOwner->getConnId());
                return m_pOwner->_onError();
            }
            if ( (input.tail() - nrecv)[1] == 0 )
            {
                // NULL auth
                m_status = STATUS_AUTH;
                // make a fake auth success pkt and call self again
                (input.tail() - nrecv)[1] = 0;
                onData(input, 2);
            }
            else if ( (input.tail() - nrecv)[1] == 2 )
            {
                // User/Pwd auth
                m_status = STATUS_AUTH;
                size_t pkt_len = 3 + strlen(m_userName) + strlen(m_passWd);
                int pos = 0;
                char* pkt_auth = new char[pkt_len];

                pkt_auth[pos++] = 1;
                pkt_auth[pos++] = (char)strlen(m_userName);
                pos += _snprintf(pkt_auth + pos, pkt_len - pos, "%s", m_userName);
                pkt_auth[pos++] = (char)strlen(m_passWd);
                pos += _snprintf(pkt_auth + pos, pkt_len - pos, "%s", m_passWd);

                if ( m_pNextLayer )
                    m_pNextLayer->send(pkt_auth, (int)pkt_len);
                else
                    m_pOwner->_send(pkt_auth, pkt_len);

                delete[] pkt_auth;
                input.erase( input.size()-nrecv, nrecv );
            }
            else {
                // server not accept our auth propose
                input.erase( input.size()-nrecv, nrecv);
                NET_LOG("LinkLayerProxy::onData, Error: socks5 server not accept our auth propose, connid=",m_pOwner->getConnId());
                return m_pOwner->_onError();
            }
        }
        else if (m_status == STATUS_AUTH)
        {
            if ( nrecv != 2 || (input.tail() - nrecv)[1] != 0 )
            {
                // auth failed
                input.erase( input.size()-nrecv, nrecv);
                NET_LOG("LinkLayerProxy::onData, Error: socks5 auth failed, connid=",m_pOwner->getConnId());
                return m_pOwner->_onError();
            }
            m_status = STATUS_CONN;
            // send connect request
            char* pkt_conn = new char[10];
            pkt_conn[0] = 5;	// socks version
            pkt_conn[1] = 1;	// command code, 1 = connect
            pkt_conn[2] = 0;	// reserved
            pkt_conn[3] = 1;	// addr type, 1 = IPv4
            *(unsigned int*)(pkt_conn + 4) = m_uHostIp;		// IPv4 addr
            *(unsigned short*)(pkt_conn + 8) = m_uHostPort;

            if ( m_pNextLayer )
                m_pNextLayer->send(pkt_conn, 10);
            else
                m_pOwner->_send(pkt_conn, 10);

            input.erase( input.size()-nrecv, nrecv);
        }
        else if (m_status == STATUS_CONN)
        {
            if ( nrecv < 4 || (input.tail() - nrecv)[0] != 5 || (input.tail() - nrecv)[1] != 0)
            {
                // conn failed
                input.erase( input.size()-nrecv, nrecv);
                NET_LOG("LinkLayerProxy::onData, Error: socks5 conn failed, connid=",m_pOwner->getConnId());
                return m_pOwner->_onError();
            }
            m_status = STATUS_EST;
            NET_LOG("LinkLayerProxy::onData, SYN auth done, connid=",m_pOwner->getConnId());
            if ( m_pPreLayer )
                m_pPreLayer->onConnected();
            else
                m_pOwner->_onConnected();

            input.erase( input.size()-nrecv, nrecv);
        }
        else if (m_status == STATUS_EST)
        {
            if ( m_pPreLayer )
                m_pPreLayer->onData(input, nrecv);
            else
                m_pOwner->_onData();
        }
    }
    else
    {
        if ( m_status != STATUS_CONN && m_status != STATUS_EST )
        {
            input.erase( input.size()-nrecv, nrecv);
            return -1;
        }
        else if ( m_status == STATUS_CONN )
        {
            unsigned int uHttpStatus;
            if (sscanf((input.tail() - nrecv), "HTTP/%*u.%*u %u", &uHttpStatus) != 1
                || uHttpStatus != 200 )
            {
                NET_LOG("LinkLayerProxy::onData, Error: HTTP server auth failed, connid=",m_pOwner->getConnId());
                m_pOwner->_onError();
            }
            else
            {
                m_status = STATUS_EST;
                if ( m_pPreLayer )
                    m_pPreLayer->onConnected();
                else
                    m_pOwner->_onConnected();
            }
            input.erase( input.size()-nrecv, nrecv);
        }
        else
        {
            if ( m_pPreLayer )
                m_pPreLayer->onData(input, nrecv);
            else
                m_pOwner->_onData();
        }
    }
    return 0;
}

