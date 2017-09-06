package com.ronda.mobilesafe.activity;

import android.net.TrafficStats;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ronda.mobilesafe.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.x;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/09/07
 * Version: v1.0
 *
 * 流量统计界面  traffic: 流，车流。 和流量是一样的，android系统中也是使用 Traffic表示流量
 */
@ContentView(R.layout.activity_traffic)
public class TrafficActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);

        // 每个应用的使用流量在本地文件中会有记录: /proc/uid_stat/ 这个文件夹中有很多以数字命名的文件夹，这个数字就表示应用程序的uid,可以通过 ApplicationInfo.uid 来获取

        // 当然若是要读取文件的话，未免有点麻烦，所以Android API中提供了 TrafficStats 类来快捷的获取流量

        // 手机(2G,3G,4G)下载流量
        long mobileRxBytes = TrafficStats.getMobileRxBytes();
        // 手机(2G,3G,4G)上传流量
        long mobileTxBytes = TrafficStats.getMobileTxBytes();

        //总的下载流量 手机 + wifi
        long totalRxBytes = TrafficStats.getTotalRxBytes();
        //总的上传流量 手机 + wifi
        long totalTxBytes = TrafficStats.getTotalTxBytes();

        // 以上获取流量的方式意义不大，因为真正决定使用流量多少是由运营商来决定的。
        // 所以，应该发送短信给运营商来获取真实的流量使用量， 或者使用 运营商提供的第三方接口来进行流量监听
    }



}
