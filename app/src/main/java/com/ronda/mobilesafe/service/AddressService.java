package com.ronda.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.db.dao.AddressDao;
import com.ronda.mobilesafe.utils.SystemBarUtils;
import com.socks.library.KLog;

/**
 * 来电提醒的服务
 * 注意: 这个服务目前是只有进入app中的设置中心中，手动开启电话归属地显示，才可以启动这个服务。
 * 要想程序一启动就开启这个服务，则可以在SplashActivity中启动这个服务。
 */
public class AddressService extends Service {

    private TelephonyManager mTelephonyManager;
    private MyListener mListener;
    private OutCallReceiver mOutCallReceiver;
    private WindowManager mWM;
    private View mToastView;
    private SharedPreferences mPreferences;
    private int startX;
    private int startY;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPreferences = this.getSharedPreferences("config", Context.MODE_PRIVATE);

        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        mListener = new MyListener();
        /**
         * 参数2：这个events标识会回调 PhoneStateListener#onCallStateChanged()方法。若想监听多个状态，只需把这几个状态相加即可，因为这些状态都刚好是2的n次方. 然后就会回调PhoneStateListener中相应的方法
         *   参数2是与参数1相关，所以这里的值是引用定义在 PhoneStateListener 中的常量，而不是定义在 TelephonyManager
         */
        mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE); //监听来电的状态

        mOutCallReceiver = new OutCallReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        this.registerReceiver(mOutCallReceiver, filter); // 动态注册去电监听的广播
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);

        this.unregisterReceiver(mOutCallReceiver); // 注销广播
    }

    class MyListener extends PhoneStateListener {
        /**
         * state 有下面三种状态：
         * TelephonyManager#CALL_STATE_IDLE (空闲)
         * TelephonyManager#CALL_STATE_RINGING (响铃)
         * TelephonyManager#CALL_STATE_OFFHOOK (挂断)
         * <p>
         * incomingNumber: 表示来电的号码
         */
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            KLog.w("state:" + state + "incomingNumber: " + incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: // 电话铃声响了
                    String address = AddressDao.getAddress(incomingNumber); // 根据来电号码查询归属地
                    //Toast.makeText(AddressService.this, address, Toast.LENGTH_SHORT).show();
                    showToast(address);
                    break;
                case TelephonyManager.CALL_STATE_IDLE: // 电话闲置状态
                    if (mWM != null && mToastView != null) {
                        mWM.removeViewImmediate(mToastView); // 从window中移除view
                        mToastView = null;
                    }
                    break;
            }
        }
    }


    /**
     * 监听去电的广播接受者
     * 需要权限：android.permission.PROCESS_OUTGOING_CALLS
     * 为了和来电监听绑定在一起，则需要在 AddressService 中动态注册和注销 OutCallReceiver
     */
    class OutCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String number = getResultData(); // 获取去电号码. 注意：这个值在平常的时候经常为null
            KLog.w("number : " + number);

            String address = AddressDao.getAddress(number);

            //Toast.makeText(context, address, Toast.LENGTH_SHORT).show();
            showToast(address);
        }
    }


    /**
     * 自定义归属地浮窗显示
     */
    private void showToast(String text) {
        mWM = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.windowAnimations = android.R.style.Animation_Toast;
        //params.type = WindowManager.LayoutParams.TYPE_TOAST; // 若是单纯的触摸交互的话，TYPE_TOAST 和 TYPE_PHONE 都可以的，并且 TYPE_TOAST 更简单，不需要权限
        params.type = WindowManager.LayoutParams.TYPE_PHONE; // 需要权限：android.permission.SYSTEM_ALERT_WINDOW.// 电话窗口。它用于电话交互（特别是呼入）。它置于所有应用程序之上
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 这一个必须要加上，否则点击屏幕上的任何一处都会触发mToastView的onTouchEvent事件.原因就是一旦可以获取焦点就相当于普通的对话框了，一弹出来焦点就聚集在当前这个对话框了，屏幕上的其他地方都暗掉了，触摸整个屏幕就相当于触摸这个对话框
        //| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;// 要变成可触摸
        params.gravity = Gravity.LEFT + Gravity.TOP; // Toast默认是居中的。这里把重心位置设置为左上角
        params.setTitle("Toast");

        int lastX = mPreferences.getInt("lastX", 0);
        int lastY = mPreferences.getInt("lastY", 0);

        // 设置浮窗的位置，基于左上方的偏移量
        params.x = lastX;
        params.y = lastY;

        mToastView = LayoutInflater.from(this).inflate(R.layout.toast_address, null);

        int styleIndex = mPreferences.getInt("address_style_index", 0);
        int[] toastBgs = {R.drawable.call_locate_white, R.drawable.call_locate_orange,
                R.drawable.call_locate_blue, R.drawable.call_locate_gray, R.drawable.call_locate_green};

        mToastView.setBackgroundResource(toastBgs[styleIndex]);

        TextView tvAddress = (TextView) mToastView.findViewById(R.id.tv_address); // 根据存储的样式的索引值更新背景
        tvAddress.setText(text);
        mWM.addView(mToastView, params);


        final int screenWidth = mWM.getDefaultDisplay().getWidth(); //获取屏幕的宽度
        final int screenHeight = mWM.getDefaultDisplay().getHeight(); // 获取屏幕的高度（包括statusBar，ActionBar，但是不包括NavigationBar）

        mToastView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 初始化起点坐标
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int endX = (int) event.getRawX();
                        int endY = (int) event.getRawY();

                        // 计算移动偏移量
                        int dx = endX - startX;
                        int dy = endY - startY;

                        // 更新浮窗位置
                        params.x += dx;
                        params.y += dy;

                        // 防止坐标偏离屏幕。虽然在来电显示界面中的这个mToastView不会拖拽出屏幕，但是它的params.x和params.y会不断增加或减少
                        if (params.x < 0) {
                            params.x = 0;
                        }

                        if (params.y < 0) {
                            params.y = 0;
                        }

                        if (params.x > screenWidth - mToastView.getWidth()) {
                            params.x = screenWidth - mToastView.getWidth();
                        }
                        if (params.y >= screenHeight - mToastView.getHeight()) { // 这里不需要减去statusbar的高度，因为mToastView就显示在windowManager上，而不是状态栏之下，导航栏之上的区域
                            params.y = screenHeight - mToastView.getHeight();
                        }

                        mWM.updateViewLayout(mToastView, params);

                        // 重新初始化起点坐标
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        SharedPreferences.Editor edit = mPreferences.edit();
                        edit.putInt("lastX", params.x);
                        edit.putInt("lastY", params.y);
                        edit.commit();
                        break;
                }
                return true;
            }
        });
    }
}
