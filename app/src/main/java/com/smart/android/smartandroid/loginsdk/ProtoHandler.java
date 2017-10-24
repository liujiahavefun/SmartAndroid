package com.smart.android.smartandroid.loginsdk;

/**
 * Created by liujia on 17/1/11.
 * 处理消息的回调接口，所有消息都是PB的格式，uri是符号号，与PB消息内部的uri一样，方便上层处理(不用解包)
 */

public interface ProtoHandler {
    void onProto(int uri, byte[] data);
}
