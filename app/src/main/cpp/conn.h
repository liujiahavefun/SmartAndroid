//
// Created by liujia on 16/8/20.
//

#ifndef CLOUDWOOD_ANDROID_NEW_CONN_H
#define CLOUDWOOD_ANDROID_NEW_CONN_H

#include "net_engine.h"
#include "blockbuf.h"
#include <string>

//先用前向声明代替
//#include "link_layer.h"

using namespace NetEngine;

class CConn;

class ILinkLayer
{
    friend class CConn;
public:
    ILinkLayer();
    virtual ~ILinkLayer();
    virtual int init(NetEngine::Extension*) = 0;

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

struct IConn
{
    virtual int onRecv() = 0;
    virtual int onSend() = 0;
    virtual bool isActive() =0;
	virtual int onTimer(int id) = 0;
};

class CConn
    : public IConn
{
public:
    CConn();
    ~CConn();

public:
    virtual int onRecv();
    virtual int onSend();
	virtual int onTimer(int id);
    virtual bool isActive();

public:
    /* return m_connId if success, else return -1 */
    int     init(NetEngine::ConnAttr* attr);
    /* return 0 if success, else return errno */
    int     connect(uint32_t ip, uint16_t port);
    int     send(char* data, size_t len);
    int     close();
    int     addTimer(int id, int interval);
	int     removeTimer(int id);
    int     _connect(uint32_t ip, uint16_t port, int sockType);
    int     _send(char* data, size_t len);
    int     _close();
	int		setNoDelay(bool flag);

public:
    int     onConnected();
    int     onError();

    int     _onMsgOOB(char* data, size_t len);      //MSG_OOB, urgent channel
    int     _onDataDirect();        //not partition.
    int     _onData();
    int     _onSend();
    int     _onConnected();
    int     _onError();

    int     getConnId() { return m_connId; }
    IEventHandler* getEvHandler() { return m_pEvH; }

    uint32_t    tryPartitionPkt();
    void        notifyConnState(int state);
    void        notifyEvent(int eventType, unsigned long retVal);
    void        notifyInData(CNetEvent* evt, Packet* pkt);
private:
    void        setNonBlock();
    uint64_t    getCurTime();
    uint32_t    peekLength(const void* d);
    ILinkLayer* createLayer(Extension* ext);

private:
    SOCKET      m_sockfd;
    int         m_connId;
    int         m_sockType; //SOCK_DGRAM or SOCK_STREAM
    CONNSTATUS  m_status;

    sockaddr_in m_localAddr;
    sockaddr_in m_remoteAddr;

    inputbuf_t  m_input;  //read buffer
    outputbuf_t m_output; //write buffer

    ILinkLayer* m_pFirstLayer;
    ILinkLayer* m_pLastLayer;

    IEventHandler*  m_pEvH;
};

#endif //CLOUDWOOD_ANDROID_NEW_CONN_H
