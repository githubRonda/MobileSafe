package com.ronda.mobilesafe.activity;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.adapter.CommonAdapter;
import com.ronda.mobilesafe.adapter.ViewHolder;
import com.ronda.mobilesafe.bean.BlackNumberInfo;
import com.ronda.mobilesafe.db.dao.BlackNumberDao;

import java.util.List;
import java.util.zip.Inflater;

/**
 * 分批加载黑名单记录
 */
public class CallSafeActivity2 extends AppCompatActivity {

    private ListView list_view;
    private LinearLayout ll_pb;

    private MyAdapter mAdapter;
    private BlackNumberDao mDao;

    private int mCurPage = 0; //当前页面
    private int mPageSize = 20; // 每页展示20条数据
    private int mTotalPage; // 一共多少页面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_safe2);

        mDao = new BlackNumberDao(CallSafeActivity2.this);

        initView();

        loadData();

    }

    private void initView() {
        list_view = (ListView) findViewById(R.id.list_view);
        ll_pb = (LinearLayout) findViewById(R.id.ll_pb);

        mAdapter = new MyAdapter();

        list_view.setAdapter(mAdapter);

        // 为ListView设置滚动监听
        list_view.setOnScrollListener(new AbsListView.OnScrollListener() {
            /**
             * @param view
             * @param scrollState  表示滚动的状态
             *                     AbsListView.OnScrollListener.SCROLL_STATE_IDLE 闲置状态
             *                     AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL 手指触摸的时候的状态
             *                     AbsListView.OnScrollListener.SCROLL_STATE_FLING 抛，仍（惯性）
             */
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:

                        int lastVisiblePosition = list_view.getLastVisiblePosition();
                        // 当滑到最后一个ItemView的时候
                        if (lastVisiblePosition == mAdapter.getData().size() - 1) {

                            // 若为最后一页
                            if (mCurPage == mTotalPage - 1) {
                                Toast.makeText(CallSafeActivity2.this, "没有更多数据了...", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // 继续加载下一页
                            mCurPage++;
                            loadData();
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void loadData() {
        // 显示加载的圆圈
        ll_pb.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {

                mTotalPage = (int) Math.ceil(mDao.getTotalRows() * 1.0 / mPageSize);  // ceil 向上取整

                final List<BlackNumberInfo> blackNumberList = mDao.findPar(mCurPage, mPageSize);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 隐藏加载的圆圈
                        ll_pb.setVisibility(View.GONE);

                        // 第一页的数据是设置，其他页的数据是追加
                        if (mCurPage == 0) {
                            mAdapter.setData(blackNumberList);
                        } else {
                            mAdapter.addData(blackNumberList);
                        }

                    }
                });
            }
        }).start();
    }


    /**
     * 点击添加黑名单按钮
     *
     * @param view
     */
    public void add(View view) {
        showAddDialog();
    }

    /**
     * 显示添加黑名单对话框
     */
    private void showAddDialog() {

        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_black_number, null);
        dialog.setView(view);

        final EditText et_number = (EditText) view.findViewById(R.id.et_number);
        final CheckBox cb_phone = (CheckBox) view.findViewById(R.id.cb_phone);
        final CheckBox cb_sms = (CheckBox) view.findViewById(R.id.cb_sms);

        Button btn_confrim = (Button) view.findViewById(R.id.btn_confirm);
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_confrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = et_number.getText().toString().trim();
                if (TextUtils.isEmpty(number)) {
                    Toast.makeText(CallSafeActivity2.this, "黑名单不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }

                String mode = null;
                if (cb_phone.isChecked() && cb_sms.isChecked()) {
                    mode = "1";
                } else if (cb_phone.isChecked()) {
                    mode = "2";
                } else if (cb_sms.isChecked()) {
                    mode = "3";
                }

                if (TextUtils.isEmpty(mode)) {
                    Toast.makeText(CallSafeActivity2.this, "请勾选拦截模式", Toast.LENGTH_SHORT).show();
                    return;
                }

                mDao.add(number, mode);

                loadData();

                dialog.dismiss();
            }
        });

        // 显示对话框
        dialog.show();
    }

    /**
     * 适配器内部类
     */
    class MyAdapter extends CommonAdapter<BlackNumberInfo> {

        public MyAdapter() {
            super(CallSafeActivity2.this, R.layout.item_call_safe);
        }

        @Override
        public void convert(ViewHolder holder, BlackNumberInfo bean, final int position) {

            holder.setText(R.id.tv_number, bean.getNumber());

            if ("1".equals(bean.getMode())) {
                holder.setText(R.id.tv_mode, "来电拦截+短信");
            } else if ("2".equals(bean.getMode())) {
                holder.setText(R.id.tv_mode, "电话拦截");
            } else if ("3".equals(bean.getMode())) {
                holder.setText(R.id.tv_mode, "短信拦截");
            }

            holder.setOnClickListener(R.id.iv_delete, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDao.delete(mAdapter.getData(position).getNumber());
                    loadData();
                }
            });
        }
    }
}
