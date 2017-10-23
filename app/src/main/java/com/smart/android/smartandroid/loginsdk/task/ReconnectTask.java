package com.smart.android.smartandroid.loginsdk.task;

import com.smart.android.smartandroid.loginsdk.LoginConstant;
import com.smart.android.smartandroid.loginsdk.LoginMgr;
import com.smart.android.smartandroid.protolink.worker.ProtoTaskRunnable;
import com.smart.android.smartandroid.protolink.worker.ProtoTimerTaskRunnable;

/**
 * Created by liujia on 17/1/19.
 */

public class ReconnectTask implements ProtoTimerTaskRunnable {
    private LoginMgr mLoginMgr;
    int mDelay = 100;
    boolean mIgnoreNetwork = true; //是否要根据当前是否有网络连接，然后重试或者不重试？？？

    public ReconnectTask(LoginMgr mgr, int delay, boolean ignoreNetwork){
        this.mLoginMgr = mgr;
        this.mDelay = delay;
        this.mIgnoreNetwork = ignoreNetwork;
    }

    @Override
    public int getTaskId() {
        return LoginConstant.PROTO_TASK_RECONNECT;
    }

    @Override
    public String getTaskName() {
        return "ReconnectTask";
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
    public int getFirstDelay() {
        return 0;
    }

    @Override
    public void run(){
        if(mLoginMgr != null) {
            mLoginMgr.realReconnect();
        }
    }
}
