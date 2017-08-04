package com.ronda.mobilesafe;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.test.ApplicationTestCase;
import android.util.Log;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
// TODO:  test 单元测试只能测试自己写的代码，所有依赖android.jar相关的代码（eg:org.json包中的相关类），默认情况都会抛出异常。
// todo 解决方法：1. 在build.gradle中使用 testCompile files('libs/xxx.jar') 的形式吧所依赖的包添加进来；2. 直接在androidTest中测试，但是需要运行在真机或模拟器上
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void add() throws Exception{
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    @Test
    public void demo1() throws Exception{

//            JSONArray jsonArray = new JSONArray("[{\"id\":\"1\"},{\"id\":\"4\"}]"); // 这里是解析json字符串
//            JSONObject jsonObject = jsonArray.getJSONObject(0); // 这里表示获取第一个json对象
//            String id = jsonObject.getString("id");
//            System.out.println("id = "+id);

        try {
            JSONArray jsonArray = new JSONArray("[{\"id\":\"1\"},{\"id\":\"4\"}]"); // 这里是解析json字符串

            jsonArray.length(); // 获取长度！！！

            System.out.println(jsonArray.length());
            Log.i("log i " ,"log i");
//            JSONObject jsonObject = jsonArray.getJSONObject(0); // 这里表示获取第一个json对象
//            String id = jsonObject.getString("id");
//            System.out.println("id = "+id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}



