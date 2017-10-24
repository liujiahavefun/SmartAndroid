package com.smart.android.smartandroid.loginsdk;

import com.smart.android.smartandroid.loginsdk.loginTask.LoginCheckDataTask;
import com.smart.android.smartandroid.loginsdk.loginTask.LoginPingTask;
import com.smart.android.smartandroid.loginsdk.proto.LoginProtoBean;
import com.smart.android.smartandroid.loginsdk.task.LvsTestTask;
import com.smart.android.smartandroid.loginsdk.task.ReconnectTask;
import com.smart.android.smartandroid.loginsdk.task.SendTask;
import com.smart.android.smartandroid.protocol.protocol;
import com.smart.android.smartandroid.protolink.ProtoInfo;
import com.smart.android.smartandroid.protolink.ProtoLogger;
import com.smart.android.smartandroid.protolink.worker.ProtoWorker;
import com.smart.android.smartandroid.protolink.worker.ProtoWorkerService;

import com.google.protobuf.InvalidProtocolBufferException;

import android.text.TextUtils;

/**
 * Created by liujia on 16/9/4.
 */
public class LoginMgr implements ProtoHandler{
    private LoginListener mLoginListener;
    private LoginLink mLoginLink;
    private ProtoWorker mLoginWorker;
    private LoginProtoHandler mLoginProtoHandler;
    private int mLoginStatus;

    private int mReconnectTimes = 0;
    private boolean mReconnectedTaskPosted = false;

    private int mEnvSetting = LoginConstant.ENV_RELEASE;
    private boolean mLvsTesting = false;

    public LoginMgr(LoginListener listener){
        mLoginLink = new LoginLink(this);
        mLoginStatus = LoginConstant.LOGIN_STATUS_IDLE;
        mLoginWorker = ProtoWorkerService.createProtoWorker();
        mLoginListener = listener;
        mLoginProtoHandler = new LoginProtoHandler(this);
        mLoginProtoHandler.addHandler(protocol.PLoginByPassportRes_uri, this);
        mLoginProtoHandler.addHandler(protocol.PLoginByUidRes_uri, this);
        mLoginProtoHandler.addHandler(protocol.PLoginLogoutRes_uri, this);
        mLoginProtoHandler.addHandler(protocol.PLoginLogoutRes_uri, this);
        mLoginProtoHandler.addHandler(protocol.PLoginKickOff_uri,this);

        mLoginWorker.startWorker();
    }

    public int setEnv(int env) {
        if (env != LoginConstant.ENV_DEVELOPMENT || env != LoginConstant.ENV_RELEASE) {
            return 1;
        }
        mEnvSetting = env;
        return 0;
    }

    public int getEnv() {
        return mEnvSetting;
    }

    public int loginByToken(String uid, String token, String dev) {
        if (mLoginStatus == LoginConstant.LOGIN_STATUS_LOGINED || mLoginStatus == LoginConstant.LOGIN_STATUS_LOGINING) {
            //TODO: log here
            return 0;
        }

        ProtoInfo.GetInstance().setUserId(uid);
        ProtoInfo.GetInstance().setUserToken(token);
        ProtoInfo.GetInstance().setDeviceId(dev);
        testLvs();

        return 1;
    }

    public int loginByPassword(String passport, String password, String dev) {
        if (mLoginStatus == LoginConstant.LOGIN_STATUS_LOGINED || mLoginStatus == LoginConstant.LOGIN_STATUS_LOGINING) {
            //TODO: log here
            return 0;
        }

        ProtoInfo.GetInstance().setPassport(passport);
        ProtoInfo.GetInstance().setPassword(password);
        ProtoInfo.GetInstance().setDeviceId(dev);
        testLvs();

        return 1;
    }

    public void leave() {
        //liujia: now do nothing
    }

    //liujia: post a testLvs task which to find the proper(fastest) login server to connect
    public void testLvs(){
        if (mLoginStatus != LoginConstant.LOGIN_STATUS_IDLE && mLoginStatus != LoginConstant.LOGIN_STATUS_DISCONNECTD) {
            //TODO: LoginMgr.testLvs, status!=IDLE && status != DISCONNECTED, do nothing, m_nStatus=%d
            return;
        }

        if (mLvsTesting == true) {
            //TODO:LoginMgr.testLvs, lvs is already in testing.
            return;
        }

        mLoginWorker.post(new LvsTestTask(this));
    }

    void setLvsTesting(boolean testing)
    {
        mLvsTesting = testing;
    }

    ProtoHandler getProtoHandler() {
        return mLoginProtoHandler;
    }

    ProtoWorker getWorker() {
        return mLoginWorker;
    }

    public LoginLink getLink(){
        return this.mLoginLink;
    }

    public int getLoginStatus(){
        return this.mLoginStatus;
    }

    public void setLoginStatus(int status){
        this.mLoginStatus = status;
    }

    public boolean hasNetwork(){
        // do something real check in future....
        return true;
    }

    /*
    * 断了重连不能一直胡逼连到死，这样弄不好后端就雪崩了
    */
    static int RETRY_DELAYS[] = {100, 1000, 1000, 1000, 3000, 5000, 5000, 15000, 30000, 60000, 120000, 120000, 120000};
    private int getReconnectDealy() {
        int delay = 100;
        int max = RETRY_DELAYS.length - 2;
        if (mReconnectTimes >= max) {
            mReconnectTimes = max;
        }

        if (mReconnectTimes > max) {
            delay = RETRY_DELAYS[max];
        } else {
            delay = RETRY_DELAYS[mReconnectTimes];
        }
        return delay;
    }

    private void reconnect() {
        //TODO:LoginMgr.reconnect, reconnectTaskPosted=%b, reconnectTimes=%d, mReconnectedTaskPosted, mReconnectTimes

        //5次都没连上，就重新从lvs获取一下ip，有可能是之前连的机器挂了
        if ( mReconnectTimes > 0 && mReconnectTimes % 5 == 0 ) {
            //TODO:LoginMgr.reconnect, try to reconnect more than 5 times, testlvs.
            testLvs();
            mReconnectTimes++;
            return;
        }

        if (!mReconnectedTaskPosted) {
            mReconnectedTaskPosted = true;
            mLoginWorker.postDelay(new ReconnectTask(this, getReconnectDealy(), false));
        }
    }

    public void realReconnect() {
        //TODO: LoginMgr.realReconnect
        mLoginLink.reconnect();
        mReconnectTimes++;
        mReconnectedTaskPosted = false;
    }

    /*
    * 连接上了
    */
    public void onConnected(){
        if (mLoginStatus == LoginConstant.LOGIN_STATUS_LOGINING ||
                mLoginStatus == LoginConstant.LOGIN_STATUS_CONNECTED ||
                mLoginStatus == LoginConstant.LOGIN_STATUS_LOGINED)
        {
            //TODO: LoginMgr.onConnected, do nothing for loginStatus=%d
            return;
        }

        setLoginStatus(LoginConstant.LOGIN_STATUS_CONNECTED);
        sendLoginReq();
    }

    /*
     * 疑似被踢了
     */
    public void onDisconnected(){
        //liujia: 这些被踢啊，登出啊，登录失败啊，就不重试了吧。。。
        if (mLoginStatus == LoginConstant.LOGIN_STATUS_KICKOFF ||
                mLoginStatus == LoginConstant.LOGIN_STATUS_LOGOFF ||
                mLoginStatus == LoginConstant.LOGIN_STATUS_FAILED)
        {
            return;
        }

        mLoginLink.close();

        //如果还需要做哪些断开连接时要通知回调的事情，这里一并做了吧

        //死皮赖脸重连
        setLoginStatus(LoginConstant.LOGIN_STATUS_DISCONNECTD);
        reconnect();
    }

    //liujia: called by LoginPingTask
    public void sendPing(){
        LoginProtoBean.PLoginPing ping = LoginProtoBean.PLoginPing.newBuilder()
                .setUid(ProtoInfo.GetInstance().getUserId())
                .setClientts(System.currentTimeMillis())
                .build();
        send(protocol.PLoginPing_uri, ping.toByteArray());
    }

    public void sendLoginReq(){
        byte[] data = null;
        if(!TextUtils.isEmpty(ProtoInfo.GetInstance().getPassport())) {
            LoginProtoBean.PLoginByPassport loginByPassport = LoginProtoBean.PLoginByPassport.newBuilder()
                    .setUri(protocol.PLoginByPassport_uri)
                    .setPassport(ProtoInfo.GetInstance().getPassport())
                    .setPassword(ProtoInfo.GetInstance().getPassword())
                    .setDeviceid("test device id")
                    .setDevicetype(protocol.DEVICE_ANDROID)
                    .build();
            send(protocol.PLoginByPassport_uri, loginByPassport.toByteArray());
        }else if(!TextUtils.isEmpty(ProtoInfo.GetInstance().getUserId())) {
            LoginProtoBean.PLoginByToken loginByToken = LoginProtoBean.PLoginByToken.newBuilder()
                    .setUri(protocol.PLoginByUid_uri)
                    .setUid(ProtoInfo.GetInstance().getUserId())
                    .setToken(ProtoInfo.GetInstance().getUserToken())
                    .setDeviceid("test device id")
                    .setDevicetype(protocol.DEVICE_ANDROID)
                    .build();
            send(protocol.PLoginByUid_uri, loginByToken.toByteArray());
        }

        //TODO: log and notify here, empty login information
        return;
    }

    private void send(int uri, byte[] data){
        if (data == null || data.length == 0) {
            return;
        }
        mLoginWorker.post(new SendTask(this, uri, data));
    }

    public void sendTransup(int svid, long uid, byte[] payload) {
        //TODO 完成上行包
        //liujia: 这里要仔细考虑，实际上下面是哥仔细考虑后的，目前的实现是哥没考虑好之前胡逼写的
        //目前发送的包，应该是4个字节的包长header，4个字节的uri(服务id和消息id各2个字节)，后面是payload
        //所有的上行包，应该统一一下，改成：4个字节的fix header(包长)，2个字节的svid(服务id)，8个字节的uid(字符串的uid太low了吧。。。)，后面是完整的消息的payload包
        //消息的payload包按照如下方式打包：4个字节的len，4个自字节的uri(服务id和消息id各2个字节), 2个字节的appid(表明平台是android ios web等)， 后面是消息真正的protobuf数据包
    }

    /*
    * 处理接收的消息
    */
    @Override
    public void onProto(int uri, byte[] data) {
        switch (uri){
            case protocol.PLoginByPassportRes_uri:
                onLoginByPassportRes(data);
                break;
            case protocol.PLoginByUidRes_uri:
                onLoginByUidRes(data);
                break;
            case protocol.PLoginPingRes_uri:
                onPingRes(data);
                break;
            case protocol.PLoginKickOff_uri:
                onKickOff(data);
                break;
            default:
                onDefault(uri);
                break;
        }
    }

    private void onDefault(int uri) {
        ProtoLogger.Log("LoginMgr.onDefault, resv invalid data, uri: 0x%x", uri);
    }

    private void onLoginByPassportRes(byte[] data) {
        try {
            LoginProtoBean.PLoginByPassportRes resp = LoginProtoBean.PLoginByPassportRes.parseFrom(data);
            int code = resp.getRescode();
            String userId = "";
            String token = "";
            if (code == protocol.RES_OK) {
                userId = resp.getUid();
                token = resp.getToken();

                this.setLoginStatus(LoginConstant.LOGIN_STATUS_LOGINED);
                ProtoInfo.GetInstance().setUserId(userId);
                ProtoInfo.GetInstance().setUserToken(token);
                ProtoLogger.Log("LoginMgr.onLoginByPassportRes, login success, code = %d, uid = %s, token = %s", code, userId, token);
            }else {
                ProtoLogger.Log("LoginMgr.onLoginByPassportRes, login failed, code = %d", code);
                this.setLoginStatus(LoginConstant.LOGIN_STATUS_FAILED);
            }

            mLoginListener.onLoginStatusChanged(this.getLoginStatus(), userId, token);
            this.getWorker().postDelay(new LoginPingTask(this));

        }catch (InvalidProtocolBufferException e) {
            ProtoLogger.Log("LoginMgr.onLoginByPassportRes, parse msg failed, %s", e.getLocalizedMessage());
        }
    }

    private void onLoginByUidRes(byte[] data) {
        try {
            LoginProtoBean.PLoginByTokenRes resp = LoginProtoBean.PLoginByTokenRes.parseFrom(data);
            int code = resp.getRescode();
            String userId = "";
            String token = "";
            if (code == protocol.RES_OK) {
                userId = resp.getUid();
                token = resp.getToken();

                this.setLoginStatus(LoginConstant.LOGIN_STATUS_LOGINED);
                ProtoInfo.GetInstance().setUserId(userId);
                ProtoInfo.GetInstance().setUserToken(token);
                ProtoLogger.Log("LoginMgr.onLoginByUidRes, login success, code = %d, uid = %s, cookie = %s", code, userId, token);
            }else {
                ProtoLogger.Log("LoginMgr.onLoginByUidRes, login failed, code = %d", code);
                this.setLoginStatus(LoginConstant.LOGIN_STATUS_FAILED);
            }

            mLoginListener.onLoginStatusChanged(this.getLoginStatus(), userId, token);
            this.getWorker().postDelay(new LoginPingTask(this));

        }catch (InvalidProtocolBufferException e) {
            ProtoLogger.Log("LoginMgr.onLoginByUidRes, parse msg failed, %s", e.getLocalizedMessage());
        }
    }

    private void onPingRes(byte[] data) {
        try {
            LoginProtoBean.PLoginPingRes resp = LoginProtoBean.PLoginPingRes.parseFrom(data);
            String userId = resp.getUid();
            long serverTs = resp.getServerts();
            ProtoInfo.GetInstance().setServerTimestamp(serverTs);
            ProtoLogger.Log("LoginMgr.onPingRes, uid: %s, serverts: %l", userId, serverTs);
        }catch (InvalidProtocolBufferException e) {
            ProtoLogger.Log("LoginMgr.onPingRes, parse msg failed, %s", e.getLocalizedMessage());
        }
    }

    private void onKickOff(byte[] data) {
        try {
            LoginProtoBean.PLoginKickOff resp = LoginProtoBean.PLoginKickOff.parseFrom(data);
            String userId = resp.getUid();
            String reason = resp.getReason();
            if (TextUtils.equals(userId, ProtoInfo.GetInstance().getUserId())) {
                ProtoLogger.Log("LoginMgr.onKickOff, kick off,  uid: %s, reason: %s", userId, reason);
                mLoginListener.onLoginStatusChanged(LoginConstant.LOGIN_STATUS_KICKOFF, userId, ProtoInfo.GetInstance().getUserToken());
            }else {
                ProtoLogger.Log("LoginMgr.onKickOff, WTF! uid: %s", userId);
            }
        }catch (InvalidProtocolBufferException e) {
            ProtoLogger.Log("LoginMgr.onKickOff, parse msg failed, %s", e.getLocalizedMessage());
        }
    }
}