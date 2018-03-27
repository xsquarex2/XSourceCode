package com.xsquare.sourcecode.android.support.v4.view;

import android.support.annotation.Nullable;

/**
     *  * 子view	                父view
         startNestedScroll	        onStartNestedScroll、onNestedScrollAccepted
         dispatchNestedPreScroll	onNestedPreScroll
         dispatchNestedScroll	    onNestedScroll
         stopNestedScroll	        onStopNestedScroll
 * Created by xsquare on 2018/3/19.
 */

public interface NestedScrollingChild{
    /**
     * 设置嵌套滑动是否可用
     */
    void setNestedScrollingEnabled(boolean var1);
    /**
     * 嵌套滑动是否可用
     */
    boolean isNestedScrollingEnabled();
    /**
     * 开始嵌套滑动
     * @param var1 ViewCompat.SCROLL_AXIS_HORIZONTAL 横向滑动
     *             ViewCompat.SCROLL_AXIS_VERTICAL 纵向滑动
     * @return
     */
    boolean startNestedScroll(int var1);
    /**
     * 停止嵌套滑动
     */
    void stopNestedScroll();
    /**
     * 是否有支持嵌套滑动的父类
     */
    boolean hasNestedScrollingParent();
    /**
     * 在处理滑动之后 调用
     * @param var1 x轴上 被消费的距离
     * @param var2 y轴上 被消费的距离
     * @param var3 x轴上 未被消费的距离
     * @param var4 y轴上 未被消费的距离
     * @param var5 view 的移动距离
     * @return
     */
    boolean dispatchNestedScroll(int var1, int var2, int var3, int var4, @Nullable int[] var5);
    /**
     * 一般在滑动之前调用, 在ontouch 中计算出滑动距离, 然后 调用改 方法, 就给支持的嵌套的父View 处理滑动事件
     * @param var1 x 轴上滑动的距离, 相对于上一次事件, 不是相对于 down事件的 那个距离
     * @param var2 y 轴上滑动的距离
     * @param var3 一个数组, 可以传 一个空的 数组,  表示 x 方向 或 y 方向的事件 是否有被消费
     * @param var4   支持嵌套滑动到额父View 消费 滑动事件后 导致 本 View 的移动距离
     * @return 支持的嵌套的父View 是否处理了 滑动事件
     */
    boolean dispatchNestedPreScroll(int var1, int var2, @Nullable int[] var3, @Nullable int[] var4);

    /**
     * @param var1 x 轴上的滑动速度
     * @param var2 y 轴上的滑动速度
     * @param var3 是否被消费
     */
    boolean dispatchNestedFling(float var1, float var2, boolean var3);
    /**
     * @param var1 x 轴上的滑动速度
     * @param var2 y 轴上的滑动速度
     */
    boolean dispatchNestedPreFling(float var1, float var2);
}
