//
// Created by liujia on 16/8/20.
//

#include "link_layer_direct.h"
#include "conn.h"

LinkLayerDirect::LinkLayerDirect()
{
}

LinkLayerDirect::~LinkLayerDirect()
{

}

int LinkLayerDirect::init(NetMod::Extension*)
{
	return 0;
}

int LinkLayerDirect::send(char* data, int len)
{
    if(m_pPreLayer)
        return m_pPreLayer->send(data, len);
    else
        return m_pOwner->_send(data, len);
}

int LinkLayerDirect::onConnected()
{
    if(m_pPreLayer)
        return m_pPreLayer->onConnected();
    else
        return m_pOwner->_onConnected();
}

int LinkLayerDirect::onData(inputbuf_t& input, size_t nrecv)
{
    //just call
    if(m_pPreLayer)
        return m_pPreLayer->onData(input, nrecv);
    else
        return m_pOwner->_onDataDirect();
}