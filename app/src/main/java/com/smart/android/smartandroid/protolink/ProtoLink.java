package com.smart.android.smartandroid.protolink;

/**
 * Created by liujia on 16/9/4.
 */
public abstract class ProtoLink implements IProtoLink, IProtoLinkHandler {
    private ProtoLinkImpl mProtoLinkImpl;

    public ProtoLink(ProtoConstant.LinkType linkType){
        mProtoLinkImpl = new ProtoLinkImpl(linkType, this);
    }

    /*
     * implements IProtoLink
     */
    public boolean connect(String ip, String port){
        return mProtoLinkImpl.connect(ip, port);
    }

    public void	send(int uri, byte[] data, int len){
        mProtoLinkImpl.send(uri, data, len);
    }

    public void close(){
        mProtoLinkImpl.close();
    }

    public boolean isConnected(){
        return mProtoLinkImpl.getStatus() == ProtoConstant.LinkStatus.LINK_CONNECTED;
    }
    public boolean isConnecting(){
        return mProtoLinkImpl.getStatus() == ProtoConstant.LinkStatus.LINK_CONNECTING;
    }

    public void onConnected(){
        throw new RuntimeException("Stub!");
    }

    public void onError(){
        throw new RuntimeException("Stub!");
    }

    public void onData(long timestamp, byte[] data, int len){
        throw new RuntimeException("Stub!");
    }

    public void onTimer(int timerId){
        throw new RuntimeException("Stub!");
    }
}
