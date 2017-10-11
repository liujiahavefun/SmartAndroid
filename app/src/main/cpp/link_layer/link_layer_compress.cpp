//
// Created by liujia on 16/8/20.
//

#include "link_layer_compress.h"

#include <assert.h>
#include <zlib.h>
#include "conn.h"

LinkLayerCompress::LinkLayerCompress(){
	mStatus = status_new;
}

int LinkLayerCompress::init(NetMod::Extension* pExt)
{
  m_uExtID = pExt->extID;

	URIreq = _PPackCompressReqURI;
	URIres = _PPackCompressResURI;

	return 0;
}

LinkLayerCompress::~LinkLayerCompress(void)
{
}

int LinkLayerCompress::onData(inputbuf_t& input, size_t nrecv)
{
	if (mStatus == status_init)
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

    _PPackCompress* pCompressRes = (_PPackCompress*)(input.tail()-nrecv);
    if (nrecv < sizeof(_PPackCompress) || pCompressRes->uri != URIres)
    {
      input.erase( input.size()-nrecv, nrecv);
      NET_LOG("LinkLayerCompress::onData, Error: auth failed, nrecv/resUri/localUri=",nrecv,pCompressRes->uri,URIres);
      return m_pOwner->_onError();
    }

	// uncompress and send up
	UINT32 destlen = pCompressRes->zsize;
	UCHAR * dest = new UCHAR[pCompressRes->zsize];
	UnzipCompress(dest, destlen, (UCHAR *)pCompressRes->pack, pCompressRes->packlen);
	// add to new pack

    input.erase(0,nrecv);
    int size = destlen + 10;
    char* newPkt = new char[size];

 		int rescode = 200, encUri = _PExchangeKeyResURI;
 		memcpy(newPkt, &size, 4);
 		memcpy(newPkt + 4, &encUri, 4);
 		memcpy(newPkt + 8, &rescode, 2);
 		memcpy(newPkt + 10, dest, destlen);
    input.append(newPkt,size);
		// release temp
		delete[] dest;
    delete[] newPkt;
		mStatus = status_xchg;

		if (m_pPreLayer)
    {
			return m_pPreLayer->onData(input,size);
		}
		else
    {
			return m_pOwner->_onData();
		}
	}
	else if (mStatus == status_xchg)
	{
		mStatus = status_compress;
    return this->onData(input,nrecv);
	}

	if (m_pPreLayer)
  {
		return m_pPreLayer->onData(input, nrecv);
	}
	else
  {
		return m_pOwner->_onData();
	}
}

int LinkLayerCompress::send(char* data, int len)
{
	if (mStatus == status_new)
  {
		UINT32 encUri = *(UINT32*)(data + 4);
		if (encUri != _PExchangeKeyURI)
		{
			mStatus = status_compress;
			return this->send(data, len);
		}
		// compress
		UINT32 destlen = compressBound(len - 10);
		UCHAR * dest = new UCHAR[destlen];
		ZipCompress(dest, destlen, (UCHAR *)data + 10, len - 10);
		// add to new pack
		int size = sizeof(struct _PPackCompress) - 1 + destlen;
    char* newPkt = new char[size];
		_PPackCompress* p = (_PPackCompress*)newPkt;
		p->uri = URIreq;
		p->ResCode = 200;
		p->len = size;
		p->zsize = len - 10;
		p->packlen = (UINT16)destlen;
		memcpy(p->pack, dest, destlen);
		// release old packet
		delete[] dest;

		mStatus = status_init;
		if (m_pNextLayer)
    {
			return m_pNextLayer->send(newPkt, size);
		}
		else
    {
			return m_pOwner->_send(newPkt, size);
		}
	}

	if (m_pNextLayer)
  {
		return m_pNextLayer->send(data, len);
	}
	else
  {
		return m_pOwner->_send(data, len);
	}
}

int LinkLayerCompress::ZipCompress( UCHAR * dest, UINT32 &destlen, UCHAR* sour, UINT32 sourlen )
{
	uLongf len = destlen;
	int ret = compress2(dest, &len, (const Bytef *)sour, (uLongf)sourlen, 1);
	destlen = (UINT32)len;
	return ret;
}

int LinkLayerCompress::UnzipCompress( UCHAR * dest, UINT32 &destlen, UCHAR* sour, UINT32 sourlen )
{
	uLongf len = destlen;
	int ret = uncompress(dest, &len, (const Bytef *)sour, (uLongf)sourlen);
	destlen = (UINT32)len;
	return ret;
}
