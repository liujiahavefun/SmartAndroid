package com.smart.android.smartandroid.protolink;

import com.smart.android.smartandroid.jni.ConnEventWrapper;

/**
 * Created by liujia on 16/9/3.
 * 底层链接上的事件回调接口，底层网络事件发生时，JNI"转几次手后"回调此接口
 */

public interface IEventHandler {
    void onEvent(int connId, int event, long val);
    void onData(int connId, int uri, byte[] data, int len);
}
