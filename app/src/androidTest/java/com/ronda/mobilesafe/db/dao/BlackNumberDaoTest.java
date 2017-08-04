package com.ronda.mobilesafe.db.dao;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.ronda.mobilesafe.db.dao.BlackNumberDao;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/06/21
 * Version: v1.0
 */
public class BlackNumberDaoTest {
    @Test
    public void add() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        BlackNumberDao dao = new BlackNumberDao(context);
        boolean b = dao.add("13300000000", "1");
        assertEquals(true, b);
    }

    @Test
    public void delete() throws Exception {

    }

    @Test
    public void updateMode() throws Exception {

    }

    @Test
    public void findModeByNumber() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        BlackNumberDao dao = new BlackNumberDao(context);

        String mode = dao.findModeByNumber("13300000001");
        assertEquals("3", mode);
    }

    @Test
    public void findAll() throws Exception {

    }

    @Test
    public void findPar() throws Exception {

    }

    @Test
    public void findPar2() throws Exception {

    }

    @Test
    public void getTotalRows() throws Exception {

    }

}