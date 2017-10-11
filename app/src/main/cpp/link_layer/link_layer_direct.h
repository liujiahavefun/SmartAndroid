//
// Created by liujia on 16/8/20.
//

#ifndef CLOUDWOOD_ANDROID_NEW_LINK_LAYER_DIRECT_H
#define CLOUDWOOD_ANDROID_NEW_LINK_LAYER_DIRECT_H

#ifndef _LINK_LAYER_DIRECT_H
#define _LINK_LAYER_DIRECT_H

#include "link_layer.h"

class LinkLayerDirect
    : public ILinkLayer
{
public:
    LinkLayerDirect();
    virtual ~LinkLayerDirect();

public:
    virtual int init(NetMod::Extension*);
    virtual int send(char* data, int len);
    virtual int onConnected();
    virtual int onData(inputbuf_t& input, size_t nrecv);
};


#endif

#endif //CLOUDWOOD_ANDROID_NEW_LINK_LAYER_DIRECT_H
