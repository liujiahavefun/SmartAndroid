package com.okhttp.callback;


import org.json.JSONObject;

import okhttp3.Request;

/**
 * Created by hxg on 16/2/17.
 */
public abstract class OkCallBack {

    public abstract void onResponse(JSONObject response);

    public void onError(Request call, Exception e) {
        //Utils.toast("网络连接失败，请检查网络设置");
    }

    public static OkCallBack CALLBACK_DEFAULT = new OkCallBack() {

        @Override
        public void onError(Request call, Exception e) {

        }

        @Override
        public void onResponse(JSONObject response) {

        }
    };
}
