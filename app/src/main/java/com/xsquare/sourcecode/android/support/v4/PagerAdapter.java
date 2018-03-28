package com.xsquare.sourcecode.android.support.v4;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import com.xsquare.sourcecode.database.DataSetObservable;
import com.xsquare.sourcecode.database.DataSetObserver;

/**
 *
 * Created by xsquare on 2018/1/26.
 */
public abstract class PagerAdapter {
    private final DataSetObservable mObservable = new DataSetObservable();
    private DataSetObserver mViewPagerObserver;

    public static final int POSITION_UNCHANGED = -1;
    public static final int POSITION_NONE = -2;


    /**
     * 抽象方法
     */
    public abstract int getCount();
    public abstract boolean isViewFromObject(View view, Object object);

    /**
     * 开始更新
     */
    public void startUpdate(ViewGroup container) {}
    /**
     * 结束更新
     */
    public void finishUpdate(ViewGroup container) {}
    /**
     * 生成Item
     * 在每次 ViewPager 需要一个用以显示的 Object 的时候，该函数都会被 ViewPager.addNewItem() 调用。
     * 在数据集发生变化的时候，一般 Activity启动流程 会调用 PagerAdapter.notifyDataSetChanged()，以通知 PagerAdapter，
     * 而 PagerAdapter 则会通知在自己这里注册过的所有 DataSetObserver。其中之一就是在 ViewPager.setAdapter()
     * 中注册过的 PageObserver。
     * PageObserver 则进而调用 ViewPager.dataSetChanged()，从而导致 ViewPager 开始触发更新其内含 View 的操作。
     */
    public Object instantiateItem(ViewGroup container, int position) {
        return null;
    }
    /**
     * 删除item
     */
    public void destroyItem(ViewGroup container, int position, Object object) {}


    /**
     * currentItem
     */
    public void setPrimaryItem(ViewGroup container, int position, Object object) {}


    /**
     * 保存状态
     */
    public Parcelable saveState() {
        return null;
    }

    /**
     * 恢复状态
     */
    public void restoreState(Parcelable state, ClassLoader loader) {
    }


    /**
     * 获取position
     * 该函数用以返回给定对象的位置，给定对象是由 instantiateItem() 的返回值。
     * 在 ViewPager.dataSetChanged() 中将对该函数的返回值进行判断，
     * 以决定是否最终触发 PagerAdapter.instantiateItem() 函数。
     * 一直返回 POSITION_UNCHANGED，从而导致 ViewPager.dataSetChanged() 被调用时，
     * 认为不必触发 PagerAdapter.instantiateItem()。
     */
    public int getItemPosition(Object object) {
        return POSITION_UNCHANGED;
    }


    /**
     * 数据变化，观察者变化
     */
    public void notifyDataSetChanged() {
        synchronized (this) {
            if (mViewPagerObserver != null) {
                mViewPagerObserver.onChanged();
            }
        }
        mObservable.notifyChanged();
    }

    /**
     * 注册观察者
     */
    public void registerDataSetObserver(DataSetObserver observer) {
        mObservable.registerObserver(observer);
    }
    /**
     * 注册被观察者
     */
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mObservable.unregisterObserver(observer);
    }



    /**
     * 设置观察者
     */
    void setViewPagerObserver(DataSetObserver observer) {
        synchronized (this) {
            mViewPagerObserver = observer;
        }
    }


    /**
     * 获取page的标题
     */
    public CharSequence getPageTitle(int position) {
        return null;
    }


    /**
     * 获取page的宽度
     */
    public float getPageWidth(int position) {
        return 1.f;
    }

}
