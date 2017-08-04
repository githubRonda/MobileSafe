package com.ronda.mobilesafe.utils;

import android.app.ActivityManager;
import android.content.Context;

import com.socks.library.KLog;

import java.util.List;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/06/06
 * Version: v1.0
 */

public class ServiceStateUtils {
    /**
     * 检测服务是否正在运行
     *
     * @param context
     * @param serviceName
     * @return
     */
    public static boolean isRunning(Context context, String serviceName) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(100); // 获取系统所有正在运行的服务,最多返回100个,一般100个也就够了

        KLog.w(serviceName);

        for (ActivityManager.RunningServiceInfo runningService : runningServices) {
            String className = runningService.service.getClassName();// 获取全限定名
            if (className.equals(serviceName)) {
                return true;
            }
        }
        return false;
    }
}
