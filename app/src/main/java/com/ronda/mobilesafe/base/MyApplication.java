package com.ronda.mobilesafe.base;

import android.app.Application;

import com.ronda.mobilesafe.BuildConfig;
import com.socks.library.KLog;

import org.xutils.x;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/05/19
 * Version: v1.0
 */

public class MyApplication extends Application {


    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();

        myApplication = this;

        init();
    }

    private void init() {
        KLog.init(BuildConfig.LOG_DEBUG, "Liu");

        x.Ext.init(this);
        //x.Ext.setDebug(false); //输出debug日志，开启会影响性能
    }

    public static synchronized MyApplication getInstance(){
        return myApplication;
    }
}
