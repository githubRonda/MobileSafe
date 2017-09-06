package com.ronda.mobilesafe.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.ronda.mobilesafe.R;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/09/06
 * Version: v1.0
 */

/**
 * 注意：
 * 1. TabActivity 比较特殊，首先它自己是一个Activity，并且内部还可以装载其他Activity
 * 2. TabActivity 早就过时了，现在几乎就没有使用TabActivity来切换页面了。Activity毕竟是重量级控件，现在一般使用Fragment这个轻量级的控件来切换
 * 3. 这里使用TabActivity主要是考虑到以后工作维护比较老的代码时可能会遇到
 */
public class BaseCacheClearActivity extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_cache_clear);

        //生成选项卡1
        TabHost.TabSpec tab1 = getTabHost().newTabSpec("clear_cache") //这里字符串是一个tag,作用就是唯一标识
                .setContent(new Intent(this, CacheClearActivity.class)) //点击这个选项卡时启动另一个Activity作为内容。参数还可以是viewId
                .setIndicator("缓存清理");//参数还可以是一个自定义的View对象

        //生成选项卡2
        TabHost.TabSpec tab2 = getTabHost().newTabSpec("clear_sd_cache") //这里字符串是一个tag,作用就是唯一标识
                .setContent(new Intent(this, SDCacheClearActivity.class)) //点击这个选项卡时启动另一个Activity作为内容。参数还可以是viewId
                .setIndicator("SD卡清理");

        //默认选中第一个tab
        getTabHost().addTab(tab1);
        getTabHost().addTab(tab2);
        //getTabHost().setCurrentTab(index);
        //getTabHost().setCurrentTabByTag(tag);
    }
}
