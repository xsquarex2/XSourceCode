package com.xsquare.sourcecode.android.support.v4.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.xsquare.sourcecode.android.support.v4.view.NestedScrollParent;
import com.xsquare.sourcecode.android.support.v4.view.NestedScrollingChild2;
import com.xsquare.sourcecode.android.support.v4.view.ScrollingView;

/**
 * @author Xiang
 * @date 2018/3/17
 * FrameLayout有实现了NestedScrollParent接口
 * NestScrollView实现了NestedScrollingChild2和ScrollingView
 */
public class NestScrollView extends FrameLayout implements NestedScrollParent,NestedScrollingChild2,ScrollingView{
    /*****************************  构造方法     **********************************************/
    public NestScrollView(@NonNull Context context) {
        super(context);
    }

    public NestScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NestScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*********************************  NestedScrollChild2接口 *****************************************/
    @Override
    public boolean startNestedScroll(int axes, int type) {
        return false;
    }

    @Override
    public void stopNestedScroll(int type) {

    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return false;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return false;
    }
    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return false;
    }


    /***************************************  scrollingView接口 ********************************************/

    @Override
    public int computeHorizontalScrollRange() {
        return 0;
    }

    @Override
    public int computeHorizontalScrollOffset() {
        return 0;
    }

    @Override
    public int computeHorizontalScrollExtent() {
        return 0;
    }

    @Override
    public int computeVerticalScrollRange() {
        return 0;
    }

    @Override
    public int computeVerticalScrollOffset() {
        return 0;
    }

    @Override
    public int computeVerticalScrollExtent() {
        return 0;
    }
}
