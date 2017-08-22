package com.ronda.mobilesafe.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.db.dao.CommonNumDao;
import com.ronda.mobilesafe.utils.ToastUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

@ContentView(R.layout.activity_common_num)
public class CommonNumActivity extends AppCompatActivity {

    @ViewInject(R.id.elv_common_num)
    private ExpandableListView mElvCommonNum;
    private List<CommonNumDao.Group> mData;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);


        CommonNumDao dao = new CommonNumDao();
        mData = dao.getGroup();

        mAdapter =  new MyAdapter();
        mElvCommonNum.setAdapter(mAdapter);

        mElvCommonNum.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                startCall(mAdapter.getChild(groupPosition, childPosition).number);
                return false;
            }
        });
    }

    /**
     * 打电话
     *
     * @param number
     */
    private void startCall(String number) {
        //拨打电话界面
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:"+number));
        startActivity(intent);
    }


    /**
     * ExpandableListView 的适配器
     */
    class MyAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return mData.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mData.get(groupPosition).childList.size();
        }

        @Override
        public CommonNumDao.Group getGroup(int groupPosition) {
            return mData.get(groupPosition);
        }

        @Override
        public CommonNumDao.Child getChild(int groupPosition, int childPosition) {
            return mData.get(groupPosition).childList.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            //这里没有考虑ConvertView复用，因为本类条目就不多
            TextView textView = new TextView(getApplicationContext());
            textView.setText("          " + getGroup(groupPosition).name);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            textView.setTextColor(Color.RED);
            return textView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            //这里没有考虑ConvertView复用，因为本类条目就不多
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_commonnum_child, parent, false);
            TextView tvName = (TextView) view.findViewById(R.id.tv_name);
            TextView tvNumber = (TextView) view.findViewById(R.id.tv_number);

            tvName.setText(getChild(groupPosition, childPosition).name);
            tvNumber.setText(getChild(groupPosition, childPosition).number);
            return view;
        }

        //这个方法的名字虽然是孩子节点是否可选中，但是实质上是孩子节点是否响应事件（包括点击），若返回false，则不会响应孩子节点的点击事件
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

}
