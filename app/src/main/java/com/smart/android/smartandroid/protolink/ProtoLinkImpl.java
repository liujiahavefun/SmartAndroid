package com.smart.android.smartandroid.protolink;

import android.text.TextUtils;

import com.smart.android.smartandroid.jni.ConnAttrWrapper;
import com.smart.android.smartandroid.jni.ConnEventWrapper;
import com.smart.android.smartandroid.jni.EventHandlerMgr;
import com.smart.android.smartandroid.jni.JniManager;

/**
 * Created by liujia on 16/9/3.
 * 长连接真正的干活的黄牛类，如其implement的三个接口，干三个活
 * 实现IProtoLink接口，可以控制连接connect、close、send
 * 实现IEventHandler接口，接收JNI发过来的网络事件
 * 实现IProtoLinkHandler，其实也可以不实现，不过为了兼容上层，实现也无妨，就是把底层的IEventHandler的事件转成IProtoLinkHandler，向上传递
 */
public class ProtoLinkImpl implements IProtoLink, IProtoLinkHandler, IEventHandler {
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

        mLinkStatus = ProtoConstant.LinkStatus.LINK_CONNECTING;

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

        //register callback
        EventHandlerMgr.registerHandler(mConnId, this);

        //disable nagle algorithm
        JniManager.GetInstance().ConnSetNoDelay(mConnId, true);

        //do connect
        if(JniManager.GetInstance().ConnConnect(mConnId, 0, (short)0) != 0 ){
            return false;
        }

        //add timer
        //JniManager.GetInstance().ConnAddTimer(mConnId, CONNECT_TIMEOUT_TIMER, 5);

        return true;
    }

    public void send(int uri, byte[] data, int len){
        if(mConnId <= 0){
            return;
        }

        if(JniManager.GetInstance().ConnSend(mConnId, uri, data, len) == 0) {
            mDataTotalSent += len;
        }else {
            ProtoLogger.LogWarning("send data failed, conn %d", mConnId);
        }
    }

    public void close(){
        mLinkStatus = ProtoConstant.LinkStatus.LINK_CLOSED;
        JniManager.GetInstance().ConnClose(mConnId);
        JniManager.GetInstance().ConnRemoveTimer(mConnId, 0); //remove all timers

        //unregister event handler
        EventHandlerMgr.unregisterHandler(mConnId);
    }

    public ProtoConstant.LinkStatus getStatus() {
        return mLinkStatus;
    }

    public void onConnected(){
        mLinkStatus = ProtoConstant.LinkStatus.LINK_CONNECTED;
        if(mProtoLinkHandler != null){
            mProtoLinkHandler.onConnected();
        }
    }

    public void onError(){
        mLinkStatus = ProtoConstant.LinkStatus.LINK_ERROR;
        if(mProtoLinkHandler != null){
            mProtoLinkHandler.onError();
        }
    }

    public void onData(int uri, byte[] data, int len){
        if(mProtoLinkHandler != null){
            mProtoLinkHandler.onData(uri, data, len);
        }
    }

    public void onTimer(int timerId){
        if(mProtoLinkHandler != null) {
            mProtoLinkHandler.onTimer(timerId);
        }
    }

    /*
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
    */
    public void onEvent(int connId, int event, long val) {
        if(mConnId != connId) {
            ProtoLogger.LogWarning("received net event of conn %d, but not my conn %d", connId, mConnId);
            return;
        }

        ProtoLogger.LogInfo(ProtoLogger.Event2String(connId, event, val));

        switch (event) {
            case ConnEventWrapper.EVENT_CONNECTING:
                break;
            case ConnEventWrapper.EVENT_CONNECTED:
                onConnected();
                break;
            case ConnEventWrapper.EVENT_ERROR:
                onError();
                close();
                break;
            case ConnEventWrapper.EVENT_TIMER:
                onTimer((int)val);
                break;
        }
    }

    public void onData(int connId, int uri, byte[] data, int len) {
        mDataTotalRecv += len;
        onData(uri, data, len);
    }
}
