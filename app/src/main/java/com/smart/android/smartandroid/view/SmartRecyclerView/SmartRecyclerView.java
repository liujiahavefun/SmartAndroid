package com.smart.android.smartandroid.view.SmartRecyclerView;

/**
 * Created by liujia on 16/8/24.
 */

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.widget.FrameLayout;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smart.android.smartandroid.R;
import com.smart.android.smartandroid.view.SmartRecyclerView.Adapter.RecyclerArrayAdapter;

import java.util.Arrays;


public class SmartRecyclerView extends FrameLayout {
    public static final String TAG = "SmartRecyclerView";
    public static boolean DEBUG = false;
    protected RecyclerView mRecycler;
    protected ViewGroup mProgressView;
    protected ViewGroup mEmptyView;
    protected ViewGroup mErrorView;
    private int mProgressId;
    private int mEmptyId;
    private int mErrorId;

    protected boolean mClipToPadding;
    protected int mPadding;
    protected int mPaddingTop;
    protected int mPaddingBottom;
    protected int mPaddingLeft;
    protected int mPaddingRight;
    protected int mScrollbarStyle;
    protected int mScrollbar;

    protected float mScrollYPosition = 0;

    protected RecyclerView.OnScrollListener mInternalOnScrollListener;
    protected RecyclerView.OnScrollListener mExternalOnScrollListener;

    protected SwipeRefreshLayout mPtrLayout;
    protected boolean mEnableRefresh = false;
    protected android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener mRefreshListener;

    public SwipeRefreshLayout getSwipeToRefresh() {
        return mPtrLayout;
    }

    public RecyclerView getRecyclerView() {
        return mRecycler;
    }

    public SmartRecyclerView(Context context) {
        super(context);
        initView();
    }

    public SmartRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initView();
    }

    public SmartRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(attrs);
        initView();
    }

    protected void initAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.smart_recycler_view_style);
        try {
            mClipToPadding = a.getBoolean(R.styleable.smart_recycler_view_style_recyclerClipToPadding, false);
            mPadding = (int) a.getDimension(R.styleable.smart_recycler_view_style_recyclerPadding, -1.0f);
            mPaddingTop = (int) a.getDimension(R.styleable.smart_recycler_view_style_recyclerPaddingTop, 0.0f);
            mPaddingBottom = (int) a.getDimension(R.styleable.smart_recycler_view_style_recyclerPaddingBottom, 0.0f);
            mPaddingLeft = (int) a.getDimension(R.styleable.smart_recycler_view_style_recyclerPaddingLeft, 0.0f);
            mPaddingRight = (int) a.getDimension(R.styleable.smart_recycler_view_style_recyclerPaddingRight, 0.0f);
            mScrollbarStyle = a.getInteger(R.styleable.smart_recycler_view_style_scrollbarStyle, -1);
            mScrollbar = a.getInteger(R.styleable.smart_recycler_view_style_scrollbars, -1);

            mEmptyId = a.getResourceId(R.styleable.smart_recycler_view_style_layout_empty, 0);
            mProgressId = a.getResourceId(R.styleable.smart_recycler_view_style_layout_progress, 0);
            mErrorId = a.getResourceId(R.styleable.smart_recycler_view_style_layout_error, 0);
        } finally {
            a.recycle();
        }
    }

    private void initView() {
        //liujia: how to trigger EDIT-MODE?
        if (isInEditMode()) {
            return;
        }

        //生成主View，主view是SwipeRefreshLayout包起来的FrameLayout，里面有recycleView和 progressView emptyView errorView
        View v = LayoutInflater.from(getContext()).inflate(R.layout.smart_recycler_view, this);
        mPtrLayout = (SwipeRefreshLayout) v.findViewById(R.id.ptr_layout);
        mPtrLayout.setEnabled(false);
        mPtrLayout.setDistanceToTriggerSync(100);

        mProgressView = (ViewGroup) v.findViewById(R.id.progress);
        if (mProgressId != 0) LayoutInflater.from(getContext()).inflate(mProgressId, mProgressView);
        mEmptyView = (ViewGroup) v.findViewById(R.id.empty);
        if (mEmptyId != 0) LayoutInflater.from(getContext()).inflate(mEmptyId, mEmptyView);
        mErrorView = (ViewGroup) v.findViewById(R.id.error);
        if (mErrorId != 0) LayoutInflater.from(getContext()).inflate(mErrorId, mErrorView);
        initRecyclerView(v);
    }

    /**
     * Implement this method to customize the AbsListView
     */
    protected void initRecyclerView(View view) {
        mRecycler = (RecyclerView) view.findViewById(android.R.id.list);
        setItemAnimator(null);

        if (mRecycler != null) {
            mRecycler.setHasFixedSize(true);
            mRecycler.setClipToPadding(mClipToPadding);

            mInternalOnScrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (mExternalOnScrollListener != null) {
                        mExternalOnScrollListener.onScrolled(recyclerView, dx, dy);
                    }

                    int topRowVerticalPosition = (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                    if (mEnableRefresh) {
                        mPtrLayout.setEnabled(topRowVerticalPosition >= 0);
                    }
                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (mExternalOnScrollListener != null)
                        mExternalOnScrollListener.onScrollStateChanged(recyclerView, newState);

                }
            };
            mRecycler.addOnScrollListener(mInternalOnScrollListener);

            mRecycler.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    /*
                    *只有到顶部后向下滑动才出发刷新
                    */
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (Math.abs(mScrollYPosition) > 1.0f && event.getY() - mScrollYPosition > 10.0f) {
                            if (!isScrollToTop()) {
                                mPtrLayout.setEnabled(false);
                            } //else {
                            //setRefreshing(true);//这里不要刷新，刷新应该由SwipeRefreshLayout处理onTouch消息后触发
                            //}
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mScrollYPosition = event.getY();
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        mScrollYPosition = 0.0f;
                    }

                    return false;
                }
            });

            if (mPadding != -1.0f) {
                mRecycler.setPadding(mPadding, mPadding, mPadding, mPadding);
            } else {
                mRecycler.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
            }

            if (mScrollbarStyle != -1) {
                mRecycler.setScrollBarStyle(mScrollbarStyle);
            }

            switch (mScrollbar) {
                case 0:
                    setVerticalScrollBarEnabled(false);
                    break;
                case 1:
                    setHorizontalScrollBarEnabled(false);
                    break;
                case 2:
                    setVerticalScrollBarEnabled(false);
                    setHorizontalScrollBarEnabled(false);
                    break;
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mPtrLayout.dispatchTouchEvent(ev);
    }

    /**
     * Set the layout manager to the recycler
     *
     * @param manager
     */
    public void setLayoutManager(RecyclerView.LayoutManager manager) {
        mRecycler.setLayoutManager(manager);
    }

    /**
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setRecyclerPadding(int left, int top, int right, int bottom) {
        this.mPaddingLeft = left;
        this.mPaddingTop = top;
        this.mPaddingRight = right;
        this.mPaddingBottom = bottom;
        mRecycler.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
    }

    public void setClipToPadding(boolean isClip) {
        mRecycler.setClipToPadding(isClip);
    }

    /**
     * 下面几个都是设置progress empty error 这三个view的
     */
    public void setEmptyView(View emptyView) {
        mEmptyView.removeAllViews();
        mEmptyView.addView(emptyView);
    }

    public void setProgressView(View progressView) {
        mProgressView.removeAllViews();
        mProgressView.addView(progressView);
    }

    public void setErrorView(View errorView) {
        mErrorView.removeAllViews();
        mErrorView.addView(errorView);
    }

    public void setEmptyView(int emptyView) {
        mEmptyView.removeAllViews();
        LayoutInflater.from(getContext()).inflate(emptyView, mEmptyView);
    }

    public void setProgressView(int progressView) {
        mProgressView.removeAllViews();
        LayoutInflater.from(getContext()).inflate(progressView, mProgressView);
    }

    public void setErrorView(int errorView) {
        mErrorView.removeAllViews();
        LayoutInflater.from(getContext()).inflate(errorView, mErrorView);
    }

    public void setErrorViewOnClickListner(final OnClickListener listner) {
        if (mErrorView.getChildCount() > 0 && listner != null) {
            mErrorView.setOnClickListener(listner);
        }
    }

    public void scrollToPosition(int position) {
        getRecyclerView().scrollToPosition(position);
    }

    /**
     * 是否滑动到顶部
     */
    public boolean isScrollToTop() {
        RecyclerView.LayoutManager manager = mRecycler.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            LinearLayoutManager linearManager = (LinearLayoutManager) manager;
            return linearManager.findFirstCompletelyVisibleItemPosition() == 0;
        } else if (manager instanceof GridLayoutManager) {
            GridLayoutManager gridManager = (GridLayoutManager) manager;
            return gridManager.findFirstCompletelyVisibleItemPosition() == 0;
        } else if (manager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggerGridManager = (StaggeredGridLayoutManager) manager;
            int[] indexs = staggerGridManager.findFirstCompletelyVisibleItemPositions(null);
            return Arrays.asList(indexs).contains(0);
        }
        return false;
    }

    @Override
    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
        mRecycler.setVerticalScrollBarEnabled(verticalScrollBarEnabled);
    }

    @Override
    public void setHorizontalScrollBarEnabled(boolean horizontalScrollBarEnabled) {
        mRecycler.setHorizontalScrollBarEnabled(horizontalScrollBarEnabled);
    }

    /*
    * View的adapter的Observer，监控list数据，是否显示empty view。看下面的setAdapter()方法
    */
    public static class EasyDataObserver extends AdapterDataObserver {
        private SmartRecyclerView recyclerView;

        public EasyDataObserver(SmartRecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            update();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            update();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            update();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            update();
        }

        @Override
        public void onChanged() {
            super.onChanged();
            update();
        }

        //自动更改Container的样式
        private void update() {
            log("update");
            int count;
            if (recyclerView.getAdapter() instanceof RecyclerArrayAdapter) {
                count = ((RecyclerArrayAdapter) recyclerView.getAdapter()).getCount();
            } else {
                count = recyclerView.getAdapter().getItemCount();
            }

            if (count == 0) {
                log("no data:" + "show empty");
                recyclerView.showEmpty();
            } else {
                log("has data");
                recyclerView.showRecycler();
            }
        }
    }

    /**
     * 设置适配器，关闭所有副view。展示recyclerView
     * 适配器有更新，自动关闭所有副view。根据条数判断是否展示EmptyView
     *
     * @param adapter
     */
    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecycler.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new EasyDataObserver(this));
        showRecycler();
    }

    /**
     * 设置适配器，关闭所有副view。展示进度条View
     * 适配器有更新，自动关闭所有副view。根据条数判断是否展示EmptyView
     * <p>
     * liujia: 这个感觉画蛇添足了
     *
     * @param adapter
     */
    public void setAdapterWithProgress(RecyclerView.Adapter adapter) {
        mRecycler.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new EasyDataObserver(this));

        //只有Adapter为空时才显示ProgressView
        if (adapter instanceof RecyclerArrayAdapter) {
            if (((RecyclerArrayAdapter) adapter).getCount() == 0) {
                showProgress();
            } else {
                showRecycler();
            }
        } else {
            if (adapter.getItemCount() == 0) {
                showProgress();
            } else {
                showRecycler();
            }
        }
    }

    /**
     * Remove the adapter from the recycler
     */
    public void clear() {
        mRecycler.setAdapter(null);
    }


    /**
     * 下面一坨都是控制progress empty error 和recycle view的展示的
     */
    private void hideAll() {
        mEmptyView.setVisibility(View.GONE);
        mProgressView.setVisibility(View.GONE);
        mErrorView.setVisibility(GONE);
        mPtrLayout.setRefreshing(false);
        mRecycler.setVisibility(View.INVISIBLE);
    }

    public void showError() {
        log("showError");
        if (mErrorView.getChildCount() > 0) {
            hideAll();
            mErrorView.setVisibility(View.VISIBLE);
        } else {
            showRecycler();
        }

    }

    public void showEmpty() {
        log("showEmpty");
        if (mEmptyView.getChildCount() > 0) {
            hideAll();
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            showRecycler();
        }
    }

    public void showProgress() {
        log("showProgress");
        if (mProgressView.getChildCount() > 0) {
            hideAll();
            mProgressView.setVisibility(View.VISIBLE);
        } else {
            showRecycler();
        }
    }

    public void showRecycler() {
        log("showRecycler");
        hideAll();
        mRecycler.setVisibility(View.VISIBLE);
    }

    /**
     * Set the listener when refresh is triggered and enable the SwipeRefreshLayout
     *
     * @param listener
     */
    public void setRefreshListener(android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener listener) {
        mEnableRefresh = true;
        mPtrLayout.setEnabled(true);
        mPtrLayout.setOnRefreshListener(listener);
        this.mRefreshListener = listener;
    }

    public void setRefreshing(final boolean isRefreshing) {
        mPtrLayout.post(new Runnable() {
            @Override
            public void run() {
                mPtrLayout.setRefreshing(isRefreshing);
            }
        });
    }

    public void setRefreshing(final boolean isRefreshing, final boolean isCallbackListener) {
        mPtrLayout.post(new Runnable() {
            @Override
            public void run() {
                mPtrLayout.setRefreshing(isRefreshing);
                if (isRefreshing && isCallbackListener && mRefreshListener != null) {
                    mRefreshListener.onRefresh();
                }
            }
        });
    }

    public void setEnableRefresh(boolean enabled) {
        mEnableRefresh = enabled;
        mPtrLayout.setEnabled(false);
    }

    /**
     * Set the colors for the SwipeRefreshLayout states
     *
     * @param colRes
     */
    /*
    public void setRefreshingColorResources(@ColorRes int... colRes) {
        mPtrLayout.setColorSchemeResources(colRes);
    }
    */

    /**
     * Set the colors for the SwipeRefreshLayout states
     *
     * @param col
     */
    public void setRefreshingColor(int... col) {
        mPtrLayout.setColorSchemeColors(col);
    }

    /**
     * Set the scroll listener for the recycler
     *
     * @param listener
     */
    public void setOnScrollListener(RecyclerView.OnScrollListener listener) {
        mExternalOnScrollListener = listener;
    }

    /**
     * Add the onItemTouchListener for the recycler
     *
     * @param listener
     */
    public void addOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        mRecycler.addOnItemTouchListener(listener);
    }

    /**
     * Remove the onItemTouchListener for the recycler
     *
     * @param listener
     */
    public void removeOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        mRecycler.removeOnItemTouchListener(listener);
    }

    /**
     * @return the recycler adapter
     */
    public RecyclerView.Adapter getAdapter() {
        return mRecycler.getAdapter();
    }


    public void setOnTouchListener(OnTouchListener listener) {
        mRecycler.setOnTouchListener(listener);
    }

    public void setItemAnimator(RecyclerView.ItemAnimator animator) {
        mRecycler.setItemAnimator(animator);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        mRecycler.addItemDecoration(itemDecoration);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration itemDecoration, int index) {
        mRecycler.addItemDecoration(itemDecoration, index);
    }

    public void removeItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        mRecycler.removeItemDecoration(itemDecoration);
    }

    /**
     * @return inflated error view or null
     */
    public View getErrorView() {
        if (mErrorView.getChildCount() > 0) return mErrorView.getChildAt(0);
        return null;
    }

    /**
     * @return inflated progress view or null
     */
    public View getProgressView() {
        if (mProgressView.getChildCount() > 0) return mProgressView.getChildAt(0);
        return null;
    }

    /**
     * @return inflated empty view or null
     */
    public View getEmptyView() {
        if (mEmptyView.getChildCount() > 0) return mEmptyView.getChildAt(0);
        return null;
    }

    private static void log(String content) {
        if (DEBUG) {
            Log.i(TAG, content);
        }
    }

    public enum EmptyType {
        empty_default,
    }

    private void setEmptyViewImage(int resId) {
        ImageView imageView = (ImageView) findViewById(R.id.empty_hint_image);
        if (imageView != null) {
            imageView.setImageResource(resId);
        }
    }

    private void setEmptyViewTextHint(CharSequence hint) {
        TextView hintTextView = (TextView) findViewById(R.id.empty_hint_text);
        if (hintTextView != null) {
            hintTextView.setText(hint);
        }
    }

    private void setEmptyViewTextHintEx(CharSequence hintEx) {
        TextView hintExTextView = (TextView) findViewById(R.id.empty_hint_text2);
        if (hintExTextView != null) {
            hintExTextView.setText(hintEx);

            if (hintEx instanceof SpannableStringBuilder) {
                hintExTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    private void setEmptyView(int resId, CharSequence hint, CharSequence hintEx) {
        setEmptyViewImage(resId);
        setEmptyViewTextHint(hint);
        setEmptyViewTextHintEx(hintEx);
    }

    private void setEmptyView(int resId, CharSequence hint, CharSequence hintEx, String buttonText, EmptyType type) {
        setEmptyViewImage(resId);
        setEmptyViewTextHint(hint);
        setEmptyViewTextHintEx(hintEx);
        setEmptyButton(buttonText, type);
    }

    private void setEmptyButton(String text, final EmptyType type) {
        /*
        TextView emptyButton = (TextView) findViewById(R.id.empty_button);
        if (emptyButton != null && !TextUtils.isEmpty(text)) {
            emptyButton.setVisibility(VISIBLE);
            emptyButton.setText(text);
            emptyButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (type) {
                        //有些空间面，可能需要按钮。。。并且按钮的行为还不一样，这里处理一下
                    }
                }
            });
        }
        */
    }

    public void setEmptyViewType(EmptyType type) {
        switch (type) {
            case empty_default:
                setEmptyView(R.mipmap.empty_default_view, getResources().getString(R.string.empty_default_hint), getResources().getString(R.string.empty_default_hint_ex));
                break;
            default:
                setEmptyView(R.mipmap.empty_default_view, "", "");
                break;
        }
    }

    public void setEmptyViewType2(EmptyType type, CharSequence msg1, CharSequence msg2) {
        switch (type) {
            default:
                setEmptyView(R.mipmap.empty_default_view, "", "");
                break;
        }
    }
}