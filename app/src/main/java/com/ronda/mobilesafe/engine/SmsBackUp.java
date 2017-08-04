package com.ronda.mobilesafe.engine;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Xml;

import com.ronda.mobilesafe.utils.Crypto;
import com.ronda.mobilesafe.utils.ToastUtils;
import com.socks.library.KLog;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/07/03
 * Version: v1.0
 * <p>
 * 短信备份
 */

public class SmsBackUp {

    private static int progress = 0;

    /**
     * 备份短信
     * 权限：<uses-permission ndroid:name="android.permission.READ_SMS" />
     *
     * @param context
     * @param callBack //情形：A,可能传递一个进度条所在的对话框ProgressDialog B,可能传递一个进度条Progress  所以用一个接口CallBack来实现
     */
    public static void backup(Context context, CallBack callBack) {
        // 先判断是否挂载SD卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ToastUtils.showToast(context, "未挂载SD卡");
            return;
        }

        FileOutputStream fos = null;
        Cursor cursor = null;

        try {
            // 序列化数据库中读取的数据,放置到xml中
            File file = new File(Environment.getExternalStorageDirectory(), "smsbackup.xml");
            fos = new FileOutputStream(file);

            //短信的uri,可以在系统应用源码中的短信内容提供者中查看
            Uri uri = Uri.parse("content://sms/");
            //获取内容解析器,获取短信数据库中数据  type = 1 --> 接收短信； type = 2 --> 发送短信
            cursor = context.getContentResolver().query(uri, new String[]{"address", "date", "type", "body"}, null, null, null);

            //得到 序列化器（android中使用 XmlSerializer 生成xml, 使用 XmlPullParser 解析xml）
            XmlSerializer xmlSerializer = Xml.newSerializer();

            //设置编码格式
            xmlSerializer.setOutput(fos, "utf-8");
            //参数2：standalone --> 表示xml是否是独立文件。 startDocument() 方法只能在 setOutput() 方法之后立即被调用
            xmlSerializer.startDocument("utf-8", true);

            xmlSerializer.startTag(null, "smss");
            xmlSerializer.attribute(null, "size", String.valueOf(cursor.getCount()));

            if (callBack != null) {
                callBack.onBefore(cursor.getCount());
            }

            while (cursor.moveToNext()) {
                KLog.i(cursor.getString(0) + "\n" + cursor.getString(1) + "\n" + cursor.getString(2) + "\n" + cursor.getString(3));

                xmlSerializer.startTag(null, "sms");

                xmlSerializer.startTag(null, "address");
                xmlSerializer.text(cursor.getString(0));
                xmlSerializer.endTag(null, "address");

                xmlSerializer.startTag(null, "date");
                xmlSerializer.text(cursor.getString(1));
                xmlSerializer.endTag(null, "date");

                xmlSerializer.startTag(null, "type");
                xmlSerializer.text(cursor.getString(2));
                xmlSerializer.endTag(null, "type");

                xmlSerializer.startTag(null, "body");
                // 读取短信内容并进行加密保存
                xmlSerializer.text(Crypto.encrypt("1234", cursor.getString(3)));
                xmlSerializer.endTag(null, "body");

                xmlSerializer.endTag(null, "sms");

                Thread.sleep(100);
                //每循环一次就需要去让进度条+1
                progress++;

                if (callBack != null) {
                    callBack.onProgress(progress);
                }
            }

            xmlSerializer.endTag(null, "smss");
            xmlSerializer.endDocument();


        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showToast(context, "备份失败！！");
        } finally {
            progress = 0;
            if (callBack != null) {
                callBack.onFinished();
            }
            try {
                if (cursor != null) {
                    cursor.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    //1.定义一个回调接口
    //情形：A,可能传递一个进度条所在的对话框ProgressDialog B,可能传递一个进度条Progress  所以用一个接口CallBack来实现
    //2,定义接口中未实现的业务逻辑方法(短信总数设置,备份过程中短信百分比更新)
    public interface CallBack {
        void onBefore(int count);

        void onProgress(int progress);

        void onFinished();
    }
}
