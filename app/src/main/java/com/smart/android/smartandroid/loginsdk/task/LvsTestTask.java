package com.smart.android.smartandroid.loginsdk.task;

import com.smart.android.smartandroid.protolink.worker.ProtoTaskRunnable;
import com.smart.android.smartandroid.loginsdk.LoginConstant;
import com.smart.android.smartandroid.loginsdk.LoginMgr;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by liujia on 16/10/5.
 */

/*
* liujia: 还是写点注释吧，不然年老色衰以后绝逼记不得了
* 这个任务就是获取待连接的ip地址，并发起连接（端口号通常是固定的）
* 获取待连接的ip地址有几种方式
* 1）程序写死几个地址，然后连接前ping一遍或者根据上次连接的ip，选择其中一个
* 2）发起一个http请求，由服务器返回一个或者几个ip，然后ping啊或者随机啊或者怎么着，挑一个
* 3）dns解析一个写死的hostname，例如去获取login.yunlaiwu.com的地址，然后用这个ip
* 4）获取lvs server的地址(写死ip或者dns查询)，并且通过rpc或者发送socket网络包给这个地址的方式，获取一个或者一堆ip，相较于2），更安全些
* 我们这里使用3），未来考虑改为2）
*/
public class LvsTestTask implements ProtoTaskRunnable {
    private LoginMgr mLoginMgr;

    public LvsTestTask(LoginMgr mgr){
        this.mLoginMgr = mgr;
    }

    @Override
    public int getTaskId() {
        return LoginConstant.PROTO_TASK_LVSTEST;
    }

    @Override
    public String getTaskName() {
        return "LvsTestTask";
    }

    @Override
    public void run(){
        int status = this.mLoginMgr.getLoginStatus();
        //TODO: log the status, maybe useful I think.... LoginTestLvsTask.run, status = %d
        switch (status) {
            case LoginConstant.LOGIN_STATUS_IDLE:
                break;
            case LoginConstant.LOGIN_STATUS_LOGINING:
                return;
            case LoginConstant.LOGIN_STATUS_LOGINED:
                return;
            case LoginConstant.LOGIN_STATUS_KICKOFF:
                return;
            case LoginConstant.LOGIN_STATUS_CONNECTING:
                break;
            case LoginConstant.LOGIN_STATUS_CONNECTED:
                return;
            case LoginConstant.LOGIN_STATUS_DISCONNECTD:
                break;
            case LoginConstant.LOGIN_STATUS_LOGOFF:
                return;
        }

        //InetAddress[] addresses = resolveLvsIP(LoginConstant.LVS_ADDRESS);
        //InetSocketAddress bestIP = findBestIP(addresses);

        String loginHostIP = LoginConstant.LVS_FALLBACK_IP;
        int loginHostPort = LoginConstant.LVS_PORT;

        //根据测试还是正式，暂且写死一个ip
        int env = mLoginMgr.getEnv();
        if (env == LoginConstant.ENV_DEVELOPMENT) {

        }else if (env == LoginConstant.ENV_RELEASE) {

        }

        if(this.mLoginMgr.getLink().isConnected() || this.mLoginMgr.getLink().isConnecting()){
            //TODO: log here....  LoginTestLvsTask.run, connecting or connected.
        } else {
            //TODO: log here....  LoginTestLvsTask.run, start connecting, ip/port=%s:%d
            this.mLoginMgr.setLoginStatus(LoginConstant.LOGIN_STATUS_CONNECTING);
            this.mLoginMgr.getLink().connect(loginHostIP, "9100");
            //this.mLoginMgr.getLink().connect("192.168.1.38", "9100");
        }
    }

    private InetAddress[] resolveLvsIP(String address) {
        try {
            return InetAddress.getAllByName(address);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private InetSocketAddress findBestIP(InetAddress[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return new InetSocketAddress(LoginConstant.LVS_ADDRESS, LoginConstant.LVS_PORT);
        }
        return new InetSocketAddress(addresses[0], LoginConstant.LVS_PORT);
    }
}
