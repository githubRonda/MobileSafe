package com.ronda.mobilesafe.bean;

import android.graphics.drawable.Drawable;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/06/28
 * Version: v1.0
 */

public class AppInfo {
    /**
     * 图片的icon
     */
    private Drawable icon;

    /**
     * 程序的名字
     */
    private String apkName;

    /**
     * 程序大小
     */
    private long apkSize;

    /**
     * 表示到底是用户app还是系统app
     * 如果表示为true 就是用户app
     * 如果是false表示系统app
     */
    private boolean userApp;
    /**
     * 放置的位置
     */
    private boolean isRom;

    /**
     * 包名
     */
    private String apkPackageName;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public long getApkSize() {
        return apkSize;
    }

    public void setApkSize(long apkSize) {
        this.apkSize = apkSize;
    }

    public boolean isUserApp() {
        return userApp;
    }

    public void setUserApp(boolean userApp) {
        this.userApp = userApp;
    }

    public boolean isRom() {
        return isRom;
    }

    public void setRom(boolean rom) {
        isRom = rom;
    }

    public String getApkPackageName() {
        return apkPackageName;
    }

    public void setApkPackageName(String apkPackageName) {
        this.apkPackageName = apkPackageName;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "icon=" + icon +
                ", apkName='" + apkName + '\'' +
                ", apkSize=" + apkSize +
                ", userApp=" + userApp +
                ", isRom=" + isRom +
                ", apkPackageName='" + apkPackageName + '\'' +
                '}';
    }
}
