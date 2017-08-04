package com.ronda.mobilesafe.bean;

import com.ronda.mobilesafe.db.dao.AddressDao;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/07/07
 * Version: v1.0
 * <p>
 * 联系人的信息
 */

public class ContactBean {
    private String name; // 姓名
    private String number; // 号码

    private boolean checked; // 是否选中

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }


    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    // 只读的属性
    public String getAddress() {
        return AddressDao.getAddress(number);
    }


    @Override
    public String toString() {
        return "ContactBean{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", address='" + getAddress() + '\'' +
                ", checked=" + checked +
                '}';
    }
}
