package com.smart.android.smartandroid.jni;

import com.smart.android.smartandroid.util.LogUtil;

/**
 * Created by liujia on 16/8/17.
 */
public class JniManager {
    static {
        System.loadLibrary("netengine");
    }

    private static JniManager instance;
    public static synchronized JniManager GetInstance(){
        if(instance == null){
            instance = new JniManager();
        }
        return instance;
    }

    public boolean startNetEngine() {
        return NetEngineStart() == 0;
    }

    public boolean stopNetEngine() {
        return NetEngineStop() == 0;
    }


    // call NDK functiion
    public native int NetEngineStart();
    public native int NetEngineStop();
    public native int ConnCreate(ConnAttrWrapper attr);
    public native int ConnConnect(int connId, int ip, short port);
    public native int ConnClose(int connId);
    public native int ConnSend(int connId, byte[] data, int len);
    public native int ConnSetNoDelay(int connId, boolean flag);
    public native int ConnAddTimer(int connId, int timerId, int interval);
    public native int ConnRemoveTimer(int connId, int timerId);
    public native String stringFromJNI();

    // NDK call this function when something good/bad happen
    public static int OnEvent(ConnEventWrapper event, byte[] data){
        LogUtil.d("JNI", "receive network event");
        return 0;
    }
}
