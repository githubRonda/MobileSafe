package com.ronda.mobilesafe.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ronda.mobilesafe.activity.EnterPwdActivity;
import com.ronda.mobilesafe.db.dao.AppLockDao;

import java.util.List;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/09/02
 * Version: v1.0
 */
public class WatchDogService extends Service {
    private boolean isWatch;
    private ActivityManager mActivityManager;
    private AppLockDao mLockDao;
    private List<String> mPackageNameList;
    private String mSkipPackagename;
    private InnerReceiver mReceiver;
    private MyContentObserver mContentObserver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        mLockDao = AppLockDao.getInstance(this);

        //动态注册广播
        mReceiver = new InnerReceiver();
        IntentFilter filter = new IntentFilter("com.ronda.action.SKIP");
        registerReceiver(mReceiver, filter);

        //注册一个内容观察者, 观察数据库的变化,一旦数据有删除或者添加,则需要让mPacknameList重新获取一次数据
        mContentObserver = new MyContentObserver(new Handler());
        getContentResolver().registerContentObserver(Uri.parse("content://com.ronda.mobilesafe/applockdb/change"), true, mContentObserver);

        // 子线程中开启一个可控死循环，用于不断检测现在开启的应用是否是程序锁列表中要去拦截的应用
        isWatch = true;
        startWatch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isWatch = false;

        unregisterReceiver(mReceiver);//注销广播接收器

        getContentResolver().unregisterContentObserver(mContentObserver);// 注销观察者
    }

    //启动子线程循环监听
    private void startWatch() {

        new Thread() {
            @Override
            public void run() {
                //子线程中查询SQLite
                mPackageNameList = mLockDao.findAll();

                //子线程中死循环

                while (isWatch) {
                    // 此方法从api21开始就已过时，原因：会泄露个人信息
                    // 获取正在运行的任务栈(每打开一个程序都会新建一个任务栈，而且同一个程序中的不同activity可能还会处于不同任务栈中（这取决于launchMode是否为singleInstance）)
                    // 因为要获取最新开启的任务栈，所以形参maxNum取1即可。
                    // 需要权限：android.permission.GET_TASKS
                    List<ActivityManager.RunningTaskInfo> runningTasks = mActivityManager.getRunningTasks(100);
                    ComponentName topActivity = runningTasks.get(0).topActivity;//获取最上层的Activity对应的组件名。ComponentName本质上就是四大基本组件的 包名+类名(全限定名) 的描述
                    String packageName = topActivity.getPackageName();

                    //若加锁列表中包含最新打开程序的包名，则弹出拦截界面
                    //这步判断也可以给 mLockDao 添加一个 find(String packageName) 方法，不过当然是没有内存查找效率高，但是更及时，因为这种方法在WatchDogService启动后中途加锁或解锁的程序不会检测到
                    // 这里使用内存查找，虽然是有弊端，但是可以使用内容观察者完善
                    if (mPackageNameList.contains(packageName)) {
                        //if (mLockDao.find(packageName)) {
                        //通过验证的不需要再次拦截
                        if (!packageName.equals(mSkipPackagename)) {
                            // Intent.FLAG_ACTIVITY_NEW_TASK 启动activity并不一定会新建任务栈，这取决于目标activity的taskAffinity属性（如果不指定默认为包名）。
                            // 启动时首先会去寻找taskAffinity属性指定的任务栈，如果没有找到才会新建。如果找到了就会在该任务栈栈内做操作。
                            // 所以，只使用以下方式启动的 EnterPwdActivity 是和 MobileSafe 应用同在一个任务栈中。所以需要在 Manifest 中给 EnterPwdActivity 配置 singleInstance的启动模式
                            // 这样才会重新开启一个任务栈装载 EnterPwdActivity。然后当 EnterPwdActivity finish之后，界面会跳转到加锁列表中的Activity，而不是mobileSafe本身的程序中
                            Intent intent = new Intent(getApplicationContext(), EnterPwdActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("packageName", packageName);
                            startActivity(intent);
                        }
                    }

                    try {
                        Thread.sleep(500); //每隔200毫秒检测一次,节省cpu资源
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }

    /**
     * 通过拦截验证的应用会发送广播到这里
     */
    class InnerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mSkipPackagename = intent.getStringExtra("packageName"); //通过验证的加锁app的包名
        }
    }

    /**
     * 内容观察者，监视加锁app数据库数据有无变化
     */
    class MyContentObserver extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        //一旦数据库发生改变时候调用方法,重新获取包名所在集合的数据
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //子线程中查找
            new Thread() {
                @Override
                public void run() {
                    mPackageNameList = mLockDao.findAll();
                }
            }.start();
        }
    }
}
