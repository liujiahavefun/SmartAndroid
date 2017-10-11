package com.smart.android.smartandroid.protolink;

import com.smart.android.smartandroid.jni.ConnEventWrapper;

/**
 * Created by liujia on 16/9/3.
 */
public interface IEventHandler {
    void onEvent(ConnEventWrapper event, byte[] data, int len);
}
