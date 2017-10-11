package com.smart.android.smartandroid.protolink;

/**
 * Created by liujia on 17/1/18.
 */

public enum ProtoInfo {
    instance;

    private String mUserId;
    private String mUserToken;

    private String mPassport;
    private String mPassword;

    private String mDevId;

    private long mClientTimestamp = System.currentTimeMillis();
    private long mServerTimestamp = System.currentTimeMillis();

    static public ProtoInfo GetInstance() {
        return instance;
    }

    public void setUserId(String uid) {
        mUserId = uid;
    }

    public void setUserToken(String token) {
        mUserToken = token;
    }

    public void setPassport(String passport) {
        mPassport = passport;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public void setDeviceId(String devId) {
        mDevId = devId;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getUserToken() {
        return mUserToken;
    }

    public String getPassport() {
        return mPassport;
    }

    public String getPassword() {
        return mPassword;
    }

    //liujia: 要做时间修正
    public long getProtoTime() {
        return System.currentTimeMillis();
    }

    public void setServerTimestamp(long ts) {
        mServerTimestamp = ts;
    }

    public long getRTT() {
        return mClientTimestamp - mServerTimestamp;
    }
}
