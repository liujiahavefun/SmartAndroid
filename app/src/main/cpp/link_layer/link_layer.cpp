//
// Created by liujia on 16/8/20.
//

#include "link_layer.h"
#include "conn.h"

ILinkLayer::ILinkLayer()
: m_pPreLayer(NULL)
, m_pNextLayer(NULL)
, m_pOwner(NULL)
, m_uExtID(-1)
{

}

ILinkLayer::~ILinkLayer()
{

}

int ILinkLayer::connect(uint32_t ip, uint16_t port, int sockType)
{
    if (m_pNextLayer)
        return m_pNextLayer->connect(ip, port, sockType);
    else
        return m_pOwner->_connect(ip, port, sockType);
}

int ILinkLayer::send(char* data, int len)
{
    if (m_pNextLayer)
        return m_pNextLayer->send(data, len);
    else
        return m_pOwner->_send(data, len);
}

int ILinkLayer::close()
{
    if (m_pNextLayer)
        return m_pNextLayer->close();
    else
        return m_pOwner->_close();
}

int ILinkLayer::onConnected()
{
    if (m_pPreLayer)
        return m_pPreLayer->onConnected();
    else
        return m_pOwner->_onConnected();
}

int ILinkLayer::onData(inputbuf_t& input, size_t nrecv)
{
    if (m_pPreLayer)
        return m_pPreLayer->onData(input, nrecv);
    else
        return m_pOwner->_onData();
}

int ILinkLayer::onSend()
{
    if (m_pPreLayer)
        return m_pPreLayer->onSend();
    else
        return m_pOwner->_onSend();
}

int ILinkLayer::onError()
{
    if (m_pPreLayer)
        return m_pPreLayer->onError();
    else
        return m_pOwner->_onError();
}

