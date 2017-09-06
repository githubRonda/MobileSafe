package com.ronda.mobilesafe.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.ronda.mobilesafe.bean.AppInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/06/28
 * Version: v1.0
 */

public class AppInfoProvider {
    /**
     * 获取设备上的所有应用程序
     *
     * @param context
     * @return
     */
    public static List<AppInfo> getAppInfos(Context context) {

        ArrayList<AppInfo> appInfos = new ArrayList<>();

        // 获取PackageManager
        PackageManager packageManager = context.getPackageManager();
        //获取到安装包
        List<PackageInfo> installedPackageInfoList = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : installedPackageInfoList) {
            AppInfo appInfo = new AppInfo();
            //获取到应用程序的图标
            Drawable drawable = packageInfo.applicationInfo.loadIcon(packageManager); // applicationInfo 指的就是Manifest中的Application节点
            //获取到应用程序的名字 + 用户id（uid是安装应用程序的时候，操作系统为每一个应用赋值的唯一标识）
            String apkName = packageInfo.applicationInfo.loadLabel(packageManager).toString() + packageInfo.applicationInfo.uid;
            //获取到应用程序的包名
            String packageName = packageInfo.packageName;
            //获取到apk资源的路径
            String sourceDir = packageInfo.applicationInfo.sourceDir;
            //apk的大小
            long apkSize = new File(sourceDir).length();

            appInfo.setIcon(drawable);
            appInfo.setApkName(apkName);
            appInfo.setApkPackageName(packageName);
            appInfo.setApkSize(apkSize);

            //获取到安装应用程序的标记
            int flags = packageInfo.applicationInfo.flags;
            // 系统程序安装在system/data目录下，而用户程序安装在data/data目录下

            if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0) {//表示系统app
                appInfo.setUserApp(false);
            } else { //表示用户app
                appInfo.setUserApp(true);
            }

            if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {//表示在sd卡
                appInfo.setRom(false);
            } else {//表示在内部存储
                appInfo.setRom(true);
            }

            //KLog.i(appInfo.toString());
            appInfos.add(appInfo);
        }
        return appInfos;
    }
}
