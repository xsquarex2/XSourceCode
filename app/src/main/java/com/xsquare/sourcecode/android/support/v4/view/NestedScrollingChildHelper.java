package com.xsquare.sourcecode.android.support.v4.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewCompat.NestedScrollType;
import android.support.v4.view.ViewCompat.ScrollAxis;
import android.support.v4.view.ViewParentCompat;
import android.view.View;
import android.view.ViewParent;

import static android.support.v4.view.ViewCompat.TYPE_NON_TOUCH;
import static android.support.v4.view.ViewCompat.TYPE_TOUCH;

/**
 * 用于实现与Android平台版本兼容（5.0以前版本）的嵌套滚动子视图的助手类
 */
public class NestedScrollingChildHelper {
    private ViewParent mNestedScrollingParentTouch;
    private ViewParent mNestedScrollingParentNonTouch;
    private final View mView;
    private boolean mIsNestedScrollingEnabled;
    private int[] mTempNestedScrollConsumed;

    /**
     * 为给定的视图构造一个新的helper
     */
    public NestedScrollingChildHelper(@NonNull View view) {
        mView = view;
    }
    /**
     * 设置是否支持嵌套滚动
     */
    public void setNestedScrollingEnabled(boolean enabled) {
        if (mIsNestedScrollingEnabled) {
            ViewCompat.stopNestedScroll(mView);
        }
        mIsNestedScrollingEnabled = enabled;
    }
    /**
     * 是否支持嵌套滚动
     */
    public boolean isNestedScrollingEnabled() {
        return mIsNestedScrollingEnabled;
    }
    /**
     * 检查是否有支持嵌套滚动的父类
     */
    public boolean hasNestedScrollingParent() {
        return hasNestedScrollingParent(TYPE_TOUCH);
    }
    /**
     * 检查是否有支持嵌套滚动的父类
     */
    public boolean hasNestedScrollingParent(@NestedScrollType int type) {
        return getNestedScrollingParentForType(type) != null;
    }

    /**
     * 开始嵌套滚动
     * @param axes 滚动的轴向
     *             See {@link android.support.v4.view.NestedScrollingChild#startNestedScroll(int)}.
     * @return 如果有配合嵌套的父类
     */
    public boolean startNestedScroll(@ScrollAxis int axes) {
        return startNestedScroll(axes, TYPE_TOUCH);
    }
    /**
     * 开始嵌套滚动
     * @param axes 滚动轴向
     *             See {@link android.support.v4.view.NestedScrollingChild2#startNestedScroll(int,
     *             int)}.
     * @return 如果有配合嵌套的父类
     */
    public boolean startNestedScroll(@ScrollAxis int axes, @NestedScrollType int type) {
        if (hasNestedScrollingParent(type)) {
            // 已经在滚动中了
            return true;
        }
        if (isNestedScrollingEnabled()) {
            ViewParent p = mView.getParent();
            View child = mView;
            // 递归向上寻找 支持 嵌套滑动的父View
            while (p != null) {
                // 这里会调用 父View 的NestedScrollingParent.onStartNestedScroll 方法
                // 如果 父View 返回 false  则再次向上寻找父View , 直到找到支持的父View
                if (ViewParentCompat.onStartNestedScroll(p, child, mView, axes, type)) {
                    setNestedScrollingParentForType(type, p);
                    // 这里回调 父View 的onNestedScrollAccepted 方法 表示开始接收 嵌套滑动
                    ViewParentCompat.onNestedScrollAccepted(p, child, mView, axes, type);
                    return true;
                }
                if (p instanceof View) {
                    child = (View) p;
                }
                p = p.getParent();
            }
        }
        // 没有找到 支持嵌套滑动的父View  则返回false
        return false;
    }

    /**
     * 停止嵌套滚动
     */
    public void stopNestedScroll() {
        stopNestedScroll(TYPE_TOUCH);
    }

    /**
     * 停止嵌套滚动
     */
    public void stopNestedScroll(@NestedScrollType int type) {
        ViewParent parent = getNestedScrollingParentForType(type);
        if (parent != null) {
            ViewParentCompat.onStopNestedScroll(parent, mView, type);
            setNestedScrollingParentForType(type, null);
        }
    }

    /**
     * @param dxConsumed  x 上被消费的距离
     * @param dyConsumed  y 上被消费的距离
     * @param dxUnconsumed  x 上未被消费的距离
     * @param dyUnconsumed  y 上未被消费的距离
     * @param offsetInWindow  子View 位置的移动距离
     */
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
            int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        return dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow, TYPE_TOUCH);
    }
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
            int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow,
            @NestedScrollType int type) {
        if (isNestedScrollingEnabled()) {
            final ViewParent parent = getNestedScrollingParentForType(type);
            if (parent == null) {
                return false;
            }

            if (dxConsumed != 0 || dyConsumed != 0 || dxUnconsumed != 0 || dyUnconsumed != 0) {
                int startX = 0;
                int startY = 0;
                if (offsetInWindow != null) {
                    mView.getLocationInWindow(offsetInWindow);
                    startX = offsetInWindow[0];
                    startY = offsetInWindow[1];
                }
                // 父View 回调 onNestedScroll 方法, 该放在 主要会处理  dxUnconsumed dyUnconsumed 数据
                ViewParentCompat.onNestedScroll(parent, mView, dxConsumed,
                        dyConsumed, dxUnconsumed, dyUnconsumed, type);

                if (offsetInWindow != null) {
                    // 计算 子View的移动距离
                    mView.getLocationInWindow(offsetInWindow);
                    offsetInWindow[0] -= startX;
                    offsetInWindow[1] -= startY;
                }
                return true;
            } else if (offsetInWindow != null) {
                // No motion, no dispatch. Keep offsetInWindow up to date.
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    /**
     * consumed[0]  为0 时 表示 x 轴方向上事件 没有被消费
     *              不为0 时 表示 x 轴方向上事件 被消费了, 值表示 被消费的滑动距离
     * consumed[1]  为0 时 表示 y 轴方向上事件 没有被消费
     *              不为0 时 表示 y 轴方向上事件 被消费了, 值表示 被消费的滑动距离
     * @param dx
     * @param dy
     * @param consumed
     * @param offsetInWindow
     * @return
     */
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed,
            @Nullable int[] offsetInWindow) {
        return dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, TYPE_TOUCH);
    }
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed,
            @Nullable int[] offsetInWindow, @NestedScrollType int type) {
        if (isNestedScrollingEnabled()) {
            final ViewParent parent = getNestedScrollingParentForType(type);
            if (parent == null) {
                return false;
            }

            if (dx != 0 || dy != 0) {
                int startX = 0;
                int startY = 0;
                // 获取 当前View 初始位置
                if (offsetInWindow != null) {
                    mView.getLocationInWindow(offsetInWindow);
                    startX = offsetInWindow[0];
                    startY = offsetInWindow[1];
                }
                // 初始化是否被消费数据
                if (consumed == null) {
                    if (mTempNestedScrollConsumed == null) {
                        mTempNestedScrollConsumed = new int[2];
                    }
                    consumed = mTempNestedScrollConsumed;
                }
                consumed[0] = 0;
                consumed[1] = 0;
                // 这里回调 父View 的 onNestedPreScroll 方法,
                // 父View 或许会处理 相应的滑动事件,
                // 如果 处理了 则 consumed 会被赋予 相应的值
                ViewParentCompat.onNestedPreScroll(parent, mView, dx, dy, consumed, type);

                if (offsetInWindow != null) {
                    // 父View 处理了相应的滑动,  很可能导致 子View 的位置的移动
                    // 这里计算出  父view 消费 滑动事件后,  导致 子View 的移动距离
                    mView.getLocationInWindow(offsetInWindow);
                    // 这里 子View 的移动距离
                    offsetInWindow[0] -= startX;
                    offsetInWindow[1] -= startY;
                }
                // 如果  xy 方向 上 有不为0 的表示消费了 则返回true
                return consumed[0] != 0 || consumed[1] != 0;
            } else if (offsetInWindow != null) {
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    /**
     * Dispatch a nested fling operation to the current nested scrolling parent.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link android.support.v4.view.NestedScrollingChild} interface method with the same
     * signature to implement the standard policy.</p>
     *
     * @return true if the parent consumed the nested fling
     */
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        if (isNestedScrollingEnabled()) {
            ViewParent parent = getNestedScrollingParentForType(TYPE_TOUCH);
            if (parent != null) {
                return ViewParentCompat.onNestedFling(parent, mView, velocityX,
                        velocityY, consumed);
            }
        }
        return false;
    }

    /**
     * Dispatch a nested pre-fling operation to the current nested scrolling parent.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link android.support.v4.view.NestedScrollingChild} interface method with the same
     * signature to implement the standard policy.</p>
     *
     * @return true if the parent consumed the nested fling
     */
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        if (isNestedScrollingEnabled()) {
            ViewParent parent = getNestedScrollingParentForType(TYPE_TOUCH);
            if (parent != null) {
                return ViewParentCompat.onNestedPreFling(parent, mView, velocityX,
                        velocityY);
            }
        }
        return false;
    }

    /**
     * View subclasses should always call this method on their
     * <code>NestedScrollingChildHelper</code> when detached from a window.
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link android.support.v4.view.NestedScrollingChild} interface method with the same
     * signature to implement the standard policy.</p>
     */
    public void onDetachedFromWindow() {
        ViewCompat.stopNestedScroll(mView);
    }
    /**
     * Called when a nested scrolling child stops its current nested scroll operation.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link android.support.v4.view.NestedScrollingChild} interface method with the same
     * signature to implement the standard policy.</p>
     *
     * @param child Child view stopping its nested scroll. This may not be a direct child view.
     */
    public void onStopNestedScroll(@NonNull View child) {
        ViewCompat.stopNestedScroll(mView);
    }

    private ViewParent getNestedScrollingParentForType(@NestedScrollType int type) {
        switch (type) {
            case TYPE_TOUCH:
                return mNestedScrollingParentTouch;
            case TYPE_NON_TOUCH:
                return mNestedScrollingParentNonTouch;
        }
        return null;
    }

    private void setNestedScrollingParentForType(@NestedScrollType int type, ViewParent p) {
        switch (type) {
            case TYPE_TOUCH:
                mNestedScrollingParentTouch = p;
                break;
            case TYPE_NON_TOUCH:
                mNestedScrollingParentNonTouch = p;
                break;
        }
    }
}
