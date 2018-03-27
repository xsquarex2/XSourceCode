package com.xsquare.sourcecode.android.support.v4.view;

/**
 * 计算滚动条相关数据
 * @author xsquare
 * @date 2018/3/17
 */

public interface ScrollingView {
    /**
     * 计算水平滚动条表示的水平范围(单位必须同{@link #computeHorizontalScrollExtent()} and
     * {@link #computeHorizontalScrollOffset()})
     * 默认范围为绘制view的宽度
     * @see #computeHorizontalScrollExtent()
     * @see #computeHorizontalScrollOffset()
     * @return 水平滚动条表示的水平范围
     */
    int computeHorizontalScrollRange();
    /**
     * 计算水平滚动条在水平范围内的水平偏移量。这个值用于计算滚动条轨迹中拇指的位置
     * 单位可以是任意单位，但必须同{@link #computeHorizontalScrollRange()} and
     * {@link #computeHorizontalScrollExtent()}
     * 默认偏移量是该视图的滚动偏移量
     * @see #computeHorizontalScrollRange()
     * @see #computeHorizontalScrollExtent()
     * @return 滚动条的拇指的水平偏移
     */
    int computeHorizontalScrollOffset();

    /**
     * 计算横向滚动条在水平范围内的水平范围。这个值用于计算滚动条轨迹中拇指的长度
     * 单位是任意的，但必须同{@link #computeHorizontalScrollRange()} and
     * {@link #computeHorizontalScrollOffset()}
     * 默认的范围是这个视图的绘制宽度
     * @return 滚动条的拇指的水平范围
     */
    int computeHorizontalScrollExtent();

    /**
     * 垂直向同上
     */
    int computeVerticalScrollRange();

    /**
     * 垂直向同上
     */
    int computeVerticalScrollOffset();

    /**
     * 垂直向同上
     */
    int computeVerticalScrollExtent();
}
