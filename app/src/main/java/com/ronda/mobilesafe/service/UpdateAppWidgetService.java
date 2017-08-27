package com.ronda.mobilesafe.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.widget.RemoteViews;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.engine.ProcessInfoProvider;
import com.ronda.mobilesafe.receiver.MyAppWidgetProvider;
import com.socks.library.KLog;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/08/25
 * Version: v1.0
 */

/**
 * 更新Widget的时机：
 * 1. 启动UpdateAppWidgetService时(添加第一个Widget时) 2. 解锁屏的时候
 *
 * 停止更新widget的时机
 * 1. 停止UpdateAppWidgetService时(移除最后一个Widget时) 2. 锁屏的时候
 */
public class UpdateAppWidgetService extends Service {

    private Timer timer;
    private TimerTask timerTask;
    private InnerReceiver mReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //启动定时器
        startTimer();

        //动态注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        mReceiver = new InnerReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();

        //注销广播接收器
        unregisterReceiver(mReceiver);
    }

    // 停止定时器
    private void stopTimer() {
        if (timer != null) {
            timerTask.cancel();
            timer.cancel();

            timerTask = null;
            timer = null;
        }
    }

    //启动定时器
    private void startTimer() {

        if (timer == null) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    updateAppWidget();
                }
            };
            //schedule和scheduleAtFixedRate的区别在于，如果指定开始执行的时间在当前系统运行时间之前，scheduleAtFixedRate会把已经过去的时间也作为周期执行，而schedule不会把过去的时间算上。
            //scheduleAtFixedRate 效率总体上高于schedule
            timer.scheduleAtFixedRate(timerTask, 0, 5000);// 每5s执行一次
        }
    }

    /**
     * 更新widget
     */
    private void updateAppWidget() {

        KLog.d("updateAppWidget");

        //获取AppWidgetManager
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        //获取窗体小部件对应的View，用RemoteViews表示
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.process_widget_provider);

        //设置View
        remoteViews.setTextViewText(R.id.tv_process_count, "进程总数: " + ProcessInfoProvider.getProcessCount(this));
        remoteViews.setTextViewText(R.id.tv_process_memory, "可用内存: " + Formatter.formatFileSize(this, ProcessInfoProvider.getTotalSpace(this)));

        //removeViews添加事件比较特别，只能通过Intent。而且点击事件是不变的，可以放在onCreate中

        //点击窗体小部件进入应用(隐式启动HomeActivity)
        Intent intent = new Intent("android.intent.action.HOME");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.ll_root, pendingIntent);

        //通过 PendingIntent 发送广播, 在广播接收器中杀死进程
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(this, 0, new Intent("com.ronda.action.KILL_PROCESS"), PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_clear, pendingIntent1);


        //通知appWidgetManager更新AppWidget
        ComponentName componentName = new ComponentName(this, MyAppWidgetProvider.class);
        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }

    //接收锁屏和解锁屏的广播. 目的就是锁屏的时候停止widget的更新，节省电量
    class InnerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                startTimer();
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                stopTimer();
            }
        }
    }
}
