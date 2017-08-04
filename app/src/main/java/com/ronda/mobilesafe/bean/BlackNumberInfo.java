package com.ronda.mobilesafe.bean;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/06/20
 * Version: v1.0
 */

public class BlackNumberInfo {
    /**
     * 黑名单电话号码
     */
    private String number;

    /**
     * 黑名单拦截模式
     * 1 全部拦截 电话拦截 + 短信拦截
     * 2 电话拦截
     * 3 短信拦截
     */
    private String mode;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
