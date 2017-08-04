package com.ronda.mobilesafe.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.socks.library.KLog;

/**
 * 获取经纬度坐标的service
 *
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/05/31
 * Version: v1.0
 */

public class LocationService extends Service {

    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    private SharedPreferences preferences;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //List<String> allProviders = locationManager.getAllProviders();
        //System.out.println(allProviders);  //[passive, gps, network]. 表示支持GPS，网络定位。passive比较特殊，直接读取其他应用的定位信息

        myLocationListener = new MyLocationListener();

        // API23及以上需要动态申请定位权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        // TODO: 2017/5/31/0031  LocationManager 的 requestLocationUpdates() 在国内是没有效果的。原因在于：该方法使用的是Google的位置服务，而google的服务在国内是用不了的，并且国内的android系统又进行了各种深度定制。
        // todo 所以该方法注册的 LocationListener 监听器是不会调用的.
        // todo 解决方法：使用第三方的地图，eg:百度地图，高德地图等
        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener); // 使用GPS Provider

        // 提供配置，系统根据用户的配置为用户选择一个最佳的 provider
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(true);// 是否允许付费,比如使用3g网络定位
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
        criteria.setPowerRequirement(Criteria.POWER_LOW); //功耗类型选择
        String bestProvider = locationManager.getBestProvider(criteria, true);// 获取最佳位置提供者。criteria不能填null，否则出现异常
        locationManager.requestLocationUpdates(bestProvider, 0, 0, myLocationListener); // 使用最佳位置提供者
    }

    class MyLocationListener implements LocationListener {

        // 位置发生变化调用
        @Override
        public void onLocationChanged(Location location) {
            System.out.println("onLocationChanged");

            double longitude = location.getLongitude(); // 经度
            double latitude = location.getLatitude(); // 维度
            double altitude = location.getAltitude(); // 海拔
            float accuracy = location.getAccuracy(); // 精确度
            float speed = location.getSpeed(); // 速度
            float bearing = location.getBearing(); // 方向
            KLog.i("经度: " + longitude + ", latitude: " + latitude + ", 海拔:" + altitude + ", accuracy: " + accuracy);

            preferences = getSharedPreferences("config", Context.MODE_PRIVATE);

            preferences
                    .edit()
                    .putString("location", "j:"+location.getLongitude()+"; w:"+location.getLatitude()) // 保存经纬度信息
                    .commit();

            stopSelf(); //当保存完经纬度信息后，停掉service，节省资源
        }

        // 位置提供者状态发生变化. eg:被建筑物挡住时，突然间获取不到位置时
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("onStatusChanged");
        }

        // 用户打开gps
        @Override
        public void onProviderEnabled(String provider) {
            System.out.println("onProviderEnabled");
        }

        // 用户关闭gps
        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("onProviderDisabled");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(myLocationListener); // 停止更新位置，节省电量
    }
}
