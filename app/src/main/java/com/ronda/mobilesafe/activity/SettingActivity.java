package com.ronda.mobilesafe.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.service.AddressService;
import com.ronda.mobilesafe.service.CallSafeService;
import com.ronda.mobilesafe.utils.ServiceStateUtils;
import com.ronda.mobilesafe.view.SettingArrowItem;
import com.ronda.mobilesafe.view.SettingItemView;

public class SettingActivity extends AppCompatActivity {

    private SettingItemView mItemUpdate;
    private SettingItemView mItemAddress;
    private SettingArrowItem mItemAddressStyle;
    private SettingArrowItem mItemAddressLocation;
    private SettingItemView mItemBlackNumber;
    private SettingArrowItem mItemQuickCall;

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mPreferences = getSharedPreferences("config", MODE_PRIVATE);

        initUpdateView();
        initAddressView();
        initAddressStyle();
        initAddressLocation();
        initBlackNumber();
    }

    /**
     * 拦截黑名单设置
     */
    private void initBlackNumber() {
        mItemBlackNumber = (SettingItemView) findViewById(R.id.siv_black_number);

        boolean isRunning = ServiceStateUtils.isRunning(this, CallSafeService.class.getName());
        mItemBlackNumber.setChecked(isRunning);

        mItemBlackNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean checked = !mItemBlackNumber.isChecked();
                mItemBlackNumber.setChecked(checked);
                if (checked) {
                    startService(new Intent(SettingActivity.this, CallSafeService.class));
                } else {
                    stopService(new Intent(SettingActivity.this, CallSafeService.class));
                }
            }
        });
    }

    /**
     * 初始化电话归属地开关
     */
    private void initAddressView() {
        mItemAddress = (SettingItemView) findViewById(R.id.siv_adderss);
        //mItemAddress.setChecked(mPreferences.getBoolean("address", true));

        // 电话归属地显示的服务，不能持久化保存状态，因为可以手动在 设置 --> 应用程序 中关掉服务，或者一键清理也可以结束后台任务，这时就产生了后台AddressService服务已停止，但是设置界面中仍然显示开启的状态，造成状态不同步（这里最合适的应该放在onResume()中，而不是onCreate()）
        // 所以当每次进入该界面时，应该先判断后台AddressService服务的运行状态，然后根据状态设置设置界面上的View的显示
        boolean isRunning = ServiceStateUtils.isRunning(this, AddressService.class.getName()); // 把AddressService的完全限定名传过去
        mItemAddress.setChecked(isRunning);

        mItemAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = !mItemAddress.isChecked();
                mItemAddress.setChecked(b);

                Intent serviceIntent = new Intent(SettingActivity.this, AddressService.class);
                if (b) {
                    startService(serviceIntent); // 开启归属地服务
                } else {
                    stopService(serviceIntent); // 停止归属地服务
                }
            }
        });
    }

    /**
     * 初始化自动更新开关
     */
    private void initUpdateView() {
        mItemUpdate = (SettingItemView) findViewById(R.id.siv_update);

        mItemUpdate.setChecked(mPreferences.getBoolean("auto_update", true));
        mItemUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = !mItemUpdate.isChecked();
                mItemUpdate.setChecked(b);
                mPreferences.edit().putBoolean("auto_update", b).commit();
            }
        });
    }

    String[] items = new String[]{"半透明", "活力橙", "卫视蓝", "金属灰", "苹果绿"};

    /**
     * 修改提示框显示风格
     */
    private void initAddressStyle() {
        mItemAddressStyle = (SettingArrowItem) findViewById(R.id.sai_address_style);
        int styleIndex = mPreferences.getInt("address_style_index", 0); // 读取保存的style索引值
        mItemAddressStyle.setDesc(items[styleIndex]);

        mItemAddressStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSingleChooseDialog();
            }
        });
    }

    /**
     * 弹出选择风格的单选框
     */
    private void showSingleChooseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("归属地提示框风格");

        int styleIndex = mPreferences.getInt("address_style_index", 0); // 读取保存的style索引值
        builder.setSingleChoiceItems(items, styleIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 保存style索引值, 更新组合控件的描述信息, 让对话框消失
                mPreferences.edit().putInt("address_style_index", which).commit();
                mItemAddressStyle.setDesc(items[which]);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /**
     * 修改归属地显示位置
     */
    private void initAddressLocation() {

        mItemAddressLocation = (SettingArrowItem) findViewById(R.id.sai_address_location);

        mItemAddressLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, DragViewActivity.class));
            }
        });

    }
}
