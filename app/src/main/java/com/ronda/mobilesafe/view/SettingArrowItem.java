package com.ronda.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ronda.mobilesafe.R;

/**
 * 自定义的组合View
 * <p>
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/05/23
 * Version: v1.0
 */
public class SettingArrowItem extends RelativeLayout {

    private static final String NAMESPACE = "http://schemas.android.com/apk/res-auto";
    private TextView mTvTitle;
    private TextView mTvDesc;
    private String mTitle;
    private String mDesc;

    public SettingArrowItem(Context context) {
        super(context);
        initView();
    }

    public SettingArrowItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTitle = attrs.getAttributeValue(NAMESPACE, "sai_title");
        mDesc = attrs.getAttributeValue(NAMESPACE, "sai_desc");

        initView();

    }

    public SettingArrowItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.item_setting_arrow, this); // 加载布局，并且添加到RelativeLayout中

        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvDesc = (TextView) findViewById(R.id.tv_desc);

        setTitle(mTitle);
        setDesc(mDesc);
    }

    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    public void setDesc(String desc) {
        mTvDesc.setText(desc);
    }
}
