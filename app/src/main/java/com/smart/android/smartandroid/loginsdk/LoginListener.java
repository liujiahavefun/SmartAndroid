package com.smart.android.smartandroid.loginsdk;

/**
 * Created by liujia on 17/1/18.
 * 外部调用者用于获知Login状态改变的回调接口
 * 目前就放了一个登录状态改变的回调，其实这里啥都能放，各种乱七八糟的回调都能放到这里，当然不怎么优雅，但是tmd管用啊
 */

public interface LoginListener {
    void onLoginStatusChanged(int status, String uid, String token);
}
