package com.ronda.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/08/28
 * Version: v1.0
 */

public class AppLockDao {

    private Context mContext;
    private static AppLockDao mAppLockDao;
    private final AppLockOpenHelper mAppLockOpenHelper;

    private AppLockDao(Context context) {
        this.mContext = context;
        mAppLockOpenHelper = new AppLockOpenHelper(context);
    }

    public static AppLockDao getInstance(Context context) {

        if (mAppLockDao == null) {
            mAppLockDao = new AppLockDao(context);
        }

        return mAppLockDao;
    }

    //插入
    public void insert(String packageName) {
        SQLiteDatabase db = mAppLockOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("packagename", packageName);

        db.insert("applock", null, values);
        db.close();

        // 使用内容观察解析者通知数据库 数据已改变
        mContext.getContentResolver().notifyChange(Uri.parse("content://com.ronda.mobilesafe/applockdb/change"), null);
    }

    //删除
    public void delete(String packageName) {

        SQLiteDatabase db = mAppLockOpenHelper.getWritableDatabase();
        db.delete("applock", "packagename = ?", new String[]{packageName});
        db.close();

        // 使用内容观察解析者通知数据库 数据已改变
        mContext.getContentResolver().notifyChange(Uri.parse("content://com.ronda.mobilesafe/applockdb/change"), null);
    }

    //查询所有
    public List<String> findAll() {
        SQLiteDatabase db = mAppLockOpenHelper.getReadableDatabase();

        Cursor cursor = db.query("applock", new String[]{"packagename"}, null, null, null, null, null);

        List<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }

        cursor.close();
        db.close();
        return list;
    }

    public boolean find(String packageName) {
        boolean result;
        SQLiteDatabase db = mAppLockOpenHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from applock where packagename = ?", new String[]{packageName});

        if (cursor.moveToNext()) {
            result = true;
        } else {
            result = false;
        }

        cursor.close();
        db.close(); //Cannot perform this operation because the connection pool has been closed.
        return result;
    }
}
