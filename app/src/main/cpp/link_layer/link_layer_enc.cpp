//
// Created by liujia on 16/8/20.
//

#include "link_layer_enc.h"
#include "conn.h"

LinkLayerEnc::LinkLayerEnc()
: m_status(STATUS_NEW)
, m_URIreq(0)
, m_URIres(0)
{
	m_RSAKey = NETMOD_RSA_generate_key(0, 0, 0, 0);
}

LinkLayerEnc::~LinkLayerEnc()
{
	if( m_RSAKey )
	{
		NETMOD_RSA_free(m_RSAKey);
		m_RSAKey = NULL;
	}
}

int LinkLayerEnc::init(Extension* pExt)
{
    m_uExtID = pExt->extID;

    ExtEncryption* pEnc = (ExtEncryption*)pExt;

    m_URIreq = pEnc->URI;
    m_URIres = pEnc->ResURI;
    return 0;
}

int LinkLayerEnc::send(char* data, int len)
{
    if (m_status != STATUS_ENC)
        return -1;

    // decrypt and send up
    NETMOD_RC4(&m_sendKey, len, (unsigned char*)data, (unsigned char*)data);

    if (m_pNextLayer) {
        return m_pNextLayer->send(data, len);
    }
    else {
        return m_pOwner->_send(data, len);
    }
}

int LinkLayerEnc::onConnected()
{
    _PExchangeKey* pExKey = (_PExchangeKey*)malloc( sizeof(struct _PExchangeKey) );
    pExKey->uri = m_URIreq;
    pExKey->ResCode = 200;
    pExKey->plen = NETMOD_BN_bn2bin(m_RSAKey->n, pExKey->pubKey);
    pExKey->elen = NETMOD_BN_bn2bin(m_RSAKey->e, pExKey->e);
    pExKey->len  = 14 + pExKey->plen + pExKey->elen; // to change

    m_status = STATUS_INIT;

    if (m_pNextLayer)
        m_pNextLayer->send((char*)pExKey, pExKey->len);
    else
        m_pOwner->_send((char*)pExKey, pExKey->len);

    free(pExKey);

    m_pOwner->notifyConnState(CNetEventConnState::CS_ENCLAYER_START);

    return 0;
}

int LinkLayerEnc::onData(inputbuf_t& input, size_t nrecv)
{
    if ( m_status == STATUS_NEW )
    {
        input.erase( input.size()-nrecv, nrecv);
        NET_LOG("LinkLayerEnc::onData, Error: but status == STATUS_NEW, connid=",m_pOwner->getConnId());
        return m_pOwner->_onError();
    }
    else if (m_status == STATUS_INIT)
    {
        uint32_t pktLen = m_pOwner->tryPartitionPkt();
        if(pktLen == -1)
        {
            input.erase( input.size()-nrecv, nrecv);
            return m_pOwner->_onError();
        }
        else if(pktLen == 0)
        {
            return 0;
        }
        m_status = STATUS_XCHG;
        return this->onData(input, nrecv);
    }
    else if ( m_status == STATUS_XCHG )
    {
        _PExchangeKeyRes* pExRes = (_PExchangeKeyRes*)(input.tail()-nrecv);
        if (nrecv < sizeof(_PExchangeKeyRes) || pExRes->uri != m_URIres)
        {
            input.erase( input.size()-nrecv, nrecv);
            NET_LOG("LinkLayerEnc::onData, Error: auth failed, nrecv/resUri/localUri=",nrecv,pExRes->uri,m_URIres);
            return m_pOwner->_onError();
        }
        // decode the key and set rc4 key
        unsigned char key[64];
        int num = NETMOD_RSA_private_decrypt(pExRes->keylen, pExRes->sesKey, key, m_RSAKey, RSA_PKCS1_PADDING);
        if (num != SESSIONKEY_LENGTH)
        {
            input.erase( input.size()-nrecv, nrecv);
            NET_LOG("LinkLayerEnc::onData, Error: num != SESSIONKEY_LENGTH, num/SESSIONKEY_LENGTH=",num,SESSIONKEY_LENGTH);
            return m_pOwner->_onError();
        }

        NETMOD_RC4_set_key(&m_sendKey, SESSIONKEY_LENGTH, key);
        NETMOD_RC4_set_key(&m_recvKey, SESSIONKEY_LENGTH, key);

        //支持_PEchangeKeyRes中包一个协议包， 以string的格式传下来..
        //12 = sizeof(_PExchangeKeyRes.{len, uri, resCode, keylen})
        //2 是 _PExchangeKeyRes包的带外数据是通过string下发的，数据需要便宜两个字节.
        uint32_t baseLen = 12 + pExRes->keylen + 2;
        if(pExRes->len > baseLen)
        {
            NET_LOG("LinkLayerEnc, get OOB MSG from PExchangeKeyRes, connid/pExRes->len/OOB_MSG->len=", m_pOwner->getConnId(), pExRes->len, pExRes->len-baseLen);
            m_pOwner->_onMsgOOB(input.tail()-nrecv+baseLen, pExRes->len - baseLen);
        }

        m_status = STATUS_ENC;
		NET_LOG("LinkLayerEnc::onData, RSA done. connid=",m_pOwner->getConnId());
        m_pOwner->notifyConnState(CNetEventConnState::CS_ENCLAYER_FINISH);

        // this layer is ready, call next layer's OnConnect
        if (m_pPreLayer)
            m_pPreLayer->onConnected();
        else
            m_pOwner->_onConnected();

        // if we have more data, call self's OnData to dec
        if (nrecv > pExRes->len)
        {
            input.erase( input.size()-nrecv, pExRes->len );
            return this->onData( input, (nrecv - pExRes->len));
        }
        else
            input.erase( input.size()-nrecv, nrecv );
    }
    else
    {
        //in case
        if ( input.size() < nrecv )
        {
            NET_LOG("LinkLayerEnc::onData, Error: input.size() < nrecv, can't RC4 decrypt, size/nrecv=",input.size(), nrecv);
            return m_pOwner->_onError();
        }
        // decrypt and send up
        NETMOD_RC4(&m_recvKey, nrecv, (unsigned char*)input.tail()-nrecv, (unsigned char*)input.tail()-nrecv);
        if (m_pPreLayer) {
            return m_pPreLayer->onData(input, nrecv);
        }
        else {
            return m_pOwner->_onData();
        }
    }
    return 0;
}
