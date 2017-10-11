package com.smart.android.smartandroid.protolink;

/**
 * Created by root on 16/10/3.
 */

public abstract class ProtoUDPLink extends ProtoLink {
    public ProtoUDPLink(){
        super(ProtoConstant.LinkType.UDP_LINK);
    }
}
