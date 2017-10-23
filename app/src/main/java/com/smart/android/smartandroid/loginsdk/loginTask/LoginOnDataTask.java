package com.smart.android.smartandroid.loginsdk.loginTask;

import com.smart.android.smartandroid.loginsdk.LoginConstant;
import com.smart.android.smartandroid.loginsdk.ProtoHandler;
import com.smart.android.smartandroid.protolink.worker.ProtoTaskRunnable;

/**
 * Created by liujia on 17/1/25.
 * 收到数据后用来处理的Task，处理包括根据uri解包，并调用对应的处理逻辑
 */

public class LoginOnDataTask implements ProtoTaskRunnable {
    private ProtoHandler mProtoHandler;
    private int mUri;
    private byte[] mDataBuf;
    private int mDataLen;

    public LoginOnDataTask(ProtoHandler protoHandler, int uri, byte[] data, int len){
        this.mProtoHandler = protoHandler;
        this.mUri = uri;
        this.mDataBuf = data;
        this.mDataLen = len;
    }

    @Override
    public int getTaskId() {
        return LoginConstant.LOGIN_TASK_RECVDATA;
    }

    @Override
    public String getTaskName() {
        return "LoginOnDataTask";
    }

    @Override
    public void run(){
        if(mProtoHandler != null) {
            mProtoHandler.onProto(mUri, mDataBuf);
        }
    }
}
