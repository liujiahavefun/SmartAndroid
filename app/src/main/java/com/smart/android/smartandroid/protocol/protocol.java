package com.smart.android.smartandroid.protocol;

/**
 * Created by liujia on 17/1/19.
 */

/*
 * liujia: 这个很重要哦
 * 所有的消息，都是32位整数，高位16bit为消息的子服务号，低位16bit为消息的协议号，这个会写在消息包里
 * 消息的子服务号，上行消息用于派发给对应的子服务系统，下行消息用于客户端分发给对应的消息处理函数
 * 所有后缀有Res，代表Response，即消息回复或者ACK，随便你怎么理解了，理解就好了。尽量消息和其res，都放一起，分别是奇数和大一个的偶数
 */

public class protocol {
    /*
    * 设备类型
    */
    public static final String DEVICE_WEB = "web";
    public static final String DEVICE_ANDROID = "android";
    public static final String DEVICE_IOS = "ios";
    public static final String DEVICE_WINDOWS = "win";
    public static final String DEVICE_MACOS = "mac";

    /*
    * 错误码，和服务端保持一致
    */
    public static final int RES_OK = 0;
    public static final int RES_FAIL = 1;
    public static final int RES_NO_USER = 101;
    public static final int RES_INVALID_PASSWORD = 102;
    public static final int RES_INVALID_TOKEN = 103;

    /*
    * 子服务的ID
    */
    public static final int SVID_ALL = 0;
    public static final int SVID_LOGIN = 16;
    public static final int SVID_PUSH = 2;

    /*
    * Login子服务的protocol id
    */
    public static final int PLoginByPassport_uri = (SVID_LOGIN << 16 | 1);
    public static final int PLoginByPassportRes_uri = (SVID_LOGIN << 16 | 2);
    public static final int PLoginByUid_uri = (SVID_LOGIN << 16 | 3);
    public static final int PLoginByUidRes_uri = (SVID_LOGIN << 16 | 4);
    public static final int PLoginLogout_uri = (SVID_LOGIN << 16 | 5);
    public static final int PLoginLogoutRes_uri = (SVID_LOGIN << 16 | 6);
    public static final int PLoginPing_uri = (SVID_LOGIN << 16 | 7);
    public static final int PLoginPingRes_uri = (SVID_LOGIN << 16 | 8);
    public static final int PLoginKickOff_uri = (SVID_LOGIN << 16 | 9);

}
