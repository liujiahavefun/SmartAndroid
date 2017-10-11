package com.smart.android.smartandroid.protolink.worker;

import com.smart.android.smartandroid.protolink.ProtoLogger;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * Created by liujia on 2016/12/25.
 */

public class ProtoThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    private ConcurrentHashMap<String, Date> mStartTimes;

    public ProtoThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
        mStartTimes = new ConcurrentHashMap<>();
    }

    @Override
    public void shutdown() {
        ProtoLogger.Log("ProtoThreadPoolExecutor: Going to shutdown.");
        ProtoLogger.Log("ProtoThreadPoolExecutor: Executed tasks: %d", getCompletedTaskCount());
        ProtoLogger.Log("ProtoThreadPoolExecutor: Running tasks: %d", getActiveCount());
        ProtoLogger.Log("ProtoThreadPoolExecutor: Pending tasks: %d", getQueue().size());

        super.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        ProtoLogger.Log("ProtoThreadPoolExecutor: Going to immediately shutdown.");
        ProtoLogger.Log("ProtoThreadPoolExecutor: Executed tasks: %d", getCompletedTaskCount());
        ProtoLogger.Log("ProtoThreadPoolExecutor: Running tasks: %d", getActiveCount());
        ProtoLogger.Log("ProtoThreadPoolExecutor: Pending tasks: %d", getQueue().size());

        return super.shutdownNow();
    }

    /*
    * liujia: 在我们submit的Runnable被真正调度执行之前，被回调。
    * 注意这里的Runnable不是俺们的Runnable，而是ScheduleFutureTask或者FutureTask，被ThreadPoolExecutor又包了一层。
    */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        ProtoLogger.Log("ProtoThreadPoolExecutor: A task is beginning: %s : %s", t.getName(), r.hashCode());
        mStartTimes.put(String.valueOf(r.hashCode()), new Date());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Future<?> result = (Future<?>)r;
        try {
            ProtoLogger.Log("*********************************");
            ProtoLogger.Log("ProtoThreadPoolExecutor: A task is finishing.");
            ProtoLogger.Log("ProtoThreadPoolExecutor: Result: %s", result.get());

            Date startDate = mStartTimes.remove(String.valueOf(r.hashCode()));
            Date finishDate = new Date();
            long diff = finishDate.getTime()-startDate.getTime();

            ProtoLogger.Log("ProtoThreadPoolExecutor: Execution Duration: %d", diff);
            ProtoLogger.Log("*********************************");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
        return super.decorateTask(runnable, task);
    }

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
        return super.decorateTask(callable, task);
    }

    /*
    @Override
    protected <V> RunnableFuture<V> newTaskFor(final Runnable runnable, V v) {
        return new FutureTask<V>(runnable, v) {
            public String toString() {
                if (runnable instanceof ProtoTaskRunnable) {
                    return ((ProtoTaskRunnable)runnable).getTaskName();
                }
                return runnable.toString();
            }
        };
    }
    */
}
