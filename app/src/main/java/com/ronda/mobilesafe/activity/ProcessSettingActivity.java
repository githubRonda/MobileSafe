package com.ronda.mobilesafe.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.service.LockScreenService;
import com.ronda.mobilesafe.utils.AppConst;
import com.ronda.mobilesafe.utils.SPUtils;
import com.ronda.mobilesafe.utils.ServiceStateUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

@ContentView(R.layout.activity_process_setting)
public class ProcessSettingActivity extends AppCompatActivity {


    @ViewInject(R.id.cb_show_sys)
    private CheckBox mCbShowSys;
    @ViewInject(R.id.cb_lock_clear)
    private CheckBox mCbLockClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);


        initView();
    }

    private void initView() {
        // 是否显示系统进程
        //对之前存储过的状态进行回显
        boolean isShow = SPUtils.getBoolean(AppConst.SHOW_SYSTEM, false);
        mCbShowSys.setChecked(isShow);
        if (isShow) {
            mCbShowSys.setText("显示系统进程");
        } else {
            mCbShowSys.setText("隐藏系统进程");
        }


        // 锁屏清理
        // 分析：1. 锁屏清理功能应该独立于本应用程序，即当本程序退出之后，该功能仍然可以存在，所以应该放到服务中。
        // 2. 这个锁屏清理的CheckBox的选中与否应该根据服务是否在后台运行来设定

        boolean isRunning = ServiceStateUtils.isRunning(this, "com.ronda.mobilesafe.service.LockScreenService");
        mCbLockClear.setChecked(isRunning);
        if (isRunning) {
            mCbLockClear.setText("锁屏清理已开启");
        } else {
            mCbLockClear.setText("锁屏清理已关闭");
        }
    }

    @Event(type = CompoundButton.OnCheckedChangeListener.class, value = {R.id.cb_show_sys, R.id.cb_lock_clear})
    private void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_show_sys:
                //保存状态
                SPUtils.putBoolean(AppConst.SHOW_SYSTEM, isChecked);
                //更新View
                if (isChecked) {
                    mCbShowSys.setText("显示系统进程");
                } else {
                    mCbShowSys.setText("隐藏系统进程");
                }
                break;
            case R.id.cb_lock_clear:

                if (isChecked) {
                    startService(new Intent(this, LockScreenService.class)); // 开启服务
                    mCbLockClear.setText("锁屏清理已开启");
                } else {
                    mCbLockClear.setText("锁屏清理已关闭");
                    stopService(new Intent(this, LockScreenService.class)); // 关闭服务
                }
                break;
        }
    }
}
