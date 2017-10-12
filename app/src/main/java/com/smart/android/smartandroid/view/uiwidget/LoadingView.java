package com.smart.android.smartandroid.view.uiwidget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smart.android.smartandroid.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by liujia on 2017/10/12.
 */

public class LoadingView extends LinearLayout {
    @BindView(R.id.loading_message)
    TextView messageTextView;

    @BindView(R.id.loading_progress)
    SmartProgressBar mLoadingProgress;

    @BindView(R.id.loading_content)
    RelativeLayout mContentRel;

    private AnimationDrawable mAnimationDrawable;

    public LoadingView(Context context) {
        super(context);
        View.inflate(context, R.layout.widget_loading_view, this);
        ButterKnife.bind(this);
    }

    public void setMessage(String msg) {
        messageTextView.setText(msg);
    }

    public void setBg2White() {
        mContentRel.setBackgroundColor(Color.WHITE);
    }
}
