package com.xsquare.sourcecode.android.app;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.xsquare.sourcecode.android.view.Window;

/**
 * Created by xsquare on 2018/3/21.
 */

public class Activity extends ContextThemeWrapper
        implements LayoutInflater.Factory2,
        View.OnCreateContextMenuListener, ComponentCallbacks2{
    /**
     * PhoneWindow对象，继承于Window，是窗口对象。
     * 每个Activity对应一个Window，每个Window对应一个ViewRootImpl。
     */
    private Window mWindow;
    /**
     * WindowManagerImpl对象，实现WindowManager接口。
     */
    private WindowManager mWindowManager;
    /**
     * Thread对象，主线程。
     */
    private Thread mUiThread;
    /**
     * Activity对象，并非真正的线程，是运行在主线程里的对象。
     */
    ActivityThread mMainThread;
    /**
     * Handler对象，主线程Handler。
     * 在构造FragmentController之前
     */
    final Handler mHandler = new Handler();
    /**
     * View对象，用来显示Activity里的视图。
     */
    View mDecor = null;

    public static final int DONT_FINISH_TASK_WITH_ACTIVITY = 0;
    Activity mParent;
    public static final int RESULT_CANCELED    = 0;
    int mResultCode = RESULT_CANCELED;
    Intent mResultData = null;
    /**
     * touch事件：用户点击屏幕就会产生事件，直到处理
     * MotionEvent.ACTION_DOWN	    按下View（所有事件的开始）
     * MotionEvent.ACTION_UP	    抬起View（与DOWN对应）
     * MotionEvent.ACTION_MOVE	    滑动View
     * MotionEvent.ACTION_CANCEL	结束事件（非人为原因）
     * 处理屏幕事件，可以在分发事件之前重写此方法拦截所有touch事件
     * 但要保证保证该事件被正常处理
     * 事件处理顺序：Activity >> ViewGroup >> View
     * @param ev 触摸事件
     * @return 如果此事件被消耗则返回true
     */
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //一般事件都是ACTION_DOWN开始
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //当前用户正在操作，空实现
            onUserInteraction();
        }
        //若返回true即事件结束(交由ViewGroup#DecorView处理)，否则Activity#onTouchEvent(ev)
//        if (getWindow().superDispatchTouchEvent(ev)) {
//            return true;
//        }
        return onTouchEvent(ev);
    }

    /**
     * 当一个点击事件未被Activity下任何一个View接收 / 处理时
     * 应用场景：处理发生在Window边界外的触摸事件
     * @param event 触摸事件
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        if (mWindow.shouldCloseOnTouch(this, event)) {
            //finish();
            return true;
        }
        //即 只有在点击事件在Window边界外才会返回true，一般情况都返回false
        return false;
    }
    /**
     * 可重写此方法（当用户在当前Activity操作）
     * activity无论分发按键事件、触摸事件或者轨迹球事件都会调用Activity#onUserInteraction()。
     * 如果你想知道用户用某种方式和你正在运行的activity交互，可以重写Activity#onUserInteraction()。
     * 所有调用Activity#onUserLeaveHint()的回调都会首先回调Activity#onUserInteraction()。
     * 注意：启动另一个activity,Activity#onUserInteraction()会被调用两次，一次是activity捕获到事件，
     * 另一次是调用Activity#onUserLeaveHint()之前会调用Activity#onUserInteraction()。
     */
    public void onUserInteraction() {
    }
    /**
     * 可重写此方法（当用户离开当前Activity）
     * 当用户的操作使一个activity准备进入后台时，此方法会像activity的生命周期的一部分被调用。
     * 例如，当用户按下Home键， Activity#onUserLeaveHint()将会被回调。
     * 但是当来电导致来电activity自动占据前台，Activity#onUserLeaveHint()将不会被回调。
     * 用户手动离开当前activity，会调用该方法，比如用户主动切换任务，短按home进入桌面等。
     * 系统自动切换activity不会调用此方法，如来电，灭屏等。
     */
    protected void onUserLeaveHint() {
    }


    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public void onLowMemory() {

    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return null;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

    }
}
