package com.smart.android.smartandroid.loginsdk;

/**
 * Created by liujia on 16/10/4.
 */

public final class LoginConstant {

    //login status
    public static final int LOGIN_STATUS_IDLE = 0;
    public static final int LOGIN_STATUS_LOGINED = 1;
    public static final int LOGIN_STATUS_LOGINING = 2;
    public static final int LOGIN_STATUS_KICKOFF = 3;
    public static final int LOGIN_STATUS_CONNECTING = 4;
    public static final int LOGIN_STATUS_CONNECTED = 5;
    public static final int LOGIN_STATUS_DISCONNECTD = 6;
    public static final int LOGIN_STATUS_LOGOFF = 7;
    public static final int LOGIN_STATUS_FAILED = 8;

    //task id
    public static final int PROTO_TASK_LVSTEST = 101;
    public static final int PROTO_TASK_RECONNECT = 102;
    public static final int PROTO_TASK_SENDDATA = 103;

    public static final int LOGIN_TASK_CEHCKCONNECT = 201;
    public static final int LOGIN_TASK_CEHCKLOGIN = 202;
    public static final int LOGIN_TASK_PING = 203;
    public static final int LOGIN_TASK_CHECKDATA = 204;
    public static final int LOGIN_TASK_RECVDATA = 205;
    public static final int LOGIN_TASK_CONNECTED = 206;
    public static final int LOGIN_TASK_DISCONNECTED = 207;

    //environment
    public static final int ENV_DEVELOPMENT = 1;
    public static final int ENV_RELEASE = 2;

    //LVS setting
    public static final String LVS_ADDRESS = "www.yunlaiwu.com";
    public static final String LVS_FALLBACK_IP = "123.56.88.196";
    public static final int LVS_PORT = 9100;
}
