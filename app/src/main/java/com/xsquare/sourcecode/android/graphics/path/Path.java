package com.xsquare.sourcecode.android.graphics.path;

import android.graphics.Region;

/**
 * 路径
 * Created by xsquare on 2018/3/5.
 */
public class Path {
    public boolean isSimplePath = true;
    public Region rects;
    public long mNativePath;
    private android.graphics.Path.Direction mLastDirection = null;
    /**
     * Clear any lines and curves from the path, making it empty.
     * This does NOT change the fill-type setting.
     */
    public void reset() {
        isSimplePath = true;
        mLastDirection = null;
        if (rects != null) rects.setEmpty();
        //
        // We promised not to change this, so preserve it around the native
        // call, which does now reset fill type.
        //final android.graphics.Path.FillType fillType = getFillType();
        //nReset(mNativePath);
        //setFillType(fillType);
    }
}

