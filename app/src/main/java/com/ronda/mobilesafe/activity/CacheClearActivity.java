package com.ronda.mobilesafe.activity;

import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ronda.mobilesafe.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/09/04
 * Version: v1.0
 * <p>
 * 缓存清理界面
 */
@ContentView(R.layout.activity_cache_clear)
public class CacheClearActivity extends AppCompatActivity {

    @ViewInject(R.id.btn_clear)
    Button btnClear;
    @ViewInject(R.id.pb)
    ProgressBar pb;
    @ViewInject(R.id.tv_name)
    TextView tvName;
    @ViewInject(R.id.ll_add)
    LinearLayout llAdd;

    private static final int SCANNING = 100; //正在扫描app
    private static final int SCAN_FINISHED = 101; //扫描结束
    private static final int FOUND_CACHE_APP = 102; //发现有缓存的应用
    private static final int CLEAR_ALL_CACHE = 103; //一键清理完成


    private Thread mScanThread;
    private PackageManager mPackageManager;
    private int mProgress;

    private Random mRandom = new Random();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCANNING:
                    tvName.setText((String) msg.obj);
                    break;
                case SCAN_FINISHED:
                    tvName.setText("扫描完成");
                    break;
                case FOUND_CACHE_APP:
                    addItem(((CacheInfo) msg.obj));
                    break;
                case CLEAR_ALL_CACHE:
                    llAdd.removeAllViews();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);

        mPackageManager = getPackageManager();

        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScanThread != null) {
            mScanThread.interrupt();
            mScanThread = null;
        }
    }

    /**
     * 向界面中的LinearLayout中头部出入一个有缓存的item
     * @param cacheInfo
     */
    private void addItem(final CacheInfo cacheInfo) {

        View view = LayoutInflater.from(this).inflate(R.layout.item_cache, null);
        ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
        TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
        TextView tv_memory_info = (TextView) view.findViewById(R.id.tv_memory_info);
        ImageView iv_delete = (ImageView) view.findViewById(R.id.iv_delete);

        iv_icon.setImageDrawable(cacheInfo.icon);
        tv_name.setText(cacheInfo.appName);
        tv_memory_info.setText(Formatter.formatFileSize(this, cacheInfo.cacheSize));

        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * 清理单个应用的缓存
                 *
                 * 思路：
                 * 1. 调用 PackageManager 中的隐藏方法 deleteApplicationCacheFiles() [使用反射]
                 * 2. 该方法需要权限：android.permission.DELETE_CACHE_FILES  但是不幸的是，该权限只适用于 system app(Manifest中会有警告)，无法在user app 中使用
                 * 3. 解决方法：退一步，让我们的程序调启动setting中的应用信息界面，让用户在这个界面中再手动清理缓存(本质上就是让system app 来清除缓存)。那么如何启动应用信息界面呢？可以查看启动该界面时的log信息
                 */
                /*try {
                    Class<?> clazz = Class.forName("android.content.pm.PackageManager");
                    Method method = clazz.getDeclaredMethod("deleteApplicationCacheFiles", String.class, IPackageDataObserver.class);
                    method.invoke(mPackageManager, cacheInfo.packageName, new IPackageDataObserver.Stub(){
                        @Override
                        public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                            //该应用缓存清理完毕后，回调的方法(子线程中运行)
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }*/

                //log日志:{act=android.settings.APPLICATION_DETAILS_SETTINGS dat=package:com.android.settings
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.parse("package:"+cacheInfo.packageName));
                intent.addCategory(Intent.CATEGORY_DEFAULT);//有无没影响
                startActivity(intent);
            }
        });

        llAdd.addView(view, 0);
    }

    /**
     * 遍历手机所有的应用,选择当中有缓存的应用显示在界面列表中
     */
    private void loadData() {
        mScanThread = new Thread() {
            @Override
            public void run() {
                List<PackageInfo> installedPackages = mPackageManager.getInstalledPackages(0);
                pb.setMax(installedPackages.size());//设置最大进度

                for (PackageInfo packageInfo : installedPackages) {
                    //若此界面已销毁，则此线程就不应该在执行了
                    if (mScanThread == null || mScanThread.isInterrupted()) {
                        return;
                    }

                    //发送有缓存的package至handler，然后添加至LinearLayout中显示
                    sendCachePackage(packageInfo.packageName);

                    mProgress++;
                    pb.setProgress(mProgress);

                    try {
                        Thread.sleep(50 + mRandom.nextInt(100));//随机暂停 50~149 秒
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Message message = Message.obtain();
                    message.what = SCANNING;
                    message.obj = packageInfo.applicationInfo.loadLabel(mPackageManager).toString();
                    mHandler.sendMessage(message);
                }

                mHandler.sendEmptyMessage(SCAN_FINISHED);
            }
        };
        mScanThread.start();
    }

    /**
     * 发送带有缓存的package信息至handler中
     *
     * 思路：仿照setting源码中的应用信息的缓存获取方式
     * 1. 调用 PackageManager 中的隐藏方法 getPackageSizeInfo(String packageName, IPackageStatsObserver observer)  [使用反射]
     * 2. 该方法会自动在子线程中回调 IPackageStatsObserver 中的 onGetStatsCompleted 方法，通过形参 PackageStats 就可以获取到该package的缓存大小
     * 3. 调用 getPackageSizeInfo() 方法获取缓存时需要权限：android.permission.GET_PACKAGE_SIZE
     *
     * @param packageName
     */
    private void sendCachePackage(String packageName) {
        IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub() {
            public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {//子线程环境

                long cacheSize = stats.cacheSize;
                if (cacheSize > 0){//说明有缓存，则应该发送至handler，显示在界面中

                    CacheInfo cacheInfo = new CacheInfo();
                    try {
                        cacheInfo.packageName = stats.packageName;
                        //获取应用名称以及图标，本质上都是通过ApplicationInfo来获取,下面两种获取ApplicationInfo的形式都是可以的
                        cacheInfo.appName = mPackageManager.getPackageInfo(stats.packageName, 0).applicationInfo.loadLabel(mPackageManager).toString();
                        cacheInfo.icon = mPackageManager.getApplicationInfo(stats.packageName, 0).loadIcon(mPackageManager);
                        cacheInfo.cacheSize = cacheSize;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    Message message = Message.obtain();
                    message.what = FOUND_CACHE_APP;
                    message.obj = cacheInfo;
                    mHandler.sendMessage(message);
                }
            }
        };

        try {
            Class<?> clazz = Class.forName("android.content.pm.PackageManager");//获取字节码文件
            //public Method[] getMethods()返回某个类的所有公用（public）方法包括其继承类的公用方法，当然也包括它所实现接口的方法。
            //public Method[] getDeclaredMethods()对象表示的类或接口声明的所有方法，包括公共、保护、默认（包）访问和私有方法，但不包括继承的方法。当然也包括它所实现接口的方法。
            Method method = clazz.getDeclaredMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);//获取method对象
            method.invoke(mPackageManager, packageName, mStatsObserver);//执行Method (需要权限:android.permission.GET_PACKAGE_SIZE)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Event(type = View.OnClickListener.class, value = {R.id.btn_clear})
    private void onClick(View v) {
        /**
         * 一键清理缓存：
         * 思路：
         * 1. 调用 PackageManager 中的隐藏方法 freeStorageAndNotify() [使用反射]
         * 2. 该方法需要权限：android.permission.CLEAR_APP_CACHE
         * 3. freeStorageAndNotify(long freeStorageSize, IPackageDataObserver observer)的解释：
         *      1)向系统申请释放存储空间，freeStorageSize 表示申请释放空间的大小，若是当前系统剩余空间大于这个值，则不会清理任何缓存文件；
         *        若是当前系统剩余空间小于这个值，则会清理部分或者所有缓存文件以满足需求。所以这里设置为Long类型的最大值，就会清理所有缓存文件
         *      2) 清理缓存结束时，会在子线程中回调 IPackageDataObserver中的onRemoveCompleted()方法
         */
        try {
            Class<?> clazz = Class.forName("android.content.pm.PackageManager");
            Method method = clazz.getDeclaredMethod("freeStorageAndNotify", long.class, IPackageDataObserver.class);
            method.invoke(mPackageManager, Long.MAX_VALUE,  new IPackageDataObserver.Stub() {
                @Override
                public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {//子线程环境
                    //缓存释放完之后，应该移除掉所有ItemView
                    mHandler.sendEmptyMessage(CLEAR_ALL_CACHE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class CacheInfo{
        public Drawable icon;
        public String appName;
        public String packageName;
        public long cacheSize;
    }
}
