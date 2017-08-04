package com.ronda.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ronda.mobilesafe.bean.BlackNumberInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/06/20
 * Version: v1.0
 */

public class BlackNumberDao {
    private BlackNumberOpenHelper helper;

    public BlackNumberDao(Context context) {
        helper = new BlackNumberOpenHelper(context);
    }

    /**
     * @param number 黑名单号码
     * @param mode   拦截模式
     * @return
     */
    public boolean add(String number, String mode) {

        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("number", number);
        contentValues.put("mode", mode);

        long rowId = db.insert("blacknumber", null, contentValues);
        if (rowId == -1) {
            return false;
        }
        return true;
    }

    /**
     * 通过电话号码删除
     *
     * @param number 电话号码
     */
    public boolean delete(String number) {

        SQLiteDatabase db = helper.getWritableDatabase();
        int affectedRows = db.delete("blacknumber", "number = ?", new String[]{number});
        if (affectedRows == 0)
            return false;
        return true;
    }

    /**
     * 通过电话号码修改拦截的模式
     *
     * @param number
     */
    public boolean updateMode(String number, String mode) {

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mode", mode);
        int affectedRows = db.update("blacknumber", values, "number = ?", new String[]{number});
        if (affectedRows == 0)
            return false;
        return true;
    }

    /**
     * 返回一个黑名单号码拦截模式
     *
     * @return
     */
    public String findModeByNumber(String number) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select mode from blacknumber where number = ?", new String[]{number});
        //Cursor c = db.query("blacknumber", new String[]{"mode"}, "number=?", new String[]{number}, null, null, null);
        String mode = null;
        if (cursor.moveToNext()) {
            mode = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return mode;
    }

    /**
     * 查询所有的黑名单
     *
     * @return
     */
    public List<BlackNumberInfo> findAll() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select number, mode from blacknumber", null);
        //db.query("blacknumber", new String[]{"number", "mode"}, null, null, null, null, null);
        List<BlackNumberInfo> blackNumberInfos = new ArrayList<>();
        while (cursor.moveToNext()) {
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
            blackNumberInfo.setNumber(cursor.getString(0));
            blackNumberInfo.setMode(cursor.getString(1));
            blackNumberInfos.add(blackNumberInfo);
        }
        cursor.close();
        db.close();

        return blackNumberInfos;
    }

    /**
     * 分页加载数据
     *
     * @param pageCode 表示当前是哪一页, 即页码
     * @param pageSize 表示每一页有多少条数据
     * @return limit 表示限制当前有多少数据
     * offset 表示跳过 从第几条开始
     * order by _id desc 按 _id 降序排列
     */
    public List<BlackNumberInfo> findPar(int pageCode, int pageSize) {

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select number, mode from blacknumber order by _id desc limit ? offset ?", new String[]{String.valueOf(pageSize), String.valueOf(pageCode * pageSize)});

        List<BlackNumberInfo> blackNumberInfos = new ArrayList<>();
        while (cursor.moveToNext()) {
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
            blackNumberInfo.setNumber(cursor.getString(0));
            blackNumberInfo.setMode(cursor.getString(1));
            blackNumberInfos.add(blackNumberInfo);
        }
        cursor.close();
        db.close();
        return blackNumberInfos;
    }

    /**
     * 分批加载数据
     *
     * @param startIndex 开始的位置
     * @param maxCount   每页展示的最大的条目
     * @return
     */
    public List<BlackNumberInfo> findPar2(int startIndex, int maxCount) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select number, mode from blacknumber order by _id desc limit ? offset ?", new String[]{String.valueOf(maxCount), String.valueOf(startIndex)});
        List<BlackNumberInfo> blackNumberInfos = new ArrayList<>();
        while (cursor.moveToNext()) {
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
            blackNumberInfo.setNumber(cursor.getString(0));
            blackNumberInfo.setMode(cursor.getString(1));
            blackNumberInfos.add(blackNumberInfo);
        }
        cursor.close();
        db.close();
        return blackNumberInfos;
    }

    /**
     * 获取总记录数
     *
     * @return
     */
    public int getTotalRows() {

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from blacknumber", null);
        int rows = 0;
        if (cursor.moveToNext()) {
            rows = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return rows;
    }
}
