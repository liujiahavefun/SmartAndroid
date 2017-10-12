package com.smart.android.smartandroid.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import com.anthonycr.grant.PermissionsManager;

import com.okhttp.OkHttpUtils;
import com.smart.android.smartandroid.MainActivity;
import com.smart.android.smartandroid.R;
import com.smart.android.smartandroid.view.uiwidget.LoadingView;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by liujia on 2017/10/12.
 */
public abstract class BaseActivity extends FragmentActivity implements ActivityPageSetting, View.OnClickListener {

    protected Context mContext;
    protected LoadingView mLoadingView;
    protected Handler mUIHandler; //liujia: 很多地方用，refactor不好用，我有点无力回天了，只能找个时间人肉改名了

    protected boolean mAutoShowHint = true;
    protected boolean mIsVisible = false;
    protected boolean mIsActive = false; //当前activity是否是活动的

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        ActivityManager.getInstance().pushActivity(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContent();
        mUIHandler = new Handler();

        ButterKnife.bind(this);

        //统一的加载动画
        mLoadingView = createLoadingView();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(mLoadingView, params);
        showPage(false, "");

        setupView();
        mIsActive = true;

        EventBus.getDefault().register(this);
        setModel();

        mIsActive = true;
    }

    /*
    @Nullable
    @OnClick(R.id.left_icon)
    public void onBack() {
        onBackPressed();
    }
    */

    @Override
    protected void onResume() {
        super.onResume();
        mIsVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsVisible = false;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if (ActivityManager.getInstance().getStackSize() == 1 && !(ActivityManager.getInstance().currentActivity() instanceof MainActivity)) {
            Intent intent = new Intent();
            intent.setClass(mContext, MainActivity.class);
            startActivity(intent);
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mIsActive = false;
        ActivityManager.getInstance().popActivity(this);
        EventBus.getDefault().unregister(this);
        //当前页面销毁,停止网络请求
        OkHttpUtils.getInstance().cancelTag(this);
    }

    protected LoadingView createLoadingView() {
        return new LoadingView(mContext);
    }

    public void showPage(boolean show, String message) {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show) {
                mLoadingView.setMessage(message);
            }
        }
    }

    public void showSwitchPage(final String msg) {
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setMessage(TextUtils.isEmpty(msg) ? "切换中..." : msg);
        mLoadingView.setBg2White();

        //切换中，其它按钮不可点击
        mLoadingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public boolean isLoadingPage() {
        return mLoadingView.getVisibility() == View.VISIBLE;
    }

    protected <T extends View> T getView(int viewId) {
        return (T) this.findViewById(viewId);
    }

    public interface BaseTouchListener {
        void onTouchEvent(MotionEvent event);
    }

    private ArrayList<BaseTouchListener> myTouchListeners = new ArrayList<BaseTouchListener>();

    public void registerBaseTouchListener(BaseTouchListener listener) {
        if (listener != null) {
            myTouchListeners.add(listener);
        }
    }

    public void unregisterBaseTouchListener(BaseTouchListener listener) {
        if (listener != null) {
            myTouchListeners.remove(listener);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (BaseTouchListener listener : myTouchListeners) {
            listener.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void onClick(View view) {
    }

    public String getPageTag() {
        return "-";
    }

    protected boolean isActive() {
        if (this.isFinishing()) {
            return false;
        }
        return mIsActive;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }
}
