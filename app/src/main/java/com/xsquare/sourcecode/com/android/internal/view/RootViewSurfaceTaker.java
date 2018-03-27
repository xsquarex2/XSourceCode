package com.xsquare.sourcecode.com.android.internal.view;

import android.view.InputQueue;
import android.view.SurfaceHolder;

/**
 * Created by xsquare on 2018/3/27.
 */

public interface RootViewSurfaceTaker {
    SurfaceHolder.Callback2 willYouTakeTheSurface();
    void setSurfaceType(int type);
    void setSurfaceFormat(int format);
    void setSurfaceKeepScreenOn(boolean keepOn);
    InputQueue.Callback willYouTakeTheInputQueue();
    void onRootViewScrollYChanged(int scrollY);
}
