package com.ronda.mobilesafe.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.utils.StreamUtils;
import com.socks.library.KLog;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 细节问题：
 * 1. 对于更新提示对话框，若点击outside或返回键，取消对话框时，会停在启动页。解决方法：给对话框设置一个 setOnCancelListener 监听器，内部执行 goHome() 方法
 * 2. 当最新版本下载完毕跳转到系统安装页面时，这时若点击“取消安装”，程序又会停在启动页。 解决方法：用 startActivityForResult() 启动系统安装界面，然后在 onActivityResult() 回调方法中调用 goHome()
 */
public class SplashActivity extends AppCompatActivity {

    private static final int CODE_GO_HOME = -1; // 无版本更新时，直接进入HomeActivity
    private static final int CODE_UPDATE_DIALOG = 0;
    private static final int CODE_URL_ERROR = 1;
    private static final int CODE_NET_ERROR = 2;
    private static final int CODE_JSON_ERROR = 3;

    private static final int CODE_REQUEST_INSTALL = 0; // requestCode: 请求跳转到系统安装软件的Activity界面

    private RelativeLayout mRootView;
    private TextView mTvVersion;
    private TextView mTvProgress;

    // 从服务器端获取到的有关更新的数据
    private String mRemoteVersionName;
    private int mRemoteVersionCode;
    private String mRemoteDescription;
    private String mDownloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        mRootView = (RelativeLayout) findViewById(R.id.rl_root);
        mTvVersion = (TextView) findViewById(R.id.tv_version);
        mTvVersion.setText("版本：" + getVersionName());

        mTvProgress = (TextView) findViewById(R.id.tv_progress);


        initDB();

        createShortcut();//创建快捷方式

        //根据配置信息，决定是否检查更新
        if (getSharedPreferences("config", MODE_PRIVATE).getBoolean("auto_update", true)) {
            checkVersion();
        } else {
            mHandler.sendEmptyMessageDelayed(CODE_GO_HOME, 2000);
        }

        //渐变的动画效果
        AlphaAnimation anim = new AlphaAnimation(0.3f, 1.0f);
        anim.setDuration(2000);
        mRootView.startAnimation(anim);
    }

    private void initDB() {
        copyDB("address.db"); // 拷贝归属地查询数据库
        copyDB("commonnum.db"); //拷贝常用号码数据库
        copyDB("antivirus.db"); //拷贝病毒数据库
    }

    /**
     * 创建快捷方式
     */
    private void createShortcut() {

        //其实创建和删除快捷方式都是通过Intent发送广播给Launcher这个系统应用程序，然后由Launcher中注册的相关广播接收器来完成的（通过查看Launcher中的manifest.xml文件可知）
        // 创建快捷方式的权限： <uses-permission android:name="com.android.launcher.permission.INSTALL_SHORTCUT"/>
        // 删除快捷方式的权限： <uses-permission android:name="com.android.launcher.permission.UNINSTALL_SHORTCUT"/>
        //创建快捷方式的Intent包含四部分内容：1. 创建shortcut的Action；2. shortcut_name(快捷方式名称); 3. shortcut_icon(快捷方式图标); 4. shortcut_intent(快捷方式的关联动作)
        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT"); // 1. 创建快捷方式的action
        intent.putExtra("duplicate", false); // 不允许重复创建。 也可以使用SharedPreferences来保存是否创建了快捷方式，但是删除的时候也需要修改SharedPreferences

        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "手机卫士"); // 2. 快捷方式的图标
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)); // 3. 快捷方式的名称
        //intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher)); // 也是可以的

        // 快速拨号的Intent
        //Intent doIntent = new Intent(Intent.ACTION_CALL);
        //doIntent.setData(Uri.parse("tel://110"));

        // 快速启动程序的Intent
        //Intent doIntent = new Intent(this, HomeActivity.class); // 会提示未安装应用。原因很简单：对于桌面而言，无法知道this是什么。所以此时只能使用隐式Intent
        Intent doIntent = new Intent(this, HomeActivity.class);
        doIntent.setAction("android.intent.action.HOME"); // 这个action的名字可以随便取，只要和 Manifest.xml 中HomeActivity的Action是一样的即可
        doIntent.addCategory("android.intent.category.DEFAULT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, doIntent); // 4. 快捷方式关联的动作意图
        sendBroadcast(intent);
    }


    private String getVersionName() {

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private int getVersionCode() {

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;

    }

    /**
     * 提示升级对话框
     */
    private void showUpdateDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最新版本：" + mRemoteVersionName)
                .setMessage(mRemoteDescription)
                //.setCancelable(false) // 点击对话框外部或者返回键时，让对话框不消失，即强制让用户点击确定或取消按钮。后果：用户体验太差，尽量不要用
                .setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        KLog.e("立即更新");
                        download();
                    }
                })
                .setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goHome();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() { //当用户点击对话框外部或返回键取消对话框时，会触发这个监听器。点击“确定”或“取消”按钮不会触发这个监听器
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        goHome();
                    }
                });

        builder.create().show();
    }


    /**
     * 跳转到主界面
     */
    private void goHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CODE_REQUEST_INSTALL:
                goHome();
                break;
        }
    }

    /**
     * 拷贝数据库
     *
     * @param DBName
     */
    private void copyDB(String DBName) {
        InputStream in = null;
        OutputStream out = null;
        File destFile = new File(getFilesDir(), DBName);

        if (destFile.exists()) {
            KLog.w("数据库" + DBName + "已存在！");
            return;
        }

        try {
            in = getAssets().open(DBName);
            out = new FileOutputStream(destFile);
            KLog.i("files: " + destFile.toString());

            int len;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CODE_GO_HOME:
                    goHome();
                    break;
                case CODE_UPDATE_DIALOG:
                    showUpdateDialog();
                    break;
                case CODE_URL_ERROR:
                    Toast.makeText(SplashActivity.this, "url错误", Toast.LENGTH_SHORT).show();
                    goHome(); // 当网络访问出错时，也应该可以进入主界面
                    break;
                case CODE_NET_ERROR:
                    Toast.makeText(SplashActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    goHome();
                    break;
                case CODE_JSON_ERROR:
                    Toast.makeText(SplashActivity.this, "json数据格式错误", Toast.LENGTH_SHORT).show();
                    goHome();
                    break;
            }
        }
    };

    //=================后台服务器相关===========================

    /**
     * 从服务器获取版本信息进行校验
     */
    private void checkVersion() {

        final long startTime = System.currentTimeMillis();

        // 网络请求放在子线程中进行
        new Thread() {
            @Override
            public void run() {

                Message msg = mHandler.obtainMessage();
                msg.what = -1;
                HttpURLConnection conn = null;

                try {
                    // 本机地址用localhost，但是如果使用Android模拟器加载本机地址时，可以用ip(10.0.2.2)来替换
                    //URL url = new URL("http:10.0.2.2:8080/update.json");
                    URL url = new URL("http://192.168.0.105:8080/update.json");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);// 连接超时时间
                    conn.setReadTimeout(5000); //  设置响应超时时间
                    conn.connect();// 连接服务器

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) { // 响应成功
                        InputStream in = conn.getInputStream();
                        String result = StreamUtils.readFromStream(in);

                        JSONObject jo = new JSONObject(result);

                        mRemoteVersionCode = jo.getInt("versionCode");
                        mRemoteVersionName = jo.getString("versionName");
                        mRemoteDescription = jo.getString("description");
                        mDownloadUrl = jo.getString("downloadUrl");

                        if (mRemoteVersionCode > getVersionCode()) { // 若有版本更新
                            msg.what = CODE_UPDATE_DIALOG;
                        } else { // 没有版本更新
                            msg.what = CODE_GO_HOME; // todo 这里不能直接 goHome()， 因为msg.what的初始值为0和CODE_UPDATE_DIALOG相等，所以即使无版本更新，依然会弹出更新对话框
                        }
                    }
                } catch (MalformedURLException e) {
                    // url错误
                    msg.what = CODE_URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e) { // 这个也也可以用来检测是否有网络连接（因为每次进入启动页都要访问后台update数据）
                    // 网络错误
                    msg.what = CODE_NET_ERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
                    // json数据格式错误
                    msg.what = CODE_JSON_ERROR;
                    e.printStackTrace();
                } finally {
                    // 让启动页至少展示2s钟
                    long useTime = System.currentTimeMillis() - startTime;
                    if (useTime < 2000) {
                        try {
                            Thread.sleep(2000 - useTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    mHandler.sendMessage(msg);
                    if (conn != null) {
                        conn.disconnect(); // 关闭网络连接
                    }
                }
            }
        }.start();
    }


    /**
     * 下载最新apk文件
     */
    private void download() {

        // 检测有无挂载SD卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "无sd卡", Toast.LENGTH_SHORT).show();
            return;
        }

        String tartget = Environment.getExternalStorageDirectory() + "/update.apk";

        //XUtils3框架
        //RequestParams params = new RequestParams("http://192.168.0.105:8080/MobileSafe-v2.0.apk");
        RequestParams params = new RequestParams(mDownloadUrl);
        params.setSaveFilePath(tartget);
        // params.setAutoRename(true); //自动命名
        x.http().post(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                mTvProgress.setVisibility(View.VISIBLE);
                KLog.e("onStarted: " + Thread.currentThread().getId());
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                KLog.e("下载进度：" + current + "/" + total);
                mTvProgress.setText("下载进度：" + 100 * current / total + "%");
                KLog.e("onLoading: " + Thread.currentThread().getId());
            }

            @Override
            public void onSuccess(File result) {
                KLog.e("下载成功");

                // 跳转到系统安装软件的界面
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setDataAndType(Uri.fromFile(result), "application/vnd.android.package-archive");
                startActivityForResult(intent, CODE_REQUEST_INSTALL);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                KLog.e("下载失败");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                mTvVersion.setVisibility(View.GONE);
                KLog.e("onFinished: " + Thread.currentThread().getId());
            }
        });
    }
}

