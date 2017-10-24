package com.smart.android.smartandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.smart.android.smartandroid.jni.JniManager;
import com.smart.android.smartandroid.loginsdk.LoginListener;
import com.smart.android.smartandroid.loginsdk.LoginMgr;
import com.smart.android.smartandroid.util.LogUtil;

public class MainActivity extends AppCompatActivity implements LoginListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(JniManager.GetInstance().stringFromJNI());

        JniManager.GetInstance().NetEngineStart();
        LoginMgr loginMgr = new LoginMgr(this);
        loginMgr.loginByPassword("liujia", "123456", "android");
    }

    public void onLoginStatusChanged(int status, String uid, String token) {
        LogUtil.e("SMARTGO", String.format("status: %d, uid: %s, token: %s", status, uid, token));
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    /*
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("netengine");
    }
    */
}
