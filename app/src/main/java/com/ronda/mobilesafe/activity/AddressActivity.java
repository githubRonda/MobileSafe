package com.ronda.mobilesafe.activity;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.db.dao.AddressDao;

public class AddressActivity extends AppCompatActivity {

    private EditText mEtNumber;
    private TextView mTvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        mEtNumber = (EditText) findViewById(R.id.et_number);
        mTvResult = (TextView) findViewById(R.id.tv_result);

        mEtNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String address = AddressDao.getAddress(s.toString());
                mTvResult.setText(address);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void query(View view) {

        String number = mEtNumber.getText().toString().trim();
        if (!TextUtils.isEmpty(number)) {
            String address = AddressDao.getAddress(number);
            mTvResult.setText(address);
        }else{
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            // 这里的Interpolator是一个接口，getInterpolation()的新参就相当于数学函数中的x, 返回值相当于y,就是一个x对y的函数
            // eg：LinearInterpolator 类的实现y=x; CycleInterpolator 类中的实现就是一个正弦函数。所以插补器的原理其实也很简单，我们自己就可以实现一个
//            shake.setInterpolator(new Interpolator() {
//                @Override
//                public float getInterpolation(float input) {
//                    return 0;
//                }
//            });
            mEtNumber.startAnimation(shake);

            vibrate(); // 手机提示震动
        }
    }

    /**
     * 手机震动
     * 权限:android.permission.VIBRATE
     */
    private void vibrate(){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);// 震动两秒

        /**
         * 参数1：是一个long类型的数组，值依次循环表示，turn off, turn on ...
         * 参数2：表示参数1这个震动组合数组是从哪个索引值开始循环重复震动，eg:若为1，则表示从第二次循环开始只循环1,2,3 1,2,3...。-1表示只执行一次，不循环
         */
        vibrator.vibrate(new long[]{1000, 2000, 1000, 3000}, -1); // 先等待1秒，再震动2s，再等待1秒，再震动3s

        //vibrator.cancel(); // 取消震动
    }
}
