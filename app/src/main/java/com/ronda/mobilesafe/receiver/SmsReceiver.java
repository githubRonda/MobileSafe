package com.ronda.mobilesafe.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.service.LocationService;
import com.socks.library.KLog;

import java.util.Date;

/**
 * 拦截短信
 * 众所周知Android在4.4上增加了不少安全措施，除了把SELinux设置为enforce外，在短信方向也加强了限制。
 * 4.4之后，新增了一个default sms的机制
 * 简而言之，就是如果要在4.4之后实现短信拦截功能，就必须成为default sms，把所有短信相关的功能都包揽了，然后再做短信拦截。
 * 但这种做法，适配性和兼容性的工作是非常巨大的，短信、wapush（多种）、彩信、单双卡等等，相当于要求短信拦截类的软件要集成一个功能非常完善的通讯录类应用的功能。
 * <p>
 * 4.4以上有两种短信广播，一种必须成为系统短信应用才能收到 另一种只要声明权限即可 android.provider.Telephony.SMS_DELIVER  默认短信应用才可以收到、阻断 android.provider.Telephony.SMS_RECEIVED
 * 只要注册声明权限即可收到、阻断想要阻断短信通知必须成为系统默认短信应用，并且阻断两种广播才能做到
 * <p>
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/05/28
 * Version: v1.0
 */

public class SmsReceiver extends BroadcastReceiver {

    private SharedPreferences preferences;
    private ComponentName mDeviceAdminSample;
    private DevicePolicyManager mDPM;

    @Override
    public void onReceive(Context context, Intent intent) {


        System.out.println(intent.getExtras().get("pdus")); //[Ljava.lang.Object;@9d3ec850
        System.out.println((intent.getExtras().get("pdus")).getClass().isArray()); // true
        System.out.println((intent.getExtras().get("pdus")) instanceof Object[]); // true
        System.out.println((intent.getExtras().get("pdus")) instanceof byte[]); // false

        // TODO: 2017/5/28/0028  这里的短信接收很奇怪，我在模拟器中测试的现象：当短信过长时，会被拆分成多条，但是 onReceive() 也是被调用多次的。而且此时短信的内容会变掉，很奇怪。而且下面的for循环永远只循环一次。
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");

        // 短信最多140字节, 超出的话,会分为多条短信发送,所以是一个数组,因为我们的短信指令很短,所以for循环只执行一次。// TODO: 2017/5/28/0028 感觉这里的说法是有问题的。 和我测试的现象有区别。
        for (Object pdu : pdus) {
            System.out.println(pdu instanceof byte[]); // true

            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
            String originatingAddress = message.getOriginatingAddress(); // 短信来源号码
            String messageBody = message.getMessageBody(); // 短信内容
            String date = new Date(message.getTimestampMillis()).toLocaleString();//发送时间

            KLog.w(originatingAddress + " : " + messageBody);

            if ("#*alarm*#".equals(messageBody)) {
                // 播放报警音乐, 即使手机调为静音,也能播放音乐, 因为使用的是媒体声音的通道,和铃声无关。 媒体音乐，铃声，以及闹钟是三个相互独立的声音系统
                MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);
                player.setVolume(1f, 1f); // 左右声道
                player.setLooping(true); // 循环播放
                player.start();

                // 中断短信广播的传递, 从而系统短信app就收不到内容了
                // TODO: 2017/5/29/0029 在Android4.4及之后的版本的系统，这里的中断短信的广播传递就无效了，手机还是可以收到短信的，即使在Manifest中注册广播时设置最大优先级也是没有用的
                abortBroadcast();
            } else if ("#*location*#".equals(messageBody)) {
                // 开启定位服务
                context.startService(new Intent(context, LocationService.class));

                preferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
                String location = preferences.getString("location", ""); // 这里获取的其实是上次启动LocationService保存的定位信息。所以我们追踪的时候就要不断发送 #*location*# 短信
                KLog.i("location: " + location);

                abortBroadcast();
            } else if ("#*lockscreen*#".equals(messageBody)) {
                // 获取设备策略服务
                mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                // 设备管理组件
                mDeviceAdminSample = new ComponentName(context, AdminReceiver.class);

                if (mDPM.isAdminActive(mDeviceAdminSample)) { // 判断设备管理器是否已经激活
                    mDPM.lockNow();
                    mDPM.resetPassword("123", 0); // 设置锁屏密码
                } else {
                    Toast.makeText(context, "必须先激活设备管理器", Toast.LENGTH_SHORT).show();
                    activeAdmin(context);
                }
            } else if ("#*wipedata*#".equals(messageBody)) {
                // 获取设备策略服务
                mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                // 设备管理组件
                mDeviceAdminSample = new ComponentName(context, AdminReceiver.class);

                if (mDPM.isAdminActive(mDeviceAdminSample)) {
                    mDPM.wipeData(DevicePolicyManager.WIPE_RESET_PROTECTION_DATA); // 清除数据,恢复出厂设置(不包括SD卡)
                } else {
                    Toast.makeText(context, "必须先激活设备管理器", Toast.LENGTH_SHORT).show();
                    activeAdmin(context);
                }
            }
        }

    }

    // 激活设备管理器
    public void activeAdmin(Context context) {
        mDeviceAdminSample = new ComponentName(context, AdminReceiver.class);

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "哈哈哈, 我们有了超级设备管理器, 好NB!");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
