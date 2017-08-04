package com.ronda.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 与流相关的工具类
 * <p>
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/05/19
 * Version: v1.0
 */

public class StreamUtils {

    public static String readFromStream(InputStream in) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int len = 0;
        byte[] buf = new byte[1024];

        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        String result = out.toString();
        in.close();
        out.close();
        return result;
    }
}
