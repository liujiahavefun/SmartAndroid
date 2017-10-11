package com.smart.android.smartandroid.protolink.worker.SampleTask;

import com.smart.android.smartandroid.protolink.worker.ProtoTaskRunnable;
import com.smart.android.smartandroid.util.LogUtil;

/**
 * Created by liujia on 2016/12/25.
 */

public class SampleTaskNormal_3 implements ProtoTaskRunnable {
    private long mRunFor;

    public SampleTaskNormal_3(int runFor){
        this.mRunFor = runFor;
    }

    @Override
    public int getTaskId() {
        return 3;
    }

    @Override
    public String getTaskName() {
        return "SampleTaskNormal_3";
    }

    @Override
    public void run(){
        try {
            LogUtil.e("ProtoWorker", "start to run SampleTaskNormal_3");
            Thread.currentThread().sleep(mRunFor);
            LogUtil.e("ProtoWorker", "finish run SampleTaskNormal_3");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
