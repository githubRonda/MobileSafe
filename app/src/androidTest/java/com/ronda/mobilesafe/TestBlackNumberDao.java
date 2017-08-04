package com.ronda.mobilesafe;

import android.test.AndroidTestCase;

import com.ronda.mobilesafe.bean.BlackNumberInfo;
import com.ronda.mobilesafe.db.dao.BlackNumberDao;

import java.util.List;
import java.util.Random;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/06/21
 * Version: v1.0
 */
// AndroidTestCase已过时， 被 InstrumentationRegistry 取代. 但是 InstrumentationRegistry 类是被 final 修饰的，不能被继承

public class TestBlackNumberDao extends AndroidTestCase {


    public void testAdd() {
        BlackNumberDao dao = new BlackNumberDao(mContext); // mContext是AndroidTestCase中protected修饰的成员，也可以使用getContext()方法获取
        Random random = new Random();
        for (int i = 0; i < 200; i++) {

            long number = 13300000000L + i;
            dao.add(number + "", String.valueOf(random.nextInt(3) + 1));

        }
    }

    public void testDelete() {
        BlackNumberDao dao = new BlackNumberDao(mContext);
        boolean b = dao.delete("13300000000");
        assertEquals(true, b);
    }

    public void testUpdateMode() {
        BlackNumberDao dao = new BlackNumberDao(mContext);
        boolean b = dao.updateMode("13300000001", "3");
        assertEquals(true, b);
    }

    public void testFindModeByNumber() {
        BlackNumberDao dao = new BlackNumberDao(mContext);

        String mode = dao.findModeByNumber("13300000001");
        assertEquals("3", mode);//System.out.println(mode);
    }

    public void testFindAll() {
        BlackNumberDao dao = new BlackNumberDao(mContext);
        List<BlackNumberInfo> list = dao.findAll();
        System.out.println("list.size(): " + list.size());
        for (BlackNumberInfo blackNumberInfo : list) {
            System.out.println(blackNumberInfo.getNumber());
        }
    }
}
