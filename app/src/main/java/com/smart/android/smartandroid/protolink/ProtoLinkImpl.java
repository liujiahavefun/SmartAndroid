package com.smart.android.smartandroid.protolink;

import android.text.TextUtils;

import com.smart.android.smartandroid.jni.ConnAttrWrapper;
import com.smart.android.smartandroid.jni.ConnEventWrapper;
import com.smart.android.smartandroid.jni.JniEventHandler;
import com.smart.android.smartandroid.jni.JniManager;
import com.smart.android.smartandroid.protolink.IEventHandler;
import com.smart.android.smartandroid.protolink.IProtoLinkHandler;
import com.smart.android.smartandroid.protolink.IProtoLink;

/**
 * Created by liujia on 16/9/3.
 */
public class ProtoLinkImpl implements IProtoLinkHandler, IEventHandler, IProtoLink {
    private static final int CONNECT_TIMEOUT_TIMER = 0xFFFF;

    private IProtoLinkHandler mProtoLinkHandler;
    private int mConnId = -1;
    private ProtoConstant.LinkType mLinkType = ProtoConstant.LinkType.TCP_LINK;
    private ProtoConstant.LinkStatus mLinkStatus = ProtoConstant.LinkStatus.LINK_INITED;
    private String mStrIPAddress = "";
    private String mStrIPPort = "";

    private long mDataTotalSent = 0;
    private long mDataTotalRecv = 0;

    public ProtoLinkImpl(ProtoConstant.LinkType linkType, IProtoLinkHandler handler){
        this.mLinkType = linkType;
        this.mProtoLinkHandler = handler;
    }

    public boolean connect(String ip, String port){
        if(TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)){
            return false;
        }

        //create conn
        ConnAttrWrapper attr = new ConnAttrWrapper();
        attr.ConnType = (mLinkType == ProtoConstant.LinkType.TCP_LINK ? ConnAttrWrapper.SOCKET_TCP : ConnAttrWrapper.SOCKET_UDP);
        attr.RemoteIP = ip;
        attr.RemotePort = port;
        attr.LocalIP = "";
        attr.LocalPort = "";
        mConnId = JniManager.GetInstance().ConnCreate(attr);
        if(mConnId == 0){
            return false;
        }

        //disable nagle algorithm
        //JniManager.GetInstance().ConnSetNoDelay(mConnId, true);

        //do connect
        if(JniManager.GetInstance().ConnConnect(mConnId, 0, (short)0) != 0 ){
            return false;
        }

        //register event handler and to connect!
        JniEventHandler.registerHandler(mConnId, this);
        JniManager.GetInstance().ConnAddTimer(mConnId, CONNECT_TIMEOUT_TIMER, 5);

        return true;
    }

    public void send(int uri, byte[] data, int len){
        if(mConnId <= 0){
            return;
        }
        mDataTotalSent += len;
        JniManager.GetInstance().ConnSend(uri, mConnId, data, len);
    }

    public void close(){
        mLinkStatus = ProtoConstant.LinkStatus.LINK_CLOSED;
        JniManager.GetInstance().ConnClose(mConnId);
        JniManager.GetInstance().ConnRemoveTimer(mConnId, 0); //remove all timers

        //unregister event handler
        JniEventHandler.unregisterHandler(mConnId);
    }

    public ProtoConstant.LinkStatus getStatus() {
        return mLinkStatus;
    }

    public void onConnected(){
        if(mProtoLinkHandler != null){
            mProtoLinkHandler.onConnected();
        }
    }

    public void onError(){
        if(mProtoLinkHandler != null){
            mProtoLinkHandler.onError();
        }
    }

    public void onData(long timestamp, byte[] data, int len){
        if(mProtoLinkHandler != null){
            mProtoLinkHandler.onData(timestamp, data, len);
        }
    }

    public void onTimer(int timerId){
        if(mProtoLinkHandler != null) {
            mProtoLinkHandler.onTimer(timerId);
        }
    }

    public void onEvent(ConnEventWrapper event, byte[] data, int len){
        switch (event.eventType){
            case ConnEventWrapper.EVENT_CONNECTED:
                mLinkStatus = ProtoConstant.LinkStatus.LINK_CONNECTED;
                onConnected();
                break;
            case ConnEventWrapper.EVENT_ERROR:
                mLinkStatus = ProtoConstant.LinkStatus.LINK_ERROR;
                close();
                onError();
                break;
            case ConnEventWrapper.EVENT_IN:
                mDataTotalRecv += len;
                onData(event.timestamp, data, len);
                break;
            case ConnEventWrapper.EVENT_TIMER:
                if(event.retVal == CONNECT_TIMEOUT_TIMER){
                    if(mLinkStatus != ProtoConstant.LinkStatus.LINK_CONNECTED){
                        onError();
                    }
                    JniManager.GetInstance().ConnRemoveTimer(mConnId, CONNECT_TIMEOUT_TIMER);
                }else{
                    onTimer((int)event.retVal);
                }
                break;
            default:
                break;
        }
    }
}
