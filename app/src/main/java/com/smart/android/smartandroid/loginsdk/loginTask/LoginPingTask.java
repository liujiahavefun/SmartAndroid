package com.smart.android.smartandroid.loginsdk.loginTask;

import com.smart.android.smartandroid.loginsdk.LoginConstant;
import com.smart.android.smartandroid.loginsdk.LoginMgr;
import com.smart.android.smartandroid.protolink.worker.ProtoTimerTaskRunnable;

/**
 * Created by root on 16/10/3.
 */

public class LoginPingTask implements ProtoTimerTaskRunnable {
    private LoginMgr mLoginMgr;
    private int mInterval = 2*1000; //liujia: 也是拍的脑门，如果不合适，下次拍屁股试试

    public LoginPingTask(LoginMgr mgr){
        this.mLoginMgr = mgr;
    }

    @Override
    public int getTaskId() {
        return LoginConstant.LOGIN_TASK_PING;
    }

    @Override
    public String getTaskName() {
        return "LoginPingTask";
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
        this.mLoginMgr.sendPing();
    }
}
