package com.ronda.mobilesafe.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.bean.AppInfo;
import com.ronda.mobilesafe.db.dao.AppLockDao;
import com.ronda.mobilesafe.engine.AppInfoProvider;

import java.util.ArrayList;
import java.util.List;

public class AppLockActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnUnlock, mBtnLock;
    private LinearLayout mLlUnlock, mLlLock;
    private TextView mTvUnlock, mTvLock;
    private ListView mLvUnlock, mLvLock;


    //区分已加锁应用和未加锁应用
    List<AppInfo> mUnLockList = new ArrayList<AppInfo>();
    List<AppInfo> mLockList = new ArrayList<>();

    private AppLockDao mAppLockDao;

    private MyAdapter mLockAdapter; //加锁列表的适配器
    private MyAdapter mUnlockAdapter; //未加锁列表的适配器
    private TranslateAnimation mLockAnim; //锁定时的动画 (右移)
    private TranslateAnimation mUnlockAnim; //解锁时的动画 (左移)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);


        initView();

        loadData();

        initAnim();
    }

    private void initAnim() {
        //动画效果：Y轴不变，X轴向右平移相对于View自身的长度
        mLockAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mLockAnim.setDuration(500);

        mUnlockAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mUnlockAnim.setDuration(500);
    }

    private void initView() {
        mBtnUnlock = (Button) findViewById(R.id.btn_unlock);
        mBtnLock = (Button) findViewById(R.id.btn_lock);

        mLlUnlock = (LinearLayout) findViewById(R.id.ll_unlock);
        mLlLock = (LinearLayout) findViewById(R.id.ll_lock);

        mTvUnlock = (TextView) findViewById(R.id.tv_unlock);
        mTvLock = (TextView) findViewById(R.id.tv_lock);

        mLvUnlock = (ListView) findViewById(R.id.lv_unlock);
        mLvLock = (ListView) findViewById(R.id.lv_lock);


        mLockAdapter = new MyAdapter(true);
        mUnlockAdapter = new MyAdapter(false);

        mLvUnlock.setAdapter(mUnlockAdapter);
        mLvLock.setAdapter(mLockAdapter);

        mBtnLock.setOnClickListener(this);
        mBtnUnlock.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_unlock:
                mBtnUnlock.setBackgroundResource(R.drawable.tab_left_pressed);
                mBtnLock.setBackgroundResource(R.drawable.tab_right_default);
                mLlUnlock.setVisibility(View.VISIBLE);
                mLlLock.setVisibility(View.GONE);
                break;
            case R.id.btn_lock:
                mBtnUnlock.setBackgroundResource(R.drawable.tab_left_default);
                mBtnLock.setBackgroundResource(R.drawable.tab_right_pressed);
                mLlUnlock.setVisibility(View.GONE);
                mLlLock.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void loadData() {

        new Thread() {
            @Override
            public void run() {

                //获取所有手机中的应用
                List<AppInfo> appInfos = AppInfoProvider.getAppInfos(getApplicationContext());

                //获取数据库中已加锁应用包名的集合
                mAppLockDao = AppLockDao.getInstance(getApplicationContext());
                List<String> lockedPackageNameList = mAppLockDao.findAll();

                for (AppInfo appInfo : appInfos) {
                    if (lockedPackageNameList.contains(appInfo.getApkPackageName())) {
                        mLockList.add(appInfo);
                    } else {
                        mUnLockList.add(appInfo);
                    }
                }

                //通知主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLockAdapter.notifyDataSetChanged();
                        mUnlockAdapter.notifyDataSetChanged();
                    }
                });

            }
        }.start();
    }


    class MyAdapter extends BaseAdapter {

        //已加锁和未加锁列表公用一个适配器类，用isLock来区分
        private final boolean isLock;

        public MyAdapter(boolean isLock) {
            this.isLock = isLock;
        }

        @Override
        public int getCount() {
            if (isLock) {
                mTvLock.setText("已加锁应用：" + mLockList.size());
                return mLockList.size();
            } else {
                mTvUnlock.setText("未加锁应用：" + mUnLockList.size());
                return mUnLockList.size();
            }
        }

        @Override
        public AppInfo getItem(int position) {
            if (isLock) {
                return mLockList.get(position);
            } else {
                return mUnLockList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_lock, parent, false);
                holder = new ViewHolder();
                holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                holder.ivLock = (ImageView) convertView.findViewById(R.id.iv_lock);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final AppInfo appInfo = getItem(position);
            holder.ivIcon.setImageDrawable(appInfo.getIcon());
            holder.tvName.setText(appInfo.getApkName());
            if (isLock) {
                holder.ivLock.setImageResource(R.drawable.lock);
            } else {
                holder.ivLock.setImageResource(R.drawable.unlock);
            }

            final View itemView = convertView;
            //给锁添加点击事件： 1. 设置动画 2. 一个集合删除元素，另一个集合添加元素 3. sqlite也要对应 插入/删除 数据
            holder.ivLock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isLock) {
                        itemView.startAnimation(mUnlockAnim);

                        //动画是异步执行的，会造成动画刚一开始执行界面数据就更新完成了。所以要保证这个过程是同步进行，即动画执行结束后再删除数据。所以使用动画监听器
                        mUnlockAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                //当动画结束时
                                mLockList.remove(appInfo);
                                mUnLockList.add(appInfo);
                                mAppLockDao.delete(appInfo.getApkPackageName());
                                mLockAdapter.notifyDataSetChanged();
                                mUnlockAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });

                    } else {
                        itemView.startAnimation(mLockAnim);

                        mLockAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                //当动画结束时
                                mUnLockList.remove(appInfo);
                                mLockList.add(appInfo);
                                mAppLockDao.insert(appInfo.getApkPackageName());
                                mLockAdapter.notifyDataSetChanged();
                                mUnlockAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                    }
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView ivIcon;
        ImageView ivLock;
        TextView tvName;
    }
}
