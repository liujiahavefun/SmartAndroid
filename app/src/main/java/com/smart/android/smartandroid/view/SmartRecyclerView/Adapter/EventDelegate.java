package com.smart.android.smartandroid.view.SmartRecyclerView.Adapter;

import android.view.View;

/**
 * Created by liujia on 16/8/24.
 */
public interface EventDelegate {
    void addData(int length);
    void clear();

    void stopLoadMore();
    void pauseLoadMore();
    void resumeLoadMore();

    void setNoMore(View view);
    void setMore(View view, RecyclerArrayAdapter.OnLoadMoreListener listener);
    void setErrorMore(View view);
}