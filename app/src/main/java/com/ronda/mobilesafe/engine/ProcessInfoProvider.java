package com.ronda.mobilesafe.engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Debug;
import android.os.Process;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.bean.ProcessInfo;
import com.ronda.mobilesafe.utils.CloseUtils;
import com.socks.library.KLog;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/07/21
 * Version: v1.0
 */

public class ProcessInfoProvider {

    /**
     * 获取进程总数(ActivityManager.getRunningAppProcesses())
     *
     * @param context
     * @return
     */
    public static int getProcessCount(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        return runningAppProcesses.size();
    }

    /**
     * 获取剩余的内存 ActivityManager.getMemoryInfo();
     *
     * @param context
     * @return 内存数，byte
     */
    public static long getAvailSpace(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    /**
     * 获取总内存数
     *
     * @param context
     * @return 内存数，byte 返回0说明异常
     */
    public static long getTotalSpace(Context context) {
        //ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        //am.getMemoryInfo(memoryInfo);
        //return memoryInfo.totalMem;// 这个memoryInfo.totalMem的API必须要api16才可以使用

        // /proc/meminfo --> 内存的实时信息文件  /proc/cpuinfo --> cup 的实时信息文件
        // 所以现在就是要读取 /proc/meminfo 文件的第一行，提取其中的数字字符， 还要转成kb转成byte
        // MemTotal:        1551640 kB
        // MemFree:         1014716 kB

        BufferedReader bufferedReader = null;
        long totalSpace = 0;
        try {
            FileReader fileReader = new FileReader("/proc/meminfo");
            bufferedReader = new BufferedReader(fileReader);
            String firstLine = bufferedReader.readLine();
            char[] chars = firstLine.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : chars) {
                // 提取数字字符
                if (c >= '0' && c <= '9') {
                    builder.append(c);
                }
            }

            totalSpace = Long.parseLong(builder.toString()) * 1024;// 单位是KB, 转成byte
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.close(bufferedReader);
        }
        return totalSpace;
    }


    /**
     * 获取所有正在运行的进程信息，给ListView显示
     *
     * @param context
     * @return
     */
    public static List<ProcessInfo> getProcessInfos(Context context) {

        List<ProcessInfo> processInfoList = new ArrayList<>();
        // ActivityManager : 与系统中正在运行的所有活动进行交互。
        // 对应的是TaskInfo, StackInfo, ServiceInfo, MemoryInfo, ProcessInfo, DeviceConfigurationInfo等
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // PackageManager : 检索当前安装在设备上的应用程序包相关的各种信息
        PackageManager packageManager = context.getPackageManager();

        // 使用ActivityManager.getRunningAppProcesses()获取正在运行的进程的集合
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        // 遍历集合，获取进程相关的信息（名称，包名，图标，使用内存大小，是否为系统进程（状态机））
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {
            ProcessInfo processInfo = new ProcessInfo();

            // 进程的名称 == 应用的包名
            processInfo.setPackageName(info.processName);

            // 获取进程占用的内存大小（传递一个进程对应的pid数组, 返回的也是一个MemoryInfo数组，索引值是一一对应的，所以可以获取多个进程的MemoryInfo,只要有pid即可）
            Debug.MemoryInfo[] processMemoryInfo = activityManager.getProcessMemoryInfo(new int[]{info.pid});
            processInfo.setMemSize(processMemoryInfo[0].getTotalPrivateDirty() * 1024);// Dirty意思是脏的，就是表示已使用的，返回的是KB，所以要乘以1024

            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(info.processName, 0);

                //使用ApplicationInfo获取图标
                processInfo.setIcon(applicationInfo.loadIcon(packageManager));

                //使用ApplicationInfo获取应用名称
                processInfo.setName(applicationInfo.loadLabel(packageManager).toString());

                // 判断是否为系统进程
                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                    processInfo.setSystem(true);
                } else {
                    processInfo.setSystem(false);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                // 若没有根据包名找到ApplicationInfo, 则需要自己处理 图标，名称，是否是系统进程
                processInfo.setIcon(context.getResources().getDrawable(R.mipmap.ic_launcher));// 图标设为本应用的启动图标
                processInfo.setName(info.processName); // 名称设为包名
                processInfo.setSystem(true); // 设为系统进程
            }

            processInfoList.add(processInfo);
        }
        return processInfoList;
    }


    /**
     * 杀死指定名称（包名）的进程。使用ActivityManager.killBackgroundProcesses(String)方法 （仅限于可以被杀死的进程）
     * 权限：<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES"/>
     *
     * @param context
     * @param processInfo
     */
    public static void killProcess(Context context, ProcessInfo processInfo) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(processInfo.getPackageName());
    }

    /**
     * 杀死所有正在运行的进程（除了手机卫士）
     *
     * @param context
     */
    public static void killAll(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        //杀死除了安全卫士之外的其他所有正在运行的进程
        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
            if (context.getPackageName().equals(runningAppProcess.processName)) {
                continue;
            }
            activityManager.killBackgroundProcesses(runningAppProcess.processName);
        }
    }
}
