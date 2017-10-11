package com.smart.android.smartandroid.protolink;

/**
 * Created by liujia on 16/9/3.
 */
public class ProtoConstant {
    public enum LinkStatus {
        LINK_INITED,
        LINK_CONNECTING,
        LINK_PENDING,
        LINK_CONNECTED,
        LINK_DISCONNECTED,
        LINK_RECONNECTING,
        LINK_CLOSED,
        LINK_ERROR
    }

    public enum LinkType
    {
        TCP_LINK,
        UDP_LINK
    }

    static public int INVALID_PROTO_TASK_ID = -1;
    static public String DEFAULT_PROTO_TASK_NAME = "DEFAULT_PROTO_TASK_NAME";
}
