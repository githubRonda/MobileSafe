package com.ronda.mobilesafe.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.db.dao.VirusDao;
import com.ronda.mobilesafe.utils.MD5Utils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/09/03
 * Version: v1.0
 * <p>
 * 手机杀毒页面
 */
@ContentView(R.layout.activity_anit_virus)
public class AnitVirusActivity extends AppCompatActivity {

    @ViewInject(R.id.iv_scanning)
    ImageView ivScanning;
    @ViewInject(R.id.tv_name)
    TextView tvName;
    @ViewInject(R.id.pb)
    ProgressBar pb;
    @ViewInject(R.id.ll_add_text)
    LinearLayout llAddText;

    private static final int INIT = 1;
    private static final int SCANNING = 2;
    private static final int SCAN_FINISH = 3;

    private int mProcess; //进度条的进度
    private List<ScanInfo> mVirusScanInfoList; //病毒数据库的集合
    private Thread mCheckVirusThread;
    private Random mRandom = new Random();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);

        initAnim();

        //开始检测病毒
        checkVirus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCheckVirusThread != null) {
            mCheckVirusThread.interrupt();
            mCheckVirusThread = null;
        }
    }

    /**
     * 初始化动画，并启动
     */
    private void initAnim() {

        RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnimation.setDuration(1000);//间隔时间
        //rotateAnimation.setFillAfter(true);//动画结束时，停留在最后一帧
        rotateAnimation.setRepeatCount(Animation.INFINITE);//无限次循环
        rotateAnimation.setInterpolator(new LinearInterpolator());//匀速循环,不停顿
        ivScanning.startAnimation(rotateAnimation);
    }

    /**
     * 检测病毒
     * 分析：获取手机上所有应用的签名文件的md5码，然后判断病毒数据库中有无该md5码，若有，则判断为病毒程序
     */
    private void checkVirus() {

        mCheckVirusThread = new Thread() {
            @Override
            public void run() {

                mHandler.sendEmptyMessage(INIT);
                try {
                    //强制休眠2秒 显示初始化
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //当前界面显示的扫描信息的集合
                List<ScanInfo> scanInfoList = new ArrayList<>();
                //病毒信息的集合，是scanInfoList的子集.便于卸载病毒程序时使用
                mVirusScanInfoList = new ArrayList<>();


                //获取数据库中所有的病毒的md5码
                List<String> virusList = VirusDao.getAllVirus();


                //获取手机上的所有包的签名文件信息，包括卸载残留的包
                PackageManager packageManager = getPackageManager();
                List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES + PackageManager.MATCH_UNINSTALLED_PACKAGES);

                pb.setMax(packageInfoList.size());

                for (PackageInfo packageInfo : packageInfoList) {

                    //当按返回键退出时，此线程就不应该在继续执行了
                    if (mCheckVirusThread == null || mCheckVirusThread.isInterrupted()) {
                        return;
                    }

                    Signature[] signatures = packageInfo.signatures;//获取签名文件的数组 (打印出来的所有长度都为1)
                    //取第一个元素，然后进行md5,将此md5和数据库中的md5比对
                    String s = signatures[0].toCharsString();
                    String md5 = MD5Utils.encode(s); //32位字符串,16进制字符(0-f)

                    ScanInfo scanInfo = new ScanInfo();
                    scanInfo.appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                    scanInfo.packageName = packageInfo.packageName;
                    // 注意:virusList 是病毒数据库中的的md5的集合。一般来说病毒数据库比较大，使用这种方式一下子获取所有数据就不太可取了。几百万、几千万条数据就可能会照成内存溢出
                    // 此时就应该在 VirusDao 中定义一个 isVirus(String md5) 方法
                    if (virusList.contains(md5)) {//
                        //if (VirusDao.isVirus(md5)) {
                        //标记为病毒
                        scanInfo.isVirus = true;

                        mVirusScanInfoList.add(scanInfo);
                    } else {
                        scanInfo.isVirus = false;
                    }
                    scanInfoList.add(scanInfo);


                    //更新进度条(ProgressBar比较特殊，可以子线程中更新进度)
                    mProcess++;
                    pb.setProgress(mProcess);

                    try {
                        // 模拟不同应用程序的扫描时间不一样。仅仅是为了视觉效果，提升用户体验而已
                        Thread.sleep(50 + mRandom.nextInt(100));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //通知主线程更新UI(1:顶部扫描应用的名称2:扫描过程中往线性布局中添加view)
                    Message message = Message.obtain();
                    message.what = SCANNING;
                    message.obj = scanInfo;
                    mHandler.sendMessage(message);
                }

                Message message = Message.obtain();
                message.what = SCAN_FINISH;
                mHandler.sendMessage(message);
            }
        };
        mCheckVirusThread.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT:
                    tvName.setText("正在初始化8核杀毒引擎");
                    break;
                case SCANNING:
                    ScanInfo info = ((ScanInfo) msg.obj);

                    tvName.setText("正在扫描:" + info.appName);

                    TextView textView = new TextView(getApplicationContext());
                    if (info.isVirus) {
                        textView.setText("发现病毒：" + info.appName);
                        textView.setTextColor(Color.RED);
                    } else {
                        textView.setText("扫描安全：" + info.appName);
                        textView.setTextColor(Color.BLACK);
                    }

                    //从顶部插入
                    llAddText.addView(textView, 0);
                    break;
                case SCAN_FINISH:
                    tvName.setText("扫描完成");

                    //清除动画
                    ivScanning.clearAnimation();

                    if (mVirusScanInfoList.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "你的手机很安全了，继续加油哦!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        showAlertDialog();
                    }

                    break;
            }
        }
    };

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("警告!");
        builder.setMessage("发现" + mVirusScanInfoList.size() + "个病毒, 非常危险,赶紧清理!");
        builder.setPositiveButton("立即清理",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        unInstallVirus();
                    }
                });

        builder.setNegativeButton("下次再说", null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);// 点击弹窗外面,弹窗不消失
        dialog.show();
    }

    /**
     * 卸载病毒程序
     */
    private void unInstallVirus() {
        for (ScanInfo scanInfo : mVirusScanInfoList) {
            String packageName = scanInfo.packageName;
            //源码
            Intent intent = new Intent("android.intent.action.DELETE");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }
    }


    /**
     * 显示当前扫描信息的javaBean
     */
    class ScanInfo {
        public boolean isVirus; // 是否是病毒
        public String packageName; // 包名
        public String appName; //程序名
    }
}
