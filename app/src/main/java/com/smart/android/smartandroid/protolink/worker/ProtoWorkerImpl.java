package com.smart.android.smartandroid.protolink.worker;

import com.smart.android.smartandroid.protolink.ProtoLogger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by liujia on 16/10/3.
 */

public class ProtoWorkerImpl implements ProtoTaskExecutionInterface{
    public class FutureWrapper implements Comparable<FutureWrapper>{
        private int taskId;
        private int taskHashCode;
        private Future<?> future;

        public FutureWrapper(int taskId, int taskHashCode, Future<?> future) {
            this.taskId = taskId;
            this.taskHashCode = taskHashCode;
            this.future = future;
        }

        public int getId() {
            return taskId;
        }

        public int getTaskHashCode() {
            return taskHashCode;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        public int compareTo(FutureWrapper another) {
            return this.taskHashCode - another.getTaskHashCode();
        }
    }

    private static final int TASK_NORMAL = 1;
    private static final int TASK_DELAY = 2;
    private static final int TASK_PERIODIC = 3;

    public class TaskWrapper implements Runnable {
        private final ProtoTaskExecutionInterface execution;
        private final ProtoTaskRunnable runnable;
        private final int type;

        public TaskWrapper(ProtoTaskExecutionInterface execution, ProtoTaskRunnable r, int type) {
            this.execution = execution;
            this.runnable = r;
            this.type =type;
        }

        @Override
        public void run() {
            try {
                ProtoLogger.Log("***********before run " + runnable.getTaskName());
                this.execution.beforeExecute(runnable, type);
                runnable.run();
            }catch (Exception e) {
                e.printStackTrace();
            } finally{
                ProtoLogger.Log("***********after run " + runnable.getTaskName());
                this.execution.afterExecute(runnable, type);
            }
        }
    }

    private static int WORKER_THREAD_NUM = 1;

    private ConcurrentLinkedQueue<FutureWrapper> mFutureQueue;
    private ConcurrentLinkedQueue<FutureWrapper> mTimerFutureQueue;

    private ProtoThreadPoolExecutor mProtoExecutor;
    private AtomicBoolean mRunning = new AtomicBoolean(false);

    public ProtoWorkerImpl() {
        mFutureQueue = new ConcurrentLinkedQueue<>();
        mTimerFutureQueue = new ConcurrentLinkedQueue<>();

        mProtoExecutor = new ProtoThreadPoolExecutor(WORKER_THREAD_NUM, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new ProtoThread(r);
            }
        });
    }

    public int startWorker() {
        mRunning.set(true);
        return 0;
    }

    public int stopWorker() {
        mRunning.set(false);
        clear();
        return 0;
    }

    public <T extends ProtoTaskRunnable> void post(T task) {
        if (mRunning.get() == false) {
            return;
        }
        Future<?> f = mProtoExecutor.submit(new TaskWrapper(this, task, TASK_NORMAL));
        mFutureQueue.add(new FutureWrapper(task.getTaskId(), task.hashCode(), f));
    }

    public <T extends ProtoTimerTaskRunnable> void postDelay(T task) {
        if (mRunning.get() == false) {
            return;
        }

        Future<?> f;

        //liujia: 这块可以细思量，一次性延时任务直接延迟执行就ok了，多次定时任务，究竟从当前开始定时之前，还是延迟一个定时后再执行好呢？
        if (task.isRepeat()) {
            f = mProtoExecutor.scheduleAtFixedRate(new TaskWrapper(this, task, TASK_PERIODIC), task.getFirstDelay(), task.getInterval(), TimeUnit.MILLISECONDS);
        }else {
            f = mProtoExecutor.schedule(new TaskWrapper(this, task, TASK_DELAY), task.getFirstDelay(), TimeUnit.MILLISECONDS);
        }

        mTimerFutureQueue.add(new FutureWrapper(task.getTaskId(), task.hashCode(), f));
    }

    public void remove(int taskId) {
        removeFuture(taskId);
    }

    public <T extends ProtoTaskRunnable> void execute(T task) {
        mProtoExecutor.execute(task);
    }

    private void clear() {
        mProtoExecutor.shutdownNow();
        removeAllTask();
    }

    private void removeAllTask() {
        for (FutureWrapper fw : mFutureQueue) {
            fw.cancel(true);
        }

        for (FutureWrapper fw : mTimerFutureQueue) {
            fw.cancel(true);
        }

        mFutureQueue.clear();
        mTimerFutureQueue.clear();
    }

    private void removeFuture(int taskId) {
        //liujia：先尝试remove定时的，通常也是这种需求。没有再去remove非定时的，反正要么是定时要么是非定时，如果既是定时又是非定时，拉出去砍了
        if (internaleRemoveTimerFuture(taskId) > 0) {
            return;
        }

        internaleRemoveFuture(taskId);
    }

    private int internaleRemoveFuture(int taskId) {
        if (mFutureQueue.isEmpty()) {
            return 0;
        }

        Queue<FutureWrapper> foundItems = new LinkedList<>();
        for (FutureWrapper f : mFutureQueue) {
            if (f.getId() == taskId) {
                f.cancel(false);
                foundItems.add(f);
            }
        }

        mFutureQueue.removeAll(foundItems);
        return foundItems.size();
    }

    private int internaleRemoveTimerFuture(int taskId) {
        if (mTimerFutureQueue.isEmpty()) {
            return 0;
        }

        List<FutureWrapper> foundItems = new ArrayList<>();
        for (FutureWrapper f : mTimerFutureQueue) {
            if (f.getId() == taskId) {
                f.cancel(false);
                foundItems.add(f);
            }
        }

        mTimerFutureQueue.removeAll(foundItems);
        return foundItems.size();
    }

    @Override
    public void beforeExecute(ProtoTaskRunnable task, int type) {
        //do nothing now
    }

    @Override
    public void afterExecute(ProtoTaskRunnable task, int type) {
        if (type == TASK_PERIODIC) {
            return;
        }

        if(type == TASK_NORMAL) {
            for (FutureWrapper f : mFutureQueue) {
                if (f.getTaskHashCode() == task.hashCode()) {
                    mFutureQueue.remove(f);
                    return;
                }
            }
        }else if(type == TASK_DELAY) {
            for (FutureWrapper f : mTimerFutureQueue) {
                if (f.getTaskHashCode() == task.hashCode()) {
                    mTimerFutureQueue.remove(f);
                    return;
                }
            }
        }
    }

    /*
    private void dumpFuture() {
        ProtoLogger.Log("dump timer future");
        for (FutureWrapper f : mTimerFutureQueue) {
            ProtoLogger.Log("Timer Future, " + f.getId() + " " + f.getTaskHashCode());
        }

        ProtoLogger.Log("dump future");
        for (FutureWrapper f : mFutureQueue) {
            ProtoLogger.Log("Future, " + f.getId() + " " + f.getTaskHashCode());
        }
    }
    */
}
