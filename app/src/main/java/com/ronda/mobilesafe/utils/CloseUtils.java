package com.ronda.mobilesafe.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/07/21
 * Version: v1.0
 */

public class CloseUtils {
    public static void close(Closeable closeable) {

        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
