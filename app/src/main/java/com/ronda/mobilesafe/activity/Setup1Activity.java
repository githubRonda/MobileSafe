package com.ronda.mobilesafe.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ronda.mobilesafe.R;

/**
 * 第一个向导页
 */
public class Setup1Activity extends BaseSetupActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup1);
    }


    @Override
    public void showPreviousPage() {

    }

    @Override
    public void showNextPage() {
        startActivity(new Intent(this, Setup2Activity.class));
        finish();

        overridePendingTransition(R.anim.next_trans_in, R.anim.next_tran_out);
    }
}
