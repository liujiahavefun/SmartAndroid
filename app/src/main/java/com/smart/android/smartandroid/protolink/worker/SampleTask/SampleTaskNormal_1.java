package com.smart.android.smartandroid.protolink.worker.SampleTask;

import com.smart.android.smartandroid.protolink.worker.ProtoTaskRunnable;
import com.smart.android.smartandroid.util.LogUtil;

/**
 * Created by liujia on 2016/12/25.
 */

public class SampleTaskNormal_1 implements ProtoTaskRunnable{
    private long mRunFor;

    public SampleTaskNormal_1(int runFor){
        this.mRunFor = runFor;
    }

    @Override
    public int getTaskId() {
        return 1;
    }

    @Override
    public String getTaskName() {
        return "SampleTaskNormal_1";
    }

    @Override
    public void run(){
        try {
            LogUtil.e("ProtoWorker", "start to run SampleTaskNormal_1");
            Thread.currentThread().sleep(mRunFor);
            LogUtil.e("ProtoWorker", "finish run SampleTaskNormal_1");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
