package com.xsquare.sourcecode.android.support.v4;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * 适用于fragment较多的情况
 * 只保留当前页面，当页面离开视线后，就会被消除，释放其资源；
 *
 * 方案:分别重载 getItem() 设置加载一次性的数据以及 instantiateItem() 对象设置动态更新数据
 *  不要忘记重载 getItemPosition() 函数，返回 POSITION_NONE
 *
 * Created by xsquare on 2018/1/26.
 */

public abstract class FragmentStatePagerAdapter extends PagerAdapter {
    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;

    private ArrayList<Fragment.SavedState> mSavedState = new ArrayList<Fragment.SavedState>();
    private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
    private Fragment mCurrentPrimaryItem = null;

    public FragmentStatePagerAdapter(FragmentManager fm) {
        mFragmentManager = fm;
    }

    /*********************************** 抽象方法 *************************************************/
    /**
     * Fragment.setArguments() 这种只会在新建 Fragment 时执行一次的参数传递代码，可以放在这里。
     */
    public abstract Fragment getItem(int position);

    /*********************************** 继承方法 *************************************************/
    @Override
    public void startUpdate(ViewGroup container) {
        if (container.getId() == View.NO_ID) {
            throw new IllegalStateException("ViewPager with adapter " + this
                    + " requires a view id");
        }
    }
    /**
     * 与FragmentPagerAdapter不同
     * 除非碰到 FragmentManager 刚好从 SavedState 中恢复了对应的 Fragment 的情况外，
     * 该函数将会调用 getItem() 函数，生成新的 Fragment 对象。新的对象将被 FragmentTransaction.add()。
     * FragmentStatePagerAdapter 就是通过这种方式，每次都创建一个新的 Fragment，
     * 而在不用后就立刻释放其资源，来达到节省内存占用的目的的。
     *
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //如果已经有fragment直接返回
        if (mFragments.size() > position) {
            Fragment f = mFragments.get(position);
            if (f != null) {
                return f;
            }
        }
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        Fragment fragment = getItem(position);
        //恢复保存的状态
        if (mSavedState.size() > position) {
            Fragment.SavedState fss = mSavedState.get(position);
            if (fss != null) {
                fragment.setInitialSavedState(fss);
            }
        }
        while (mFragments.size() <= position) {
            mFragments.add(null);
        }
        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);
        //设置该position下的fragment
        mFragments.set(position, fragment);
        //将该fragment添加到transaction中
        mCurTransaction.add(container.getId(), fragment);
        return fragment;
    }
    /**
     * 与FragmentPagerAdapter不同
     * 将 Fragment 移除，即调用 FragmentTransaction.remove()，并释放其资源。
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        while (mSavedState.size() <= position) {
            mSavedState.add(null);
        }
        //fragment销毁时保存状态
        mSavedState.set(position, fragment.isAdded()
                ? mFragmentManager.saveFragmentInstanceState(fragment) : null);
        //设置该位置的fragment为空
        mFragments.set(position, null);
        //从transaction中移除fragment
        mCurTransaction.remove(fragment);
    }
    @Override
    @SuppressWarnings("ReferenceEquality")
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment)object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }
    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitNowAllowingStateLoss();
            mCurTransaction = null;
        }
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment)object).getView() == view;
    }

    /**
     * 与FragmentPagerAdapter不同
     */
    @Override
    public Parcelable saveState() {
        Bundle state = null;
        if (mSavedState.size() > 0) {
            state = new Bundle();
            Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size()];
            mSavedState.toArray(fss);
            state.putParcelableArray("states", fss);
        }
        for (int i=0; i<mFragments.size(); i++) {
            Fragment f = mFragments.get(i);
            if (f != null && f.isAdded()) {
                if (state == null) {
                    state = new Bundle();
                }
                String key = "f" + i;
                mFragmentManager.putFragment(state, key, f);
            }
        }
        return state;
    }
    /**
     * 与FragmentPagerAdapter不同
     */
    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle)state;
            bundle.setClassLoader(loader);
            Parcelable[] fss = bundle.getParcelableArray("states");
            mSavedState.clear();
            mFragments.clear();
            if (fss != null) {
                for (int i=0; i<fss.length; i++) {
                    mSavedState.add((Fragment.SavedState)fss[i]);
                }
            }
            Iterable<String> keys = bundle.keySet();
            for (String key: keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = mFragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        while (mFragments.size() <= index) {
                            mFragments.add(null);
                        }
                        f.setMenuVisibility(false);
                        mFragments.set(index, f);
                    } else {
                    }
                }
            }
        }
    }
}
