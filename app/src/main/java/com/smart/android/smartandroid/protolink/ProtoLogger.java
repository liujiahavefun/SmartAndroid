package com.smart.android.smartandroid.protolink;

import com.smart.android.smartandroid.util.LogUtil;

/**
 * Created by liujia on 16/12/27.
 */

public class ProtoLogger {
    static public void Log(final String msg) {
        LogUtil.e(msg);
    }

    static public void Log(final String format, Object... args) {
        LogUtil.e(String.format(format, args));
    }
}
