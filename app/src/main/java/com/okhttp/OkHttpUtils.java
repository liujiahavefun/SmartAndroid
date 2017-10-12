package com.okhttp;

import android.os.Handler;
import android.os.Looper;
import android.net.Uri;
import android.text.TextUtils;

import com.okhttp.callback.OkCallBack;
import com.okhttp.request.OkHttpRequest;
import com.smart.android.smartandroid.util.UriHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import org.greenrobot.eventbus.EventBus;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by hxg on 16/2/17.
 */
public class OkHttpUtils {

    private static OkHttpUtils mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mDelivery;

    public static OkHttpUtils getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpUtils.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpUtils();
                }
            }
        }
        return mInstance;
    }

    public String appendUrlParam(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        if (url.contains("?")) {
            if (url.endsWith("&")) {
                return url + getPageParam();
            } else {
                return url + "&" + getPageParam();
            }
        }

        return url + "?" + getPageParam();
    }

    protected String getPageParam() {
        return "from=android";
    }

    static Map<String, String> sanityCharSet = new HashMap<String, String>() {{
        put("|", "%7c");
        put("^", "%5e");
        put("#", "%23");
        put(",", "%2c");
    }};

    private String sanityUrl(String url) {
        for (Map.Entry<String, String> entry : sanityCharSet.entrySet()) {
            url = url.replace(entry.getKey(), entry.getValue());
        }

        return url;
    }

    private OkHttpUtils() {
        mDelivery = new Handler(Looper.getMainLooper());
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        //mOkHttpClient.setCookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));

        EventBus.getDefault().register(this);
    }

    /*
    //当使用SSL且为自生成证书时，用这个试试
    private void OkHttpUtilsSecure() {
        mDelivery = new Handler(Looper.getMainLooper());

        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);

            InputStream certificate = AppEngine.getAppContext().getAssets().open("yunlaiwu.cer");

            int index = 0;
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

            try {
                if (certificate != null) {
                    certificate.close();
                }
            } catch (IOException e) {
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

            X509TrustManager trustManager = Platform.get().trustManager(sslContext.getSocketFactory());

            mOkHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                    .build();

            //facebook Stetho
            //mOkHttpClient.networkInterceptors().add(new StethoInterceptor());

            EventBus.getDefault().register(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    /**
     * 异步的get请求
     *
     * @param url
     * @param callback
     */
    public void getAsyn(String url, Object tag, final OkCallBack callback) {
        String new_url = appendUrlParam(url);
        OkHttpRequest request = new OkHttpRequest(sanityUrl(new_url), tag);
        deliveryResult(callback == null ? OkCallBack.CALLBACK_DEFAULT : callback, request.mRequest);
    }

    /**
     * 异步的post请求
     *
     * @param url
     * @param callback
     * @param params
     */
    public void postAsyn(String url, Object tag, final OkCallBack callback, JSONObject params) {
        String new_url = appendUrlParam(url);
        OkHttpRequest request = new OkHttpRequest(sanityUrl(new_url), tag, params);
        deliveryResult(callback == null ? OkCallBack.CALLBACK_DEFAULT : callback, request.mRequest);
    }

    /**
     * 同步的get请求
     *
     * @param url
     * @param callback
     */
    public void getSync(String url, Object tag, final OkCallBack callback) {
        String new_url = appendUrlParam(url);
        OkHttpRequest request = new OkHttpRequest(sanityUrl(new_url), tag);
        deliveryResultSync(callback == null ? OkCallBack.CALLBACK_DEFAULT : callback, request.mRequest);
    }

    /**
     * 同步的post请求
     *
     * @param url
     * @param callback
     * @param params
     */
    public void postSync(String url, Object tag, final OkCallBack callback, JSONObject params) {
        String new_url = appendUrlParam(url);
        OkHttpRequest request = new OkHttpRequest(sanityUrl(new_url), tag, params);
        deliveryResultSync(callback == null ? OkCallBack.CALLBACK_DEFAULT : callback, request.mRequest);
    }

    /**
     * 异步的下载文件
     *
     * @param url
     * @param fileDir  文件存放目录
     * @param fileName 文件名
     * @param tag
     * @param callback
     */
    public void downloadAsyn(String url, final String fileDir, final String fileName, Object tag, final OkCallBack callback) {
        String new_url = appendUrlParam(url);
        final OkHttpRequest request = new OkHttpRequest(new_url, tag);
        mOkHttpClient.newCall(request.mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedStringCallback(call.request(), e, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    File file = new File(fileDir, fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    sendSuccessResultCallback(new JSONObject("{\"path\":\"" + file.getAbsolutePath() + "\"}"), callback);
                } catch (IOException e) {
                    sendFailedStringCallback(response.request(), e, callback);
                } catch (JSONException e) {
                    e.printStackTrace();
                    sendFailedStringCallback(response.request(), e, callback);
                } finally {
                    try {
                        if (is != null) is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });

    }

    /**
     * 异步上传图片
     *
     * @param url
     * @param uri      图片uri
     * @param tag
     * @param callback
     */
    public void uploadPictureAsyn(String url, final Uri pictureUri, final Object tag, final OkCallBack callback) {
        if (TextUtils.isEmpty(url) || pictureUri == null) {
            return;
        }

        String typeString = UriHelper.getMimeType(pictureUri);
        if (TextUtils.isEmpty(typeString) || (!typeString.equals("image/png") && !typeString.equals("image/jpeg"))) {
            return;
        }

        String new_url = appendUrlParam(url);
        final OkHttpRequest request = new OkHttpRequest(new_url, tag, typeString.equals("image/jpeg") ? OkHttpRequest.JPEGTYPE : OkHttpRequest.PNGTYPE, pictureUri);
        deliveryResult(callback, request.mRequest);
        return;
    }

    private void deliveryResult(final OkCallBack callback, final Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (TextUtils.equals(e.getMessage(), "Canceled")) {
                    return;
                }
                sendFailedStringCallback(request, e, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String str = response.body().string();
                    sendSuccessResultCallback(new JSONObject(str), callback);
                } catch (JSONException e) {
                    sendFailedStringCallback(request, e, callback);
                    e.printStackTrace();
                }
            }
        });

    }

    private void deliveryResultSync(final OkCallBack callback, final Request request) {
        try {
            Response response = mOkHttpClient.newCall(request).execute();
            String str = response.body().string();
            if (callback != null) {
                callback.onResponse(new JSONObject(str));
            }
        } catch (IOException io) {
            if (callback != null)
                callback.onError(request, io);
        } catch (Exception e) {
            if (callback != null)
                callback.onError(request, e);
        }
    }

    private void sendFailedStringCallback(final Request request, final Exception e, final OkCallBack callback) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null)
                    callback.onError(request, e);
            }
        });
    }

    private void sendSuccessResultCallback(final JSONObject result, final OkCallBack callback) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onResponse(result);
                }
            }
        });
    }

    public void cancelTag(Object tag) {
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    public void cancelAll() {
        mOkHttpClient.dispatcher().cancelAll();
    }
}
