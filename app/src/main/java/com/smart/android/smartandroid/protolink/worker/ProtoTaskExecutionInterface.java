package com.smart.android.smartandroid.protolink.worker;

/**
 * Created by liujia on 2016/12/25.
 */

public interface ProtoTaskExecutionInterface {
    void beforeExecute(ProtoTaskRunnable task, int type);
    void afterExecute(ProtoTaskRunnable task, int type);
}
