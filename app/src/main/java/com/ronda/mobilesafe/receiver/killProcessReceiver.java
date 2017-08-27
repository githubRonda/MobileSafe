package com.ronda.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ronda.mobilesafe.engine.ProcessInfoProvider;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/08/27
 * Version: v1.0
 */

public class killProcessReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //杀死所有进程
        ProcessInfoProvider.killAll(context);
    }
}
