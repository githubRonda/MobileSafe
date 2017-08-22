package com.ronda.mobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/07/31
 * Version: v1.0
 * <p>
 * 常用号码dao操作
 */

public class CommonNumDao {

    private static final String PATH = "data/data/com.ronda.mobilesafe/files/commonnum.db";


    public List<Group> getGroup() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(PATH, null, SQLiteDatabase.OPEN_READONLY); //只读方式

        Cursor cursor = db.query("classList", new String[]{"name", "idx"}, null, null, null, null, null);
        List<Group> groupList = new ArrayList<>();
        while (cursor.moveToNext()) {
            Group group = new Group();
            group.name = cursor.getString(0);
            group.idx = cursor.getString(1);
            group.childList = getChild(group.idx);

            groupList.add(group);
        }

        cursor.close();
        db.close();

        return groupList;
    }

    public List<Child> getChild(String idx) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(PATH, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.rawQuery("select * from table" + idx, null);

        List<Child> childList = new ArrayList<>();
        while (cursor.moveToNext()) {
            Child child = new Child();
            child._id = cursor.getString(0);
            child.number = cursor.getString(1);
            child.name = cursor.getString(2);

            childList.add(child);
        }

        cursor.close();
        db.close();
        return childList;

    }


    public class Group {
        public String name;
        public String idx;

        public List<Child> childList;
    }

    public class Child {
        public String _id;
        public String number;
        public String name;
    }

}

