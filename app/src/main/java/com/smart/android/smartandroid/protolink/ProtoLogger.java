package com.smart.android.smartandroid.protolink;

import com.smart.android.smartandroid.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    static Map<Integer, String> EVENT_MAP = new HashMap<Integer, String>() {
        {
            put(0, "[0]未设置");
            put(1, "[1]连接中");
            put(2, "[2]已连接");
            put(3, "[3]已关闭");
            put(4, "[4]错误");
            put(5, "[5]收到数据包");
            put(6, "[6]收到数据流");
            put(7, "[7]已发送");
            put(8, "[8]连接状态");
            put(9, "[9]定时器");
        }
    };

    static SimpleDateFormat TIME_FORMATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    static public String Event2String(int connId, int event, long val) {
        String type = "[-1]未知";
        if(EVENT_MAP.containsKey(event)) {
            type = EVENT_MAP.get(event);
        }

        String conn = String.format("connId:%d", connId);
        String time = TIME_FORMATE.format(new Date(System.currentTimeMillis()));
        String retVal = String.format("val:%d", val);

        return time + ":" + type + "," + connId + "," + retVal;
    }
}
