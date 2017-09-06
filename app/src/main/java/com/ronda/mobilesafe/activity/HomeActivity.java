package com.ronda.mobilesafe.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.bean.HomeItemBean;
import com.ronda.mobilesafe.utils.MD5Utils;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {


    private GridView mGridView;
    private List<HomeItemBean> mData = new ArrayList<>();
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mPreferences = getSharedPreferences("config", MODE_PRIVATE);

        initData();

        mGridView = (GridView) findViewById(R.id.gv_home);
        mGridView.setAdapter(new HomeAdapter());

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: //手机防盗
                        showPasswordDialog();
                        break;
                    case 1: // 通讯卫士
                        startActivity(new Intent(getApplicationContext(), CallSafeActivity2.class));
                        break;
                    case 2: // 软件管理
                        startActivity(new Intent(getApplicationContext(), AppManageActivity.class));
                        break;
                    case 3: // 进程管理
                        startActivity(new Intent(getApplicationContext(), ProcessManagerActivity.class));
                        break;
                    case 4: // 流量统计
                        startActivity(new Intent(getApplicationContext(), TrafficActivity.class));
                        break;
                    case 5: // 手机杀毒
                        startActivity(new Intent(getApplicationContext(), AnitVirusActivity.class));
                        break;
                    case 6: // 缓存清理
                        //startActivity(new Intent(getApplicationContext(), CacheClearActivity.class));
                        startActivity(new Intent(getApplicationContext(), BaseCacheClearActivity.class));
                        break;
                    case 7: // 高级工具
                        startActivity(new Intent(getApplicationContext(), AToolsActivity.class));
                        break;
                    case 8: // 设置中心
                        startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                        break;

                }
            }
        });

    }

    private void showPasswordDialog() {
        //若没有设置过密码，则弹出设置密码的对话框；反之，则弹出输入密码的对话框
        if (TextUtils.isEmpty(mPreferences.getString("password", null))) {
            showSetPasswordDialog();
        } else {
            showInputPasswordDialog();
        }
    }

    /**
     * 显示输入密码登录的对话框
     */
    private void showInputPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();

        View view = View.inflate(this, R.layout.dialog_input_password, null);
        dialog.setView(view, 0, 0, 0, 0);// 左上右下的边距为0；直接setView()在2.3版本会出现兼容性问题，即有默认边距

        dialog.show();

        final EditText et_password = (EditText) view.findViewById(R.id.et_password);

        Button btn_confirm = (Button) view.findViewById(R.id.btn_confirm);
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String password = et_password.getText().toString().trim();

                if (!TextUtils.isEmpty(password)) {
                    String savedPassword = mPreferences.getString("password", null);
                    if (MD5Utils.encode(password).equals(savedPassword)) {
                        //Toast.makeText(HomeActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(HomeActivity.this, LostFindActivity.class));
                        dialog.dismiss();
                    } else {
                        Toast.makeText(HomeActivity.this, "密码错误！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "输入框内容不能为空！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 显示设置密码的对话框
     */
    private void showSetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();

        View view = View.inflate(this, R.layout.dialog_set_password, null);
        dialog.setView(view, 0, 0, 0, 0);// 左上右下的边距为0；直接setView()在2.3版本会出现兼容性问题，即有默认边距

        dialog.show();

        final EditText et_password = (EditText) view.findViewById(R.id.et_password);
        final EditText et_password_confirm = (EditText) view.findViewById(R.id.et_password_confirm);


        Button btn_confirm = (Button) view.findViewById(R.id.btn_confirm);
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String password = et_password.getText().toString().trim();
                String passwordConfirm = et_password_confirm.getText().toString().trim();

                if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordConfirm)) {

                    if (password.equals(passwordConfirm)) {
                        mPreferences.edit().putString("password", MD5Utils.encode(password)).commit();// 持久化保存password

                        //Toast.makeText(HomeActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(HomeActivity.this, "两次密码不一致！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "输入框内容不能为空！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void initData() {
        mData.add(new HomeItemBean(R.drawable.home_safe, "手机防盗"));
        mData.add(new HomeItemBean(R.drawable.home_callmsgsafe, "通讯卫视"));
        mData.add(new HomeItemBean(R.drawable.home_apps, "软件管理"));
        mData.add(new HomeItemBean(R.drawable.home_taskmanager, "进程管理"));
        mData.add(new HomeItemBean(R.drawable.home_netmanager, "流量统计"));
        mData.add(new HomeItemBean(R.drawable.home_trojan, "手机杀毒"));
        mData.add(new HomeItemBean(R.drawable.home_sysoptimize, "缓存清理"));
        mData.add(new HomeItemBean(R.drawable.home_tools, "高级工具"));
        mData.add(new HomeItemBean(R.drawable.home_settings, "设置中心"));
    }


    class HomeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public HomeItemBean getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(HomeActivity.this, R.layout.item_home_list, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.mImageView.setImageResource(mData.get(position).getItemIconId());
            holder.mTextView.setText(mData.get(position).getItemDesc());


            return convertView;
        }

        class ViewHolder {
            ImageView mImageView;
            TextView mTextView;

            public ViewHolder(View view) {
                mImageView = (ImageView) view.findViewById(R.id.iv_item);
                mTextView = (TextView) view.findViewById(R.id.tv_item);
            }
        }
    }
}
