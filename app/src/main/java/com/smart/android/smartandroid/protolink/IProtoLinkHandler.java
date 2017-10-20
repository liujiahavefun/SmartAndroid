package com.smart.android.smartandroid.protolink;

/**
 * Created by liujia on 16/9/3.
 * 链路长连接的事件回调接口，上层使用者传入此回调接口，ProtoLink在事件发生时，回调此接口，由上层处理事件
 */
public interface IProtoLinkHandler {
    void onConnected();
    void onError();
    void onData(int uri, byte[] data, int len);
    void onTimer(int timerId);
}