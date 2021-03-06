package com.smart.android.smartandroid.loginsdk.task;

import com.smart.android.smartandroid.loginsdk.LoginConstant;
import com.smart.android.smartandroid.loginsdk.LoginMgr;
import com.smart.android.smartandroid.protolink.worker.ProtoTaskRunnable;

/**
 * Created by liujia on 17/1/20.
 */

public class SendTask implements ProtoTaskRunnable {
    private LoginMgr mLoginMgr;
    private int mUri;
    private byte[] mData;

    public SendTask(LoginMgr mgr, int uri, byte[] data){
        this.mLoginMgr = mgr;
        this.mUri = uri;
        this.mData = data;
    }

    @Override
    public int getTaskId() {
        return LoginConstant.PROTO_TASK_SENDDATA;
    }

    @Override
    public String getTaskName() {
        return "SendTask";
    }

    @Override
    public void run(){
        if(mLoginMgr == null || mData == null) {
            return;
        }

        mLoginMgr.getLink().send(mUri, mData, mData.length);
    }
}
