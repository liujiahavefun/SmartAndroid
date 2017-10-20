package com.smart.android.smartandroid.protolink;

/**
 * Created by root on 16/10/3.
 * 抽象TCP连接类，上层extend此类，并且可以要么重新实现要么用默认实现的IProtoLink, 但要重新实现IProtoLinkHandler
 */

public abstract class ProtoTCPLink extends ProtoLink {
    public ProtoTCPLink(){
        super(ProtoConstant.LinkType.TCP_LINK);
    }
}
