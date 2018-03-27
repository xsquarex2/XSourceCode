package com.xsquare.sourcecode.android.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import android.util.ArraySet;
import android.view.*;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.xsquare.sourcecode.android.os.SystemProperties;

import java.util.ArrayList;

/**
 * WindowManagerService是位于Framework层的窗口管理服务，它的职责是管理系统中的所有窗口，也就是Window
 * Created by xsquare on 2018/3/26.
 */

public class WindowManagerGlobal {
    private final Object mLock = new Object();

    private final ArrayList<View> mViews = new ArrayList<View>();
    private final ArrayList<ViewRootImpl> mRoots = new ArrayList<ViewRootImpl>();
    private final ArrayList<WindowManager.LayoutParams> mParams =
            new ArrayList<WindowManager.LayoutParams>();
    @SuppressLint("NewApi")
    private final ArraySet<View> mDyingViews = new ArraySet<View>();
    private Runnable mSystemPropertyUpdater;
    private static WindowManagerGlobal sDefaultWindowManager;

    public static WindowManagerGlobal getInstance() {
        synchronized (WindowManagerGlobal.class) {
            if (sDefaultWindowManager == null) {
                sDefaultWindowManager = new WindowManagerGlobal();
            }
            return sDefaultWindowManager;
        }
    }

    /**
     * IWindowSession对象，Session的代理对象，用来和Session进行通信，
     * 同一进程里的所有ViewRootImpl对象只对应同一个Session代理对象。
     *
     * Android的各种服务都是基于C/S结构来设计的，系统层提供服务，应用层使用服务。
     * WindowManager也是一样，它与 WindowManagerService的通信是通过WindowSession来完成的。
     *
     * 1. 首先调用ServiceManager.getService("window")获取WindowManagerService，该方法返回的是IBinder对象，
     * 然后调用IWindowManager.Stub.asInterface()方法将WindowManagerService转换为一个IWindowManager对象。
     * 2. 然后调用openSession()方法与WindowManagerService建立一个通信会话，方便后续的跨进程通信。这个通信会话就是后面我们用到的WindowSession。
     */
    public static IWindowSession getWindowSession() {
        synchronized (WindowManagerGlobal.class) {
            if (sWindowSession == null) {
                try {
                    InputMethodManager imm = InputMethodManager.getInstance();
                    // //获取WindowManagerService对象，并将它转换为IWindowManager类型
                    IWindowManager windowManager = getWindowManagerService();
                    //调用openSession()方法与WindowManagerService建立一个通信会话，方便后续的
                    //跨进程通信。
                    sWindowSession = windowManager.openSession(
                            new IWindowSessionCallback.Stub() {
                                @Override
                                public void onAnimatorScaleChanged(float scale) {
                                    ValueAnimator.setDurationScale(scale);
                                }
                            },
                            imm.getClient(), imm.getInputContext());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            return sWindowSession;
        }
    }
    public static IWindowManager getWindowManagerService() {
        synchronized (WindowManagerGlobal.class) {
            if (sWindowManagerService == null) {
                //调用ServiceManager.getService("window")获取WindowManagerService，该方法返回的是IBinder对象
                //，然后调用IWindowManager.Stub.asInterface()方法将WindowManagerService转换为一个IWindowManager对象
                sWindowManagerService = IWindowManager.Stub.asInterface(
                        ServiceManager.getService("window"));
                try {
                    if (sWindowManagerService != null) {
                        ValueAnimator.setDurationScale(
                                sWindowManagerService.getCurrentAnimatorScale());
                    }
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            return sWindowManagerService;
        }
    }

    /**
     * 添加view
     * @param view 要被添加的view
     * @param params params
     * @param display display mContext.getDisplay()
     * @param parentWindow 添加的parentWindow
     */
    public void addView(android.view.View view, android.view.ViewGroup.LayoutParams params,
                        Display display, Window parentWindow) {
        //校验参数合法性
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        }
        if (display == null) {
            throw new IllegalArgumentException("display must not be null");
        }
        if (!(params instanceof android.view.WindowManager.LayoutParams)) {
            throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
        }

        final android.view.WindowManager.LayoutParams wparams = (android.view.WindowManager.LayoutParams) params;
        if (parentWindow != null) {
            //为父window不为null，也会为父window设置LayoutParams相关属性(flag、packageName、title)
            parentWindow.adjustLayoutParamsForSubWindow(wparams);
        } else {
            // 如果parentWindow为null,如果应用设置了硬件加速，也会为该wparams设置硬件加速
            final Context context = view.getContext();
            if (context != null
                    && (context.getApplicationInfo().flags
                    & ApplicationInfo.FLAG_HARDWARE_ACCELERATED) != 0) {
                wparams.flags |= android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            }
        }
        //ViewRootImpl封装了View与WindowManager的交互,是View与WindowManagerService通信的桥梁
        ViewRootImpl root;
        View panelParentView = null;

        synchronized (mLock) {
            //监听系统配置变化
            if (mSystemPropertyUpdater == null) {
                mSystemPropertyUpdater = new Runnable() {
                    @Override public void run() {
                        synchronized (mLock) {
                            for (int i = mRoots.size() - 1; i >= 0; --i) {
                                //系统配置变化，则为所有ViewRootImpl重新加载系统配置
                                mRoots.get(i).loadSystemProperties();
                            }
                        }
                    }
                };
                //监听系统配置变化，添加回调
                SystemProperties.addChangeCallback(mSystemPropertyUpdater);
            }
            //找出该view的系列号
            int index = findViewLocked(view, false);
            if (index >= 0) {
                //如果该view将要消除，将立即删除（之前发送msg等待队列执行移除操作）
                if (mDyingViews.contains(view)) {
                    mRoots.get(index).doDie();
                } else {
                    throw new IllegalStateException("View " + view
                            + " has already been added to the window manager.");
                }
                // 之前的removeView（）尚未完成执行。, 现在它已经完成。
            }

            // 如果这是一个面板窗口，那么找到它所连接的窗口以备将来参考。
            if (wparams.type >= android.view.WindowManager.LayoutParams.FIRST_SUB_WINDOW &&
                    wparams.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW) {
                final int count = mViews.size();
                for (int i = 0; i < count; i++) {
                    if (mRoots.get(i).mWindow.asBinder() == wparams.token) {
                        panelParentView = mViews.get(i);
                    }
                }
            }
            //通过上下文构建ViewRootImpl
            root = new ViewRootImpl(view.getContext(), display);
            view.setLayoutParams(wparams);
            //mViews存储着所有Window对应的View对象
            mViews.add(view);
            ////mRoots存储着所有Window对应的ViewRootImpl对象
            mRoots.add(root);
            ////mParams存储着所有Window对应的WindowManager.LayoutParams对象
            mParams.add(wparams);
            //最后做这件事是因为它触发了消息开始做事
            try {
                //调用ViewRootImpl.setView()方法完成Window的添加并更新界面
                root.setView(view, wparams, panelParentView);
            } catch (RuntimeException e) {
                // BadTokenException or InvalidDisplayException, clean up.
                if (index >= 0) {
                    removeViewLocked(index, true);
                }
                throw e;
            }
        }
    }
    //更新view布局

    /**
     * 1. 更新View的LayoutParams参数，查找Viewd的索引，更新mParams里的参数。
     * 2. 调用ViewRootImpl.setLayoutParams()方法完成重新布局的工作，在setLayoutParams()方法里最终会调用
     * @param view
     * @param params
     */
    public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
        //校验合法性
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        }
        if (!(params instanceof WindowManager.LayoutParams)) {
            throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
        }
        final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams)params;
        //设置布局params
        view.setLayoutParams(wparams);
        //锁住，重新设置view的布局参数更新view
        synchronized (mLock) {
            //查找Viewd的索引，更新mParams里的参数
            int index = findViewLocked(view, true);
            ViewRootImpl root = mRoots.get(index);
            mParams.remove(index);
            mParams.add(index, wparams);
            //调用ViewRootImpl.setLayoutParams()完成重新布局的工作。
            root.setLayoutParams(wparams, false);
        }
    }

    /**
     * Window的删除流程：
     * 1. 查找待删除View的索引
     * 2. 调用removeViewLocked()完成View的删除, removeViewLocked()方法继续调用ViewRootImpl.die()方法来完成View的删除。
     * 3. ViewRootImpl.die()方法根据immediate参数来判断是执行异步删除还是同步删除，
     * 如果是异步删除则则发送一个删除View的消息MSG_DIE就会直接返回。 如果是同步删除，则调用doDie()方法。
     * 4. doDie()方法调用dispatchDetachedFromWindow()完成View的删除，在该方法里首先回调View的dispatchDetachedFromWindow方法，
     * 通知该View已从Window中移除， 然后调用WindowSession.remove()方法，这同样是一个IPC过程，
     * 最终调用的是WindowManagerService.removeWindow()方法来移除Window。
     * @param view 要移除的view
     * @param immediate
     */
    public void removeView(View view, boolean immediate) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        }
        synchronized (mLock) {
            //1. 查找待删除View的索引
            int index = findViewLocked(view, true);
            View curView = mRoots.get(index).getView();
            //2. 调用removeViewLocked()完成View的删除, removeViewLocked()方法
            //继续调用ViewRootImpl.die()方法来完成View的删除。
            removeViewLocked(index, immediate);
            if (curView == view) {
                return;
            }
            throw new IllegalStateException("Calling with view " + view
                    + " but the ViewAncestor is attached to " + curView);
        }
    }

    /**
     * 移除view
     * @param index 所有views中的序列号
     * @param immediate 是否立即
     */
    private void removeViewLocked(int index, boolean immediate) {
        //从mRoots中获取viewRootImpl
        ViewRootImpl root = mRoots.get(index);
        //获取DecorView
        View view = root.getView();
        if (view != null) {
            InputMethodManager imm = InputMethodManager.getInstance();
            if (imm != null) {
                imm.windowDismissed(mViews.get(index).getWindowToken());
            }
        }

        boolean deferred = root.die(immediate);
        if (view != null) {
            view.assignParent(null);
            if (deferred) {
                //添加至将要消亡的集合
                mDyingViews.add(view);
            }
        }
    }

    /**
     * 已经从WindowManagerService中移除
     * 从mRoots,mParams,mDyingViews移除
     * @param root
     */
    void doRemoveView(ViewRootImpl root) {
        synchronized (mLock) {
            final int index = mRoots.indexOf(root);
            if (index >= 0) {
                mRoots.remove(index);
                mParams.remove(index);
                final View view = mViews.remove(index);
                mDyingViews.remove(view);
            }
        }
        if (ThreadedRenderer.sTrimForeground && ThreadedRenderer.isAvailable()) {
            doTrimForeground();
        }
    }

    /**
     * 找到window中所有view的序列号
     * @param view  找出view
     * @param required
     * @return 序列号
     */
    private int findViewLocked(View view, boolean required) {
        final int index = mViews.indexOf(view);
        if (required && index < 0) {
            throw new IllegalArgumentException("View=" + view + " not attached to window manager");
        }
        return index;
    }
}
