package com.ronda.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.socks.library.KLog;

/**
 * 监听手机开机启动的广播
 * 需要在清单文件中注册，并且需要 RECEIVE_BOOT_COMPLETED 权限
 * <p>
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/05/25
 * Version: v1.0
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences preferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);

        // 只有在防盗保护开启的情况下，才做sim卡判断
        if (preferences.getBoolean("protect", false)) {
            String sim = preferences.getString("sim", null); // 获取之前绑定的sim卡序列号信息
            if (!TextUtils.isEmpty(sim)) {
                // 获取当前手机的sim卡序列号信息，和之前已绑定的进行比对
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                //String curSim = tm.getSimSerialNumber();
                String curSim = tm.getSimSerialNumber() + "111";

                if (sim.equals(curSim)) {
                    KLog.w("手机安全");
                } else {
                    KLog.e("sim卡已改变，发送报警短信！！");
                    String phone = preferences.getString("safe_phone", "");

                    // 发送短信给安全号码
                    SmsManager smsManager = SmsManager.getDefault();
                    /**
                     * sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent)
                     * destinationAddress: 目的地址，即短信的接收方号码
                     * scAddress: 如果为null, 则使用当前默认的 SMSC
                     * text: 发送的内容文本
                     * sentIntent: 发送的Intent，发送成功与否的结果
                     * deliveryIntent: 交付，投递的Intent，接收成功与否的结果
                     */
                    smsManager.sendTextMessage(phone, null, "sim card changed!", null, null);// 模拟器只能识别英文，中文会乱码
                }
            }
        }
    }
}
