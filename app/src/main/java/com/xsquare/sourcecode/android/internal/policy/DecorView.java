package com.xsquare.sourcecode.android.internal.policy;

import android.content.Context;
import android.util.AttributeSet;

import com.xsquare.sourcecode.android.widget.FrameLayout;

/**
 * Created by xsquare on 2018/3/27.
 */

public class DecorView extends FrameLayout {
    //当前DecorView
    private PhoneWindow mWindow;

    public DecorView(Context context) {
        super(context);
    }

    public DecorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DecorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置Window
     * @param phoneWindow phoneWindow
     */
    void setWindow(PhoneWindow phoneWindow) {
        mWindow = phoneWindow;
        Context context = getContext();
        if (context instanceof DecorContext) {
            DecorContext decorContext = (DecorContext) context;
            decorContext.setPhoneWindow(mWindow);
        }
    }
}
