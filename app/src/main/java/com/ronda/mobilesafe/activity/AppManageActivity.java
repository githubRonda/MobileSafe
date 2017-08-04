package com.ronda.mobilesafe.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.bean.AppInfo;
import com.ronda.mobilesafe.engine.AppInfoProvider;
import com.ronda.mobilesafe.utils.ToastUtils;
import com.socks.library.KLog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * 软件管理界面
 */
@ContentView(R.layout.activity_app_manage)
public class AppManageActivity extends AppCompatActivity implements View.OnClickListener {
    @ViewInject(R.id.tv_rom)
    TextView mTvRom;
    @ViewInject(R.id.tv_sd)
    TextView mTvSd;
    @ViewInject(R.id.tv_category)
    TextView mTvCategory;
    @ViewInject(R.id.list_view)
    ListView mListView;

    private MyAdapter mAdapter;

    private List<AppInfo> mUserAppInfos;
    private List<AppInfo> mSysAppInfos;
    private PopupWindow mPopupWindow;
    private AppInfo mClickItemData;
    private UninstallReceiver mUninstallReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_app_manage);
        x.view().inject(this);

        initView();

        initEvent();

        loadData();

        //监听卸载程序的广播
        mUninstallReceiver = new UninstallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(mUninstallReceiver, filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissPopupWindow();
        unregisterReceiver(mUninstallReceiver); //注销广播
    }

    private void initView() {

        //获取到rom内部存储的剩余空间
        long romFreeSpace = Environment.getDataDirectory().getFreeSpace();
        //获取到SD卡的剩余空间
        long sdFreeSpace = Environment.getExternalStorageDirectory().getFreeSpace();

        // 格式化大小
        mTvRom.setText("内部存储可用：" + Formatter.formatFileSize(this, romFreeSpace));
        mTvSd.setText("sd卡可用：" + Formatter.formatFileSize(this, sdFreeSpace));


        //mListView.setAdapter(mAdapter);
    }

    private void initEvent() {
        //设置listview的滚动监听
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            /**
             *
             * @param view
             * @param firstVisibleItem 第一个可见的条的位置
             * @param visibleItemCount 一页可以展示多少个条目
             * @param totalItemCount   总共的item的个数
             */
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                // 滚动时隐藏PopupWindow
                dismissPopupWindow();

                if (mUserAppInfos != null && mSysAppInfos != null) {
                    if (firstVisibleItem > mUserAppInfos.size()) {
                        //系统应用程序
                        mTvCategory.setText("系统程序(" + mSysAppInfos.size() + ")个");
                    } else {
                        //用户应用程序
                        mTvCategory.setText("用户程序(" + mUserAppInfos.size() + ")个");
                    }
                }
            }
        });

        //设置listview的点击弹出popupwindow事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // 获取到当前点击的ItemView对应的数据
                Object obj = mListView.getItemAtPosition(position);
                if (obj == null || !(obj instanceof AppInfo)) { //去除特殊项的Item
                    return;
                }

                mClickItemData = (AppInfo) obj;

                showPopupWindow(view);
            }
        });
    }

    private void showPopupWindow(View view) {
        View contentView = LayoutInflater.from(AppManageActivity.this).inflate(R.layout.pop_app_manage, null);
        mPopupWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        //需要注意：使用PopupWindow 必须设置背景。不然没有动画,也不能按返回键取消
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //获取view展示到窗体上面的位置
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        mPopupWindow.showAtLocation(view, Gravity.LEFT + Gravity.TOP, 70, location[1]);
        //mPopupWindow.showAsDropDown(view, 70, -view.getHeight()); // 均可以定位

        ScaleAnimation sa = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); // 后面四个参数是指明缩放中心，相对于自己的正中心缩放
        AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
        Interpolator interpolator = new LinearInterpolator();
        AnimationSet as = new AnimationSet(true);
        as.setDuration(500);
        as.setInterpolator(interpolator);
        as.addAnimation(sa);
        as.addAnimation(aa);

        contentView.setAnimation(as);

        // 设置监听器
        contentView.findViewById(R.id.ll_uninstall).setOnClickListener(AppManageActivity.this);
        contentView.findViewById(R.id.ll_run).setOnClickListener(AppManageActivity.this);
        contentView.findViewById(R.id.ll_share).setOnClickListener(AppManageActivity.this);
        contentView.findViewById(R.id.ll_detail).setOnClickListener(AppManageActivity.this);
    }

    /**
     * 隐藏popupWindow
     */
    private void dismissPopupWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
    }

    /**
     * 异步加载数据
     */
    private void loadData() {

        new Thread() {

            @Override
            public void run() {
                //获取到所有安装到手机上面的应用程序, 然后拆成 用户程序的集合 + 系统程序两个集合
                List<AppInfo> allAppInfos = AppInfoProvider.getAppInfos(AppManageActivity.this);// 这个集合不对外提供，所以声明为局部变量

                //用户程序的集合
                mUserAppInfos = new ArrayList<>();
                //系统程序的集合
                mSysAppInfos = new ArrayList<>();

                for (AppInfo info : allAppInfos) {

                    if (info.isUserApp()) {
                        mUserAppInfos.add(info);
                    } else {
                        mSysAppInfos.add(info);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvCategory.setText("用户程序(" + mUserAppInfos.size() + ")个"); // 先初始化这个标题中的数据
                        mAdapter = new MyAdapter();
                        mListView.setAdapter(mAdapter);
                    }
                });
            }
        }.start();
    }

    @Override
    public void onClick(View v) {

        dismissPopupWindow();

        switch (v.getId()) {
            case R.id.ll_uninstall: {//卸载
                if (!mClickItemData.isUserApp()) {
                    ToastUtils.showToast(this, "此应用不能卸载");
                    return;
                }
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + mClickItemData.getApkPackageName()));
                uninstallIntent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(uninstallIntent); // 这里卸载完成后要更新ListView的数据, 不能用 startActivityForResult， 因为这里弹出的是一个对话框而不是跳转到一个新页面，所以onActivityResult 不会回调
                break;
            }
            case R.id.ll_run: {//运行
                Intent intent = getPackageManager().getLaunchIntentForPackage(mClickItemData.getApkPackageName());
                if (intent != null) {
                    startActivity(intent);
                } else {
                    ToastUtils.showToast(this, "此应用不能被开启");
                }
                break;
            }
            case R.id.ll_share: {//（短信,邮件等）分享
                // 通过短信应用，向外发送短信
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
                //点击地址会自动跳转到谷歌应用商店去下载对应的软件，这种写法的格式是固定的
                intent.putExtra(Intent.EXTRA_TEXT,
                        "Hi！推荐您使用软件：" + mClickItemData.getApkName() + "下载地址:" + "https://play.google.com/store/apps/details?id=" + mClickItemData.getApkPackageName());
                startActivity(Intent.createChooser(intent, "分享"));//若有多个应用程序与这个Intent匹配的话，则会出现一个列表对话框，对话框的标题为分享

                break;
            }
            case R.id.ll_detail: {//详情--> 跳转到设置中的应用详情中
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + mClickItemData.getApkPackageName()));
                startActivity(intent);
                break;
            }
        }
    }

    private class MyAdapter extends BaseAdapter {

        //获取数据适配器中条目类型的总数,修改成两种(纯文本,图片+文字)
        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1; //2
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 || position == mUserAppInfos.size() + 1) {
                //返回0,代表纯文本条目的状态码
                return 0;
            } else {
                //返回1,代表图片+文本条目状态码
                return 1;
            }
        }

        @Override
        public int getCount() {
            return mUserAppInfos.size() + mSysAppInfos.size() + 2;
        }

        @Override
        public AppInfo getItem(int position) {
            if (position == 0 || position == mUserAppInfos.size() + 1) { // 这是两个特殊的Item
                return null;
            }

            if (position < mUserAppInfos.size() + 1) {
                return mUserAppInfos.get(position - 1);
            } else {
                return mSysAppInfos.get(position - mUserAppInfos.size() - 2);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);

            if (type == 0) { //展示灰色纯文本条目
                if (convertView == null) {
                    TextView textView = new TextView(AppManageActivity.this);
                    textView.setTextColor(Color.WHITE);
                    textView.setBackgroundColor(Color.GRAY);

                    convertView = textView; // 代码创建ItemView，而不是使用inflate()加载布局文件
                    ViewTitleHolder titleHolder = new ViewTitleHolder();
                    titleHolder.tvTitle = textView;
                    convertView.setTag(titleHolder);
                }
                ViewTitleHolder titleHolder = (ViewTitleHolder) convertView.getTag();

                //如果当前的position等于0 表示用户程序
                if (position == 0) {
                    titleHolder.tvTitle.setText("用户程序(" + mUserAppInfos.size() + ")");
                } else {
                    titleHolder.tvTitle.setText("系统程序(" + mSysAppInfos.size() + ")");
                }

                return convertView;

            } else  { //展示图片+文字条目 type == 1

                if (convertView == null) {
                    convertView = LayoutInflater.from(AppManageActivity.this).inflate(R.layout.item_app_manage, null);
                    ViewHolder holder = new ViewHolder();
                    holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
                    holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                    holder.tvSize = (TextView) convertView.findViewById(R.id.tv_size);
                    holder.tvLocation = (TextView) convertView.findViewById(R.id.tv_location);
                    convertView.setTag(holder);
                }
                ViewHolder holder = (ViewHolder) convertView.getTag();

                holder.ivIcon.setImageDrawable(getItem(position).getIcon());
                holder.tvName.setText(getItem(position).getApkName());
                holder.tvSize.setText(Formatter.formatFileSize(AppManageActivity.this, getItem(position).getApkSize()));
                if (getItem(position).isRom()) {
                    holder.tvLocation.setText("内部存储");
                } else {
                    holder.tvLocation.setText("外部SD卡存储");
                }
                return convertView;
            }
        }
    }

    static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvSize;
        TextView tvLocation;
    }

    static class ViewTitleHolder {
        TextView tvTitle;
    }


    class UninstallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            KLog.w("接收到卸载的广播");

            // 重新加载数据
            loadData();
        }
    }

}
