package com.smart.android.smartandroid.loginsdk.loginTask;

import com.smart.android.smartandroid.loginsdk.LoginConstant;
import com.smart.android.smartandroid.loginsdk.LoginLinkListener;
import com.smart.android.smartandroid.protolink.worker.ProtoTimerTaskRunnable;

/**
 * Created by liujia on 17/1/10.
 * 这个东东是定期检查是不是好久没收到服务端数据包了，时间过长绝逼是断了，因为我们5秒ping一次滴
 */

public class LoginCheckDataTask implements ProtoTimerTaskRunnable {
    private LoginLinkListener mLoginListener;
    private int mInterval = 2*1000;

    public LoginCheckDataTask(LoginLinkListener listener){
        this.mLoginListener = listener;
    }

    @Override
    public int getTaskId() {
        return LoginConstant.LOGIN_TASK_CHECKDATA;
    }

    @Override
    public String getTaskName() {
        return "LoginDataTask";
    }

    @Override
    public boolean isRepeat() {
        return true;
    }

    @Override
    public int getInterval() {
        return mInterval;
    }

    @Override
    public void run(){
        mLoginListener.onDataCheck();
    }
}
