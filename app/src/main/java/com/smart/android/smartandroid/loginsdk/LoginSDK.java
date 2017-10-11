package com.smart.android.smartandroid.loginsdk;

/**
 * Created by liujia on 17/1/18.
 */

public class LoginSDK {
    private LoginMgr mLoginMgr;

    public LoginSDK(LoginListener listener) {
        mLoginMgr = new LoginMgr(listener);
    }

    public int setEnv(int env) {
        return mLoginMgr.setEnv(env);
    }

    public int loginByToken(String uid, String token, String dev) {
        return mLoginMgr.loginByToken(uid, token, dev);
    }

    public int loginByPassword(String passport, String password, String dev) {
        return mLoginMgr.loginByPassword(passport, password, dev);
    }
}
