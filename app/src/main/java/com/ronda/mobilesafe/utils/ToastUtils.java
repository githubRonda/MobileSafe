package com.ronda.mobilesafe.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/05/25
 * Version: v1.0
 *
 * Toast 只能在主线程中弹出，在子线程中弹出会报错： java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
 * 解决方法（两种）：
 * 1. 判断是否为主线程，若不是，则使用 Looper.prepare() 和 Looper.loop();
 * 2. 判断是否为主线程，若不是，则使用 activity.runOnUIThread() 弹出
 */

public class ToastUtils {
    public static void showToast(Context context, String msg) {
        if ("main".equals(Thread.currentThread().getName())) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        } else {
            Looper.prepare();
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    public static void showToast(final Activity activity, final String msg) {
        if ("main".equals(Thread.currentThread().getName())) {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
