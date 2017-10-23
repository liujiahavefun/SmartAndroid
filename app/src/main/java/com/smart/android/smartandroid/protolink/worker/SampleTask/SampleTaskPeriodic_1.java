package com.smart.android.smartandroid.protolink.worker.SampleTask;

import com.smart.android.smartandroid.protolink.worker.ProtoTimerTaskRunnable;
import com.smart.android.smartandroid.util.LogUtil;

/**
 * Created by liujia on 2016/12/25.
 */

public class SampleTaskPeriodic_1 implements ProtoTimerTaskRunnable {
    private long mRunFor;

    public SampleTaskPeriodic_1(int runFor){
        this.mRunFor = runFor * 100;
    }

    @Override
    public int getTaskId() {
        return 10;
    }

    @Override
    public String getTaskName() {
        return "SampleTaskPeriodic_1";
    }

    @Override
    public boolean isRepeat() {
        return true;
    }

    @Override
    public int getInterval() {
        return 500;
    }

    @Override
    public int getFirstDelay() {
        return 10;
    }

    @Override
    public void run(){
        try {
            LogUtil.e("ProtoWorker", "start to run SampleTaskPeriodic_1");
            Thread.currentThread().sleep(mRunFor);
            LogUtil.e("ProtoWorker", "finish run SampleTaskPeriodic_1");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
