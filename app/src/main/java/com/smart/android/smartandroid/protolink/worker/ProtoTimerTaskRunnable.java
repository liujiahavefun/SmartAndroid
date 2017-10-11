package com.smart.android.smartandroid.protolink.worker;

/**
 * Created by liujia on 16/12/19.
 */

public interface ProtoTimerTaskRunnable extends ProtoTaskRunnable {
    /*
     * 定时任务是否重复
     */
    boolean isRepeat();

    /*
     * 定时任务执行间隔，单位ms。
     */
    int getInterval();
}
