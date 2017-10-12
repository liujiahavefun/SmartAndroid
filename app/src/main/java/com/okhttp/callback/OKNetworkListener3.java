package com.okhttp.callback;

/**
 * Created by liujia on 16/12/30.
 */

public interface OKNetworkListener3<T1, T2, T3> {
    void onNetworkReceived(T1 ret1, T2 ret2, T3 ret3);
    void onNetworkError(Exception e);
}
