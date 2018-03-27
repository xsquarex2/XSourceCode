package com.xsquare.sourcecode.database;

/**
 * 数据改变的回调和刷新
 * Created by xsquare on 2018/1/26.
 */

public abstract class DataSetObserver {
    public void onChanged() {
        // Do nothing
    }
    public void onInvalidated() {
        // Do nothing
    }
}
