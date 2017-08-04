package com.ronda.mobilesafe.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.engine.SmsBackUp;
import com.ronda.mobilesafe.utils.ToastUtils;
import com.socks.library.KLog;

import java.io.File;

/**
 * 高级工具
 * AToolsActivity 其中 A 是 advance 的意思
 */
public class AToolsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atools);
    }

    // 归属地查询按钮
    public void numberAddressQuery(View view) {
        startActivity(new Intent(this, AddressActivity.class));
    }

    // 短信备份
    public void backup(View view) {

        //创建一个带进度条的对话框
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIcon(R.mipmap.ic_launcher);
        //指定进度条的样式为水平
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();

        // 这里我一开始比较疑惑的是子线程中竟然也是可以更新 ProgressDialog 不报错。后来查看源码发现：ProgressDialog的 setMax(), setProgress(), dismiss()等方法都是借助 handler机制的
        new Thread() {
            @Override
            public void run() {
                SmsBackUp.backup(AToolsActivity.this, new SmsBackUp.CallBack() {
                    @Override
                    public void onBefore(final int count) {
                        dialog.setMax(count);
                    }

                    @Override
                    public void onProgress(final int progress) {
                        dialog.setProgress(progress);
                    }

                    @Override
                    public void onFinished() {
                        dialog.dismiss();
                    }
                });
            }
        }.start();
    }

    //常用号码查询
    public void commonnumQuery(View view){
        startActivity(new Intent(this, CommonNumActivity.class));
    }
}
