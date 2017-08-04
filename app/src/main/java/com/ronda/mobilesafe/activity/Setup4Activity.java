package com.ronda.mobilesafe.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.ronda.mobilesafe.R;

/**
 * 第四个向导页
 */
public class Setup4Activity extends BaseSetupActivity {

    private CheckBox mCbProtect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup4);

        mCbProtect = (CheckBox) findViewById(R.id.cb_protect);
        
        boolean protect = mPreferences.getBoolean("protect", false);
        if (protect) {
            mCbProtect.setChecked(true);
            mCbProtect.setText("防盗保护已开启");
        } else {
            mCbProtect.setChecked(false);
            mCbProtect.setText("防盗保护没有开启");
        }

        mCbProtect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCbProtect.setText("防盗保护已开启");
                    mPreferences.edit().putBoolean("protect", true).commit(); // 持久化保存
                } else {
                    mCbProtect.setText("防盗保护没有开启");
                    mPreferences.edit().putBoolean("protect", false).commit();
                }
            }
        });
    }

    @Override
    public void showPreviousPage() {
        startActivity(new Intent(this, Setup3Activity.class));
        finish();
        overridePendingTransition(R.anim.previous_trans_in, R.anim.previous_tran_out);
    }

    @Override
    public void showNextPage() {
        mPreferences.edit().putBoolean("configed", true).commit(); //表示已经展示过向导了，下次进来就不需要再展示了

        startActivity(new Intent(this, LostFindActivity.class));
        finish();
        overridePendingTransition(R.anim.next_trans_in, R.anim.next_tran_out);
    }
}
