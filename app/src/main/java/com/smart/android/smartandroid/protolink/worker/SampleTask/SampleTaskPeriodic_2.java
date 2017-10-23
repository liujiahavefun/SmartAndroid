package com.smart.android.smartandroid.protolink.worker.SampleTask;

import com.smart.android.smartandroid.protolink.worker.ProtoTimerTaskRunnable;
import com.smart.android.smartandroid.util.LogUtil;

/**
 * Created by liujia on 2016/12/25.
 */

public class SampleTaskPeriodic_2 implements ProtoTimerTaskRunnable {
    private long mRunFor;

    public SampleTaskPeriodic_2(int runFor){
        this.mRunFor = runFor * 100;
    }

    @Override
    public int getTaskId() {
        return 11;
    }

    @Override
    public String getTaskName() {
        return "SampleTaskPeriodic_2";
    }

    @Override
    public void run(){
        try {
            LogUtil.e("ProtoWorker", "start to run SampleTaskPeriodic_2");
            Thread.currentThread().sleep(mRunFor);
            LogUtil.e("ProtoWorker", "finish run SampleTaskPeriodic_2");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRepeat() {
        return false;
    }

    @Override
    public int getInterval() {
        return 1000;
    }

    @Override
    public int getFirstDelay() {
        return 1000;
    }
}
