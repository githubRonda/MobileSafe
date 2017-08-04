package com.ronda.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ProviderInfo;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.telecom.Call;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.ronda.mobilesafe.db.dao.BlackNumberDao;
import com.socks.library.KLog;

import java.lang.reflect.Method;
import java.security.Provider;

/**
 * 短信拦截与电话拦截的服务
 */
public class CallSafeService extends Service {

    private BlackNumberDao mDao;

    private SMSReceiver mSMSReceiver;

    private TelephonyManager mTelephonyManager;
    private MyPhoneStateListener mPhoneStateListener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mDao = new BlackNumberDao(this);
        mSMSReceiver = new SMSReceiver();

        // 获取系统的电话服务
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneStateListener = new MyPhoneStateListener();
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE); // 监听 LISTEN_CALL_STATE 状态，就会回调MyPhoneStateListener#onCallStateChanged()


        // 初始化短信的广播
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(mSMSReceiver, filter);

        KLog.w("onCreate --> CallSafeService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mSMSReceiver);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    /**
     * 监听短信的广播，用于拦截短信
     */
    private class SMSReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Object[] objects = (Object[]) intent.getExtras().get("pdus");

            for (Object object : objects) {
                SmsMessage message = SmsMessage.createFromPdu((byte[]) object);
                String originatingAddress = message.getOriginatingAddress();// 短信来源号码
                String messageBody = message.getMessageBody();// 短信内容

                //通过短信的电话号码查询拦截的模式
                String mode = mDao.findModeByNumber(originatingAddress);

                /**
                 * 黑名单拦截模式
                 * 1 全部拦截 电话拦截 + 短信拦截
                 * 2 电话拦截
                 * 3 短信拦截
                 */
                if ("1".equals(mode) || "3".equals(mode)) {
                    abortBroadcast(); // 终止广播的传递
                    KLog.e("终止广播的传递 --> 号码拦截");
                }

                //智能拦截模式 发票  你的头发漂亮 (技术：分词 + 庖丁解牛)
                if (messageBody.contains("fapiao")) {
                    abortBroadcast();

                    KLog.e("终止广播的传递 --> 智能拦截");
                }
            }
        }
    }

    private class MyPhoneStateListener extends PhoneStateListener {

        //电话状态改变的监听

        /**
         * state 有下面三种状态：
         * TelephonyManager#CALL_STATE_IDLE (空闲)
         * TelephonyManager#CALL_STATE_RINGING (响铃)
         * TelephonyManager#CALL_STATE_OFFHOOK (摘机状态，电话机拿起听筒或按免提)
         * <p>
         * incomingNumber: 表示来电的号码
         */
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                //电话铃响的状态
                case TelephonyManager.CALL_STATE_RINGING:

                    String mode = mDao.findModeByNumber(incomingNumber);
                    if ("1".equals(mode) || "2".equals(mode)) {
                        KLog.e("拦截黑名单电话");

                        // 要先注册观察者，然后再挂断电话。因为挂断电话时才会在通话记录中新增数据
                        // 该uri可以通过查询系统源码中的 contactsprovider 中的CallLogProvider.java可知
                        Uri uri = Uri.parse("content://call_log/calls");
                        getContentResolver().registerContentObserver(uri, true, new MyContentObserver(new Handler(), incomingNumber)); // 参数2：true,表示 匹配派生的Uri,false表示精确匹配

                        //挂断电话
                        endCall();
                    }
                    break;
            }
        }
    }

    /**
     * 挂断电话
     * 需要权限：android.permission.CALL_PHONE
     */
    private void endCall() {
        // ServiceManager.getService(Context.TELEPHONY_SERVICE)
        try {
            //通过类加载器加载ServiceManager的字节码
            Class<?> clazz = getClassLoader().loadClass("android.os.ServiceManager");
            //通过反射得到当前的方法
            Method method = clazz.getDeclaredMethod("getService", String.class);

            //调用方法得到远程服务代理类
            IBinder iBinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
            //获取到原生未经包装的系统电话的管理服务(高版本的ITelephony.aidl这个文件依赖的文件有很多，而低版本的依赖的只需NeighboringCellInfo.aidl即可)
            ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);

            iTelephony.endCall();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 观察者类
     */
    private class MyContentObserver extends ContentObserver {

        private String incomingNumber;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MyContentObserver(Handler handler, String incomingNumber) {
            super(handler);
            this.incomingNumber = incomingNumber;
        }

        //当数据改变的时候调用的方法
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            // 当观察到变化后， 要先注销观察者，然后删除新增的数据。否则删除数据时又会触发该观察者
            getContentResolver().unregisterContentObserver(this);

            deleteCallLog(incomingNumber);
        }
    }

    /**
     * 删除通话记录
     * 需要权限： android.permission.READ_CALL_LOG，android.permission.WRITE_CALL_LOG
     * @param incomingNumber
     */
    private void deleteCallLog(String incomingNumber) {
        // 其实  CallLog.AUTHORITY == "call_log" , CallLog.Calls.NUMBER = "number"
        Uri uri = Uri.parse("content://call_log/calls");
        getContentResolver().delete(uri, "number = ? ", new String[]{incomingNumber}); // 这里是删除了所有该黑名单的记录，包括该号码未加入黑名单前的通话记录
    }
}
