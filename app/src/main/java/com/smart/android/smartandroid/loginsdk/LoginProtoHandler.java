package com.smart.android.smartandroid.loginsdk;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liujia on 17/1/10.
 * proto回调，用于外部获取并处理收到的proto消息
 * 注意，有的消息，例如对于登录相关的，内部一定会处理，同时也会开放给外部再次处理
 */

public class LoginProtoHandler implements ProtoHandler{
    private LoginMgr mLoginMgr;
    private ConcurrentHashMap<Integer, ProtoHandler> mHandlerMap = new ConcurrentHashMap<Integer, ProtoHandler>();

    public LoginProtoHandler(LoginMgr mgr){
        this.mLoginMgr = mgr;
    }

    public void addHandler(int uri, ProtoHandler handler) {
        if (handler == null) {
            return;
        }

        if (mHandlerMap.contains(uri)) {
            return;
        }

        mHandlerMap.put(uri, handler);
    }

    public void removeHandler(int uri) {
        mHandlerMap.remove(uri);
    }

    private void callHandler(int uri, byte[] data) {
        ProtoHandler protoHandler = mHandlerMap.get(uri);
        if(protoHandler != null) {
            protoHandler.onProto(uri, data);
        }
    }

    @Override
    public void onProto(int uri, byte[] data) {
        callHandler(uri, data);
    }
}
