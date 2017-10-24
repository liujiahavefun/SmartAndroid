package com.smart.android.smartandroid.protolink.worker;

import android.os.Handler;
import com.smart.android.smartandroid.protolink.worker.SampleTask.SampleTaskNormal_1;
import com.smart.android.smartandroid.protolink.worker.SampleTask.SampleTaskNormal_2;
import com.smart.android.smartandroid.protolink.worker.SampleTask.SampleTaskNormal_3;
import com.smart.android.smartandroid.protolink.worker.SampleTask.SampleTaskPeriodic_1;
import com.smart.android.smartandroid.protolink.worker.SampleTask.SampleTaskPeriodic_2;

/**
 * Created by liujia on 16/12/26.
 */

public class ProtoWorkerTest {
    static public void doTest() {
        final ProtoWorker worker = ProtoWorkerService.createProtoWorker();
        worker.startWorker();
        worker.post(new SampleTaskNormal_1(1));
        worker.post(new SampleTaskNormal_1(1));
        worker.post(new SampleTaskNormal_2(1));
        worker.post(new SampleTaskNormal_3(1));

        final ProtoTimerTaskRunnable periodicTask = new SampleTaskPeriodic_1(worker, 1);
        worker.postDelay(periodicTask);

        worker.postDelay(new SampleTaskPeriodic_2(worker, 1));

        worker.remove(2);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                worker.remove(periodicTask.getTaskId());
            }
        }, 10*1000);
    }
}
