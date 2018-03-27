package com.xsquare.sourcecode.android.view;

import android.content.Context;
import android.view.*;
import android.view.View;

import static android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;

/**
 * Window是一个抽象的概念，也就是说它并不是实际存在的，它以View的形式存在，
 * 每个Window都对应着一个View和一个ViewRootImpl，Window与View通过ViewRootImpl来建立联系。
 * 推而广之，我们可以理解 WindowManagerService实际管理的也不是Window，而是View，
 * 管理在当前状态下哪个View应该在最上层显示，SurfaceFlinger绘制也同样是View。
 *
 * Window就是手机上一块显示区域，也就是Android中的绘制画布Surface，添加一个Window的过程，
 * 也就是申请分配一块Surface的过程。而整个流程的管理者正是WindowManagerService。
 * Created by xsquare on 2018/3/21.
 */

public abstract class Window {
    private boolean mCloseOnTouchOutside = false;

    /**
     * 判断点击事件，是否关闭事件
     */
    public boolean shouldCloseOnTouch(Context context, MotionEvent event) {
        //对于处理边界外点击事件的判断：是否是DOWN事件，event的坐标是否在边界内等
        if (mCloseOnTouchOutside && event.getAction() == MotionEvent.ACTION_DOWN
                && isOutOfBounds(context, event) && peekDecorView() != null) {
            return true;
        }
        //边界外消费事件，边界内未消费
        return false;
    }
    private boolean isOutOfBounds(Context context, MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
        final View decorView = getDecorView();
        return (x < -slop) || (y < -slop)
                || (x > (decorView.getWidth()+slop))
                || (y > (decorView.getHeight()+slop));
    }
    public abstract View getDecorView();
    public abstract View peekDecorView();
    public abstract boolean superDispatchTouchEvent(MotionEvent event);

    /**
     * 为子window布局参数设置属性
     * 属性：title、flag、packageName
     * @param wp LayoutParams
     */
    void adjustLayoutParamsForSubWindow(android.view.WindowManager.LayoutParams wp) {
        //获取LayoutParams的title
        CharSequence curTitle = wp.getTitle();
        //如果SubWindow为应用子窗口
        if (wp.type >= android.view.WindowManager.LayoutParams.FIRST_SUB_WINDOW &&
                wp.type <= android.view.WindowManager.LayoutParams.LAST_SUB_WINDOW) {
            //设置LayoutParams的token
            if (wp.token == null) {
                View decor = peekDecorView();
                if (decor != null) {
                    wp.token = decor.getWindowToken();
                }
            }
            //若没有title自行设置title
            if (curTitle == null || curTitle.length() == 0) {
                final StringBuilder title = new StringBuilder(32);
                if (wp.type == android.view.WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA) {
                    title.append("Media");
                } else if (wp.type == android.view.WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA_OVERLAY) {
                    title.append("MediaOvr");
                } else if (wp.type == android.view.WindowManager.LayoutParams.TYPE_APPLICATION_PANEL) {
                    title.append("Panel");
                } else if (wp.type == android.view.WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL) {
                    title.append("SubPanel");
                } else if (wp.type == android.view.WindowManager.LayoutParams.TYPE_APPLICATION_ABOVE_SUB_PANEL) {
                    title.append("AboveSubPanel");
                } else if (wp.type == android.view.WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG) {
                    title.append("AtchDlg");
                } else {
                    title.append(wp.type);
                }
                if (mAppName != null) {
                    title.append(":").append(mAppName);
                }
                wp.setTitle(title);
            }
            //如果SubWindow为系统窗口
        } else if (wp.type >= android.view.WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW &&
                wp.type <= android.view.WindowManager.LayoutParams.LAST_SYSTEM_WINDOW) {
            //如果没有title自行设置title
            if (curTitle == null || curTitle.length() == 0) {
                final StringBuilder title = new StringBuilder(32);
                title.append("Sys").append(wp.type);
                if (mAppName != null) {
                    title.append(":").append(mAppName);
                }
                wp.setTitle(title);
            }
        } else {//如果SubWindow为应用窗口
            if (wp.token == null) {
                wp.token = mContainer == null ? mAppToken : mContainer.mAppToken;
            }
            if ((curTitle == null || curTitle.length() == 0)
                    && mAppName != null) {
                //应用程序的窗口设置title为AppName
                wp.setTitle(mAppName);
            }
        }
        //设置packageName
        if (wp.packageName == null) {
            wp.packageName = mContext.getPackageName();
        }
        //设置flag
        if (mHardwareAccelerated ||
                (mWindowAttributes.flags & FLAG_HARDWARE_ACCELERATED) != 0) {
            wp.flags |= FLAG_HARDWARE_ACCELERATED;
        }
    }
}
