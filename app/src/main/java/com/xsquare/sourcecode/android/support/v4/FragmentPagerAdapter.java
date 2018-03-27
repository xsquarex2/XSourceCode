package com.xsquare.sourcecode.android.support.v4;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

/**
 *  适用于fragment较少的情况,
 *  该类内的每一个生成的 Fragment 都将保存在内存之中，因此适用于那些相对静态的页，数量也比较少的那种
 *
 *  方案:分别重载 getItem() 设置加载一次性的数据以及 instantiateItem() 对象设置动态更新数据
 *  不要忘记重载 getItemPosition() 函数，返回 POSITION_NONE
 *
 *  @Override
    public Fragment getItem(int position) {
        MyFragment f = new MyFragment();
        return f;
    }
    @Override
     public Object instantiateItem(ViewGroup container, int position) {
        MyFragment f = (MyFragment) super.instantiateItem(container, position);
        String title = mList.get(position);
        f.setTitle(title);
        return f;
    }
    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
 * Created by xsquare on 2018/1/26.
 */

public abstract class FragmentPagerAdapter extends PagerAdapter  {

    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;
    private Fragment mCurrentPrimaryItem = null;
    public FragmentPagerAdapter(FragmentManager fm) {
        mFragmentManager = fm;
    }

    /*********************************** 私有方法 *************************************************/

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }



    /********************************************* 抽象方法 ***************************************/
    /**
     * 由instantiateItem()调用，生成新的 Fragment 对象
     * 需要向 Fragment 对象传递相对静态的数据时，我们一般通过 Fragment.setArguments() 来进行，
     * 这部分代码应当放到 getItem()。它们只会在新生成 Fragment 对象时执行一遍
     * 将数据集里面一些动态的数据传递给该 Fragment，那么，这部分代码不适合放到 getItem() 中。
     * 因为当数据集发生变化时，往往对应的 Fragment 已经生成，如果传递数据部分代码放到了 getItem() 中，
     * 这部分代码将不会被调用。
     * 这也是为什么很多人发现调用 PagerAdapter.notifyDataSetChanged() 后，getItem() 没有被调用的一个原因
     */
    public abstract Fragment getItem(int position);
    /************************************* 继承方法 ***********************************************/
    @Override
    public void startUpdate(ViewGroup container) {
        if (container.getId() == View.NO_ID) {
            throw new IllegalStateException("ViewPager with adapter " + this
                    + " requires a view id");
        }
    }
    /**
     * 与FragmentStatePagerAdapter不同
     * 如果需要在生成 Fragment 对象后，将数据集中的一些动态数据传递给该 Fragment，
     * 这部分代码应该放到这个函数的重载里。
     * 否则，如果将这部分传递数据的代码放到 getItem()中，在 PagerAdapter.notifyDataSetChanged() 后
     * ，这部分数据设置代码将不会被调用。
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        final long itemId = getItemId(position);
        //检查是否拥有该fragment，拥有将被attach()
        String name = makeFragmentName(container.getId(), itemId);
        //以后需要该 Fragment 时，都会从 FragmentManager 读取，而不会再次调用 getItem() 方法
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            mCurTransaction.attach(fragment);
        } else {
            //第一次加载时才会使用
            fragment = getItem(position);
            mCurTransaction.add(container.getId(), fragment,
                    makeFragmentName(container.getId(), itemId));
        }
        if (fragment != mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }
        return fragment;
    }

    /**
     * 与FragmentStatePagerAdapter不同
     * 这里不是 remove()，只是 detach()，
     * 因此 Fragment 还在 FragmentManager 管理中，Fragment 所占用的资源不会被释放。
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        mCurTransaction.detach((Fragment)object);
    }
    @Override
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
    @Override
    public Parcelable saveState() {
        return null;
    }
    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }
    /***************************************** Open ***********************************************/

    /**
     * 返回唯一的pagerId
     * @return position
     */
    public long getItemId(int position) {
        return position;
    }
}
