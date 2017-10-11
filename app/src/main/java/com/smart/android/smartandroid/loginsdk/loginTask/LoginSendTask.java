package com.smart.android.smartandroid.loginsdk.loginTask;

import com.smart.android.smartandroid.protolink.worker.ProtoTaskRunnable;
import com.smart.android.smartandroid.loginsdk.LoginLink;
import com.smart.android.smartandroid.loginsdk.LoginMgr;

/**
 * Created by root on 16/10/4.
 */

public class LoginSendTask implements ProtoTaskRunnable {
    private LoginMgr mLoginMgr;
    private byte[] mDataBuf;
    private int mDataLen;

    public LoginSendTask(LoginMgr mgr, byte[] data, int len){
        this.mLoginMgr = mgr;
        this.mDataBuf = data;
        this.mDataLen = len;
    }

    @Override
    public int getTaskId() {
        return 4;
    }

    @Override
    public String getTaskName() {
        return "LoginSendTask";
    }

    @Override
    public void run(){
        LoginLink loginLink = this.mLoginMgr.getLink();
        if(loginLink == null){
            return;
        }
        loginLink.send(mDataBuf, mDataLen);
    }
}
