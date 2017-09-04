package com.ronda.mobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/09/03
 * Version: v1.0
 */

public class VirusDao {

    public static final String PATH = "data/data/com.ronda.mobilesafe/files/antivirus.db";


    public static List<String> getAllVirus() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(PATH, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.query("datable", new String[]{"md5"}, null, null, null, null, null);

        List<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();
        db.close();

        return list;
    }


    /**
     * 根据签名的md5判断是否是病毒
     *
     * @param md5
     * @return 返回病毒描述, 如果不是病毒, 返回null
     */
    public static String isVirus(String md5) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(PATH, null,
                SQLiteDatabase.OPEN_READONLY);

        Cursor cursor = db.rawQuery("select desc from datable where md5=? ",
                new String[]{md5});

        String desc = null;
        if (cursor.moveToFirst()) {
            desc = cursor.getString(0);
        }

        cursor.close();
        db.close();
        return desc;
    }

}
