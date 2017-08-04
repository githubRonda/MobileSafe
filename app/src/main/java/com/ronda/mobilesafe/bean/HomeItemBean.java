package com.ronda.mobilesafe.bean;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/05/22
 * Version: v1.0
 */

public class HomeItemBean {
    private int itemIconId;
    private String itemDesc;

    public HomeItemBean(int itemIconId, String itemDesc) {
        this.itemIconId = itemIconId;
        this.itemDesc = itemDesc;
    }

    public int getItemIconId() {
        return itemIconId;
    }

    public void setItemIconId(int itemIconId) {
        this.itemIconId = itemIconId;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }
}
