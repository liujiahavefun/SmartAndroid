package com.smart.android.smartandroid.jni;

/**
 * Created by liujia on 16/8/26.
 */
public class ConnAttrWrapper {
    public static final int SOCKET_TCP = 1;
    public static final int SOCKET_UDP = 2;

    public int ConnType = SOCKET_TCP;

    //liujia: required in UDP case, optional in TCP case
    public String LocalIP = "";
    public String LocalPort = "";

    public String RemoteIP = "";
    public String RemotePort = "";
}