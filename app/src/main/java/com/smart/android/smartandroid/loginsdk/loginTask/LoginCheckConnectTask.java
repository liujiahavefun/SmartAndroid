package com.smart.android.smartandroid.loginsdk.loginTask;

import com.smart.android.smartandroid.loginsdk.LoginConstant;
import com.smart.android.smartandroid.loginsdk.LoginLink;
import com.smart.android.smartandroid.loginsdk.LoginMgr;
import com.smart.android.smartandroid.protolink.ProtoLogger;
import com.smart.android.smartandroid.protolink.worker.ProtoTimerTaskRunnable;

/**
 * Created by root on 16/10/4.
 */

public class LoginCheckConnectTask implements ProtoTimerTaskRunnable {
    private LoginMgr mLoginMgr;
    private int mInterval = 5*1000;

    public LoginCheckConnectTask(LoginMgr mgr){
        this.mLoginMgr = mgr;
    }

    @Override
    public int getTaskId() {
        return LoginConstant.LOGIN_TASK_CEHCKCONNECT;
    }

    @Override
    public String getTaskName() {
        return "LoginCheckConnectTask";
    }

    @Override
    public boolean isRepeat() {
        return false;
    }

    @Override
    public int getInterval() {
        return mInterval;
    }

    @Override
    public void run(){
        ProtoLogger.Log("LoginCheckConnectTask.run");

        LoginLink loginLink = this.mLoginMgr.getLink();
        if(loginLink == null){
            ProtoLogger.Log("LoginCheckConnectTask.run, link == null");
            return;
        }

        if (!loginLink.isConnected()) {
            ProtoLogger.Log("LoginCheckConnectTask.run, not connected");
            if (!this.mLoginMgr.hasNetwork()) {
                ProtoLogger.Log("LoginCheckConnectTask.run, no network, do nothing");
                return;
            }
            loginLink.reconnect();
            return;
        }

        ProtoLogger.Log("LoginCheckConnectTask.run, connected, do nothing");
    }
}
