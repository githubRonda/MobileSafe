package com.ronda.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/05/22
 * Version: v1.0
 */

public class FocusedTextView extends TextView {
    // java中直接new时，会走此方法
    public FocusedTextView(Context context) {
        super(context);
    }

    // xml中有属性时会走此方法
    public FocusedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // xml中有style样式的话，会走此方法
    public FocusedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    // 有没有获取焦点, 强制返回true，实现跑马灯效果
    @Override
    public boolean isFocused() {
        return true;
    }
}
