package com.ronda.mobilesafe.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * 设置引导页的基类，不需要再清单文件中注册，因为不需要界面展示
 * <p>
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/05/25
 * Version: v1.0
 */

public abstract class BaseSetupActivity extends AppCompatActivity {

    private GestureDetector mDetector; // 手势检测器

    protected SharedPreferences mPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = getSharedPreferences("config", MODE_PRIVATE);


        // 创建手势检测器
        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            // 监听手势投掷事件，e1,e2分别表示滑动的起点和终点。velocityX和velocityY分别表示水平速度和垂直速度
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // 判断纵向滑动幅度是否过大, 过大的话不允许切换界面
                if (Math.abs(e1.getRawY() - e2.getRawY()) > 100) {
                    Toast.makeText(BaseSetupActivity.this, "不允许这样滑哦！", Toast.LENGTH_SHORT).show();
                    return true;
                }

                // 判断滑动是否过慢
                if (Math.abs(velocityX) < 100) {
                    Toast.makeText(BaseSetupActivity.this, "滑动的太慢了！", Toast.LENGTH_SHORT).show();
                    return true;
                }

                // 向右滑，上一页
                if (e2.getRawX() - e1.getRawX() > 200) {
                    showPreviousPage();
                }

                // 向左滑，下一页
                if (e1.getRawX() - e2.getRawX() > 200) {
                    showNextPage();
                }

                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }


    // 点击上一页按钮
    public void previous(View v) {
        showPreviousPage();
    }

    // 点击下一页按钮
    public void next(View v) {
        showNextPage();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }


    // 需要子类实现的两个方法
    public abstract void showPreviousPage();

    public abstract void showNextPage();
}
