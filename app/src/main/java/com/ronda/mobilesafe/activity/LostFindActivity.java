package com.ronda.mobilesafe.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ronda.mobilesafe.R;

/**
 * 手机防盗页
 */
public class LostFindActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;

    private TextView mTvSafePhone;
    private ImageView mIvProtect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mPreferences = getSharedPreferences("config", MODE_PRIVATE);
        if (mPreferences.getBoolean("configed", false)) { // 判断是否进入过向导
            setContentView(R.layout.activity_lost_find);

            mTvSafePhone = (TextView) findViewById(R.id.tv_safe_phone);
            mIvProtect = (ImageView) findViewById(R.id.iv_protect);

            mTvSafePhone.setText(mPreferences.getString("safe_phone", null));
            mIvProtect.setImageResource(mPreferences.getBoolean("protect", false) ? R.drawable.lock : R.drawable.unlock);


        } else {
            //跳转至向导页
            startActivity(new Intent(this, Setup1Activity.class));
            finish();
        }
    }


    /**
     * 重新进入向导页
     *
     * @param v
     */
    public void reEnter(View v) {
        startActivity(new Intent(this, Setup1Activity.class));
        finish();
    }
}
