package com.smart.android.smartandroid.protolink.worker;


/**
 * Created by liujia on 16/10/3.
 */

public interface ProtoTaskRunnable extends Runnable{
    /*
     * 任务Id，这个Id别重复，一个任务用一个独立的Id
     */
    int getTaskId();

    /*
     * 任务名
     */
    String getTaskName();
}
