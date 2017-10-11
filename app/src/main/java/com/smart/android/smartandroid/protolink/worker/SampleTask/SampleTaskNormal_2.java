package com.smart.android.smartandroid.protolink.worker.SampleTask;

import com.smart.android.smartandroid.protolink.worker.ProtoTaskRunnable;
import com.smart.android.smartandroid.util.LogUtil;

/**
 * Created by liujia on 2016/12/25.
 */

public class SampleTaskNormal_2 implements ProtoTaskRunnable{
    private long mRunFor;

    public SampleTaskNormal_2(int runFor){
        this.mRunFor = runFor;
    }

    @Override
    public int getTaskId() {
        return 2;
    }

    @Override
    public String getTaskName() {
        return "SampleTaskNormal_2";
    }

    @Override
    public void run(){
        try {
            LogUtil.e("ProtoWorker", "start to run SampleTaskNormal_2");
            Thread.currentThread().sleep(mRunFor);
            LogUtil.e("ProtoWorker", "finish run SampleTaskNormal_2");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
