package com.ronda.mobilesafe.utils;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/05/23
 * Version: v1.0
 */

public class MD5Utils {
    /**
     * 对密码进行MD5加密
     *
     * @param password
     * @return
     */
    public static String encode(String password) {
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5"); // 获取MD5算法对象
            byte[] digest = instance.digest(password.getBytes()); // 对字符串加密，返回字节数组

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                int i = b & 0xff; // 获取字节低八位有效值，因为b有可能是负值
                String hexString = Integer.toHexString(i);

                if (hexString.length() < 2) {
                    hexString = "0" + hexString;
                }

                sb.append(hexString);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }


    /**
     * 获取某个文件的md5
     * @param path
     * @return
     */
    public static String getFileMd5(String path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            FileInputStream in = new FileInputStream(path);

            int len = 0;
            byte[] buffer = new byte[1024];

            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }

            byte[] result = digest.digest();

            StringBuffer sb = new StringBuffer();
            for (byte b : result) {
                int i = b & 0xff;// 将字节转为整数
                String hexString = Integer.toHexString(i);// 将整数转为16进制

                if (hexString.length() == 1) {
                    hexString = "0" + hexString;// 如果长度等于1, 加0补位
                }

                sb.append(hexString);
            }

            System.out.println(sb.toString());// 打印得到的md5
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
