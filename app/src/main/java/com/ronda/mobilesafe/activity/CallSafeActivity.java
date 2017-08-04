package com.ronda.mobilesafe.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
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

/**
 * 分页加载黑名单记录
 */
public class CallSafeActivity extends AppCompatActivity {

    private ListView list_view;
    private EditText et_page_code;
    private TextView tv_page_code;
    private LinearLayout ll_pb;

    private MyAdapter mAdapter;
    private BlackNumberDao mDao;

    private int mCurPage = 0; //当前页面
    private int mPageSize = 20; // 每页展示20条数据
    private int mTotalPage; // 一共多少页面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_safe);

        mDao = new BlackNumberDao(CallSafeActivity.this);

        initView();

        loadData();

    }

    private void initView() {
        list_view = (ListView) findViewById(R.id.list_view);
        et_page_code = (EditText) findViewById(R.id.et_page_code);
        tv_page_code = (TextView) findViewById(R.id.tv_page_code);
        ll_pb = (LinearLayout) findViewById(R.id.ll_pb);

        mAdapter = new MyAdapter();

        list_view.setAdapter(mAdapter);
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
                        mAdapter.setData(blackNumberList);
                        tv_page_code.setText((mCurPage + 1) + "/" + mTotalPage);
                    }
                });
            }
        }).start();
    }

    /**
     * 点击上一页按钮
     *
     * @param view
     */
    public void prePage(View view) {
        if (mCurPage <= 0) {
            Toast.makeText(this, "已经是第一页了", Toast.LENGTH_SHORT).show();
            return;
        }
        mCurPage--;

        loadData();
    }

    /**
     * 点击下一页按钮
     *
     * @param view
     */
    public void nextPage(View view) {

        if (mCurPage >= mTotalPage - 1) {
            Toast.makeText(this, "已经是最后一页了", Toast.LENGTH_SHORT).show();
            return;
        }
        mCurPage++;

        loadData();
    }


    /**
     * 点击跳转按钮
     *
     * @param view
     */
    public void jump(View view) {

        String str_page = et_page_code.getText().toString().trim();
        if (TextUtils.isEmpty(str_page)) {
            Toast.makeText(this, "页码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        int page = Integer.valueOf(str_page);
        if (page < 1 || page > mTotalPage) {
            Toast.makeText(this, "请输入正确的页码", Toast.LENGTH_SHORT).show();
            return;
        }

        mCurPage = page - 1; // mCurPage 从0开始

        loadData();
    }

    /**
     * 适配器内部类
     */
    class MyAdapter extends CommonAdapter<BlackNumberInfo> {

        public MyAdapter() {
            super(CallSafeActivity.this, R.layout.item_call_safe);
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
