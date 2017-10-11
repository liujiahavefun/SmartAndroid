//
// Created by liujia on 16/8/20.
//

#ifndef CLOUDWOOD_ANDROID_NEW_LINK_LAYER_H
#define CLOUDWOOD_ANDROID_NEW_LINK_LAYER_H

#include "extension.h"
#include "blockbuf.h"

class CConn;
class ILinkLayer
{
    friend class CConn;
public:
    ILinkLayer();
    virtual ~ILinkLayer();
    virtual int init(NetMod::Extension*) = 0;

public:
    virtual int connect(uint32_t ip, uint16_t port, int sockType);
    virtual int send(char* data, int len);
    virtual int close();

public:
    virtual int onConnected();
    /*
    * operation should be focus on last received data (input.tail()-nrecv, nrecv), buffer reference(input) should be setted as one of the params
    * because operation here may include 'new' or 'delete', should increase len or do some operations on input when 'new' or 'delete' operation happened
    * @param input : buffer reference which stored data recv from socket
    * @param nrecv : last recv data len from socket(::recv)
    */
    virtual int onData(inputbuf_t& input, size_t nrecv);
    virtual int onSend();
    virtual int onError();


protected:
    ILinkLayer*     m_pPreLayer;
    ILinkLayer*     m_pNextLayer;
    CConn*          m_pOwner;
    int             m_uExtID;
};


#endif //CLOUDWOOD_ANDROID_NEW_LINK_LAYER_H
