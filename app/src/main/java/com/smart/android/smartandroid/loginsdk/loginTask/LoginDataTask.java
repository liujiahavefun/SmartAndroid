package com.smart.android.smartandroid.loginsdk.loginTask;

import com.smart.android.smartandroid.loginsdk.LoginConstant;
import com.smart.android.smartandroid.loginsdk.LoginLink;
import com.smart.android.smartandroid.loginsdk.LoginMgr;
import com.smart.android.smartandroid.protolink.worker.ProtoTaskRunnable;

/**
 * Created by liujia on 17/1/25.
 */

public class LoginDataTask implements ProtoTaskRunnable {
    private LoginMgr mLoginMgr;
    private byte[] mDataBuf;
    private int mDataLen;

    public LoginDataTask(LoginMgr mgr, byte[] data, int len){
        this.mLoginMgr = mgr;
        this.mDataBuf = data;
        this.mDataLen = len;
    }

    @Override
    public int getTaskId() {
        return LoginConstant.LOGIN_TASK_RECVDATA;
    }

    @Override
    public String getTaskName() {
        return "LoginDataTask";
    }

    @Override
    public void run(){
        /*
        LoginLink loginLink = this.mLoginMgr.getLink();
        if(loginLink == null){
            return;
        }
        loginLink.send(mDataBuf, mDataLen);
        */
    }
}
