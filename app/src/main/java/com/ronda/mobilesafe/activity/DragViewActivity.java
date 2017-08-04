package com.ronda.mobilesafe.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ronda.mobilesafe.R;
import com.ronda.mobilesafe.utils.SystemBarUtils;
import com.socks.library.KLog;

/**
 * View 的绘制流程： onMeasure()[测量view大小等] --> onLayout()[确定View的位置] --> onDraw()[View内容的绘制]
 * View.requestLayout() 请求重新布局
 * View.invalidate()    刷新视图，相当于调用View.onDraw()方法
 */
public class DragViewActivity extends AppCompatActivity {

    private TextView mTvTop;
    private TextView mTvBottom;
    private ImageView mIvDrag;
    private int mStartX;
    private int mStartY;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_view);

        mPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);

        mTvTop = (TextView) findViewById(R.id.tv_top);
        mTvBottom = (TextView) findViewById(R.id.tv_bottom);
        mIvDrag = (ImageView) findViewById(R.id.iv_drag);

        int lastX = mPreferences.getInt("lastX", 0);
        int lastY = mPreferences.getInt("lastY", 0);


        // getSize()
        final int screenWidth = getWindowManager().getDefaultDisplay().getWidth(); //获取屏幕的宽度
        final int screenHeight = getWindowManager().getDefaultDisplay().getHeight(); // 获取屏幕的高度（包括statusBar，ActionBar，但是不包括NavigationBar）

        KLog.e("screenWidth: " + screenWidth + ", screenHeight: " + screenHeight);

        // View 的绘制流程： onMeasure()[测量view大小等] --> onLayout()[确定View的位置] --> onDraw()[View内容的绘制]
        // 注意：下面直接这样绘制mIvDrag的位置时没有效果的，因为这里是在onCreate()方法中，此时View都还没有调用 onMeasure()方法测量大小，所以getWidth()和getHeight()都为0。
        // 解决方法：既然这里还不能调用layout方法重新定位位置，那么我们可以设置其layoutParams参数，这样View在显示过程中回调onLayout()方法时，就可以使用我们设置的layoutParams参数了
        // mIvDrag.layout(lastX, lastY, mIvDrag.getWidth() + lastX, mIvDrag.getHeight() + lastY); // onCreate() 中还未测量View

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mIvDrag.getLayoutParams(); // 获取布局参数
        params.leftMargin = lastX;// 更新左边距
        params.topMargin = lastY; // 更新上边距
        mIvDrag.setLayoutParams(params); // 这句代码可加可不加，因为java中都是引用，而这个params是从mIvDrag中获取来的


        mIvDrag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 初始化起点坐标
                        mStartX = (int) event.getRawX();
                        mStartY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int endX = (int) event.getRawX();
                        int endY = (int) event.getRawY();

                        //KLog.w("endX: " + endX + " endY: " + endY);

                        // 计算移动偏移量
                        int dx = endX - mStartX;
                        int dy = endY - mStartY;

                        // 更新左上右下距离
                        int left = mIvDrag.getLeft() + dx;
                        int top = mIvDrag.getTop() + dy;
                        int right = mIvDrag.getRight() + dx;
                        int bottom = mIvDrag.getBottom() + dy;

                        // 判断是否超出屏幕边界, 注意状态栏的高度
                        // screenHeight包括顶部的StatusBar，而layout()方法的形参其实就是layoutParams参数，而这个绘制的参考点是StatusBar和ActionBar下边部分的左上角的那个参考点
                        if (left < 0 || right > screenWidth || top < 0 || bottom > screenHeight - SystemBarUtils.getStatusBarHeight(DragViewActivity.this)) {
                            break;
                        }

                        // 根据DragView图片的位置,决定提示框显示和隐藏
                        if (top >= (screenHeight - SystemBarUtils.getStatusBarHeight(DragViewActivity.this)) / 2) {// 上边显示,下边隐藏
                            mTvTop.setVisibility(View.VISIBLE);
                            mTvBottom.setVisibility(View.INVISIBLE);
                        } else {
                            mTvTop.setVisibility(View.INVISIBLE);
                            mTvBottom.setVisibility(View.VISIBLE);
                        }

                        // 更新界面:调用layout()重新绘制mIvDrag的位置。
                        // 其实也可以使用setLayoutParams()来实现： mIvDrag.setLayoutParams(params);  mIvDrag.requestLayout(); //请求重新布局
                        mIvDrag.layout(left, top, right, bottom);

                        // 重新初始化起点坐标
                        mStartX = endX;
                        mStartY = endY;
                        break;
                    case MotionEvent.ACTION_UP:
                        // 持久化记录坐标点位置
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putInt("lastX", mIvDrag.getLeft());
                        editor.putInt("lastY", mIvDrag.getTop());
                        editor.commit();
                        break;
                }
                return false; // 不消费该事件，否者onClick就接收不到事件了
            }
        });

        final long[] mHits = new long[2];
        mIvDrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);// 每次点击其实就是把mHits这个long类型的数组向前移动一位
                mHits[mHits.length - 1] = SystemClock.uptimeMillis(); // uptimeMillis() 从开机到现在的毫秒数（手机睡眠的时间不包括在内）
                if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                    // 把图片水平居中
                    mIvDrag.layout((screenWidth - mIvDrag.getWidth()) / 2, mIvDrag.getTop(),
                            screenWidth / 2 + mIvDrag.getWidth() / 2, mIvDrag.getBottom());

                    // 这里有点小瑕疵：
                    // 这个mIvDrag的水平居中其实并不等于来电提示时的归属地View的水平居中，因为这两个View的大小是不一样。
                    // 解决方法：把mIvDrag的这个图片p大一点，和归属地提示的View的大小一样，并且还要限制归属地提示View的大小和显示的字符
                    SharedPreferences.Editor edit = mPreferences.edit();
                    edit.putInt("lastX", mIvDrag.getLeft());
                    edit.putInt("lastY", mIvDrag.getTop());
                    edit.commit();
                }
            }
        });
    }
}
