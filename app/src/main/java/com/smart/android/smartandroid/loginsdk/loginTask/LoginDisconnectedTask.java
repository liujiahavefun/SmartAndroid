package com.smart.android.smartandroid.loginsdk.loginTask;

import com.smart.android.smartandroid.loginsdk.LoginConstant;
import com.smart.android.smartandroid.loginsdk.LoginMgr;
import com.smart.android.smartandroid.protolink.worker.ProtoTaskRunnable;

/**
 * Created by liujia on 17/1/25.
 */

public class LoginDisconnectedTask implements ProtoTaskRunnable {
    private LoginMgr mLoginMgr;

    public LoginDisconnectedTask(LoginMgr mgr){
        this.mLoginMgr = mgr;
    }

    @Override
    public int getTaskId() {
        return LoginConstant.LOGIN_TASK_DISCONNECTED;
    }

    @Override
    public String getTaskName() {
        return "LoginDisconnectedTask";
    }

    @Override
    public void run(){
        mLoginMgr.onDisconnected();
    }
}
