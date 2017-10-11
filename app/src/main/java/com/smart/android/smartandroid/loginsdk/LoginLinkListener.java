package com.smart.android.smartandroid.loginsdk;

import com.smart.android.smartandroid.loginsdk.loginTask.LoginCheckConnectTask;
import com.smart.android.smartandroid.loginsdk.loginTask.LoginCheckLoginTask;
import com.smart.android.smartandroid.loginsdk.loginTask.LoginCheckDataTask;
import com.smart.android.smartandroid.loginsdk.loginTask.LoginConnectedTask;
import com.smart.android.smartandroid.loginsdk.loginTask.LoginDataTask;
import com.smart.android.smartandroid.loginsdk.loginTask.LoginDisconnectedTask;
import com.smart.android.smartandroid.loginsdk.loginTask.LoginPingTask;
import com.smart.android.smartandroid.protolink.ProtoInfo;
import com.smart.android.smartandroid.protolink.ProtoLogger;
import com.smart.android.smartandroid.protolink.ProtoTCPLink;

/**
 * Created by liujia on 16/10/3.
 */

public class LoginLinkListener extends ProtoTCPLink {
    private LoginMgr mLoginMgr;

    long mLastDataRecvTime = 0;

    public LoginLinkListener(LoginMgr mgr){
        this.mLoginMgr = mgr;
    }

    /*
    *  override IProtoLink interface functions of ProtoLink(ProtoTCPLink/ProtoUDPLink)
    */
    public void onConnected(){
        ProtoLogger.Log("LoginLinkListener.onConnected");

        startPing();
        mLoginMgr.getWorkder().post(new LoginConnectedTask(mLoginMgr));
    }

    public void onError(){
        ProtoLogger.Log("LoginLinkListener.onError");

        stopPing();
        mLoginMgr.getWorkder().post(new LoginDisconnectedTask(mLoginMgr));
    }

    public void onData(long timestamp, byte[] data, int len){
        mLastDataRecvTime = ProtoInfo.GetInstance().getProtoTime();
        mLoginMgr.getWorkder().post(new LoginDataTask(mLoginMgr, data, len));
    }

    public void onTimer(int timerId){
        //do nothing....
    }

    public void onDataCheck() {
        long diff = ProtoInfo.GetInstance().getProtoTime() - mLastDataRecvTime;
        if (mLastDataRecvTime != 0 && diff > 15*1000) {
            ProtoLogger.Log("LoginLinkListener.onDataCheck, not recv data from server more than %l milisecs, reconnect", diff);
            mLoginMgr.onDisconnected();
        }
    }

    /*
    * 心跳
    */
    public void startPing() {
        ProtoLogger.Log("LoginLinkListener.startPing");
        mLoginMgr.getWorkder().postDelay(new LoginPingTask(mLoginMgr));
        mLoginMgr.getWorkder().postDelay(new LoginCheckDataTask(this));
    }

    public void stopPing() {
        ProtoLogger.Log("LoginLinkListener.stopPing");
        mLoginMgr.getWorkder().remove(LoginConstant.LOGIN_TASK_PING);
        mLoginMgr.getWorkder().remove(LoginConstant.LOGIN_TASK_CHECKDATA);
    }

    /*
    * 开始连接并登录后，设置两个延时任务(单次并延时执行)，检查连接和登录是否ok，并决定是否要重连如果没连上或者没登录上的情况下
    */
    public void startCheckConnect() {
        mLoginMgr.getWorkder().postDelay(new LoginCheckConnectTask(mLoginMgr));
    }

    public void stopCheckConnect() {
        mLoginMgr.getWorkder().remove(LoginConstant.LOGIN_TASK_CEHCKCONNECT);
    }

    public void startCheckLogin() {
        mLoginMgr.getWorkder().postDelay(new LoginCheckLoginTask(mLoginMgr));
    }

    public void stopCheckLogin() {
        mLoginMgr.getWorkder().remove(LoginConstant.LOGIN_TASK_CEHCKLOGIN);
    }
}
