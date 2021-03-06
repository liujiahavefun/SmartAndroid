package com.smart.android.smartandroid.jni;

import com.smart.android.smartandroid.protolink.IEventHandler;
import com.smart.android.smartandroid.protolink.ProtoLogger;
import com.smart.android.smartandroid.util.LogUtil;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liujia on 16/8/27.
 * This class is used for Native code's callback, don't use it in JAVA and modify it!
 */
public final class EventHandlerMgr {
    /*
     * NativeLog and OneNetEvent is called by native codes
     */
    public static void NativeLog(int level, String msg){
        if(level == 0){
            LogUtil.v(TAG, msg);
        } else if(level == 1){
            LogUtil.d(TAG, msg);
        } else if(level == 2){
            LogUtil.i(TAG, msg);
        } else if(level == 3){
            LogUtil.w(TAG, msg);
        } else{
            LogUtil.e(TAG, msg);
        }
    }

    /*
    public static void OnNetEvent(ConnEventWrapper event, byte[] data, int len) {
        if(event == null || (event.eventType == ConnEventWrapper.EVENT_IN && data.length == 0)) {
            return;
        }

        if(event.eventType != ConnEventWrapper.EVENT_IN) {
            LogUtil.d(TAG, event.toString());
        }else {
            LogUtil.d(TAG, String.format("%s,数据包长度:%d", event.toString(), len));
        }

        if(handlers.containsKey(event.connId)){
            IEventHandler handler = handlers.get(event.connId);
            if(handler != null){
                handler.onEvent(event, data, len);
            }
        }else{
            //LogUtil.w(TAG, "conn id: " + event.connId + " with Event, not exist" );
            //close the conn in NetEngine???
        }
    }
    */

    public static void onEvent(int connId, int event, long val) {
        if(handlers.containsKey(connId)){
            IEventHandler handler = handlers.get(connId);
            if(handler != null){
                handler.onEvent(connId, event, val);
            }
        }else{
            ProtoLogger.LogWarning("connId %d, event %d, not register handler", connId, event);
            //close the conn in NetEngine???
        }
    }

    public static void onData(int connId, int uri, byte[] data, int len) {
        if(handlers.containsKey(connId)){
            IEventHandler handler = handlers.get(connId);
            if(handler != null){
                handler.onData(connId, uri, data, len);
            }
        }else{
            ProtoLogger.LogWarning("connId %d, event IN, not register handler", connId);
            //close the conn in NetEngine???
        }
    }

    /*
     * manage event handler
     */
    public static String TAG = "JNI";
    public static ConcurrentHashMap<Integer, IEventHandler> handlers = new ConcurrentHashMap<>();

    public static void registerHandler(int id, IEventHandler handler){
        handlers.put(id, handler);
    }

    public static void unregisterHandler(int id){
        handlers.remove(id);
    }
}
