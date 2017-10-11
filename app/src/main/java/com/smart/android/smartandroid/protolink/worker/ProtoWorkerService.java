package com.smart.android.smartandroid.protolink.worker;

/**
 * Created by liujia on 16/12/19.
 */

public class ProtoWorkerService implements ProtoWorker {
    private ProtoWorkerImpl mProtoWorkerImpl;

    private ProtoWorkerService() {
        mProtoWorkerImpl = new ProtoWorkerImpl();
    }

    static public ProtoWorker createProtoWorker() {
        return new ProtoWorkerService();
    }

    /*
    * implements ProtoWorker Interface
    */
    public int startWorker() {
        return mProtoWorkerImpl.startWorker();
    }

    public int stopWorker() {
        return mProtoWorkerImpl.stopWorker();
    }

    public <T extends ProtoTaskRunnable> void post(T task) {
        mProtoWorkerImpl.post(task);
    }

    public <T extends ProtoTimerTaskRunnable> void postDelay(T task) {
        mProtoWorkerImpl.postDelay(task);
    }

    public void remove(int taskId) {
        mProtoWorkerImpl.remove(taskId);
    }
}
