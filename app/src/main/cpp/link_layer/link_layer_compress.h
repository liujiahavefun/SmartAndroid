//
// Created by liujia on 16/8/20.
//

#ifndef CLOUDWOOD_ANDROID_NEW_LINK_LAYER_COMPRESS_H
#define CLOUDWOOD_ANDROID_NEW_LINK_LAYER_COMPRESS_H

#include "link_layer.h"

class LinkLayerCompress : public ILinkLayer
{
private:
//  	const static uint32_t _PPackCompressReqURI = 22 << 8 | protocol::SESSION_SVID;
//  	const static uint32_t _PPackCompressResURI = 23 << 8 | protocol::SESSION_SVID;
//  	const static uint32_t _PExchangeKeyURI = 17 << 8 | protocol::SESSION_SVID;
//  	const static uint32_t _PExchangeKeyResURI = 135 << 8 | protocol::SESSION_SVID;
  const static uint32_t _PPackCompressReqURI = 22 << 8 | 4;
  const static uint32_t _PPackCompressResURI = 23 << 8 | 2;
  const static uint32_t _PExchangeKeyURI = 17 << 8 | 4;
  const static uint32_t _PExchangeKeyResURI = 135 << 8 | 2;

public:
	LinkLayerCompress();
	virtual ~LinkLayerCompress(void);
	int init(NetMod::Extension* pExt);
	int onData(inputbuf_t& input, size_t nrecv);
	int send(char* data, int len);

private:
	int ZipCompress(UCHAR * dest, UINT32 &destlen, UCHAR* sour, UINT32 sourlen);
	int UnzipCompress(UCHAR * dest, UINT32 &destlen, UCHAR* sour, UINT32 sourlen);

private:
	enum {status_new, status_init, status_xchg, status_compress};
	int				mStatus;
	UINT32			URIreq;
	UINT32			URIres;

#pragma pack(push)
#pragma pack(1)
	struct _PPackCompress{
		UINT32	len;
		UINT32	uri;
		UINT16	ResCode;
		UINT32	zsize;
		UINT32	packlen;
		UCHAR	pack[1];
	};
#pragma pack (pop)

};

#endif //CLOUDWOOD_ANDROID_NEW_LINK_LAYER_COMPRESS_H
