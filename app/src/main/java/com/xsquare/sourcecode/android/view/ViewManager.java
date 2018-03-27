package com.xsquare.sourcecode.android.view;

import android.view.View;

/**
 * ViewManager
 * Created by xsquare on 2018/3/25.
 */
public interface ViewManager {
    public void addView(android.view.View view, android.view.ViewGroup.LayoutParams params);
    public void updateViewLayout(android.view.View view, android.view.ViewGroup.LayoutParams params);
    public void removeView(View view);
}
