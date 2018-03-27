package com.xsquare.sourcecode.android.support.v4.view;

/**
 * @author xsquare
 * @date 2018/3/17
 */

import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;

/**
 * 由子view实现
 * 该接口由view的子类实现，提供支持与父类处理嵌套滑动
 * 需要创建一个{@link NestedScrollingChildHelper}进行代理执行
 */
public interface NestedScrollingChild2 extends NestedScrollingChild{
    /**
     * 从给定的方向，开始嵌套行动
     * @param axes {@link ViewCompat#SCROLL_AXIS_HORIZONTAL}
     *             and/or {@link ViewCompat#SCROLL_AXIS_VERTICAL}
     * @param type 导致滚动事件的输入类型
     * @return 为true表示当前父类能够配合当前事件嵌套滚动
     * @see #stopNestedScroll(int)
     * @see #dispatchNestedPreScroll(int, int, int[], int[], int)
     * @see #dispatchNestedScroll(int, int, int, int, int[], int)
     */
    boolean startNestedScroll(@ViewCompat.ScrollAxis int axes, @ViewCompat.NestedScrollType int type);
    /**
     *
     * @param type 引起嵌套滑动的输入类型
     */
    void stopNestedScroll(@ViewCompat.NestedScrollType int type);
    /**
     * 若给定的输入类型是否有嵌套滑动的父类
     * @param type 引起嵌套滚动的输入类型
     * @return view是否有嵌套滑动的父类
     */
    boolean hasNestedScrollingParent(@ViewCompat.NestedScrollType int type);
    /**
     *
     * @param dxConsumed 这个view在滚动中被消耗的水平距离（像素）
     * @param dyConsumed 这个view在滑动中被消耗的垂直距离（像素）
     * @param dxUnconsumed 这个view在滑动中没有被消耗的水平距离
     * @param dyUnconsumed 这个view在滑动中没有被消耗的垂直距离
     * @param offsetInWindow 可选的参数，如果不为null，此参数包含了当前view的坐标偏移量
     *                       view的实现类可用此来调整预期输入坐标
     * @param type 引起滑动事件的输入类型
     * @return 如果事件分发了返回true
     * @see #dispatchNestedPreScroll(int, int, int[], int[], int)
     */
    boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
                                 int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow,
                                 @ViewCompat.NestedScrollType int type);
    /**
     * @param dx 水平滚动距离（像素）
     * @param dy 垂直滚动距离（像素）
     * @param consumed consumed[0]、consumed[1]水平垂直上消耗的距离
     * @param offsetInWindow 可选的
     * @param type 引起嵌套滑动的输入类型
     * @return 如果parent消耗了部分或者所有返回true
     */
    boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed,
                                    @Nullable int[] offsetInWindow, @ViewCompat.NestedScrollType int type);
}
