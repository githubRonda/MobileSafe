package com.ronda.mobilesafe.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.MenuRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.utils.ToastUtils;
import com.ronda.mobilesafe.view.SettingItemView;
import com.socks.library.KLog;

/**
 * 第二个向导页
 */
public class Setup2Activity extends BaseSetupActivity {

    private SettingItemView mItemBind;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup2);

        mItemBind = (SettingItemView) findViewById(R.id.siv_bind_sim);

        String sim = mPreferences.getString("sim", null);
        if (TextUtils.isEmpty(sim)) {
            mItemBind.setChecked(false);
        } else {
            mItemBind.setChecked(true);
        }


        mItemBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = mItemBind.isChecked();
                if (b) {
                    mItemBind.setChecked(false);
                    mPreferences.edit().remove("sim").commit();
                } else {
                    mItemBind.setChecked(true);
                    // 保存sim序列号信息
                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String simSerialNumber = tm.getSimSerialNumber(); // 需要 READ_PHONE_STATE 权限
                    KLog.e("simSerialNumber: " + simSerialNumber);
                    mPreferences.edit().putString("sim", simSerialNumber).commit();
                }

            }
        });
    }

    @Override
    public void showPreviousPage() {
        startActivity(new Intent(this, Setup1Activity.class));
        finish();
        overridePendingTransition(R.anim.previous_trans_in, R.anim.previous_tran_out);
    }


    @Override
    public void showNextPage() {
        // 如果没有绑定 sim 卡，就不允许进入下一个页面
        if (!mItemBind.isChecked()) {
            Toast.makeText(this, "必须要绑定sim卡", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(new Intent(this, Setup3Activity.class));
        finish();
        overridePendingTransition(R.anim.next_trans_in, R.anim.next_tran_out);
    }
}
