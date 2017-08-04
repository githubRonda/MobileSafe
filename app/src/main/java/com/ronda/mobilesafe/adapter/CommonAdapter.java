package com.ronda.mobilesafe.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/06/21
 * Version: v1.0
 */

public abstract class CommonAdapter<T> extends BaseAdapter {
    protected Context mContext;
    protected List<T> mData;
    protected int layoutId;

    public CommonAdapter(Context context, List<T> datas, int layoutId) {
        mContext = context;
        mData = (datas != null ? datas : new ArrayList<T>());
        this.layoutId = layoutId;
    }

    public CommonAdapter(Context context, int layoutId) {
        this(context, null, layoutId);
    }

    @Override
    public int getCount() {
        if (mData == null) {
            return 0;
        }
        return mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.get(mContext, convertView, parent, layoutId, position);
        convert(holder, getItem(position), position);
        return holder.getConvertView();
    }

    /**
     * 控件赋值
     *
     * @param holder
     * @param bean
     */
    public abstract void convert(ViewHolder holder, T bean, int position);


    //==================对集合常用的操作==================
    public void setData(List<? extends T> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();

    }

    public void addData(T data) {
        addData(mData.size(), data);
        if (data == null) {
            return;
        }
        notifyDataSetChanged();
    }

    public void addData(int index, T data) {
        if (data == null) {
            return;
        }
        mData.add(index, data);
        notifyDataSetChanged();
    }

    public void addData(List<? extends T> data) {
        if (data == null) {
            return;
        }
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void removeData(T data) {
        if (data == null || !mData.contains(data)) {
            return;
        }

        int index = mData.indexOf(data);
        mData.remove(data);
        notifyDataSetChanged();
    }

    public void removeData(int position) {

        if (position < 0 || position >= mData.size()) {
            return;
        }
        mData.remove(position);
        notifyDataSetChanged();
    }

    public void clearData() {
        mData.clear();
        notifyDataSetChanged();

    }

    public List<T> getData() {
        return mData;
    }

    public T getData(int position) {
        return mData.get(position);
    }

    public boolean isEmpty() {
        return mData.isEmpty();
    }
}
