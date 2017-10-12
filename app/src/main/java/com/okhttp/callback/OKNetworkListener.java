package com.okhttp.callback;

/**
 * Created by hxg on 16/2/18.
 */
public interface OKNetworkListener<T> {
    void onNetworkReceived(T received);

    void onNetworkError(Exception e);
}
