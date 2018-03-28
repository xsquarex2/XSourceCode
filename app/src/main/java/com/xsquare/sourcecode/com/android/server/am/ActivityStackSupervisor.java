package com.xsquare.sourcecode.com.android.server.am;

import android.util.SparseArray;

/**
 * 管理者多个ActivityStack，当前只会有一个获取焦点（focused）的ActivityStack。
 * Created by xsquare on 2018/3/28.
 */
public class ActivityStackSupervisor {
    /**
     * 	主屏(桌面)所在ActivityStack
     */
    ActivityStack mHomeStack;
    /**
     * 表示焦点ActivityStack，它能够获取用户输入
     */
    ActivityStack mFocusedStack;
    /**
     * 上一个焦点ActivityStack
     */
    private ActivityStack mLastFocusedStack;
    /**
     * 表示当前的显示设备，ActivityDisplay中绑定了若干ActivityStack。
     * 通过该属性就能间接获取所有ActivityStack的信息
     */
    private final SparseArray<ActivityDisplay> mActivityDisplays = new SparseArray<>();
}
