package com.xsquare.sourcecode.android.internal.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Scene;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.util.TypedValue;
import android.view.InputQueue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.xsquare.sourcecode.android.view.Window;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.Window.FEATURE_CONTENT_TRANSITIONS;
import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
import static android.view.WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

/**
 * Created by xsquare on 2018/3/22.
 */

public class PhoneWindow extends Window {
    // 这是窗口的顶层视图，包含窗口装饰。
    // DecorView是Activity中的顶级View，它本质上是一个FrameLayout，
    // 一般说来它内部包含标题栏和内容栏（com.android.internal.R.id.content）。
    private DecorView mDecor;
    // 这是放置窗口内容的视图。 它可以是mDecor本身，也可以是mDecor的内容所在的子view。
    ViewGroup mContentParent;
    //左上角图标
    private ImageView mLeftIconView;
    //右上角图标
    private ImageView mRightIconView;
    //圆形loading条
    private ProgressBar mCircularProgressBar;
    //水平loading条
    private ProgressBar mHorizontalProgressBar;
    private int mFeatures;
    private final Context mContext;
    //是否使用DecorContext
    private boolean mUseDecorContext = false;
    //主题
    private int mTheme = -1;
    //当前window的style
    private TypedArray mWindowStyle;
    boolean mIsFloating;
    private android.view.Window.Callback mCallback;
    //布局加载器
    private LayoutInflater mLayoutInflater;

    public PhoneWindow(Context context) {
        super(context);
    }
    public boolean hasFeature(int feature) {
        return (getFeatures() & (1 << feature)) != 0;
    }
    protected final int getFeatures()
    {
        return mFeatures;
    }

    /**
     * 这个时候DecorView还没有真正的被WindowManager添加到Window中，它还无法接受用户的输入信息和焦点事件，
     * 这个时候就相当于走到了Activity的onCreate()流程，界面还 未展示给用户。
     * 直到走到Activity的onResume()方法，它会调用Activity的makeVisiable()方法，DecorView才真正的被用户所看到。
     * @param layoutResID 布局Id
     */
    @Override
    public void setContentView(int layoutResID) {
// Note: FEATURE_CONTENT_TRANSITIONS may be set in the process of installing the window
        // decor, when theme attributes and the like are crystalized. Do not check the feature
        // before this happens.
        if (mContentParent == null) {
            installDecor();
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }
        if (hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            final Scene newScene = Scene.getSceneForLayout(mContentParent, layoutResID,
                    getContext());
            transitionTo(newScene);
        } else {
            mLayoutInflater.inflate(layoutResID, mContentParent);
        }
        mContentParent.requestApplyInsets();
        final Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            cb.onContentChanged();
        }
        mContentParentExplicitlySet = true;
    }

    @Override
    public void setContentView(View view) {
        setContentView(view, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (mContentParent == null) {
            //1. 如果没有DecorView则创建它，并将创建好的DecorView赋值给mContentParent
            installDecor();
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }

        if (hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            view.setLayoutParams(params);
            final Scene newScene = new Scene(mContentParent, view);
            transitionTo(newScene);
        } else {
            //2. 将Activity传入的布局文件生成View并添加到mContentParent中
            mContentParent.addView(view, params);
        }
        mContentParent.requestApplyInsets();
        final Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            //3. 回调Window.Callback里的onContentChanged()方法，这个Callback也被Activity
            //所持有，因此它实际回调的是Activity里的onContentChanged()方法，通知Activity
            //视图已经发生改变。
            cb.onContentChanged();
        }
        mContentParentExplicitlySet = true;
    }

    @Override
    public boolean superDispatchTouchEvent(MotionEvent event) {
        return false;
    }


    @Override
    public View getDecorView() {
        return null;
    }

    @Override
    public View peekDecorView() {
        return null;
    }



    private void installDecor() {
        mForceDecorInstall = false;
        if (mDecor == null) {
            //生成DecorView
            mDecor = generateDecor(-1);
            //设置ViewGroup是否拦截子View事件
            mDecor.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            mDecor.setIsRootNamespace(true);
            if (!mInvalidatePanelMenuPosted && mInvalidatePanelMenuFeatures != 0) {
                mDecor.postOnAnimation(mInvalidatePanelMenuRunnable);
            }
        } else {
            mDecor.setWindow(this);
        }
        if (mContentParent == null) {
            mContentParent = generateLayout(mDecor);

            // Set up decor part of UI to ignore fitsSystemWindows if appropriate.
            mDecor.makeOptionalFitsSystemWindows();

            final DecorContentParent decorContentParent = (DecorContentParent) mDecor.findViewById(
                    R.id.decor_content_parent);

            if (decorContentParent != null) {
                mDecorContentParent = decorContentParent;
                mDecorContentParent.setWindowCallback(getCallback());
                if (mDecorContentParent.getTitle() == null) {
                    mDecorContentParent.setWindowTitle(mTitle);
                }

                final int localFeatures = getLocalFeatures();
                for (int i = 0; i < FEATURE_MAX; i++) {
                    if ((localFeatures & (1 << i)) != 0) {
                        mDecorContentParent.initFeature(i);
                    }
                }

                mDecorContentParent.setUiOptions(mUiOptions);

                if ((mResourcesSetFlags & FLAG_RESOURCE_SET_ICON) != 0 ||
                        (mIconRes != 0 && !mDecorContentParent.hasIcon())) {
                    mDecorContentParent.setIcon(mIconRes);
                } else if ((mResourcesSetFlags & FLAG_RESOURCE_SET_ICON) == 0 &&
                        mIconRes == 0 && !mDecorContentParent.hasIcon()) {
                    mDecorContentParent.setIcon(
                            getContext().getPackageManager().getDefaultActivityIcon());
                    mResourcesSetFlags |= FLAG_RESOURCE_SET_ICON_FALLBACK;
                }
                if ((mResourcesSetFlags & FLAG_RESOURCE_SET_LOGO) != 0 ||
                        (mLogoRes != 0 && !mDecorContentParent.hasLogo())) {
                    mDecorContentParent.setLogo(mLogoRes);
                }

                // Invalidate if the panel menu hasn't been created before this.
                // Panel menu invalidation is deferred avoiding application onCreateOptionsMenu
                // being called in the middle of onCreate or similar.
                // A pending invalidation will typically be resolved before the posted message
                // would run normally in order to satisfy instance state restoration.
                PanelFeatureState st = getPanelState(FEATURE_OPTIONS_PANEL, false);
                if (!isDestroyed() && (st == null || st.menu == null) && !mIsStartingWindow) {
                    invalidatePanelMenu(FEATURE_ACTION_BAR);
                }
            } else {
                mTitleView = findViewById(R.id.title);
                if (mTitleView != null) {
                    if ((getLocalFeatures() & (1 << FEATURE_NO_TITLE)) != 0) {
                        final View titleContainer = findViewById(R.id.title_container);
                        if (titleContainer != null) {
                            titleContainer.setVisibility(View.GONE);
                        } else {
                            mTitleView.setVisibility(View.GONE);
                        }
                        mContentParent.setForeground(null);
                    } else {
                        mTitleView.setText(mTitle);
                    }
                }
            }

            if (mDecor.getBackground() == null && mBackgroundFallbackResource != 0) {
                mDecor.setBackgroundFallback(mBackgroundFallbackResource);
            }

            // Only inflate or create a new TransitionManager if the caller hasn't
            // already set a custom one.
            if (hasFeature(FEATURE_ACTIVITY_TRANSITIONS)) {
                if (mTransitionManager == null) {
                    final int transitionRes = getWindowStyle().getResourceId(
                            R.styleable.Window_windowContentTransitionManager,
                            0);
                    if (transitionRes != 0) {
                        final TransitionInflater inflater = TransitionInflater.from(getContext());
                        mTransitionManager = inflater.inflateTransitionManager(transitionRes,
                                mContentParent);
                    } else {
                        mTransitionManager = new TransitionManager();
                    }
                }

                mEnterTransition = getTransition(mEnterTransition, null,
                        R.styleable.Window_windowEnterTransition);
                mReturnTransition = getTransition(mReturnTransition, USE_DEFAULT_TRANSITION,
                        R.styleable.Window_windowReturnTransition);
                mExitTransition = getTransition(mExitTransition, null,
                        R.styleable.Window_windowExitTransition);
                mReenterTransition = getTransition(mReenterTransition, USE_DEFAULT_TRANSITION,
                        R.styleable.Window_windowReenterTransition);
                mSharedElementEnterTransition = getTransition(mSharedElementEnterTransition, null,
                        R.styleable.Window_windowSharedElementEnterTransition);
                mSharedElementReturnTransition = getTransition(mSharedElementReturnTransition,
                        USE_DEFAULT_TRANSITION,
                        R.styleable.Window_windowSharedElementReturnTransition);
                mSharedElementExitTransition = getTransition(mSharedElementExitTransition, null,
                        R.styleable.Window_windowSharedElementExitTransition);
                mSharedElementReenterTransition = getTransition(mSharedElementReenterTransition,
                        USE_DEFAULT_TRANSITION,
                        R.styleable.Window_windowSharedElementReenterTransition);
                if (mAllowEnterTransitionOverlap == null) {
                    mAllowEnterTransitionOverlap = getWindowStyle().getBoolean(
                            R.styleable.Window_windowAllowEnterTransitionOverlap, true);
                }
                if (mAllowReturnTransitionOverlap == null) {
                    mAllowReturnTransitionOverlap = getWindowStyle().getBoolean(
                            R.styleable.Window_windowAllowReturnTransitionOverlap, true);
                }
                if (mBackgroundFadeDurationMillis < 0) {
                    mBackgroundFadeDurationMillis = getWindowStyle().getInteger(
                            R.styleable.Window_windowTransitionBackgroundFadeDuration,
                            DEFAULT_BACKGROUND_FADE_DURATION_MS);
                }
                if (mSharedElementsUseOverlay == null) {
                    mSharedElementsUseOverlay = getWindowStyle().getBoolean(
                            R.styleable.Window_windowSharedElementsUseOverlay, true);
                }
            }
        }
    }

    /**
     * 自动生成DecorView
     */
    protected DecorView generateDecor(int featureId) {
        // 系统进程没有应用程序上下文，在这种情况下，我们需要直接使用我们拥有的上下文。,
        // 否则，我们需要应用程序上下文，所以我们不拘泥于Activity。
        Context context;
        if (mUseDecorContext) {
            Context applicationContext = getContext().getApplicationContext();
            if (applicationContext == null) {
                context = getContext();
            } else {
                context = new DecorContext(applicationContext, getContext().getResources());
                if (mTheme != -1) {
                    context.setTheme(mTheme);
                }
            }
        } else {
            context = getContext();
        }
        return new DecorView(context, featureId, this, getAttributes());
    }

    public final Context getContext() {
        return mContext;
    }

    /**
     * 获取当前Window的style
     * @return typedArray
     */
    public final TypedArray getWindowStyle() {
        synchronized (this) {
            if (mWindowStyle == null) {
                mWindowStyle = mContext.obtainStyledAttributes(
                        com.android.internal.R.styleable.Window);
            }
            return mWindowStyle;
        }
    }

    /**
     * 设置布局
     * @param width width
     * @param height height
     */
    public void setLayout(int width, int height) {
        final WindowManager.LayoutParams attrs = getAttributes();
        attrs.width = width;
        attrs.height = height;
        //window属性变化的回调
        dispatchWindowAttributesChanged(attrs);
    }
    public void setFlags(int flags, int mask) {
        final WindowManager.LayoutParams attrs = getAttributes();
        attrs.flags = (attrs.flags&~mask) | (flags&mask);
        mForcedWindowFlags |= mask;
        dispatchWindowAttributesChanged(attrs);
    }

    /**
     * 执行window参数变化的回调
     * @param attrs
     */
    protected void dispatchWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        if (mCallback != null) {
            mCallback.onWindowAttributesChanged(attrs);
        }
    }
        /**
         * 自动生成布局
         * @param decor decor
         * @return ViewGroup
         */
    protected ViewGroup generateLayout(DecorView decor) {
        // 应用当前主题数据
        TypedArray a = getWindowStyle();
        mIsFloating = a.getBoolean(R.styleable.Window_windowIsFloating, false);
        int flagsToUpdate = (FLAG_LAYOUT_IN_SCREEN|FLAG_LAYOUT_INSET_DECOR)
                & (~getForcedWindowFlags());
        if (mIsFloating) {
            //设置布局参数
            setLayout(WRAP_CONTENT, WRAP_CONTENT);
            setFlags(0, flagsToUpdate);
        } else {
            setFlags(FLAG_LAYOUT_IN_SCREEN|FLAG_LAYOUT_INSET_DECOR, flagsToUpdate);
        }
        //设置Feature
        if (a.getBoolean(R.styleable.Window_windowNoTitle, false)) {
            requestFeature(FEATURE_NO_TITLE);
        } else if (a.getBoolean(R.styleable.Window_windowActionBar, false)) {
            // 如果没有标题，不允许ActionBar
            requestFeature(FEATURE_ACTION_BAR);
        }
        if (a.getBoolean(R.styleable.Window_windowActionBarOverlay, false)) {
            requestFeature(FEATURE_ACTION_BAR_OVERLAY);
        }

        if (a.getBoolean(R.styleable.Window_windowActionModeOverlay, false)) {
            requestFeature(FEATURE_ACTION_MODE_OVERLAY);
        }

        if (a.getBoolean(R.styleable.Window_windowSwipeToDismiss, false)) {
            requestFeature(FEATURE_SWIPE_TO_DISMISS);
        }

        if (a.getBoolean(R.styleable.Window_windowFullscreen, false)) {
            setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN & (~getForcedWindowFlags()));
        }

        if (a.getBoolean(R.styleable.Window_windowTranslucentStatus,
                false)) {
            setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS
                    & (~getForcedWindowFlags()));
        }

        if (a.getBoolean(R.styleable.Window_windowTranslucentNavigation,
                false)) {
            setFlags(FLAG_TRANSLUCENT_NAVIGATION, FLAG_TRANSLUCENT_NAVIGATION
                    & (~getForcedWindowFlags()));
        }

        if (a.getBoolean(R.styleable.Window_windowOverscan, false)) {
            setFlags(FLAG_LAYOUT_IN_OVERSCAN, FLAG_LAYOUT_IN_OVERSCAN&(~getForcedWindowFlags()));
        }

        if (a.getBoolean(R.styleable.Window_windowShowWallpaper, false)) {
            setFlags(FLAG_SHOW_WALLPAPER, FLAG_SHOW_WALLPAPER&(~getForcedWindowFlags()));
        }

        if (a.getBoolean(R.styleable.Window_windowEnableSplitTouch,
                getContext().getApplicationInfo().targetSdkVersion
                        >= android.os.Build.VERSION_CODES.HONEYCOMB)) {
            setFlags(FLAG_SPLIT_TOUCH, FLAG_SPLIT_TOUCH&(~getForcedWindowFlags()));
        }

        a.getValue(R.styleable.Window_windowMinWidthMajor, mMinWidthMajor);
        a.getValue(R.styleable.Window_windowMinWidthMinor, mMinWidthMinor);
        if (DEBUG) Log.d(TAG, "Min width minor: " + mMinWidthMinor.coerceToString()
                + ", major: " + mMinWidthMajor.coerceToString());
        if (a.hasValue(R.styleable.Window_windowFixedWidthMajor)) {
            if (mFixedWidthMajor == null) mFixedWidthMajor = new TypedValue();
            a.getValue(R.styleable.Window_windowFixedWidthMajor,
                    mFixedWidthMajor);
        }
        if (a.hasValue(R.styleable.Window_windowFixedWidthMinor)) {
            if (mFixedWidthMinor == null) mFixedWidthMinor = new TypedValue();
            a.getValue(R.styleable.Window_windowFixedWidthMinor,
                    mFixedWidthMinor);
        }
        if (a.hasValue(R.styleable.Window_windowFixedHeightMajor)) {
            if (mFixedHeightMajor == null) mFixedHeightMajor = new TypedValue();
            a.getValue(R.styleable.Window_windowFixedHeightMajor,
                    mFixedHeightMajor);
        }
        if (a.hasValue(R.styleable.Window_windowFixedHeightMinor)) {
            if (mFixedHeightMinor == null) mFixedHeightMinor = new TypedValue();
            a.getValue(R.styleable.Window_windowFixedHeightMinor,
                    mFixedHeightMinor);
        }
        if (a.getBoolean(R.styleable.Window_windowContentTransitions, false)) {
            requestFeature(FEATURE_CONTENT_TRANSITIONS);
        }
        if (a.getBoolean(R.styleable.Window_windowActivityTransitions, false)) {
            requestFeature(FEATURE_ACTIVITY_TRANSITIONS);
        }

        mIsTranslucent = a.getBoolean(R.styleable.Window_windowIsTranslucent, false);
        final Context context = getContext();
        final int targetSdk = context.getApplicationInfo().targetSdkVersion;
        final boolean targetPreHoneycomb = targetSdk < android.os.Build.VERSION_CODES.HONEYCOMB;
        final boolean targetPreIcs = targetSdk < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        final boolean targetPreL = targetSdk < android.os.Build.VERSION_CODES.LOLLIPOP;
        final boolean targetHcNeedsOptions = context.getResources().getBoolean(
                R.bool.target_honeycomb_needs_options_menu);
        final boolean noActionBar = !hasFeature(FEATURE_ACTION_BAR) || hasFeature(FEATURE_NO_TITLE);

        if (targetPreHoneycomb || (targetPreIcs && targetHcNeedsOptions && noActionBar)) {
            setNeedsMenuKey(WindowManager.LayoutParams.NEEDS_MENU_SET_TRUE);
        } else {
            setNeedsMenuKey(WindowManager.LayoutParams.NEEDS_MENU_SET_FALSE);
        }

        if (!mForcedStatusBarColor) {
            mStatusBarColor = a.getColor(R.styleable.Window_statusBarColor, 0xFF000000);
        }
        if (!mForcedNavigationBarColor) {
            mNavigationBarColor = a.getColor(R.styleable.Window_navigationBarColor, 0xFF000000);
        }

        WindowManager.LayoutParams params = getAttributes();

        // Non-floating windows on high end devices must put up decor beneath the system bars and
        // therefore must know about visibility changes of those.
        if (!mIsFloating && ActivityManager.isHighEndGfx()) {
            if (!targetPreL && a.getBoolean(
                    R.styleable.Window_windowDrawsSystemBarBackgrounds,
                    false)) {
                setFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                        FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS & ~getForcedWindowFlags());
            }
            if (mDecor.mForceWindowDrawsStatusBarBackground) {
                params.privateFlags |= PRIVATE_FLAG_FORCE_DRAW_STATUS_BAR_BACKGROUND;
            }
        }
        if (a.getBoolean(R.styleable.Window_windowLightStatusBar, false)) {
            decor.setSystemUiVisibility(
                    decor.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if (mAlwaysReadCloseOnTouchAttr || getContext().getApplicationInfo().targetSdkVersion
                >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            if (a.getBoolean(
                    R.styleable.Window_windowCloseOnTouchOutside,
                    false)) {
                setCloseOnTouchOutsideIfNotSet(true);
            }
        }

        if (!hasSoftInputMode()) {
            params.softInputMode = a.getInt(
                    R.styleable.Window_windowSoftInputMode,
                    params.softInputMode);
        }

        if (a.getBoolean(R.styleable.Window_backgroundDimEnabled,
                mIsFloating)) {
            /* All dialogs should have the window dimmed */
            if ((getForcedWindowFlags()&WindowManager.LayoutParams.FLAG_DIM_BEHIND) == 0) {
                params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            }
            if (!haveDimAmount()) {
                params.dimAmount = a.getFloat(
                        android.R.styleable.Window_backgroundDimAmount, 0.5f);
            }
        }

        if (params.windowAnimations == 0) {
            params.windowAnimations = a.getResourceId(
                    R.styleable.Window_windowAnimationStyle, 0);
        }

        // The rest are only done if this window is not embedded; otherwise,
        // the values are inherited from our container.
        if (getContainer() == null) {
            if (mBackgroundDrawable == null) {
                if (mBackgroundResource == 0) {
                    mBackgroundResource = a.getResourceId(
                            R.styleable.Window_windowBackground, 0);
                }
                if (mFrameResource == 0) {
                    mFrameResource = a.getResourceId(R.styleable.Window_windowFrame, 0);
                }
                mBackgroundFallbackResource = a.getResourceId(
                        R.styleable.Window_windowBackgroundFallback, 0);
                if (false) {
                    System.out.println("Background: "
                            + Integer.toHexString(mBackgroundResource) + " Frame: "
                            + Integer.toHexString(mFrameResource));
                }
            }
            if (mLoadElevation) {
                mElevation = a.getDimension(R.styleable.Window_windowElevation, 0);
            }
            mClipToOutline = a.getBoolean(R.styleable.Window_windowClipToOutline, false);
            mTextColor = a.getColor(R.styleable.Window_textColor, Color.TRANSPARENT);
        }
        // 加载window 的decorView
        int layoutResource;
        int features = getLocalFeatures();
        // System.out.println("Features: 0x" + Integer.toHexString(features));
        if ((features & (1 << FEATURE_SWIPE_TO_DISMISS)) != 0) {
            layoutResource = R.layout.screen_swipe_dismiss;
            setCloseOnSwipeEnabled(true);
        } else if ((features & ((1 << FEATURE_LEFT_ICON) | (1 << FEATURE_RIGHT_ICON))) != 0) {
            if (mIsFloating) {
                TypedValue res = new TypedValue();
                getContext().getTheme().resolveAttribute(
                        R.attr.dialogTitleIconsDecorLayout, res, true);
                layoutResource = res.resourceId;
            } else {
                layoutResource = R.layout.screen_title_icons;
            }
            // XXX Remove this once action bar supports these features.
            removeFeature(FEATURE_ACTION_BAR);
            // System.out.println("Title Icons!");
        } else if ((features & ((1 << FEATURE_PROGRESS) | (1 << FEATURE_INDETERMINATE_PROGRESS))) != 0
                && (features & (1 << FEATURE_ACTION_BAR)) == 0) {
            // Special case for a window with only a progress bar (and title).
            // XXX Need to have a no-title version of embedded windows.
            layoutResource = R.layout.screen_progress;
            // System.out.println("Progress!");
        } else if ((features & (1 << FEATURE_CUSTOM_TITLE)) != 0) {
            // Special case for a window with a custom title.
            // If the window is floating, we need a dialog layout
            if (mIsFloating) {
                TypedValue res = new TypedValue();
                getContext().getTheme().resolveAttribute(
                        R.attr.dialogCustomTitleDecorLayout, res, true);
                layoutResource = res.resourceId;
            } else {
                layoutResource = R.layout.screen_custom_title;
            }
            // XXX Remove this once action bar supports these features.
            removeFeature(FEATURE_ACTION_BAR);
        } else if ((features & (1 << FEATURE_NO_TITLE)) == 0) {
            // If no other features and not embedded, only need a title.
            // If the window is floating, we need a dialog layout
            if (mIsFloating) {
                TypedValue res = new TypedValue();
                getContext().getTheme().resolveAttribute(
                        R.attr.dialogTitleDecorLayout, res, true);
                layoutResource = res.resourceId;
            } else if ((features & (1 << FEATURE_ACTION_BAR)) != 0) {
                layoutResource = a.getResourceId(
                        R.styleable.Window_windowActionBarFullscreenDecorLayout,
                        R.layout.screen_action_bar);
            } else {
                layoutResource = R.layout.screen_title;
            }
            // System.out.println("Title!");
        } else if ((features & (1 << FEATURE_ACTION_MODE_OVERLAY)) != 0) {
            layoutResource = R.layout.screen_simple_overlay_action_mode;
        } else {
            // Embedded, so no decoration is needed.
            layoutResource = R.layout.screen_simple;
            // System.out.println("Simple!");
        }

        mDecor.startChanging();
        mDecor.onResourcesLoaded(mLayoutInflater, layoutResource);

        ViewGroup contentParent = (ViewGroup)findViewById(ID_ANDROID_CONTENT);
        if (contentParent == null) {
            throw new RuntimeException("Window couldn't find content container view");
        }

        if ((features & (1 << FEATURE_INDETERMINATE_PROGRESS)) != 0) {
            ProgressBar progress = getCircularProgressBar(false);
            if (progress != null) {
                progress.setIndeterminate(true);
            }
        }

        if ((features & (1 << FEATURE_SWIPE_TO_DISMISS)) != 0) {
            registerSwipeCallbacks(contentParent);
        }

        // Remaining setup -- of background and title -- that only applies
        // to top-level windows.
        if (getContainer() == null) {
            final Drawable background;
            if (mBackgroundResource != 0) {
                background = getContext().getDrawable(mBackgroundResource);
            } else {
                background = mBackgroundDrawable;
            }
            mDecor.setWindowBackground(background);

            final Drawable frame;
            if (mFrameResource != 0) {
                frame = getContext().getDrawable(mFrameResource);
            } else {
                frame = null;
            }
            mDecor.setWindowFrame(frame);

            mDecor.setElevation(mElevation);
            mDecor.setClipToOutline(mClipToOutline);

            if (mTitle != null) {
                setTitle(mTitle);
            }

            if (mTitleColor == 0) {
                mTitleColor = mTextColor;
            }
            setTitleColor(mTitleColor);
        }
        mDecor.finishChanging();
        return contentParent;
    }

    /**
     * 通过featureId请求Feature
     */
    @Override
    public boolean requestFeature(int featureId) {
        if (mContentParentExplicitlySet) {
            throw new AndroidRuntimeException("requestFeature() must be called before adding content");
        }
        final int features = getFeatures();
        final int newFeatures = features | (1 << featureId);
        if ((newFeatures & (1 << FEATURE_CUSTOM_TITLE)) != 0 &&
                (newFeatures & ~CUSTOM_TITLE_COMPATIBLE_FEATURES) != 0) {
            // Another feature is enabled and the user is trying to enable the custom title feature
            // or custom title feature is enabled and the user is trying to enable another feature
            throw new AndroidRuntimeException(
                    "You cannot combine custom titles with other title features");
        }
        if ((features & (1 << FEATURE_NO_TITLE)) != 0 && featureId == FEATURE_ACTION_BAR) {
            return false; // Ignore. No title dominates.
        }
        if ((features & (1 << FEATURE_ACTION_BAR)) != 0 && featureId == FEATURE_NO_TITLE) {
            // Remove the action bar feature if we have no title. No title dominates.
            removeFeature(FEATURE_ACTION_BAR);
        }

        if ((features & (1 << FEATURE_ACTION_BAR)) != 0 && featureId == FEATURE_SWIPE_TO_DISMISS) {
            throw new AndroidRuntimeException(
                    "You cannot combine swipe dismissal and the action bar.");
        }
        if ((features & (1 << FEATURE_SWIPE_TO_DISMISS)) != 0 && featureId == FEATURE_ACTION_BAR) {
            throw new AndroidRuntimeException(
                    "You cannot combine swipe dismissal and the action bar.");
        }
        if (featureId == FEATURE_INDETERMINATE_PROGRESS &&
                getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            throw new AndroidRuntimeException("You cannot use indeterminate progress on a watch.");
        }
        return super.requestFeature(featureId);
    }
}
