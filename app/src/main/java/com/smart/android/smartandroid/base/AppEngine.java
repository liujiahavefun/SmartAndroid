package com.smart.android.smartandroid.base;

import android.content.Context;
import android.text.TextUtils;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * Created by liujia on 2017/10/12.
 */

public class AppEngine {
    private static AppEngine sInstance = null;
    private Context mAppContext = null;

    private AppEngine() {
    }

    public static synchronized AppEngine getInstance() {
        if (sInstance == null) {
            sInstance = new AppEngine();
        }
        return sInstance;
    }

    public static Context getAppContext() {
        return AppEngine.getInstance().mAppContext;
    }

    private String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        android.app.ActivityManager mActivityManager = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    public void initialize(Context context) {
        mAppContext = context;

        String appName = getCurrentProcessName(mAppContext);
        if (!TextUtils.equals(appName, mAppContext.getPackageName())) {
            return;
        }

        deferInitialize();
    }

    public void deferInitialize() {
        String appName = getCurrentProcessName(mAppContext);
        if (!TextUtils.equals(appName, mAppContext.getPackageName())) {
            return;
        }

        // 创建默认的ImageLoader配置参数
        DisplayImageOptions defaultOptions = new DisplayImageOptions
                .Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration
                .Builder(mAppContext)
                .defaultDisplayImageOptions(defaultOptions)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();

        ImageLoader.getInstance().init(configuration);
    }
}
