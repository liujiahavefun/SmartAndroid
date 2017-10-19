package com.smart.android.smartandroid.loginsdk;

import com.smart.android.smartandroid.protolink.ProtoLogger;

/**
 * Created by root on 16/10/3.
 */

public class LoginLink {
    private LoginMgr mLoginMgr;
    private LoginLinkListener mLinkImpl;

    private String mTcpIP;
    private String mTcpPort;

    public LoginLink(LoginMgr mgr){
        this.mLoginMgr = mgr;
        this.mLinkImpl = new LoginLinkListener(mgr);
    }

    public LoginMgr getLoginMgr() {
        return mLoginMgr;
    }

    public void connect(String ip, String port){
        ProtoLogger.Log("LoginLink.connect, ip/port=%s:%s", ip, port);

        mTcpIP = ip;
        mTcpPort = port;
        mLinkImpl.connect(mTcpIP, mTcpPort);
    }
    public void close(){
        mLinkImpl.close();
    }

    public void send(int uri, byte[] data, int len){
        if(data == null || len == 0){
            return;
        }
        if(mLinkImpl.isConnected()){
            mLinkImpl.send(uri, data, len);
        }
    }

    public void reconnect(){
        ProtoLogger.Log("LoginLink.reconnect, ip/port=%s:%s", mTcpIP, mTcpPort);

        mLoginMgr.setLoginStatus(LoginConstant.LOGIN_STATUS_DISCONNECTD);
        close();
        connect(mTcpIP, mTcpPort);
    }

    public boolean isConnected(){
        return mLinkImpl.isConnected();
    }

    public boolean isConnecting(){
        return mLinkImpl.isConnecting();
    }

    public void startCheckLogin(){
        mLinkImpl.startCheckLogin();
    }
    public void stopCheckLogin(){
        mLinkImpl.stopCheckLogin();
    }

    public void startPing(){
        mLinkImpl.startPing();
    }

    public void stopPing(){
        mLinkImpl.stopPing();
    }

    public void startCheckConnect(){
        mLinkImpl.startCheckConnect();
    }

    public void stopCheckConnect(){
        mLinkImpl.stopCheckConnect();
    }
}
