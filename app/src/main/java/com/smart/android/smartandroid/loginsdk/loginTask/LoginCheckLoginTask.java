package com.smart.android.smartandroid.loginsdk.loginTask;

import com.smart.android.smartandroid.loginsdk.LoginConstant;
import com.smart.android.smartandroid.loginsdk.LoginLink;
import com.smart.android.smartandroid.loginsdk.LoginMgr;
import com.smart.android.smartandroid.protolink.ProtoLogger;
import com.smart.android.smartandroid.protolink.worker.ProtoTimerTaskRunnable;

/**
 * Created by liujia on 17/1/25.
 * 检查登录状态，通常是开始登录后post此task，5秒后执行，检查是否登录上了，没登上就重连
 */

public class LoginCheckLoginTask implements ProtoTimerTaskRunnable {
    private LoginMgr mLoginMgr;

    //5秒后检查一次
    int mDelay = 5*1000;

    public LoginCheckLoginTask(LoginMgr mgr){
        this.mLoginMgr = mgr;
    }

    @Override
    public int getTaskId() {
        return LoginConstant.LOGIN_TASK_CEHCKLOGIN;
    }

    @Override
    public String getTaskName() {
        return "LoginCheckLoginTask";
    }

    @Override
    public boolean isRepeat() {
        return false;
    }

    @Override
    public int getInterval() {
        return mDelay;
    }

    @Override
    public void run(){
        ProtoLogger.Log("LoginCheckLoginTask.run");

        LoginLink loginLink = mLoginMgr.getLink();
        if (loginLink == null) {
            ProtoLogger.Log("LoginCheckLoginTask.run, link == null");
            return;
        }

        if(mLoginMgr.getLoginStatus() != LoginConstant.LOGIN_STATUS_LOGINED) {
            ProtoLogger.Log("LoginCheckLoginTask.run, not logined.");

            //没网就不尝试了。。。
            if (mLoginMgr.hasNetwork() == false) {
                ProtoLogger.Log("LoginCheckConnectTask.run, no network, do nothing");
                return;
            }

            loginLink.reconnect();
            return;
        }

        ProtoLogger.Log("LoginCheckLoginTask.run, logined, do nothing");
    }
}
