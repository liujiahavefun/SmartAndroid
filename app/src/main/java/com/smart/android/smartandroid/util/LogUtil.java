package com.smart.android.smartandroid.util;

import android.text.TextUtils;
import android.util.Log;

public class LogUtil {

    static final String DEFAULT_TAG = "smartgo";

    /**
     * 此常量用于控制是否打日志到Logcat中 release版本中本变量应置为false.
     */
    public static final boolean LOGGABLE = true;

    private LogUtil() {
    }

    /**
     * 打印debug级别的log.
     *
     * @param tag tag标签
     * @param str 内容
     */
    public static void d(String tag, String str) {
        if (LOGGABLE) {
            if(!TextUtils.isEmpty(tag) && !TextUtils.isEmpty(str)) {
                Log.d(tag, str);
            }
        }
    }

    /**
     * 打印debug级别的log.
     *
     * @param str 内容
     */
    public static void d(String str) {
        if (LOGGABLE) {
            if(!TextUtils.isEmpty(str)) {
                Log.d(DEFAULT_TAG, str);
            }
        }
    }

    /**
     * 打印warning级别的log.
     *
     * @param tag tag标签
     * @param str 内容
     */
    public static void w(String tag, String str) {
        if (LOGGABLE) {
            if(!TextUtils.isEmpty(tag) && !TextUtils.isEmpty(str)) {
                Log.w(tag, str);
            }
        }
    }

    /**
     * 打印warning级别的log.
     *
     * @param str 内容
     */
    public static void w(String str) {
        if (LOGGABLE) {
            if(!TextUtils.isEmpty(str)) {
                Log.w(DEFAULT_TAG, str);
            }
        }
    }

    public static void w(Throwable e) {
        if (LOGGABLE) {
            if(e != null && !TextUtils.isEmpty(e.getMessage())) {
                Log.v(DEFAULT_TAG, e.getMessage());
            }
        }
    }

    /**
     * 打印error级别的log.
     *
     * @param tag tag标签
     * @param msg 内容
     * @param e   错误对象.
     */
    public static void e(String tag, String msg, Throwable e) {
        if (LOGGABLE) {
            if(!TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg) && e != null ) {
                Log.e(tag, msg, e);
            }
        }
    }

    /**
     * 打印error级别的log.
     *
     * @param tag tag标签
     * @param msg 内容
     * @param e   错误对象.
     */
    public static void e(String tag, String msg) {
        if (LOGGABLE) {
            if(!TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg)) {
                Log.e(tag, msg);
            }
        }
    }

    public static void e(String tag, Throwable e) {
        if (LOGGABLE) {
            if(!TextUtils.isEmpty(tag) && e != null && !TextUtils.isEmpty(e.getMessage())) {
                Log.e(tag, e.getMessage());
            }
        }
    }

    /**
     * 打印error级别的log.
     *
     * @param str 内容
     */
    public static void e(String str) {
        if (LOGGABLE) {
            if(!TextUtils.isEmpty(str)) {
                Log.e(DEFAULT_TAG, str);
            }
        }
    }

    /**
     * 打印info级别的log.
     *
     * @param tag tag标签
     * @param str 内容
     */
    public static void i(String tag, String str) {
        if (LOGGABLE) {
            if(!TextUtils.isEmpty(tag) && !TextUtils.isEmpty(str)) {
                Log.i(tag, str);
            }
        }
    }

    /**
     * 打印info级别的log.
     *
     * @param str 内容
     */
    public static void i(String str) {
        if (LOGGABLE) {
            if(!TextUtils.isEmpty(str)) {
                Log.i(DEFAULT_TAG, str);
            }
        }
    }

    /**
     * 打印verbose级别的log.
     *
     * @param tag tag标签
     * @param str 内容
     */
    public static void v(String tag, String str) {
        if (LOGGABLE) {
            if(!TextUtils.isEmpty(tag) && !TextUtils.isEmpty(str)) {
                Log.v(tag, str);
            }
        }
    }

    /**
     * 打印verbose级别的log.
     *
     * @param str 内容
     */
    public static void v(String str) {
        if (LOGGABLE) {
            if(!TextUtils.isEmpty(str)) {
                Log.v(DEFAULT_TAG, str);
            }
        }
    }
}
