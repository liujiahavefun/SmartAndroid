package com.smart.android.smartandroid.loginsdk;

import com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean;
import com.smart.android.smartandroid.protocol.protocol;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liujia on 17/1/10.
 */

public class LoginProtoHandler implements ProtoHandler{
    private LoginMgr mLoginMgr;
    private ConcurrentHashMap<Integer, ProtoHandler> mHandlerMap = new ConcurrentHashMap<Integer, ProtoHandler>() {
        {
            put(protocol.PLoginByPassportRes_uri, LoginProtoHandler.this);
            put(protocol.PLoginByUidRes_uri, LoginProtoHandler.this);
        }
    };

    public LoginProtoHandler(LoginMgr mgr){
        this.mLoginMgr = mgr;
    }

    public void addHandler(int id, ProtoHandler handler) {
        if (handler == null) {
            return;
        }

        if (mHandlerMap.contains(id)) {
            return;
        }

        mHandlerMap.put(id, handler);
    }

    public void onProto(byte[] data, int len) {
        //从data里解析出消息号(可以是整数id，或者更好的),然后跟消息号查找消息handler处理
        //所有的消息都有一个required msg_type字段，字段值为enum MsgType，所有消息分配单独的消息id
        //似有更好的做法，因为这样的话，多人协作，必须有一个"皇帝"来分配这个id保证不重复
        //更好的做法，我感觉是可以用消息（最后在pb消息外部再包一层）中的string代表消息号，这个string就是消息标识，多人之间分配不同的前缀，
        //然后用自己的前缀搭配不同后缀来标识一个消息，如果这样还能重，那拉出去剁了就是了
        //TODO: 消息格式，我构想的是：4字节包长度 + 类型数据（可以简单数字id，或者字符串，字符串需要4字节表示长度，后面加上\0结尾的字符串） + protobuf数据 + 4字节数据校验(crc32 adler32)

        try {
            BaseProtoBean.BaseProtoMsg base = BaseProtoBean.BaseProtoMsg.parseFrom(data);
            int uri = base.getUri();
            ProtoHandler handler = mHandlerMap.get(uri);
            if (handler != null) {
                handler.call(uri, data);
            }
        }catch (InvalidProtocolBufferException e) {

        }
    }

    /*
    public void test() {
        TestProtoBean.Test1Info test1 = TestProtoBean.Test1Info.newBuilder()
                .setMsgType(TestProtoBean.MsgType.Test1InfoMsgType)
                .setId(1)
                .setName("liujia")
                .setDesc("liujia desc")
                .build();

        byte[] data = test1.toByteArray();
        this.onProto(data, data.length);
    }
    */

    @Override
    public void call(int uri, byte[] data) {
        mLoginMgr.call(uri, data);
    }
}
