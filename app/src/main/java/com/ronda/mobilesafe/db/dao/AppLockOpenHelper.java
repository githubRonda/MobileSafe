package com.ronda.mobilesafe.db.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/08/28
 * Version: v1.0
 */

//SQLiteOpenHelper作用就是辅助创建数据库，创建表，更新表结构/数据库版本
public class AppLockOpenHelper extends SQLiteOpenHelper {

    public AppLockOpenHelper(Context context) {
        super(context, "applock.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建applock表
        db.execSQL("create table applock (_id integer primary key autoincrement, packagename varchar(50))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
