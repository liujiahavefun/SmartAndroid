package com.okhttp.callback;

/**
 * Created by liujia on 16/12/30.
 */

public interface OKNetworkListener4<T1, T2, T3, T4> {
    void onNetworkReceived(T1 ret1, T2 ret2, T3 ret3, T4 ret4);
    void onNetworkError(Exception e);
}