package com.smart.android.smartandroid.protolink;

import com.smart.android.smartandroid.util.LogUtil;

/**
 * Created by liujia on 16/12/27.
 */

public class ProtoLogger {
    static public final String TAG = "JNI";

    static public void Log(int level, final String msg) {
        switch (level){
            case 0:
                LogUtil.d(TAG, msg);
                break;
            case 1:
                LogUtil.i(TAG, msg);
                break;
            case 2:
                LogUtil.w(TAG, msg);
                break;
            case 3:
                LogUtil.e(TAG, msg);
                break;
            default:
                LogUtil.d(TAG, msg);
                break;
        }
    }

    static public void Log(final String msg) {
        LogUtil.i(TAG, msg);
    }

    static public void Log(final String format, Object... args) {
        LogUtil.i(TAG, String.format(format, args));
    }

    static public void LogDebug(final String msg) {
        LogUtil.d(TAG, msg);
    }

    static public void LogDebug(final String format, Object... args) {
        LogUtil.d(TAG, String.format(format, args));
    }

    static public void LogInfo(final String msg) {
        LogUtil.i(TAG, msg);
    }

    static public void LogInfo(final String format, Object... args) {
        LogUtil.i(TAG, String.format(format, args));
    }

    static public void LogWarning(final String msg) {
        LogUtil.w(TAG, msg);
    }

    static public void LogWarning(final String format, Object... args) {
        LogUtil.w(TAG, String.format(format, args));
    }

    static public void LogError(final String msg) {
        LogUtil.e(TAG, msg);
    }

    static public void LogError(final String format, Object... args) {
        LogUtil.e(TAG, String.format(format, args));
    }
}
