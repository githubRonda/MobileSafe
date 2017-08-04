package com.ronda.mobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 归属地数据库的查询操作
 * <p>
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/06/05
 * Version: v1.0
 */

public class AddressDao {


    private static final String PATH = "data/data/com.ronda.mobilesafe/files/address.db";

    public static String getAddress(String number) {
        String address = "未知号码";

        // 注意：openDatabase()方法只能访问data/data目录下的数据库，所以这里的形参path只能是data/data/目录下的数据库，不能是项目中的资源文件，也不能是assets目录下的文件,否则数据库访问不到。所以需要做数据库的拷贝
        SQLiteDatabase database = SQLiteDatabase.openDatabase(PATH, null, SQLiteDatabase.OPEN_READONLY); // 只读的方式打开数据库

        // 手机号码的特点：1 + (3,4,5,6,7,8) + 后面的9位数字
        // 正则表达式：^1[3-8]\d{9}$
        if (number.matches("^1[3-8]\\d{9}")) {// 如果是手机号码，则查询数据库

            Cursor cursor = database.rawQuery("SELECT location FROM data2 WHERE id = (SELECT outkey FROM data1 WHERE id = ?)", new String[]{number.substring(0, 7)});
            if (cursor.moveToNext()) { // cursor 的结果集就只有一行，所以使用if，而非while
                address = cursor.getString(0);
            }
            cursor.close();
        } else if (number.matches("^\\d+$")) { // 匹配数字
            switch (number.length()) {
                case 3://若是比较正式的话，需要查询报警电话的数据库。下面的判断也是一样
                    address = "报警电话";
                    break;
                case 4:
                    address = "模拟器";
                    break;
                case 5:
                    address = "客服电话";
                    break;
                case 7:
                case 8:
                    address = "本地电话";
                    break;
                default:
                    // 01088881234
                    // 048388888888
                    if (number.startsWith("0") && number.length() > 10) { // 可能是长途电话
                        // 有些区号是4位，有些区号是3位（包括0）. 为保证查询数据的精确性，则先查询4位，再查询3位.(感觉区号查询不准确，因为同一个区号很可能会对应多个地区，多个电信公司)
                        // 先查4位
                        Cursor cursor = database.rawQuery("SELECT location FROM data2 WHERE area = ?", new String[]{number.substring(1, 4)});
                        if (cursor.moveToNext()) {
                            address = cursor.getString(0);
                        } else {
                            cursor.close();

                            // 查询3位区号
                            cursor = database.rawQuery("SELECT location FROM data2 WHERE area = ?", new String[]{number.substring(1, 3)});
                            if (cursor.moveToNext()) {
                                address = cursor.getString(0);
                            }
                            cursor.close();
                        }
                    }
            }
        }
        database.close();
        return address;
    }
}
