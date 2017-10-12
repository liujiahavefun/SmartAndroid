package com.okhttp.callback;

/**
 * Created by liujia on 16/12/30.
 */

public interface OKNetworkListener2<T1, T2> {
    void onNetworkReceived(T1 ret1, T2 ret2);
    void onNetworkError(Exception e);
}