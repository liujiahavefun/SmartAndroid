package com.smart.android.smartandroid.base;

import android.app.Activity;

import com.smart.android.smartandroid.MainActivity;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by liujia on 2017/10/12.
 */

public class ActivityManager {
    private static ActivityManager instance;
    private ActivityManager() {
    }

    public static ActivityManager getInstance() {
        if (null == instance) {
            synchronized (ActivityManager.class) {
                if (null == instance) {
                    instance = new ActivityManager();
                }
            }
        }
        return instance;
    }

    private Deque<Activity> activityStack = new ArrayDeque<>();

    public void popAllActivityExcept(Class cls) {
        while (!activityStack.isEmpty()) {
            Activity activity = currentActivity();
            if (activity == null) {
                break;
            }
            if (activity.getClass().equals(MainActivity.class)) {
                break;
            }
            if (activity.getClass().equals(cls)) {
                break;
            }
            popActivity(activity);
            activity.finish();
        }
    }

    /*
    * 仅供app退出时调用！！！
    */
    public void popAllActivity() {
        while (!activityStack.isEmpty()) {
            Activity activity = currentActivity();
            if (activity == null) {
                break;
            }

            popActivity(activity);
            activity.finish();
        }
    }

    public void popActivity(Activity activity) {
        if (!activityStack.isEmpty() && null != activity) {
            activityStack.remove(activity);
        }
    }

    public void removeHomeActivity(Activity except) {
        if (!activityStack.isEmpty()) {
            for (Activity activity : activityStack) {
                //之前从SettingActivity现在从SwitchRoleActivity作为切换角色的跳板，这个activity不能finish
                // activity instanceof except
                if (activity.getClass().equals(except.getClass())) {
                    continue;
                }
                activity.finish();
            }
        }
    }

    public void popTopNActivity(int n) {
        for (int i = 0; i < n; i++) {
            Activity activity = currentActivity();
            if (activity == null) {
                break;
            }
            popActivity(activity);
            activity.finish();
        }
    }

    public void pushActivity(Activity activity) {
        if (null == activityStack) {
            activityStack = new ArrayDeque<>();
        }
        activityStack.addFirst(activity);
    }

    public Activity currentActivity() {
        if (!activityStack.isEmpty()) {
            return activityStack.peekFirst();
        }
        return null;
    }

    public void finishCurrentActivity() {
        Activity activity = currentActivity();
        if (activity != null) {
            popActivity(activity);
            activity.finish();
        }
    }

    public int getStackSize() {
        if (activityStack == null) {
            return 0;
        }
        return activityStack.size();
    }

    public boolean hasMainActivity() {
        for (Activity activity : activityStack) {
            if (activity instanceof MainActivity) {
                return true;
            }
        }
        return false;
    }

    /*
    public String getStackTopN(int n) {
        if (n < 1) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (Activity activity : activityStack) {
            if (index >= n) {
                break;
            }

            sb.append(activity.getLocalClassName());
            sb.append("-");
            index++;
        }

        return sb.toString();
    }
    */
}
