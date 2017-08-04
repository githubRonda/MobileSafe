package com.ronda.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ronda.mobilesafe.R;
import com.socks.library.KLog;

/**
 * 第三个向导页
 */
public class Setup3Activity extends BaseSetupActivity {

    private static final int CODE_REQUEST_CONTACT = 0;

    private EditText mEtPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup3);

        mEtPhone = (EditText) findViewById(R.id.et_phone);

        String safe_phone = mPreferences.getString("safe_phone", null);
        mEtPhone.setText(safe_phone);

    }

    // 点击选择联系人按钮
    public void selectContact(View v) {
        startActivityForResult(new Intent(this, ContactActivity.class), CODE_REQUEST_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_REQUEST_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                String phone = data.getStringExtra("phone");
                phone = phone.replaceAll("-", "").replace(" ", "");// 去掉所有的横线和空格
                mEtPhone.setText(phone);
            }
        }
    }

    @Override
    public void showPreviousPage() {
        startActivity(new Intent(this, Setup2Activity.class));
        finish();
        overridePendingTransition(R.anim.previous_trans_in, R.anim.previous_tran_out);
    }

    @Override
    public void showNextPage() {
        String phone = mEtPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "安全号码不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }

        mPreferences.edit().putString("safe_phone", phone).commit();// 保存安全号码

        startActivity(new Intent(this, Setup4Activity.class));
        finish();
        overridePendingTransition(R.anim.next_trans_in, R.anim.next_tran_out);
    }


}
