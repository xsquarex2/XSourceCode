package com.xsquare.sourcecode.android.view;

import android.view.*;

/**
 * Created by xsquare on 2018/3/27.
 */

public interface FallbackEventHandler {
    public void setView(android.view.View v);
    public void preDispatchKeyEvent(KeyEvent event);
    public boolean dispatchKeyEvent(KeyEvent event);
}
