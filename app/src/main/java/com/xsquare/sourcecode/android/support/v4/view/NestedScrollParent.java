package com.xsquare.sourcecode.android.support.v4.view;

/**
 * @author xsquare
 * @date 2018/3/17
 */

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 *  子view	                    父view
    startNestedScroll	        onStartNestedScroll、onNestedScrollAccepted
    dispatchNestedPreScroll	    onNestedPreScroll
    dispatchNestedScroll	    onNestedScroll
    stopNestedScroll	        onStopNestedScroll
 * 该接口由ViewGroup的子类去实现，该类希望支持由嵌套的子View代理滚动操作
 * 实现该接口的类需要创建一个{@link android.support.v4.view.NestedScrollingParentHelper }实例，并以此代理view或ViewGroup的方法
 */
public interface NestedScrollParent {
    /**
     * 该方法响应子View启动并声明嵌套滚动操作
     * 该方法将会在子View调用了{@link ViewCompat#startNestedScroll(View, int)}时调用
     * 当滚动完成后将会调用{@link #onStopNestedScroll(View)}
     * @param child 该ViewParent的直接子View
     * @param target 启动嵌套滚动的view
     * @param axes  {@link ViewCompat#SCROLL_AXIS_HORIZONTAL},{@link ViewCompat#SCROLL_AXIS_VERTICAL} or both
     * @return 如果返回true表示ViewParent接受了滚动操作
     */
    boolean onStartNestedScroll(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes);

    /**
     * 该方法对成功声明了嵌套滚动的响应
     * 该方向会在{@link #onStartNestedScroll(View, View, int) onStartNestedScroll}返回true时调用
     * @param child 该ViewParent的直接子View
     * @param target 启动嵌套滚动的view
     * @param axes{@link ViewCompat#SCROLL_AXIS_HORIZONTAL},{@link ViewCompat#SCROLL_AXIS_VERTICAL} or both
     * @see #onStopNestedScroll(View)
     */
    void onNestedScrollAccepted(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes);

    /**
     * 对嵌套滚动结束时的响应
     * 如果父类有实现这个方法，那么子类也需要调用父类的方法
     * @param target 启动嵌套滚动的view
     */
    void onStopNestedScroll(@NonNull View target);


    /**
     * 嵌套滚动过程中时调用
     * @param target 控制嵌套滚动的派生视图
     * @param dxConsumed 水平滚动距离，像素已经被目标消耗。
     * @param dyConsumed 垂直滚动距离的像素已经被目标消耗
     * @param dxUnconsumed 水平滚动距离的像素不被目标消耗
     * @param dyUnconsumed 垂直滚动距离的像素不被目标消耗
     */
    void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
                        int dxUnconsumed, int dyUnconsumed);

    /**
     * 在目标视图消耗部分滚动之前，对嵌套滚动进行响应
     * @param target 启动嵌套滚动的view
     * @param dx    水平向滚动的距离
     * @param dy    垂直滚动的距离
     * @param consumed  在水平和垂直方向被父类消耗的距离
     */
    void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed);

    /**
     * 嵌套滚动的后fling滑动
     * @param target    启动滚动滑动的view
     * @param velocityX 水平速度以每秒像素为单位
     * @param velocityY 垂直速度以每秒像素为单位
     * @param consumed 若为true表示子view消耗了fling滑动
     * @return 如果父view消耗或者以其他方式响应了该fling滑动返回true
     */
    boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed);

    /**
     * 在目标视图消耗它之前响应
     * @param target 启动滚动滑动的view
     * @param velocityX 水平速度以每秒像素为单位
     * @param velocityY 垂直速度以每秒像素为单位
     * @return 如果父view在子view之前消耗了fling滑动为true
     */
    boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY);

    /**
     * 返回当前滑动的轴向标志
     * @return @see ViewCompat#SCROLL_AXIS_HORIZONTAL
     * @see ViewCompat#SCROLL_AXIS_VERTICAL
     * @see ViewCompat#SCROLL_AXIS_NONE
     */
    @ViewCompat.ScrollAxis
    int getNestedScrollAxes();
}
