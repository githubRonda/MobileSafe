package com.ronda.mobilesafe.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.utils.ToastUtils;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/09/02
 * Version: v1.0
 */
public class EnterPwdActivity extends AppCompatActivity {

    private TextView tvName;
    private ImageView ivIcon;
    private EditText etPassword;
    private Button btnSubmit;
    private String mPackageName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_pwd);

        mPackageName = getIntent().getStringExtra("packageName");

        initView();
    }

    private void initView() {
        tvName = (TextView) findViewById(R.id.tv_app_name);
        ivIcon = (ImageView) findViewById(R.id.iv_app_icon);
        etPassword = (EditText) findViewById(R.id.et_password);
        btnSubmit = (Button) findViewById(R.id.btn_submit);


        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(mPackageName, 0);
            Drawable appIcon = applicationInfo.loadIcon(packageManager);
            String appName = applicationInfo.loadLabel(packageManager).toString();

            tvName.setText(appName);
            ivIcon.setImageDrawable(appIcon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etPassword.getText().toString();
                if ("123".equals(password)) {
                    //解锁，进入应用，并且通知看门狗不要再去监听已解锁的应用(使用广播)
                    Intent intent = new Intent("com.ronda.action.SKIP");
                    intent.putExtra("packageName", mPackageName);
                    sendBroadcast(intent);

                    finish();
                } else {
                    ToastUtils.showToast(getApplicationContext(), "密码错误");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); //结束当前activity

        /* Launcher 的 IntentFilter
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.HOME" />
            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.MONKEY"/>
        </intent-filter>
        */

        //跳转至launcher
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
