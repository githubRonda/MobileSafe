package com.ronda.mobilesafe.bean;

import android.graphics.drawable.Drawable;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/07/21
 * Version: v1.0
 *
 * 进程信息的JavaBean
 */

public class ProcessInfo {
    private String name; //应用名称
    private Drawable icon; // 应用图标
    private long memSize; // 使用内存数
    private boolean isCheck; // 是否选中
    private boolean isSystem; //是否是系统进程
    private String packageName; //包名，如果进程没有名称，则显示包名

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public long getMemSize() {
        return memSize;
    }

    public void setMemSize(long memSize) {
        this.memSize = memSize;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
