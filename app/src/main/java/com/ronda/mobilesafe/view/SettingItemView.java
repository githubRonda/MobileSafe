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
public class SettingItemView extends RelativeLayout {

    private static final String NAMESPACE = "http://schemas.android.com/apk/res-auto";
    private TextView mTvTitle;
    private TextView mTvDesc;
    private CheckBox mCbStatus;

    private String mTitle;
    private String mDescOff;
    private String mDescOn;

    public SettingItemView(Context context) {
        super(context);
        initView();
    }

    public SettingItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 根据属性名称获取属性的值
        mTitle = attrs.getAttributeValue(NAMESPACE, "title");
        mDescOn = attrs.getAttributeValue(NAMESPACE, "desc_on");
        mDescOff = attrs.getAttributeValue(NAMESPACE, "desc_off");

        initView();
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.item_setting_check, this); // 加载布局，并且添加到RelativeLayout中

        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvDesc = (TextView) findViewById(R.id.tv_desc);
        mCbStatus = (CheckBox) findViewById(R.id.cb_status);

        setTitle(mTitle); // 设置标题
        // 设置desc要放在SettingActivity中，调用 setChecked()，因为desc与CheckBox的选中状态有关,是动态的.
    }

    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    public boolean isChecked() {
        return mCbStatus.isChecked();
    }

    public void setChecked(boolean checked) {
        mCbStatus.setChecked(checked);
        // 根据选中的状态，更新描述
        if (checked) {
            mTvDesc.setText(mDescOn);
        } else {
            mTvDesc.setText(mDescOff);
        }
    }
}
