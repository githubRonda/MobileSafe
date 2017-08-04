package com.ronda.mobilesafe.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.bean.ProcessInfo;
import com.ronda.mobilesafe.engine.ProcessInfoProvider;
import com.ronda.mobilesafe.utils.AppConst;
import com.ronda.mobilesafe.utils.SPUtils;
import com.ronda.mobilesafe.utils.ToastUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * 进程管理界面
 */
@ContentView(R.layout.activity_process_manager)
public class ProcessManagerActivity extends AppCompatActivity {

    @ViewInject(R.id.tv_process_count)
    private TextView mTvProcessCount;
    @ViewInject(R.id.tv_memory_info)
    private TextView mTvMemoryInfo;
    @ViewInject(R.id.lv_process)
    private ListView mLvProcess;
    @ViewInject(R.id.tv_category)
    private TextView mTvCategory;

    private MyAdapter mAdapter;

    private List<ProcessInfo> mUserProcessInfos; // 用户进程集合
    private List<ProcessInfo> mSysProcessInfos; // 系统进程集合
    private int mProcessCount; // 进程的数量
    private long mAvailSpace; // 剩余的内存空间
    private String mTotalSpace; // 总内存大小


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);


        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initView();

        loadData();

    }

    private void initView() {
        mProcessCount = ProcessInfoProvider.getProcessCount(this); // 释放内存时，会使用到这个值，所以声明为成员变量
        mTvProcessCount.setText("进程总数:" + mProcessCount);

        // 获取可用内存大小，并且格式化
        mAvailSpace = ProcessInfoProvider.getAvailSpace(this); // 释放内存时，会使用到这个值，所以声明为成员变量
        String availSpace = Formatter.formatFileSize(this, mAvailSpace);
        mTotalSpace = Formatter.formatFileSize(this, ProcessInfoProvider.getTotalSpace(this));// 释放内存时，会使用到这个值，所以声明为成员变量
        mTvMemoryInfo.setText("剩余/总共:" + availSpace + "/" + mTotalSpace);
    }


    private void initEvent() {
        //ListView的滚动监听
        mLvProcess.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (mUserProcessInfos == null || mSysProcessInfos == null) {
                    return;
                }

                if (firstVisibleItem >= mUserProcessInfos.size() + 1) {
                    mTvCategory.setText("系统进程(" + mSysProcessInfos.size() + ")个");
                } else {
                    mTvCategory.setText("用户进程(" + mUserProcessInfos.size() + ")个");
                }
            }
        });

        // ListView的点击监听
        mLvProcess.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // 纯文本条目不响应点击事件
                if (position == 0 || position == mUserProcessInfos.size() + 1) {
                    return;
                }

                ProcessInfo info = null;
                if (position < mUserProcessInfos.size() + 1) {
                    info = mUserProcessInfos.get(position - 1);
                } else {
                    info = mSysProcessInfos.get(position - mUserProcessInfos.size() - 2);
                }

                // 本应用的进程也不响应点击事件
                if (info.getPackageName().equals(getPackageName())) {
                    return;
                }

                //改变数据和View（直接更新View，不使用Adapter.notifyDataSetChanged()）
                info.setCheck(!info.isCheck());
                CheckBox cb = (CheckBox) view.findViewById(R.id.cb_checked);
                cb.setChecked(info.isCheck());
            }
        });
    }


    @Event({R.id.btn_select_all, R.id.btn_select_reverse, R.id.btn_clear, R.id.btn_setting})
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_select_all:
                selectAll(); // 全选
                break;
            case R.id.btn_select_reverse:
                selectReverse(); // 反选
                break;
            case R.id.btn_clear:
                killSelect();
                break;
            case R.id.btn_setting:
                startActivityForResult(new Intent(this, ProcessSettingActivity.class), 0);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 在ProcessSettingActivity中设置是否隐藏系统进程的实现逻辑很简单，即：
         * 1. 修改MyAdapter中的getCount()方法的返回值即可。
         * 2. 当返回到这个界面时，再使用notifyDataSetChanged()方法通知Adapter即可。
         */
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 全选
     */
    private void selectAll() {

        // 遍历用户进程，把所有的check数据设为true(要除去本应用所在的进程)
        for (ProcessInfo info : mUserProcessInfos) {
            if (info.getPackageName().equals(getPackageName())) {
                continue;
            }
            info.setCheck(true);
        }

        //遍历系统进程，把所有的check数据设为true
        for (ProcessInfo info : mSysProcessInfos) {
            info.setCheck(true);
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 反选
     */
    private void selectReverse() {
        // 遍历用户进程，把所有的check数据置反(要除去本应用所在的进程)
        for (ProcessInfo info : mUserProcessInfos) {
            if (info.getPackageName().equals(getPackageName())) {
                continue;
            }
            info.setCheck(!info.isCheck());
        }

        //遍历系统进程，把所有的check数据置反
        for (ProcessInfo info : mSysProcessInfos) {
            info.setCheck(!info.isCheck());
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 删除选中的进程
     */
    private void killSelect() {
        // 遍历用户进程，对选中的ItemView进行处理(要除去本应用所在的进程)
        // 要注意：在遍历集合的过程中不能删除集合中的元素，否则会产生安全性问题。所以这里采取使用另一个集合来暂存要删除的ItemView的对应的数据

        List<ProcessInfo> killProcessList = new ArrayList<>();

        for (ProcessInfo info : mUserProcessInfos) {
            if (info.getPackageName().equals(getPackageName())) {
                continue;
            }

            if (info.isCheck()) {
                // 遍历集合时，不能删除集合中的元素
                //mUserProcessInfos.remove(info);
                killProcessList.add(info);
            }
        }

        //遍历系统进程，对选中的ItemView进行处理
        for (ProcessInfo info : mSysProcessInfos) {
            if (info.isCheck()) {
                killProcessList.add(info);
            }
        }

        long releaseSpace = 0; // 记录释放的内存空间

        // 遍历暂存要删除进程的集合
        for (ProcessInfo info : killProcessList) {
            if (mUserProcessInfos.contains(info)) {
                mUserProcessInfos.remove(info);
            }

            if (mSysProcessInfos.contains(info)) {
                mSysProcessInfos.remove(info);
            }

            // 杀死进程
            ProcessInfoProvider.killProcess(this, info);

            releaseSpace += info.getMemSize();
        }

        // 通知Adapter更新
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

        // 更新顶部的数据（进程总数，剩余内存）[这里的更新并不是重新获取这些数据, 有些进程是杀不掉的。 这样做是为了提高用户的体验而已]
        mProcessCount -= killProcessList.size();
        mAvailSpace += releaseSpace;

        mTvProcessCount.setText("进程总数:" + mProcessCount);
        String availSpace = Formatter.formatFileSize(this, mAvailSpace);
        mTvMemoryInfo.setText("剩余/总共:" + availSpace + "/" + mTotalSpace);

        //ToastUtils.showToast(getApplicationContext(), "杀死了" + killProcessList.size() + "个进程，释放" + Formatter.formatFileSize(this, releaseSpace) + "空间"); // 这两种写法均可
        ToastUtils.showToast(getApplicationContext(), String.format("杀死了%d个进程，释放%s空间", killProcessList.size(), Formatter.formatFileSize(this, releaseSpace)));
    }

    /**
     * 异步加载数据
     */
    private void loadData() {

        new Thread() {

            @Override
            public void run() {
                //获取到所有手机上正在运行的进程信息, 然后拆成 用户进程集合 + 系统进程两个集合
                List<ProcessInfo> runningProcessInfos = ProcessInfoProvider.getProcessInfos(ProcessManagerActivity.this); // 这个集合不对外提供，所以声明为局部变量

                //用户进程的集合
                mUserProcessInfos = new ArrayList<>();
                //系统进程的集合
                mSysProcessInfos = new ArrayList<>();

                for (ProcessInfo info : runningProcessInfos) {
                    if (info.isSystem()) {
                        mSysProcessInfos.add(info);
                    } else {
                        mUserProcessInfos.add(info);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvCategory.setText("用户进程(" + mUserProcessInfos.size() + ")个");// 先初始化这个标题中的数据
                        mAdapter = new MyAdapter();
                        mLvProcess.setAdapter(mAdapter);
                    }
                });
            }
        }.start();
    }


    class MyAdapter extends BaseAdapter {

        // 两种类型的ItemView: 纯文本,图片+文字
        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1; //2
        }

        @Override
        public int getItemViewType(int position) {
            //代表纯文本条目
            if (position == 0 || position == mUserProcessInfos.size() + 1) {
                return 0;
            } else { //代表 图片+文字 条目
                return 1;
            }
        }

        @Override
        public int getCount() {
            boolean isShow = SPUtils.getBoolean(AppConst.SHOW_SYSTEM, false);
            if (isShow) { // 显示系统进程
                return mUserProcessInfos.size() + mSysProcessInfos.size() + 2;
            } else { // 隐藏系统进程
                return mUserProcessInfos.size() + 1;
            }
        }

        @Override
        public ProcessInfo getItem(int position) {
            if (position == 0 || position == mUserProcessInfos.size() + 1) {
                return null;
            } else {
                if (position < mUserProcessInfos.size() + 1) {
                    return mUserProcessInfos.get(position - 1);
                } else {//返回系统进程对应条目数据
                    return mSysProcessInfos.get(position - mUserProcessInfos.size() - 2);
                }
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            int type = getItemViewType(position);

            if (type == 0) { // 纯文本条目

                if (convertView == null) {
                    TextView textView = new TextView(ProcessManagerActivity.this);
                    textView.setTextColor(Color.WHITE);
                    textView.setBackgroundColor(Color.GRAY);

                    convertView = textView;
                    ViewTitleHolder viewTitleHolder = new ViewTitleHolder();
                    viewTitleHolder.tvTitle = textView;
                    convertView.setTag(viewTitleHolder);
                }

                ViewTitleHolder viewTitleHolder = (ViewTitleHolder) convertView.getTag();
                if (position == 0) {
                    viewTitleHolder.tvTitle.setText("用户进程(" + mUserProcessInfos.size() + ")个");
                } else if (position == mUserProcessInfos.size() + 1) {
                    viewTitleHolder.tvTitle.setText("系统进程(" + mSysProcessInfos.size() + ")个");
                }

                return convertView;
            } else { // type == 1表示系统进程

                if (convertView == null) {
                    convertView = LayoutInflater.from(ProcessManagerActivity.this).inflate(R.layout.item_process, parent, false);
                    ViewHolder viewHolder = new ViewHolder();
                    viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                    viewHolder.tvMemSize = (TextView) convertView.findViewById(R.id.tv_mem_size);
                    viewHolder.cbChecked = (CheckBox) convertView.findViewById(R.id.cb_checked);

                    convertView.setTag(viewHolder);
                }

                ViewHolder holder = (ViewHolder) convertView.getTag();
                holder.ivIcon.setImageDrawable(getItem(position).getIcon());
                holder.tvName.setText(getItem(position).getName());
                holder.tvMemSize.setText(Formatter.formatFileSize(getApplicationContext(), getItem(position).getMemSize()));

                // 本应用所在的进程不能被选中，所以将checkBox隐藏掉
                if (getItem(position).getPackageName().equals(getPackageName())) {
                    holder.cbChecked.setVisibility(View.GONE);
                } else {
                    holder.cbChecked.setVisibility(View.VISIBLE);
                }

                holder.cbChecked.setChecked(getItem(position).isCheck());
                return convertView;
            }
        }
    }

    static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvMemSize;
        CheckBox cbChecked;
    }

    static class ViewTitleHolder {
        TextView tvTitle;
    }
}
