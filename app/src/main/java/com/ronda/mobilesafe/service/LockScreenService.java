package com.ronda.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.ronda.mobilesafe.engine.ProcessInfoProvider;
import com.socks.library.KLog;

public class LockScreenService extends Service {

    ScreenOffReceiver mReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        KLog.e("LockScreenService -- > oncreate");

        // 动态注册监听锁屏的广播(无须权限)
        //监听屏幕关闭的广播, 注意,该广播只能在代码中注册,不能在清单文件中注册.因为只有动态注册的广播才可以动态注销掉
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenOffReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }


    /**
     * 用于接收锁屏通知的广播接收器
     */
    class ScreenOffReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            // 清理手机中所有（可以被kill）的进程（自己除外）
            ProcessInfoProvider.killAll(context);
        }
    }
}
