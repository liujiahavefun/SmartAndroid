package com.smart.android.smartandroid.jni;

import com.smart.android.smartandroid.protolink.ProtoLogger;

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
    public native int ConnSetNoDelay(int connId, boolean flag);
    public native int ConnSend(int connId, int uri, byte[] data, int len);
    public native int ConnAddTimer(int connId, int timerId, int interval);
    public native int ConnRemoveTimer(int connId, int timerId);
    public native String stringFromJNI();

    // callback function from JNI
    public static void OnEvent(int connId, int eventId, long val){
        EventHandlerMgr.onEvent(connId, eventId, val);
    }

    public static void OnData(int connId, int uri, byte[] data, int len){
        EventHandlerMgr.onData(connId, uri, data, len);
    }

    public static void OnLog(int level, String msg){
        ProtoLogger.Log(level, msg);
    }
}
