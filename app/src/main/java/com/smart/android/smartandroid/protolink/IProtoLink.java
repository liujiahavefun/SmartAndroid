package com.smart.android.smartandroid.protolink;

/**
 * Created by liujia on 16/9/3.
 * 链路长连接接口， 用于连接，发送数据，关闭连接
 */
public interface IProtoLink {
    boolean connect(String ip, String port);
    void	send(byte[] data, int len);
    void    close();
}
