package com.smart.android.smartandroid.jni;

/**
 * Created by liujia on 16/8/26.
 */
public class ConnEventWrapper {
    //event type
    public static final int EVENT_CONNECTED = 0;
    public static final int EVENT_IN = 1;
    public static final int EVENT_ERROR = 2;
    public static final int EVENT_ALIVE = 3;
    public static final int EVENT_SENT = 4;
    public static final int EVENT_NETWORK_LOST = 5;
    public static final int EVENT_NETWORK_RESUME = 6;
    public static final int EVENT_CONN_STATE = 7;
    public static final int EVENT_IN_STREAM = 8;
    public static final int EVENT_TIMER = 9;

    //conn state
    public static final int CONNSTATE_TRANSPTLAYER_CONNECTING = 0;
    public static final int CONNSTATE_TRANSPTLAYER_CONNECTED = 1;
    public static final int CONNSTATE_PROXYLAYER_CONNECTING = 2;
    public static final int CONNSTATE_PROXYLAYER_CONNECTED = 3;
    public static final int CONNSTATE_ENCLAYER_START = 4;
    public static final int CONNSTATE_ENCLAYER_FINISH = 5;
    public static final int CONNSTATE_CONNECTED = 10;

    public int connId = 0;
    public int eventType = -1;
    public int connState = -1;
    public long retVal = -1;
    public long timestamp = -1;
}