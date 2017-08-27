package com.ronda.mobilesafe.receiver;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ronda.mobilesafe.service.UpdateAppWidgetService;
import com.socks.library.KLog;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/08/23
 * Version: v1.0
 */

public class MyAppWidgetProvider extends AppWidgetProvider {

    //每次调用下面的生命周期方法时，onReceive方法都会被调用一次
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        KLog.d("onReceive");
    }

    //创建第一个窗口小部件时调用
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        //启动用于更新widget的服务

        context.startService(new Intent(context, UpdateAppWidgetService.class));
    }

    //每创建一个widget时调用,或者widget更新时,调用onUpdate 更新时间取决于xml中配置的时间,最短为半小时
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        KLog.d("onUpdate");
    }

    //窗口小部件的大小改变时调用
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        KLog.d("onAppWidgetOptionsChanged");
    }

    //每删除一个窗口小部件时调用
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        KLog.d("onDeleted");
    }

    //删除最后一个窗口小部件时调用
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        KLog.d("onDisabled");
    }
}
