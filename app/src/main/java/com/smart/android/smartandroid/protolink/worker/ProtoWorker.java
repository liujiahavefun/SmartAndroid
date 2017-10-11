package com.smart.android.smartandroid.protolink.worker;

/**
 * Created by liujia on 16/12/19.
 */

public interface ProtoWorker {
    /*
     * 启动worker，启动后才可以接受任务，返回0表示成功，当然了哥不会不返回0的
     */
    int startWorker();

    /*
     * 停止worker，启动后才可以接受任务，返回0表示成功，当然了哥不会不返回0的
     */
    int stopWorker();

    /*
     * 投递普通任务
     */
    <T extends ProtoTaskRunnable> void post(T task);

    /*
     * 投递定时任务，
     */
    <T extends ProtoTimerTaskRunnable> void postDelay(T task);

    /*
     * 取消任务，根据Id，注意如果重复投递同一个任务的话，会取消所有的
     */
    void remove(int taskId);
}