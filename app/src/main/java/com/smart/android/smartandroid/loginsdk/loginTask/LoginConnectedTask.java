package com.smart.android.smartandroid.loginsdk.loginTask;

import com.smart.android.smartandroid.loginsdk.LoginConstant;
import com.smart.android.smartandroid.protolink.worker.ProtoTaskRunnable;
import com.smart.android.smartandroid.loginsdk.LoginMgr;

/**
 * Created by liujia on 16/10/4.
 */

public class LoginConnectedTask implements ProtoTaskRunnable {
    private LoginMgr mLoginMgr;

    public LoginConnectedTask(LoginMgr mgr){
        this.mLoginMgr = mgr;
    }

    @Override
    public int getTaskId() {
        return LoginConstant.LOGIN_TASK_CONNECTED;
    }

    @Override
    public String getTaskName() {
        return "LoginConnectedTask";
    }

    @Override
    public void run(){
        mLoginMgr.onConnected();
    }
}
