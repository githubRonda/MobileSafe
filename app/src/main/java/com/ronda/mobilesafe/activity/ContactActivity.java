package com.ronda.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.ronda.mobilesafe.R;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactActivity extends Activity {

    private ListView mLvContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        mLvContact = (ListView) findViewById(R.id.lv_contact);

        mLvContact.setAdapter(new SimpleAdapter(this, readContact(), R.layout.item_list_contact, new String[]{"name", "phone"}, new int[]{R.id.tv_name, R.id.tv_phone}));

        mLvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> map = (Map<String, String>) mLvContact.getAdapter().getItem(position);
                String phone = map.get("phone");

                Intent intent = new Intent();
                intent.putExtra("phone", phone);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }


    /**
     * 读取联系人的姓名和手机号码
     * 需要 READ_CONTACTS 权限
     *
     * @return
     */
    private List<Map<String, String>> readContact() {

        // 首先，从 raw_contacts 中读取联系人的id("contact_id")
        // 其次，根据 contact_id 从 data 表中查询出相应的电话号码和联系人名称
        // 然后，根据 mimetype 来区分哪个是联系人名称，哪个是电话号码

        Uri rawContactsUri = Uri.parse("content://com.android.contacts/raw_contacts"); // raw_contacts 是表名
        Uri dataUri = Uri.parse("content://com.android.contacts/data"); // data 是表名

        List<Map<String, String>> list = new ArrayList<>();

        // 查询 raw_contacts 表
        Cursor rawContactsCursor = getContentResolver().query(rawContactsUri, new String[]{"contact_id"}, null, null, null);
        if (rawContactsCursor != null) {
            while (rawContactsCursor.moveToNext()) {
                String contactId = rawContactsCursor.getString(0);
                KLog.e("contact_id: " + contactId);

                // 根据 contact_id 查询 data 表的记录, 实际上查询的是 view_data 视图， data 表本身是没有 contact_id 和 mimetype 字段的，但是 view_data 视图中有所有与 data 表关联的字段
                Cursor dataCursor = getContentResolver().query(dataUri, new String[]{"data1", "mimetype"}, "contact_id=?", new String[]{contactId}, null);
                if (dataCursor != null) {
                    Map<String, String> map = new HashMap<>();
                    while (dataCursor.moveToNext()) {
                        String data1 = dataCursor.getString(0);
                        String mimetype = dataCursor.getString(1);
                        KLog.i("data1: " + data1 + ", mimetype: " + mimetype);

                        if ("vnd.android.cursor.item/name".equals(mimetype)) {
                            map.put("name", data1);
                        } else if ("vnd.android.cursor.item/phone_v2".equals(mimetype)) {
                            map.put("phone", data1);
                        }
                    }
                    list.add(map);
                    dataCursor.close(); // 关闭cursor
                }
            }
            rawContactsCursor.close(); // 关闭cursor
        }
        return list;
    }
}
