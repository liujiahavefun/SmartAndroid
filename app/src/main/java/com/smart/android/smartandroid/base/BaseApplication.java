package com.smart.android.smartandroid.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

/**
 * Created by liujia on 2017/10/12.
 */

public class BaseApplication extends MultiDexApplication {
    private int mAliveCount;

    @Override
    public void onCreate() {
        super.onCreate();

        AppEngine.getInstance().initialize(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (mAliveCount == 0) {
                    //从后台切换到前台了
                }
                mAliveCount++;
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                if (mAliveCount == 1) {
                    //被系统停止了
                }
                mAliveCount--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }
}
