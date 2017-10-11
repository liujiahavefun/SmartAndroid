package com.smart.android.smartandroid.protolink.worker;

import com.smart.android.smartandroid.protolink.ProtoLogger;

import java.util.Date;

/**
 * Created by liujia on 2016/12/24.
 */

public class ProtoThread extends Thread{
    private Runnable mTarget;

    private String mThreadName = "PROTO_WORKER_THREAD";
    private Date mStartTime;

    public ProtoThread(Runnable target) {
        this.mTarget = target;
    }

    public ProtoThread(Runnable target, final String name) {
        this.mTarget = target;
        this.mThreadName = name;
    }

    @Override
    public void run() {
        try {
            mStartTime = new Date();
            mTarget.run();
        } finally {
            final long ts = (new Date()).getTime() - mStartTime.getTime();
            ProtoLogger.Log(this.mThreadName + " exit, total running time " + String.valueOf(ts) + " ms");
        }
    }
}
