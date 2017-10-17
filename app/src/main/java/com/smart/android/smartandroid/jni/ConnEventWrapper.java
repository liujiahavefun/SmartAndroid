package com.smart.android.smartandroid.jni;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liujia on 16/8/26.
 */
public class ConnEventWrapper  {
    //event type
    public static final int EVENT_UNKNOWN = 0;
    public static final int EVENT_CONNECTING = 1;       //连接中
    public static final int EVENT_CONNECTED = 2;        //已连接
    public static final int EVENT_CLOSED = 3;           //关闭
    public static final int EVENT_ERROR = 4;            //错误
    public static final int EVENT_IN = 5;               //收到数据包
    public static final int EVENT_INSTREAM = 6;         //收到数据流
    public static final int EVENT_SENT = 7;             //已发送
    public static final int EVENT_CONNSTATE = 8;        //连接状态，具体见下面的CNetEventConnState定义
    public static final int EVENT_TIMER = 9;            //定时器
    public static final int EVENT_ALIVE = 10;           //收到ping回复

    //conn state
    public static final int CONNSTATE_UNKNOWN = -1;
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

    static Map<Integer, String> EVENT_MAP = new HashMap<Integer, String>() {
        {
            put(0, "[0]未设置");
            put(1, "[1]连接中");
            put(2, "[2]已连接");
            put(3, "[3]已关闭");
            put(4, "[4]错误");
            put(5, "[5]收到数据包");
            put(6, "[6]收到数据流");
            put(7, "[7]已发送");
            put(8, "[8]连接状态");
            put(9, "[9]定时器");
        }
    };

    static SimpleDateFormat TIME_FORMATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    public String toString() {
        String type = "[-1]未知";
        if(EVENT_MAP.containsKey(this.eventType)) {
            type = EVENT_MAP.get(this.eventType);
        }

        String connId = String.format("connId:%d", this.connId);
        String time =TIME_FORMATE.format(new Date(this.timestamp));
        String retVal = String.format("retVal:%d", this.retVal);
        String connState = String.format("connState:%d", this.retVal);

        return time + ":" + type + "," + connId + "," + retVal + "," + connState;
    }
}