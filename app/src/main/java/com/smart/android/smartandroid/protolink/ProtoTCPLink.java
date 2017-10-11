package com.smart.android.smartandroid.protolink;

/**
 * Created by root on 16/10/3.
 */

public abstract class ProtoTCPLink extends ProtoLink {
    public ProtoTCPLink(){
        super(ProtoConstant.LinkType.TCP_LINK);
    }
}
