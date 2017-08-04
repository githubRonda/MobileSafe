package com.ronda.mobilesafe.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.PopupWindow;

import java.lang.reflect.Method;

/**
 * Author: Ronda(1575558177@qq.com)
 * Date: 2017/06/12
 * Version: v1.0
 */

public class SystemBarUtils {

    private static final String TAG = SystemBarUtils.class.getSimpleName();

    /**
     * //透明状态栏
     * getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
     * //透明导航栏
     * getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    /**
     * 检查该设备是是否有底部导航栏
     * 注意：这个方法检查的是该设备本身固有的属性，即若是设备本身是有导航栏的，但是被代码隐藏掉了，该方法返回的还是true.
     * eg: 小米4返回的是false, 虚拟机返回的是true
     *
     * @param context
     * @return
     */
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return hasNavigationBar;
    }

    /**
     * 获取导航栏高度
     * 若该设备不存在导航栏的话，则返回0
     *
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && checkDeviceHasNavigationBar(context)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }


    /**
     * 直接隐藏导航栏
     *
     * @param activity
     */
    public static void hiddenNavigationBar(Activity activity) {
        int flags;
        int curApiVersion = android.os.Build.VERSION.SDK_INT;
        if (curApiVersion >= Build.VERSION_CODES.KITKAT) {
            // This work only for android 4.4+
            // hide navigation bar permanently in android activity
            // touch the screen, the navigation bar will not show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
        } else {
            // touch the screen, the navigation bar will show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // must be executed in main thread :)
        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    /**
     * 监听navigationbar显示与否，若显示，则让其隐藏
     *
     * @param activity
     */
    public static void Listen2hiddenNavigationBar(final Activity activity) {
        final View decorView = activity.getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                new Handler().post(new Runnable() {
                                       @Override
                                       public void run() {
                                           hiddenNavigationBar(activity);
                                       }

                                   }
                );
            }
        });
    }

    public static void setHideVirtalKey(PopupWindow popupWindow) {
        //保持布局状态
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                //布局位于状态栏下方
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                //全屏
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                //隐藏导航栏
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 19) {
            uiOptions |= 0x00001000;
        } else {
            uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }

//        Field field = null;
//        try {
//            field = popupWindow.getClass().getDeclaredField("mDecorView");
//            field.setAccessible(true);
//            View  view = (View) field.get(popupWindow);
//
//            view.setSystemUiVisibility(uiOptions);
//
//            KLog.e("aaaaaaaaaaaaaaaaaaaaaaa");
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }

        popupWindow.getContentView().setSystemUiVisibility(uiOptions);
    }

    public static void setHideVirtualKey(Window window) {
        //保持布局状态
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                //布局位于状态栏下方
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                //全屏
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                //隐藏导航栏
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 19) {
            uiOptions |= 0x00001000;
        } else {
            uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        window.getDecorView().setSystemUiVisibility(uiOptions);
    }

    public static void setAndListen2Hide(final Window window) {
        setHideVirtualKey(window);

        window.getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                setHideVirtualKey(window);
            }
        });
    }


    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
    public static void toggleHideyBar(Window window) {

        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        //int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int uiOptions = window.getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(TAG, "Turning immersive mode mode off. ");
        } else {
            Log.i(TAG, "Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        //getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        window.getDecorView().setSystemUiVisibility(newUiOptions);
    }

}
