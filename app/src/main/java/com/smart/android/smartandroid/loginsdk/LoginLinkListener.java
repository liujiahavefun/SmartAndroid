package com.smart.android.smartandroid.loginsdk;

import com.smart.android.smartandroid.loginsdk.loginTask.LoginCheckConnectTask;
import com.smart.android.smartandroid.loginsdk.loginTask.LoginCheckLoginTask;
import com.smart.android.smartandroid.loginsdk.loginTask.LoginConnectedTask;
import com.smart.android.smartandroid.loginsdk.loginTask.LoginOnDataTask;
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
        mLoginMgr.getWorker().post(new LoginConnectedTask(mLoginMgr));
    }

    public void onError(){
        ProtoLogger.Log("LoginLinkListener.onError");

        stopPing();
        mLoginMgr.getWorker().post(new LoginDisconnectedTask(mLoginMgr));
    }

    public void onData(int uri, byte[] data, int len){
        ProtoLogger.Log("LoginLinkListener.onData, uri %d", uri);
        mLastDataRecvTime = ProtoInfo.GetInstance().getProtoTime();
        mLoginMgr.getWorker().post(new LoginOnDataTask(mLoginMgr.getProtoHandler(), uri, data, len));
    }

    public void onTimer(int timerId){
        //do nothing....
        ProtoLogger.Log("LoginLinkListener.onTimer");
        onDataCheck();
    }

    public void onDataCheck() {
        long diff = ProtoInfo.GetInstance().getProtoTime() - mLastDataRecvTime;
        if (mLastDataRecvTime != 0 && diff > 15*1000) {
            ProtoLogger.Log("LoginLinkListener.onDataCheck, not recv data from server more than %d millisecs, reconnect", diff);
            mLoginMgr.onDisconnected();
        }
    }

    public void startPing() {
        mLoginMgr.getWorker().postDelay(new LoginPingTask(mLoginMgr));
        //this.getWorker().postDelay(new LoginCheckDataTask(mLoginListener));
    }

    public void stopPing() {
        mLoginMgr.getWorker().remove(LoginConstant.LOGIN_TASK_PING);
        //this.getWorker().remove(LoginConstant.LOGIN_TASK_CHECKDATA);
    }

    /*
    * 开始连接并登录后，设置两个延时任务(单次并延时执行)，检查连接和登录是否ok，并决定是否要重连如果没连上或者没登录上的情况下
    */
    public void startCheckConnect() {
        mLoginMgr.getWorker().postDelay(new LoginCheckConnectTask(mLoginMgr));
    }

    public void stopCheckConnect() {
        mLoginMgr.getWorker().remove(LoginConstant.LOGIN_TASK_CEHCKCONNECT);
    }

    public void startCheckLogin() {
        mLoginMgr.getWorker().postDelay(new LoginCheckLoginTask(mLoginMgr));
    }

    public void stopCheckLogin() {
        mLoginMgr.getWorker().remove(LoginConstant.LOGIN_TASK_CEHCKLOGIN);
    }
}
