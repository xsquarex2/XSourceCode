package com.xsquare.sourcecode.android.view;

import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.*;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.xsquare.sourcecode.android.content.Context;

/**
 * Created by xsquare on 2018/3/26.
 */

public class WindowManagerImpl implements WindowManager {
    private final WindowManagerGlobal mGlobal = WindowManagerGlobal.getInstance();
    private final Context mContext;
    private final Window mParentWindow;

    private IBinder mDefaultToken;

    /**
     * 构造方法
     */
    public WindowManagerImpl(Context context) {
        this(context, null);
    }
    private WindowManagerImpl(Context context, Window parentWindow) {
        mContext = context;
        mParentWindow = parentWindow;
    }

    /**
     * 创建实例
     */
    public WindowManagerImpl createLocalWindowManager(Window parentWindow) {
        return new WindowManagerImpl(mContext, parentWindow);
    }
    public WindowManagerImpl createPresentationWindowManager(Context displayContext) {
        return new WindowManagerImpl(displayContext, mParentWindow);
    }

    /**
     * 设置window token
     */
    public void setDefaultToken(IBinder token) {
        mDefaultToken = token;
    }

    /**
     * 添加view
     */
    @Override
    public void addView(View view, ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }

    /**
     * 更新view布局
     */
    @Override
    public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.updateViewLayout(view, params);
    }

    /**
     * 移除view
     */
    @Override
    public void removeView(View view) {
        mGlobal.removeView(view, false);
    }
    /**
     * 移除view立即
     * @param view 将被移除的view
     */
    @Override
    public void removeViewImmediate(View view) {
        mGlobal.removeView(view, true);
    }

    /**
     * 获取默认显示
     * @return Display
     */
    @Override
    public Display getDefaultDisplay() {
        return mContext.getDisplay();
    }






    private void applyDefaultToken(@NonNull ViewGroup.LayoutParams params) {
        // 如果没有父window，只能使用默认token
        if (mDefaultToken != null && mParentWindow == null) {
            if (!(params instanceof android.view.WindowManager.LayoutParams)) {
                throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
            }
            // 如果我们没有token，只能使用默认token
            final android.view.WindowManager.LayoutParams wparams = (android.view.WindowManager.LayoutParams) params;
            if (wparams.token == null) {
                wparams.token = mDefaultToken;
            }
        }
    }
}
