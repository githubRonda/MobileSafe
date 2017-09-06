package com.ronda.mobilesafe.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ronda.mobilesafe.R;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/09/06
 * Version: v1.0
 */
public class SDCacheClearActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sd_cache_clear);
        /**
         * sd卡中是无法确定是哪个文件夹用于缓存的
         * 解决方案：自己收集其他应用在sd卡中的缓存文件夹，形成一个数据库(clearpath.db)导入进来。然后就直接是用java File 相关的API，就可以删除这些文件/文件夹
         * 缺点：得实时更新这个数据库, 而且缓存文件的可变性太大了。
         */
    }
}
