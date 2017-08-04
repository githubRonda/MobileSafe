package com.ronda.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.ronda.mobilesafe.db.dao.AddressDao;
import com.socks.library.KLog;

/**
 * 监听去电的广播接受者
 * 需要权限：android.permission.PROCESS_OUTGOING_CALLS
 * 为了和来电监听绑定在一起，则需要在 AddressService 中动态注册和注销 OutCallReceiver
 */
public class OutCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String number = getResultData(); // 获取去电号码. 注意：这个值在平常的时候经常为null
        KLog.w("number : " + number);

        String address = AddressDao.getAddress(number);

        Toast.makeText(context, address, Toast.LENGTH_SHORT).show();
    }
}
