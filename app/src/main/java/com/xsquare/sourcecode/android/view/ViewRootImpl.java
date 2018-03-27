package com.xsquare.sourcecode.android.view;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Choreographer;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.inputmethod.InputMethodManager;

import com.xsquare.sourcecode.android.os.SystemProperties;
import com.xsquare.sourcecode.com.android.internal.view.BaseSurfaceHolder;
import com.xsquare.sourcecode.com.android.internal.view.RootViewSurfaceTaker;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import static android.view.Display.INVALID_DISPLAY;

/**
 * Created by xsquare on 2018/3/26.
 */

public class ViewRootImpl implements ViewParent {


    private final static int MSG_INVALIDATE = 1;
    private final static int MSG_INVALIDATE_RECT = 2;
    private final static int MSG_DIE = 3;
    private final static int MSG_RESIZED = 4;
    private final static int MSG_RESIZED_REPORT = 5;
    private final static int MSG_WINDOW_FOCUS_CHANGED = 6;
    private final static int MSG_DISPATCH_INPUT_EVENT = 7;
    private final static int MSG_DISPATCH_APP_VISIBILITY = 8;
    private final static int MSG_DISPATCH_GET_NEW_SURFACE = 9;
    private final static int MSG_DISPATCH_KEY_FROM_IME = 11;
    private final static int MSG_CHECK_FOCUS = 13;
    private final static int MSG_CLOSE_SYSTEM_DIALOGS = 14;
    private final static int MSG_DISPATCH_DRAG_EVENT = 15;
    private final static int MSG_DISPATCH_DRAG_LOCATION_EVENT = 16;
    private final static int MSG_DISPATCH_SYSTEM_UI_VISIBILITY = 17;
    private final static int MSG_UPDATE_CONFIGURATION = 18;
    private final static int MSG_PROCESS_INPUT_EVENTS = 19;
    private final static int MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST = 21;
    private final static int MSG_INVALIDATE_WORLD = 22;
    private final static int MSG_WINDOW_MOVED = 23;
    private final static int MSG_SYNTHESIZE_INPUT_EVENT = 24;
    private final static int MSG_DISPATCH_WINDOW_SHOWN = 25;
    private final static int MSG_REQUEST_KEYBOARD_SHORTCUTS = 26;
    private final static int MSG_UPDATE_POINTER_ICON = 27;
    private final static int MSG_POINTER_CAPTURE_CHANGED = 28;
    private final static int MSG_DRAW_FINISHED = 29;

    private static final String TAG = "ViewRootImpl";

    View mView;
    final W mWindow;
    boolean mIsInTraversal;
    //是否正在drawing
    boolean mIsDrawing;
    //ViewToot的handler
    final ViewRootHandler mHandler = new ViewRootHandler();
    //当前线程
    final Thread mThread;
    /** 一旦调用了doDie（），则设置为true。 */
    private boolean mRemoved;
    //view是否已添加
    boolean mAdded;
    //View添加到父window的数据
    final View.AttachInfo mAttachInfo;

    //view的display信息
    @NonNull
    Display mDisplay;
    final DisplayManager mDisplayManager;
    //view布局的初始方向
    private int mViewLayoutDirectionInitial;
    //回退事件处理器
    FallbackEventHandler mFallbackEventHandler;
    //packageName
    final String mBasePackageName;
    //WindowAttributes
    final WindowManager.LayoutParams mWindowAttributes = new WindowManager.LayoutParams();
    //tag
    private String mTag = TAG;
    //跟踪view的标志
    int mClientWindowLayoutFlags;

    SurfaceHolder.Callback2 mSurfaceHolderCallback;
    BaseSurfaceHolder mSurfaceHolder;

    // 每个显示界面的窗口都是一个Surface。
    // 这些可以被任何线程访问，必须用锁来保护。
    // surface 永远不能被重新分配或清除（使用Surface.clear（））。
    final Surface mSurface = new Surface();

    public ViewRootImpl(Context context, Display display) {
        mContext = context;
        mWindowSession = WindowManagerGlobal.getWindowSession();
        mDisplay = display;
        mBasePackageName = context.getBasePackageName();
        mThread = Thread.currentThread();
        mLocation = new WindowLeaked(null);
        mLocation.fillInStackTrace();
        mWidth = -1;
        mHeight = -1;
        mDirty = new Rect();
        mTempRect = new Rect();
        mVisRect = new Rect();
        mWinFrame = new Rect();
        mWindow = new W(this);
        mTargetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        mViewVisibility = android.view.View.GONE;
        mTransparentRegion = new Region();
        mPreviousTransparentRegion = new Region();
        mFirst = true; // true for the first time the view is added
        mAdded = false;
        mAttachInfo = new android.view.View.AttachInfo(mWindowSession, mWindow, display, this, mHandler, this,
                context);
        mAccessibilityManager = AccessibilityManager.getInstance(context);
        mAccessibilityManager.addAccessibilityStateChangeListener(
                mAccessibilityInteractionConnectionManager, mHandler);
        mHighContrastTextManager = new HighContrastTextManager();
        mAccessibilityManager.addHighTextContrastStateChangeListener(
                mHighContrastTextManager, mHandler);
        mViewConfiguration = ViewConfiguration.get(context);
        mDensity = context.getResources().getDisplayMetrics().densityDpi;
        mNoncompatDensity = context.getResources().getDisplayMetrics().noncompatDensityDpi;
        mFallbackEventHandler = new PhoneFallbackEventHandler(context);
        mChoreographer = Choreographer.getInstance();
        mDisplayManager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);

        if (!sCompatibilityDone) {
            sAlwaysAssignFocus = true;

            sCompatibilityDone = true;
        }
        loadSystemProperties();
    }

    /**
     * ViewRootImpl加载系统配置
     */
    public void loadSystemProperties() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Profiling
                mProfileRendering = SystemProperties.getBoolean(PROPERTY_PROFILE_RENDERING, false);
                profileRendering(mAttachInfo.mHasWindowFocus);

                // Hardware rendering
                if (mAttachInfo.mThreadedRenderer != null) {
                    if (mAttachInfo.mThreadedRenderer.loadSystemProperties()) {
                        invalidate();
                    }
                }

                // Layout debugging
                boolean layout = SystemProperties.getBoolean(android.view.View.DEBUG_LAYOUT_PROPERTY, false);
                if (layout != mAttachInfo.mDebugLayout) {
                    mAttachInfo.mDebugLayout = layout;
                    if (!mHandler.hasMessages(MSG_INVALIDATE_WORLD)) {
                        mHandler.sendEmptyMessageDelayed(MSG_INVALIDATE_WORLD, 200);
                    }
                }
            }
        });
    }

    /**
     * 移除view
     * @param immediate
     * @return
     */
    boolean die(boolean immediate) {
        //立即删除
        //根据immediate参数来判断是执行异步删除还是同步删除
        if (immediate && !mIsInTraversal) {
            doDie();
            return false;
        }
        //是否正在drawing
        if (!mIsDrawing) {
            //如果没有draw了，停止并清除螺纹渲染器
            destroyHardwareRenderer();
        } else {
        }
        //如果是异步删除，则发送一个删除View的消息MSG_DIE就会直接返回
        mHandler.sendEmptyMessage(MSG_DIE);
        return true;
    }
    /**
     * 移除view
     */
    void doDie() {
        //非UI线程报错
        checkThread();
        synchronized (this) {
            //已经移除则返回
            if (mRemoved) {
                return;
            }
            //若已移除设置为true
            mRemoved = true;
            if (mAdded) {
                //调用dispatchDetachedFromWindow()完成View的删除（调用windowManagerService移除view）
                dispatchDetachedFromWindow();
            }
            if (mAdded && !mFirst) {
                //移除硬件渲染
                destroyHardwareRenderer();

                if (mView != null) {
                    int viewVisibility = mView.getVisibility();
                    boolean viewVisibilityChanged = mViewVisibility != viewVisibility;
                    if (mWindowAttributesChanged || viewVisibilityChanged) {
                        // 如果布局参数已更改，请首先将布局参数提供给WindowManager，以确保其具有正确的动画信息。
                        try {
                            if ((relayoutWindow(mWindowAttributes, viewVisibility, false)
                                    & WindowManagerGlobal.RELAYOUT_RES_FIRST_TIME) != 0) {
                                mWindowSession.finishDrawing(mWindow);
                            }
                        } catch (RemoteException e) {
                        }
                    }
                    //释放surface
                    mSurface.release();
                }
            }
            mAdded = false;
        }
        //刷新数据，将当前移除View的相关信息从我们上面说过了三个列表：mRoots、mParms和mViews中移除。
        WindowManagerGlobal.getInstance().doRemoveView(this);
    }
    /**
     * 从window中移除
     */
    void dispatchDetachedFromWindow() {
        if (mView != null && mView.mAttachInfo != null) {
            //android.view.ViewTreeObserver.OnWindowAttachListener 告诉它dettached从window
            mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(false);
            //1. 回调View的dispatchDetachedFromWindow方法，通知该View已从Window中移除
            mView.dispatchDetachedFromWindow();
        }
        mAccessibilityInteractionConnectionManager.ensureNoConnection();
        mAccessibilityManager.removeAccessibilityStateChangeListener(
                mAccessibilityInteractionConnectionManager);
        mAccessibilityManager.removeHighTextContrastStateChangeListener(
                mHighContrastTextManager);
        removeSendWindowContentChangedCallback();
        //移除硬件渲染
        destroyHardwareRenderer();
        setAccessibilityFocus(null, null);
        mView.assignParent(null);
        mView = null;
        mAttachInfo.mRootView = null;
        //释放surface
        mSurface.release();
        if (mInputQueueCallback != null && mInputQueue != null) {
            mInputQueueCallback.onInputQueueDestroyed(mInputQueue);
            mInputQueue.dispose();
            mInputQueueCallback = null;
            mInputQueue = null;
        }
        if (mInputEventReceiver != null) {
            mInputEventReceiver.dispose();
            mInputEventReceiver = null;
        }
        try {
            //调用WindowSession.remove()方法，这同样是一个IPC过程，最终调用的是
            //WindowManagerService.removeWindow()方法来移除Window。
            mWindowSession.remove(mWindow);
        } catch (RemoteException e) {
        }
        // 在删除窗口后配置输入通道，以便窗口管理器不会将输入通道解释为异常终止。
        if (mInputChannel != null) {
            mInputChannel.dispose();
            mInputChannel = null;
        }
        //取消display监听器
        mDisplayManager.unregisterDisplayListener(mDisplayListener);
        unscheduleTraversals();
    }

    /**
     * 停止硬件渲染
     */
    private void destroyHardwareRenderer() {
        //螺纹渲染器
        ThreadedRenderer hardwareRenderer = mAttachInfo.mThreadedRenderer;
        if (hardwareRenderer != null) {
            if (mView != null) {
                hardwareRenderer.destroyHardwareResources(mView);
            }
            hardwareRenderer.destroy();
            hardwareRenderer.setRequested(false);
            mAttachInfo.mThreadedRenderer = null;
            mAttachInfo.mHardwareAccelerated = false;
        }
    }
    void checkThread() {
        if (mThread != Thread.currentThread()) {
            throw new CalledFromWrongThreadException(
                    "Only the original thread that created a view hierarchy can touch its views.");
        }
    }

    /**
     * 1. 调用requestLayout()完成界面异步绘制的请求, requestLayout()会去调用scheduleTraversals()来完成View的绘制，
     * scheduleTraversals()方法将一个TraversalRunnable提交到工作队列中执行View的绘制。
     * 而 TraversalRunnable最终调用了performTraversals()方法来完成实际的绘制操作。
     * 2. 创建WindowSession并通过WindowSession请求WindowManagerService来完成Window添加的过程这是一个IPC的过程，
     * WindowManagerService作为实际的窗口管理者，窗口的创建、删除和更新都是由它来完成的，
     * 它同时还负责了窗口的层叠排序和大小计算 等工作。
     * @param view
     * @param attrs
     * @param panelParentView
     */
    public void setView(View view, android.view.WindowManager.LayoutParams attrs, android.view.View panelParentView) {
        synchronized (this) {
            if (mView == null) {
                mView = view;
                mAttachInfo.mDisplayState = mDisplay.getState();
                //注册display的监听器
                mDisplayManager.registerDisplayListener(mDisplayListener, mHandler);
                //获取mView的布局方向
                mViewLayoutDirectionInitial = mView.getRawLayoutDirection();
                //回退事件处理器设置view
                mFallbackEventHandler.setView(view);
                //复制一份LayoutParams并设置packageName
                mWindowAttributes.copyFrom(attrs);
                if (mWindowAttributes.packageName == null) {
                    mWindowAttributes.packageName = mBasePackageName;
                }
                attrs = mWindowAttributes;
                //设置tag
                setTag();
                // 跟踪view提供的实际窗口标志。
                mClientWindowLayoutFlags = attrs.flags;
                setAccessibilityFocus(null, null);
                //如果view实现了RootViewSurfaceTaker
                if (view instanceof RootViewSurfaceTaker) {
                    mSurfaceHolderCallback =
                            ((RootViewSurfaceTaker)view).willYouTakeTheSurface();
                    if (mSurfaceHolderCallback != null) {
                        mSurfaceHolder = new TakenSurfaceHolder();
                        mSurfaceHolder.setFormat(PixelFormat.UNKNOWN);
                        mSurfaceHolder.addCallback(mSurfaceHolderCallback);
                    }
                }

                // 计算在指定的Z值下绘制所需的表面插图。
                // TODO: Use real shadow insets for a constant max Z.
                if (!attrs.hasManualSurfaceInsets) {
                    attrs.setSurfaceInsets(view, false /*manual*/, true /*preservePrevious*/);
                }
                CompatibilityInfo compatibilityInfo =
                        mDisplay.getDisplayAdjustments().getCompatibilityInfo();
                mTranslator = compatibilityInfo.getTranslator();
                // If the application owns the surface, don't enable hardware acceleration
                if (mSurfaceHolder == null) {
                    enableHardwareAcceleration(attrs);
                }
                boolean restore = false;
                if (mTranslator != null) {
                    mSurface.setCompatibilityTranslator(mTranslator);
                    restore = true;
                    attrs.backup();
                    mTranslator.translateWindowLayout(attrs);
                }
                if (!compatibilityInfo.supportsScreen()) {
                    attrs.privateFlags |= android.view.WindowManager.LayoutParams.PRIVATE_FLAG_COMPATIBLE_WINDOW;
                    mLastInCompatMode = true;
                }
                mSoftInputMode = attrs.softInputMode;
                mWindowAttributesChanged = true;
                mWindowAttributesChangesFlag = android.view.WindowManager.LayoutParams.EVERYTHING_CHANGED;
                mAttachInfo.mRootView = view;
                mAttachInfo.mScalingRequired = mTranslator != null;
                mAttachInfo.mApplicationScale =
                        mTranslator == null ? 1.0f : mTranslator.applicationScale;
                if (panelParentView != null) {
                    mAttachInfo.mPanelParentWindowToken
                            = panelParentView.getApplicationWindowToken();
                }
                mAdded = true;
                int res; /* = WindowManagerImpl.ADD_OKAY; */
                //安排第一个布局 - 在添加到窗口管理器之前，确保我们在从系统接收到任何其他事件之前执行重新布局。
                //1. 调用requestLayout()完成界面异步绘制的请求,最终调用performTraversals()进行测量、布局、绘制
                requestLayout();
                if ((mWindowAttributes.inputFeatures
                        & android.view.WindowManager.LayoutParams.INPUT_FEATURE_NO_INPUT_CHANNEL) == 0) {
                    mInputChannel = new InputChannel();
                }
                mForceDecorViewVisibility = (mWindowAttributes.privateFlags
                        & PRIVATE_FLAG_FORCE_DECOR_VIEW_VISIBILITY) != 0;
                try {
                    mOrigWindowType = mWindowAttributes.type;
                    mAttachInfo.mRecomputeGlobalAttributes = true;
                    collectViewAttributes();
                    //2. 创建WindowSession并通过WindowSession请求WindowManagerService来完成Window添加的过程
                    //这是一个IPC的过程。
                    res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes,
                            getHostVisibility(), mDisplay.getDisplayId(),
                            mAttachInfo.mContentInsets, mAttachInfo.mStableInsets,
                            mAttachInfo.mOutsets, mInputChannel);
                } catch (RemoteException e) {
                    mAdded = false;
                    mView = null;
                    mAttachInfo.mRootView = null;
                    mInputChannel = null;
                    mFallbackEventHandler.setView(null);
                    unscheduleTraversals();
                    setAccessibilityFocus(null, null);
                    throw new RuntimeException("Adding window failed", e);
                } finally {
                    if (restore) {
                        attrs.restore();
                    }
                }
                if (mTranslator != null) {
                    mTranslator.translateRectInScreenToAppWindow(mAttachInfo.mContentInsets);
                }
                mPendingOverscanInsets.set(0, 0, 0, 0);
                mPendingContentInsets.set(mAttachInfo.mContentInsets);
                mPendingStableInsets.set(mAttachInfo.mStableInsets);
                mPendingVisibleInsets.set(0, 0, 0, 0);
                mAttachInfo.mAlwaysConsumeNavBar =
                        (res & WindowManagerGlobal.ADD_FLAG_ALWAYS_CONSUME_NAV_BAR) != 0;
                mPendingAlwaysConsumeNavBar = mAttachInfo.mAlwaysConsumeNavBar;
                if (res < WindowManagerGlobal.ADD_OKAY) {
                    mAttachInfo.mRootView = null;
                    mAdded = false;
                    mFallbackEventHandler.setView(null);
                    unscheduleTraversals();
                    setAccessibilityFocus(null, null);
                    switch (res) {
                        case WindowManagerGlobal.ADD_BAD_APP_TOKEN:
                        case WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN:
                            throw new android.view.WindowManager.BadTokenException(
                                    "Unable to add window -- token " + attrs.token
                                            + " is not valid; is your activity running?");
                        case WindowManagerGlobal.ADD_NOT_APP_TOKEN:
                            throw new android.view.WindowManager.BadTokenException(
                                    "Unable to add window -- token " + attrs.token
                                            + " is not for an application");
                        case WindowManagerGlobal.ADD_APP_EXITING:
                            throw new android.view.WindowManager.BadTokenException(
                                    "Unable to add window -- app for token " + attrs.token
                                            + " is exiting");
                        case WindowManagerGlobal.ADD_DUPLICATE_ADD:
                            throw new android.view.WindowManager.BadTokenException(
                                    "Unable to add window -- window " + mWindow
                                            + " has already been added");
                        case WindowManagerGlobal.ADD_STARTING_NOT_NEEDED:
                            // Silently ignore -- we would have just removed it
                            // right away, anyway.
                            return;
                        case WindowManagerGlobal.ADD_MULTIPLE_SINGLETON:
                            throw new android.view.WindowManager.BadTokenException("Unable to add window "
                                    + mWindow + " -- another window of type "
                                    + mWindowAttributes.type + " already exists");
                        case WindowManagerGlobal.ADD_PERMISSION_DENIED:
                            throw new android.view.WindowManager.BadTokenException("Unable to add window "
                                    + mWindow + " -- permission denied for window type "
                                    + mWindowAttributes.type);
                        case WindowManagerGlobal.ADD_INVALID_DISPLAY:
                            throw new android.view.WindowManager.InvalidDisplayException("Unable to add window "
                                    + mWindow + " -- the specified display can not be found");
                        case WindowManagerGlobal.ADD_INVALID_TYPE:
                            throw new WindowManager.InvalidDisplayException("Unable to add window "
                                    + mWindow + " -- the specified window type "
                                    + mWindowAttributes.type + " is not valid");
                    }
                    throw new RuntimeException(
                            "Unable to add window -- unknown error code " + res);
                }

                if (view instanceof RootViewSurfaceTaker) {
                    mInputQueueCallback =
                            ((RootViewSurfaceTaker)view).willYouTakeTheInputQueue();
                }
                if (mInputChannel != null) {
                    if (mInputQueueCallback != null) {
                        mInputQueue = new InputQueue();
                        mInputQueueCallback.onInputQueueCreated(mInputQueue);
                    }
                    mInputEventReceiver = new WindowInputEventReceiver(mInputChannel,
                            Looper.myLooper());
                }

                view.assignParent(this);
                mAddedTouchMode = (res & WindowManagerGlobal.ADD_FLAG_IN_TOUCH_MODE) != 0;
                mAppVisible = (res & WindowManagerGlobal.ADD_FLAG_APP_VISIBLE) != 0;

                if (mAccessibilityManager.isEnabled()) {
                    mAccessibilityInteractionConnectionManager.ensureConnection();
                }

                if (view.getImportantForAccessibility() == android.view.View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                    view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
                }

                // Set up the input pipeline.
                CharSequence counterSuffix = attrs.getTitle();
                mSyntheticInputStage = new SyntheticInputStage();
                InputStage viewPostImeStage = new ViewPostImeInputStage(mSyntheticInputStage);
                InputStage nativePostImeStage = new NativePostImeInputStage(viewPostImeStage,
                        "aq:native-post-ime:" + counterSuffix);
                InputStage earlyPostImeStage = new EarlyPostImeInputStage(nativePostImeStage);
                InputStage imeStage = new ImeInputStage(earlyPostImeStage,
                        "aq:ime:" + counterSuffix);
                InputStage viewPreImeStage = new ViewPreImeInputStage(imeStage);
                InputStage nativePreImeStage = new NativePreImeInputStage(viewPreImeStage,
                        "aq:native-pre-ime:" + counterSuffix);

                mFirstInputStage = nativePreImeStage;
                mFirstPostImeInputStage = earlyPostImeStage;
                mPendingInputEventQueueLengthCounterName = "aq:pending:" + counterSuffix;
            }
        }
    }

    /**
     * 焦点的设置
     */
    void setAccessibilityFocus(android.view.View view, AccessibilityNodeInfo node) {
        // 如果我们有一个具有可访问性焦点的虚拟视图，我们需要清除焦点并使虚拟视图边界无效。
        if (mAccessibilityFocusedVirtualView != null) {
            AccessibilityNodeInfo focusNode = mAccessibilityFocusedVirtualView;
            android.view.View focusHost = mAccessibilityFocusedHost;
            // Wipe the state of the current accessibility focus since
            // the call into the provider to clear accessibility focus
            // will fire an accessibility event which will end up calling
            // this method and we want to have clean state when this
            // invocation happens.
            mAccessibilityFocusedHost = null;
            mAccessibilityFocusedVirtualView = null;
            // Clear accessibility focus on the host after clearing state since
            // this method may be reentrant.
            focusHost.clearAccessibilityFocusNoCallbacks(
                    AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            AccessibilityNodeProvider provider = focusHost.getAccessibilityNodeProvider();
            if (provider != null) {
                // Invalidate the area of the cleared accessibility focus.
                focusNode.getBoundsInParent(mTempRect);
                focusHost.invalidate(mTempRect);
                // Clear accessibility focus in the virtual node.
                final int virtualNodeId = AccessibilityNodeInfo.getVirtualDescendantId(
                        focusNode.getSourceNodeId());
                provider.performAction(virtualNodeId,
                        AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS, null);
            }
            focusNode.recycle();
        }
        if ((mAccessibilityFocusedHost != null) && (mAccessibilityFocusedHost != view))  {
            // Clear accessibility focus in the view.
            mAccessibilityFocusedHost.clearAccessibilityFocusNoCallbacks(
                    AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
        }
        // Set the new focus host and node.
        mAccessibilityFocusedHost = view;
        mAccessibilityFocusedVirtualView = node;
        if (mAttachInfo.mThreadedRenderer != null) {
            mAttachInfo.mThreadedRenderer.invalidateRoot();
        }
    }

    public View getView() {
        return mView;
    }

    /**
     * 重新设置布局参数
     * @param attrs
     * @param newView
     */
    void setLayoutParams(WindowManager.LayoutParams attrs, boolean newView) {
        synchronized (this) {
            final int oldInsetLeft = mWindowAttributes.surfaceInsets.left;
            final int oldInsetTop = mWindowAttributes.surfaceInsets.top;
            final int oldInsetRight = mWindowAttributes.surfaceInsets.right;
            final int oldInsetBottom = mWindowAttributes.surfaceInsets.bottom;
            final int oldSoftInputMode = mWindowAttributes.softInputMode;
            final boolean oldHasManualSurfaceInsets = mWindowAttributes.hasManualSurfaceInsets;

            // 跟踪客户端提供的实际窗口标志。
            mClientWindowLayoutFlags = attrs.flags;
            // 保留兼容窗口标志（如果存在）。
            final int compatibleWindowFlag = mWindowAttributes.privateFlags
                    & WindowManager.LayoutParams.PRIVATE_FLAG_COMPATIBLE_WINDOW;
            // 系统UI可见性值在传输当前状态时传输。
            attrs.systemUiVisibility = mWindowAttributes.systemUiVisibility;
            attrs.subtreeSystemUiVisibility = mWindowAttributes.subtreeSystemUiVisibility;

            mWindowAttributesChangesFlag = mWindowAttributes.copyFrom(attrs);
            if ((mWindowAttributesChangesFlag
                    & WindowManager.LayoutParams.TRANSLUCENT_FLAGS_CHANGED) != 0) {
                // 重新计算系统UI的可见性。
                mAttachInfo.mRecomputeGlobalAttributes = true;
            }
            if ((mWindowAttributesChangesFlag
                    & WindowManager.LayoutParams.LAYOUT_CHANGED) != 0) {
                // 请求更新灯光中心。
                mAttachInfo.mNeedsUpdateLightCenter = true;
            }
            if (mWindowAttributes.packageName == null) {
                mWindowAttributes.packageName = mBasePackageName;
            }
            mWindowAttributes.privateFlags |= compatibleWindowFlag;
            if (mWindowAttributes.preservePreviousSurfaceInsets) {
                // 恢复旧的表面插页。
                mWindowAttributes.surfaceInsets.set(
                        oldInsetLeft, oldInsetTop, oldInsetRight, oldInsetBottom);
                mWindowAttributes.hasManualSurfaceInsets = oldHasManualSurfaceInsets;
            } else if (mWindowAttributes.surfaceInsets.left != oldInsetLeft
                    || mWindowAttributes.surfaceInsets.top != oldInsetTop
                    || mWindowAttributes.surfaceInsets.right != oldInsetRight
                    || mWindowAttributes.surfaceInsets.bottom != oldInsetBottom) {
                mNeedsRendererSetup = true;
            }

            applyKeepScreenOnFlag(mWindowAttributes);
            //如果是新View，调用requestLayout()进行重新布局
            if (newView) {
                mSoftInputMode = attrs.softInputMode;
                requestLayout();
            }
            // 不要失去我们上次自动计算的模式。
            if ((attrs.softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST)
                    == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED) {
                mWindowAttributes.softInputMode = (mWindowAttributes.softInputMode
                        & ~WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST)
                        | (oldSoftInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST);
            }
            mWindowAttributesChanged = true;
            //如果不是新View，调用requestLayout()进行重新绘制
            scheduleTraversals();
        }
    }

    /**
     * display监听器
     */
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayChanged(int displayId) {
            if (mView != null && mDisplay.getDisplayId() == displayId) {
                final int oldDisplayState = mAttachInfo.mDisplayState;
                final int newDisplayState = mDisplay.getState();
                if (oldDisplayState != newDisplayState) {
                    mAttachInfo.mDisplayState = newDisplayState;
                    pokeDrawLockIfNeeded();
                    if (oldDisplayState != Display.STATE_UNKNOWN) {
                        final int oldScreenState = toViewScreenState(oldDisplayState);
                        final int newScreenState = toViewScreenState(newDisplayState);
                        if (oldScreenState != newScreenState) {
                            mView.dispatchScreenStateChanged(newScreenState);
                        }
                        if (oldDisplayState == Display.STATE_OFF) {
                            // Draw was suppressed so we need to for it to happen here.
                            mFullRedrawNeeded = true;
                            scheduleTraversals();
                        }
                    }
                }
            }
        }

        @Override
        public void onDisplayRemoved(int displayId) {
        }

        @Override
        public void onDisplayAdded(int displayId) {
        }

        private int toViewScreenState(int displayState) {
            return displayState == Display.STATE_OFF ?
                    android.view.View.SCREEN_STATE_OFF : android.view.View.SCREEN_STATE_ON;
        }
    };
    /**
     * 请求布局
     */
    @Override
    public void requestLayout() {
        if (!mHandlingLayoutInLayoutRequest) {
            checkThread();
            mLayoutRequested = true;
            scheduleTraversals();
        }
    }

    /**
     * 设置tag
     */
    private void setTag() {
        final String[] split = mWindowAttributes.getTitle().toString().split("\\.");
        if (split.length > 0) {
            mTag = TAG + "[" + split[split.length - 1] + "]";
        }
    }
    /**
     * 将一个TraversalRunnable提交到工作队列中执行View的绘制。
     */
    void scheduleTraversals() {
        if (!mTraversalScheduled) {
            mTraversalScheduled = true;
            mTraversalBarrier = mHandler.getLooper().getQueue().postSyncBarrier();
            mChoreographer.postCallback(
                    Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);
            if (!mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
        }
    }
    final TraversalRunnable mTraversalRunnable = new TraversalRunnable();
    final class TraversalRunnable implements Runnable {
        @Override
        public void run() {
            doTraversal();
        }
    }

    /**
     * 执行界面绘制操作
     */
    void doTraversal() {
        if (mTraversalScheduled) {
            mTraversalScheduled = false;
            mHandler.getLooper().getQueue().removeSyncBarrier(mTraversalBarrier);
            if (mProfile) {
                Debug.startMethodTracing("ViewAncestor");
            }
            performTraversals();
            if (mProfile) {
                Debug.stopMethodTracing();
                mProfile = false;
            }
        }
    }
    /**
     * 最终完成界面的测量、布局、绘制
     * 1. 获取Surface对象，用于图形绘制。
     * 2. 调用performMeasure()方法测量视图树各个View的大小。
     * 3. 调用performLayout()方法计算视图树各个View的位置，进行布局。
     * 4. 调用performMeasure()方法对视图树的各个View进行绘制。
     */
    private void performTraversals() {
        // cache mView since it is used so much below...
        final View host = mView;
        if (host == null || !mAdded)
            return;
        mIsInTraversal = true;
        mWillDrawSoon = true;
        boolean windowSizeMayChange = false;
        boolean newSurface = false;
        boolean surfaceChanged = false;
        WindowManager.LayoutParams lp = mWindowAttributes;
        int desiredWindowWidth;
        int desiredWindowHeight;
        final int viewVisibility = getHostVisibility();
        final boolean viewVisibilityChanged = !mFirst
                && (mViewVisibility != viewVisibility || mNewSurfaceNeeded);
        final boolean viewUserVisibilityChanged = !mFirst &&
                ((mViewVisibility == View.VISIBLE) != (viewVisibility == View.VISIBLE));
        WindowManager.LayoutParams params = null;
        if (mWindowAttributesChanged) {
            mWindowAttributesChanged = false;
            surfaceChanged = true;
            params = lp;
        }
        CompatibilityInfo compatibilityInfo =
                mDisplay.getDisplayAdjustments().getCompatibilityInfo();
        if (compatibilityInfo.supportsScreen() == mLastInCompatMode) {
            params = lp;
            mFullRedrawNeeded = true;
            mLayoutRequested = true;
            if (mLastInCompatMode) {
                params.privateFlags &= ~WindowManager.LayoutParams.PRIVATE_FLAG_COMPATIBLE_WINDOW;
                mLastInCompatMode = false;
            } else {
                params.privateFlags |= WindowManager.LayoutParams.PRIVATE_FLAG_COMPATIBLE_WINDOW;
                mLastInCompatMode = true;
            }
        }
        mWindowAttributesChangesFlag = 0;
        Rect frame = mWinFrame;
        if (mFirst) {
            mFullRedrawNeeded = true;
            mLayoutRequested = true;
            final Configuration config = mContext.getResources().getConfiguration();
            if (shouldUseDisplaySize(lp)) {
                // NOTE -- system code, won't try to do compat mode.
                Point size = new Point();
                mDisplay.getRealSize(size);
                desiredWindowWidth = size.x;
                desiredWindowHeight = size.y;
            } else {
                desiredWindowWidth = dipToPx(config.screenWidthDp);
                desiredWindowHeight = dipToPx(config.screenHeightDp);
            }
            // We used to use the following condition to choose 32 bits drawing caches:
            // PixelFormat.hasAlpha(lp.format) || lp.format == PixelFormat.RGBX_8888
            // However, windows are now always 32 bits by default, so choose 32 bits
            mAttachInfo.mUse32BitDrawingCache = true;
            mAttachInfo.mHasWindowFocus = false;
            mAttachInfo.mWindowVisibility = viewVisibility;
            mAttachInfo.mRecomputeGlobalAttributes = false;
            mLastConfigurationFromResources.setTo(config);
            mLastSystemUiVisibility = mAttachInfo.mSystemUiVisibility;
            // Set the layout direction if it has not been set before (inherit is the default)
            if (mViewLayoutDirectionInitial == View.LAYOUT_DIRECTION_INHERIT) {
                host.setLayoutDirection(config.getLayoutDirection());
            }
            host.dispatchAttachedToWindow(mAttachInfo, 0);
            mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(true);
            dispatchApplyInsets(host);
        } else {
            desiredWindowWidth = frame.width();
            desiredWindowHeight = frame.height();
            if (desiredWindowWidth != mWidth || desiredWindowHeight != mHeight) {
                mFullRedrawNeeded = true;
                mLayoutRequested = true;
                windowSizeMayChange = true;
            }
        }
        if (viewVisibilityChanged) {
            mAttachInfo.mWindowVisibility = viewVisibility;
            host.dispatchWindowVisibilityChanged(viewVisibility);
            if (viewUserVisibilityChanged) {
                host.dispatchVisibilityAggregated(viewVisibility == View.VISIBLE);
            }
            if (viewVisibility != View.VISIBLE || mNewSurfaceNeeded) {
                endDragResizing();
                destroyHardwareResources();
            }
            if (viewVisibility == View.GONE) {
                // After making a window gone, we will count it as being
                // shown for the first time the next time it gets focus.
                mHasHadWindowFocus = false;
            }
        }
        // Non-visible windows can't hold accessibility focus.
        if (mAttachInfo.mWindowVisibility != View.VISIBLE) {
            host.clearAccessibilityFocus();
        }
        // Execute enqueued actions on every traversal in case a detached view enqueued an action
        getRunQueue().executeActions(mAttachInfo.mHandler);
        boolean insetsChanged = false;
        boolean layoutRequested = mLayoutRequested && (!mStopped || mReportNextDraw);
        if (layoutRequested) {
            final Resources res = mView.getContext().getResources();
            if (mFirst) {
                // make sure touch mode code executes by setting cached value
                // to opposite of the added touch mode.
                mAttachInfo.mInTouchMode = !mAddedTouchMode;
                ensureTouchModeLocally(mAddedTouchMode);
            } else {
                if (!mPendingOverscanInsets.equals(mAttachInfo.mOverscanInsets)) {
                    insetsChanged = true;
                }
                if (!mPendingContentInsets.equals(mAttachInfo.mContentInsets)) {
                    insetsChanged = true;
                }
                if (!mPendingStableInsets.equals(mAttachInfo.mStableInsets)) {
                    insetsChanged = true;
                }
                if (!mPendingVisibleInsets.equals(mAttachInfo.mVisibleInsets)) {
                    mAttachInfo.mVisibleInsets.set(mPendingVisibleInsets);
                }
                if (!mPendingOutsets.equals(mAttachInfo.mOutsets)) {
                    insetsChanged = true;
                }
                if (mPendingAlwaysConsumeNavBar != mAttachInfo.mAlwaysConsumeNavBar) {
                    insetsChanged = true;
                }
                if (lp.width == android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        || lp.height == android.view.ViewGroup.LayoutParams.WRAP_CONTENT) {
                    windowSizeMayChange = true;
                    if (shouldUseDisplaySize(lp)) {
                        // NOTE -- system code, won't try to do compat mode.
                        Point size = new Point();
                        mDisplay.getRealSize(size);
                        desiredWindowWidth = size.x;
                        desiredWindowHeight = size.y;
                    } else {
                        Configuration config = res.getConfiguration();
                        desiredWindowWidth = dipToPx(config.screenWidthDp);
                        desiredWindowHeight = dipToPx(config.screenHeightDp);
                    }
                }
            }
            // Ask host how big it wants to be
            windowSizeMayChange |= measureHierarchy(host, lp, res,
                    desiredWindowWidth, desiredWindowHeight);
        }
        if (collectViewAttributes()) {
            params = lp;
        }
        if (mAttachInfo.mForceReportNewAttributes) {
            mAttachInfo.mForceReportNewAttributes = false;
            params = lp;
        }
        if (mFirst || mAttachInfo.mViewVisibilityChanged) {
            mAttachInfo.mViewVisibilityChanged = false;
            int resizeMode = mSoftInputMode &
                    WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST;
            // If we are in auto resize mode, then we need to determine
            // what mode to use now.
            if (resizeMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED) {
                final int N = mAttachInfo.mScrollContainers.size();
                for (int i=0; i<N; i++) {
                    if (mAttachInfo.mScrollContainers.get(i).isShown()) {
                        resizeMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
                    }
                }
                if (resizeMode == 0) {
                    resizeMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
                }
                if ((lp.softInputMode &
                        WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST) != resizeMode) {
                    lp.softInputMode = (lp.softInputMode &
                            ~WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST) |
                            resizeMode;
                    params = lp;
                }
            }
        }

        if (params != null) {
            if ((host.mPrivateFlags & View.PFLAG_REQUEST_TRANSPARENT_REGIONS) != 0) {
                if (!PixelFormat.formatHasAlpha(params.format)) {
                    params.format = PixelFormat.TRANSLUCENT;
                }
            }
            mAttachInfo.mOverscanRequested = (params.flags
                    & WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN) != 0;
        }

        if (mApplyInsetsRequested) {
            mApplyInsetsRequested = false;
            mLastOverscanRequested = mAttachInfo.mOverscanRequested;
            dispatchApplyInsets(host);
            if (mLayoutRequested) {
                // Short-circuit catching a new layout request here, so
                // we don't need to go through two layout passes when things
                // change due to fitting system windows, which can happen a lot.
                windowSizeMayChange |= measureHierarchy(host, lp,
                        mView.getContext().getResources(),
                        desiredWindowWidth, desiredWindowHeight);
            }
        }
        if (layoutRequested) {
            // Clear this now, so that if anything requests a layout in the
            // rest of this function we will catch it and re-run a full
            // layout pass.
            mLayoutRequested = false;
        }
        boolean windowShouldResize = layoutRequested && windowSizeMayChange
                && ((mWidth != host.getMeasuredWidth() || mHeight != host.getMeasuredHeight())
                || (lp.width == android.view.ViewGroup.LayoutParams.WRAP_CONTENT &&
                frame.width() < desiredWindowWidth && frame.width() != mWidth)
                || (lp.height == ViewGroup.LayoutParams.WRAP_CONTENT &&
                frame.height() < desiredWindowHeight && frame.height() != mHeight));
        windowShouldResize |= mDragResizing && mResizeMode == RESIZE_MODE_FREEFORM;
        // If the activity was just relaunched, it might have unfrozen the task bounds (while
        // relaunching), so we need to force a call into window manager to pick up the latest
        // bounds.
        windowShouldResize |= mActivityRelaunched;
        // Determine whether to compute insets.
        // If there are no inset listeners remaining then we may still need to compute
        // insets in case the old insets were non-empty and must be reset.
        final boolean computesInternalInsets =
                mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners()
                        || mAttachInfo.mHasNonEmptyGivenInternalInsets;
        boolean insetsPending = false;
        int relayoutResult = 0;
        boolean updatedConfiguration = false;
        final int surfaceGenerationId = mSurface.getGenerationId();
        final boolean isViewVisible = viewVisibility == View.VISIBLE;
        final boolean windowRelayoutWasForced = mForceNextWindowRelayout;
        if (mFirst || windowShouldResize || insetsChanged ||
                viewVisibilityChanged || params != null || mForceNextWindowRelayout) {
            mForceNextWindowRelayout = false;
            if (isViewVisible) {
                // If this window is giving internal insets to the window
                // manager, and it is being added or changing its visibility,
                // then we want to first give the window manager "fake"
                // insets to cause it to effectively ignore the content of
                // the window during layout.  This avoids it briefly causing
                // other windows to resize/move based on the raw frame of the
                // window, waiting until we can finish laying out this window
                // and get back to the window manager with the ultimately
                // computed insets.
                insetsPending = computesInternalInsets && (mFirst || viewVisibilityChanged);
            }
            if (mSurfaceHolder != null) {
                mSurfaceHolder.mSurfaceLock.lock();
                mDrawingAllowed = true;
            }
            boolean hwInitialized = false;
            boolean contentInsetsChanged = false;
            boolean hadSurface = mSurface.isValid();
            try {
                if (mAttachInfo.mThreadedRenderer != null) {
                    // relayoutWindow may decide to destroy mSurface. As that decision
                    // happens in WindowManager service, we need to be defensive here
                    // and stop using the surface in case it gets destroyed.
                    if (mAttachInfo.mThreadedRenderer.pauseSurface(mSurface)) {
                        // Animations were running so we need to push a frame
                        // to resume them
                        mDirty.set(0, 0, mWidth, mHeight);
                    }
                    mChoreographer.mFrameInfo.addFlags(FrameInfo.FLAG_WINDOW_LAYOUT_CHANGED);
                }
                relayoutResult = relayoutWindow(params, viewVisibility, insetsPending);
                final Configuration pendingMergedConfig =
                        mPendingMergedConfiguration.getMergedConfiguration();
                if (pendingMergedConfig.seq != 0) {
                    performConfigurationChange(mPendingMergedConfiguration, !mFirst,
                            INVALID_DISPLAY /* same display */);
                    pendingMergedConfig.seq = 0;
                    updatedConfiguration = true;
                }

                final boolean overscanInsetsChanged = !mPendingOverscanInsets.equals(
                        mAttachInfo.mOverscanInsets);
                contentInsetsChanged = !mPendingContentInsets.equals(
                        mAttachInfo.mContentInsets);
                final boolean visibleInsetsChanged = !mPendingVisibleInsets.equals(
                        mAttachInfo.mVisibleInsets);
                final boolean stableInsetsChanged = !mPendingStableInsets.equals(
                        mAttachInfo.mStableInsets);
                final boolean outsetsChanged = !mPendingOutsets.equals(mAttachInfo.mOutsets);
                final boolean surfaceSizeChanged = (relayoutResult
                        & WindowManagerGlobal.RELAYOUT_RES_SURFACE_RESIZED) != 0;
                final boolean alwaysConsumeNavBarChanged =
                        mPendingAlwaysConsumeNavBar != mAttachInfo.mAlwaysConsumeNavBar;
                if (contentInsetsChanged) {
                    mAttachInfo.mContentInsets.set(mPendingContentInsets);
                }
                if (overscanInsetsChanged) {
                    mAttachInfo.mOverscanInsets.set(mPendingOverscanInsets);
                    // Need to relayout with content insets.
                    contentInsetsChanged = true;
                }
                if (stableInsetsChanged) {
                    mAttachInfo.mStableInsets.set(mPendingStableInsets);
                    // Need to relayout with content insets.
                    contentInsetsChanged = true;
                }
                if (alwaysConsumeNavBarChanged) {
                    mAttachInfo.mAlwaysConsumeNavBar = mPendingAlwaysConsumeNavBar;
                    contentInsetsChanged = true;
                }
                if (contentInsetsChanged || mLastSystemUiVisibility !=
                        mAttachInfo.mSystemUiVisibility || mApplyInsetsRequested
                        || mLastOverscanRequested != mAttachInfo.mOverscanRequested
                        || outsetsChanged) {
                    mLastSystemUiVisibility = mAttachInfo.mSystemUiVisibility;
                    mLastOverscanRequested = mAttachInfo.mOverscanRequested;
                    mAttachInfo.mOutsets.set(mPendingOutsets);
                    mApplyInsetsRequested = false;
                    dispatchApplyInsets(host);
                }
                if (visibleInsetsChanged) {
                    mAttachInfo.mVisibleInsets.set(mPendingVisibleInsets);
                }

                if (!hadSurface) {
                    if (mSurface.isValid()) {
                        // If we are creating a new surface, then we need to
                        // completely redraw it.  Also, when we get to the
                        // point of drawing it we will hold off and schedule
                        // a new traversal instead.  This is so we can tell the
                        // window manager about all of the windows being displayed
                        // before actually drawing them, so it can display then
                        // all at once.
                        newSurface = true;
                        mFullRedrawNeeded = true;
                        mPreviousTransparentRegion.setEmpty();

                        // Only initialize up-front if transparent regions are not
                        // requested, otherwise defer to see if the entire window
                        // will be transparent
                        if (mAttachInfo.mThreadedRenderer != null) {
                            try {
                                hwInitialized = mAttachInfo.mThreadedRenderer.initialize(
                                        mSurface);
                                if (hwInitialized && (host.mPrivateFlags
                                        & View.PFLAG_REQUEST_TRANSPARENT_REGIONS) == 0) {
                                    // Don't pre-allocate if transparent regions
                                    // are requested as they may not be needed
                                    mSurface.allocateBuffers();
                                }
                            } catch (Surface.OutOfResourcesException e) {
                                handleOutOfResourcesException(e);
                                return;
                            }
                        }
                    }
                } else if (!mSurface.isValid()) {
                    // If the surface has been removed, then reset the scroll
                    // positions.
                    if (mLastScrolledFocus != null) {
                        mLastScrolledFocus.clear();
                    }
                    mScrollY = mCurScrollY = 0;
                    if (mView instanceof RootViewSurfaceTaker) {
                        ((RootViewSurfaceTaker) mView).onRootViewScrollYChanged(mCurScrollY);
                    }
                    if (mScroller != null) {
                        mScroller.abortAnimation();
                    }
                    // Our surface is gone
                    if (mAttachInfo.mThreadedRenderer != null &&
                            mAttachInfo.mThreadedRenderer.isEnabled()) {
                        mAttachInfo.mThreadedRenderer.destroy();
                    }
                } else if ((surfaceGenerationId != mSurface.getGenerationId()
                        || surfaceSizeChanged || windowRelayoutWasForced)
                        && mSurfaceHolder == null
                        && mAttachInfo.mThreadedRenderer != null) {
                    mFullRedrawNeeded = true;
                    try {
                        // Need to do updateSurface (which leads to CanvasContext::setSurface and
                        // re-create the EGLSurface) if either the Surface changed (as indicated by
                        // generation id), or WindowManager changed the surface size. The latter is
                        // because on some chips, changing the consumer side's BufferQueue size may
                        // not take effect immediately unless we create a new EGLSurface.
                        // Note that frame size change doesn't always imply surface size change (eg.
                        // drag resizing uses fullscreen surface), need to check surfaceSizeChanged
                        // flag from WindowManager.
                        mAttachInfo.mThreadedRenderer.updateSurface(mSurface);
                    } catch (Surface.OutOfResourcesException e) {
                        handleOutOfResourcesException(e);
                        return;
                    }
                }

                final boolean freeformResizing = (relayoutResult
                        & WindowManagerGlobal.RELAYOUT_RES_DRAG_RESIZING_FREEFORM) != 0;
                final boolean dockedResizing = (relayoutResult
                        & WindowManagerGlobal.RELAYOUT_RES_DRAG_RESIZING_DOCKED) != 0;
                final boolean dragResizing = freeformResizing || dockedResizing;
                if (mDragResizing != dragResizing) {
                    if (dragResizing) {
                        mResizeMode = freeformResizing
                                ? RESIZE_MODE_FREEFORM
                                : RESIZE_MODE_DOCKED_DIVIDER;
                        startDragResizing(mPendingBackDropFrame,
                                mWinFrame.equals(mPendingBackDropFrame), mPendingVisibleInsets,
                                mPendingStableInsets, mResizeMode);
                    } else {
                        // We shouldn't come here, but if we come we should end the resize.
                        endDragResizing();
                    }
                }
                if (!USE_MT_RENDERER) {
                    if (dragResizing) {
                        mCanvasOffsetX = mWinFrame.left;
                        mCanvasOffsetY = mWinFrame.top;
                    } else {
                        mCanvasOffsetX = mCanvasOffsetY = 0;
                    }
                }
            } catch (RemoteException e) {
            }
            mAttachInfo.mWindowLeft = frame.left;
            mAttachInfo.mWindowTop = frame.top;

            // !!FIXME!! This next section handles the case where we did not get the
            // window size we asked for. We should avoid this by getting a maximum size from
            // the window session beforehand.
            if (mWidth != frame.width() || mHeight != frame.height()) {
                mWidth = frame.width();
                mHeight = frame.height();
            }

            if (mSurfaceHolder != null) {
                // The app owns the surface; tell it about what is going on.
                if (mSurface.isValid()) {
                    // XXX .copyFrom() doesn't work!
                    //mSurfaceHolder.mSurface.copyFrom(mSurface);
                    mSurfaceHolder.mSurface = mSurface;
                }
                mSurfaceHolder.setSurfaceFrameSize(mWidth, mHeight);
                mSurfaceHolder.mSurfaceLock.unlock();
                if (mSurface.isValid()) {
                    if (!hadSurface) {
                        mSurfaceHolder.ungetCallbacks();

                        mIsCreating = true;
                        SurfaceHolder.Callback callbacks[] = mSurfaceHolder.getCallbacks();
                        if (callbacks != null) {
                            for (SurfaceHolder.Callback c : callbacks) {
                                c.surfaceCreated(mSurfaceHolder);
                            }
                        }
                        surfaceChanged = true;
                    }
                    if (surfaceChanged || surfaceGenerationId != mSurface.getGenerationId()) {
                        SurfaceHolder.Callback callbacks[] = mSurfaceHolder.getCallbacks();
                        if (callbacks != null) {
                            for (SurfaceHolder.Callback c : callbacks) {
                                c.surfaceChanged(mSurfaceHolder, lp.format,
                                        mWidth, mHeight);
                            }
                        }
                    }
                    mIsCreating = false;
                } else if (hadSurface) {
                    mSurfaceHolder.ungetCallbacks();
                    SurfaceHolder.Callback callbacks[] = mSurfaceHolder.getCallbacks();
                    if (callbacks != null) {
                        for (SurfaceHolder.Callback c : callbacks) {
                            c.surfaceDestroyed(mSurfaceHolder);
                        }
                    }
                    mSurfaceHolder.mSurfaceLock.lock();
                    try {
                        mSurfaceHolder.mSurface = new Surface();
                    } finally {
                        mSurfaceHolder.mSurfaceLock.unlock();
                    }
                }
            }

            final ThreadedRenderer threadedRenderer = mAttachInfo.mThreadedRenderer;
            if (threadedRenderer != null && threadedRenderer.isEnabled()) {
                if (hwInitialized
                        || mWidth != threadedRenderer.getWidth()
                        || mHeight != threadedRenderer.getHeight()
                        || mNeedsRendererSetup) {
                    threadedRenderer.setup(mWidth, mHeight, mAttachInfo,
                            mWindowAttributes.surfaceInsets);
                    mNeedsRendererSetup = false;
                }
            }

            if (!mStopped || mReportNextDraw) {
                boolean focusChangedDueToTouchMode = ensureTouchModeLocally(
                        (relayoutResult&WindowManagerGlobal.RELAYOUT_RES_IN_TOUCH_MODE) != 0);
                if (focusChangedDueToTouchMode || mWidth != host.getMeasuredWidth()
                        || mHeight != host.getMeasuredHeight() || contentInsetsChanged ||
                        updatedConfiguration) {
                    int childWidthMeasureSpec = getRootMeasureSpec(mWidth, lp.width);
                    int childHeightMeasureSpec = getRootMeasureSpec(mHeight, lp.height);
                    // Ask host how big it wants to be
                    performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);

                    // Implementation of weights from WindowManager.LayoutParams
                    // We just grow the dimensions as needed and re-measure if
                    // needs be
                    int width = host.getMeasuredWidth();
                    int height = host.getMeasuredHeight();
                    boolean measureAgain = false;

                    if (lp.horizontalWeight > 0.0f) {
                        width += (int) ((mWidth - width) * lp.horizontalWeight);
                        childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width,
                                View.MeasureSpec.EXACTLY);
                        measureAgain = true;
                    }
                    if (lp.verticalWeight > 0.0f) {
                        height += (int) ((mHeight - height) * lp.verticalWeight);
                        childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height,
                                View.MeasureSpec.EXACTLY);
                        measureAgain = true;
                    }

                    if (measureAgain) {
                        performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                    }

                    layoutRequested = true;
                }
            }
        } else {
            // Not the first pass and no window/insets/visibility change but the window
            // may have moved and we need check that and if so to update the left and right
            // in the attach info. We translate only the window frame since on window move
            // the window manager tells us only for the new frame but the insets are the
            // same and we do not want to translate them more than once.
            maybeHandleWindowMove(frame);
        }

        final boolean didLayout = layoutRequested && (!mStopped || mReportNextDraw);
        boolean triggerGlobalLayoutListener = didLayout
                || mAttachInfo.mRecomputeGlobalAttributes;
        if (didLayout) {
            performLayout(lp, mWidth, mHeight);

            // By this point all views have been sized and positioned
            // We can compute the transparent area

            if ((host.mPrivateFlags & View.PFLAG_REQUEST_TRANSPARENT_REGIONS) != 0) {
                // start out transparent
                // TODO: AVOID THAT CALL BY CACHING THE RESULT?
                host.getLocationInWindow(mTmpLocation);
                mTransparentRegion.set(mTmpLocation[0], mTmpLocation[1],
                        mTmpLocation[0] + host.mRight - host.mLeft,
                        mTmpLocation[1] + host.mBottom - host.mTop);

                host.gatherTransparentRegion(mTransparentRegion);
                if (mTranslator != null) {
                    mTranslator.translateRegionInWindowToScreen(mTransparentRegion);
                }

                if (!mTransparentRegion.equals(mPreviousTransparentRegion)) {
                    mPreviousTransparentRegion.set(mTransparentRegion);
                    mFullRedrawNeeded = true;
                    // reconfigure window manager
                    try {
                        mWindowSession.setTransparentRegion(mWindow, mTransparentRegion);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
        if (triggerGlobalLayoutListener) {
            mAttachInfo.mRecomputeGlobalAttributes = false;
            mAttachInfo.mTreeObserver.dispatchOnGlobalLayout();
        }
        if (computesInternalInsets) {
            // Clear the original insets.
            final ViewTreeObserver.InternalInsetsInfo insets = mAttachInfo.mGivenInternalInsets;
            insets.reset();
            // Compute new insets in place.
            mAttachInfo.mTreeObserver.dispatchOnComputeInternalInsets(insets);
            mAttachInfo.mHasNonEmptyGivenInternalInsets = !insets.isEmpty();
            // Tell the window manager.
            if (insetsPending || !mLastGivenInsets.equals(insets)) {
                mLastGivenInsets.set(insets);
                // Translate insets to screen coordinates if needed.
                final Rect contentInsets;
                final Rect visibleInsets;
                final Region touchableRegion;
                if (mTranslator != null) {
                    contentInsets = mTranslator.getTranslatedContentInsets(insets.contentInsets);
                    visibleInsets = mTranslator.getTranslatedVisibleInsets(insets.visibleInsets);
                    touchableRegion = mTranslator.getTranslatedTouchableArea(insets.touchableRegion);
                } else {
                    contentInsets = insets.contentInsets;
                    visibleInsets = insets.visibleInsets;
                    touchableRegion = insets.touchableRegion;
                }
                try {
                    mWindowSession.setInsets(mWindow, insets.mTouchableInsets,
                            contentInsets, visibleInsets, touchableRegion);
                } catch (RemoteException e) {
                }
            }
        }
        if (mFirst && sAlwaysAssignFocus) {
            // handle first focus request
            if (mView != null) {
                if (!mView.hasFocus()) {
                    mView.restoreDefaultFocus();
                } else {
                }
            }
        }
        final boolean changedVisibility = (viewVisibilityChanged || mFirst) && isViewVisible;
        final boolean hasWindowFocus = mAttachInfo.mHasWindowFocus && isViewVisible;
        final boolean regainedFocus = hasWindowFocus && mLostWindowFocus;
        if (regainedFocus) {
            mLostWindowFocus = false;
        } else if (!hasWindowFocus && mHadWindowFocus) {
            mLostWindowFocus = true;
        }
        if (changedVisibility || regainedFocus) {
            // Toasts are presented as notifications - don't present them as windows as well
            boolean isToast = (mWindowAttributes == null) ? false
                    : (mWindowAttributes.type == WindowManager.LayoutParams.TYPE_TOAST);
            if (!isToast) {
                host.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
            }
        }
        mFirst = false;
        mWillDrawSoon = false;
        mNewSurfaceNeeded = false;
        mActivityRelaunched = false;
        mViewVisibility = viewVisibility;
        mHadWindowFocus = hasWindowFocus;
        if (hasWindowFocus && !isInLocalFocusMode()) {
            final boolean imTarget = WindowManager.LayoutParams
                    .mayUseInputMethod(mWindowAttributes.flags);
            if (imTarget != mLastWasImTarget) {
                mLastWasImTarget = imTarget;
                InputMethodManager imm = InputMethodManager.peekInstance();
                if (imm != null && imTarget) {
                    imm.onPreWindowFocus(mView, hasWindowFocus);
                    imm.onPostWindowFocus(mView, mView.findFocus(),
                            mWindowAttributes.softInputMode,
                            !mHasHadWindowFocus, mWindowAttributes.flags);
                }
            }
        }
        // Remember if we must report the next draw.
        if ((relayoutResult & WindowManagerGlobal.RELAYOUT_RES_FIRST_TIME) != 0) {
            reportNextDraw();
        }
        boolean cancelDraw = mAttachInfo.mTreeObserver.dispatchOnPreDraw() || !isViewVisible;
        if (!cancelDraw && !newSurface) {
            if (mPendingTransitions != null && mPendingTransitions.size() > 0) {
                for (int i = 0; i < mPendingTransitions.size(); ++i) {
                    mPendingTransitions.get(i).startChangingAnimations();
                }
                mPendingTransitions.clear();
            }
            performDraw();
        } else {
            if (isViewVisible) {
                // Try again
                scheduleTraversals();
            } else if (mPendingTransitions != null && mPendingTransitions.size() > 0) {
                for (int i = 0; i < mPendingTransitions.size(); ++i) {
                    mPendingTransitions.get(i).endChangingAnimations();
                }
                mPendingTransitions.clear();
            }
        }

        mIsInTraversal = false;
    }


    static class W extends IWindow.Stub {
        private final WeakReference<ViewRootImpl> mViewAncestor;
        private final IWindowSession mWindowSession;

        W(ViewRootImpl viewAncestor) {
            mViewAncestor = new WeakReference<ViewRootImpl>(viewAncestor);
            mWindowSession = viewAncestor.mWindowSession;
        }

        @Override
        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets,
                            Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw,
                            MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout,
                            boolean alwaysConsumeNavBar, int displayId) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchResized(frame, overscanInsets, contentInsets,
                        visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration,
                        backDropFrame, forceLayout, alwaysConsumeNavBar, displayId);
            }
        }

        @Override
        public void moved(int newX, int newY) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchMoved(newX, newY);
            }
        }

        @Override
        public void dispatchAppVisibility(boolean visible) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchAppVisibility(visible);
            }
        }

        @Override
        public void dispatchGetNewSurface() {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchGetNewSurface();
            }
        }

        @Override
        public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.windowFocusChanged(hasFocus, inTouchMode);
            }
        }

        private static int checkCallingPermission(String permission) {
            try {
                return ActivityManager.getService().checkPermission(
                        permission, Binder.getCallingPid(), Binder.getCallingUid());
            } catch (RemoteException e) {
                return PackageManager.PERMISSION_DENIED;
            }
        }

        @Override
        public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                final View view = viewAncestor.mView;
                if (view != null) {
                    if (checkCallingPermission(Manifest.permission.DUMP) !=
                            PackageManager.PERMISSION_GRANTED) {
                        throw new SecurityException("Insufficient permissions to invoke"
                                + " executeCommand() from pid=" + Binder.getCallingPid()
                                + ", uid=" + Binder.getCallingUid());
                    }

                    OutputStream clientStream = null;
                    try {
                        clientStream = new ParcelFileDescriptor.AutoCloseOutputStream(out);
                        ViewDebug.dispatchCommand(view, command, parameters, clientStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (clientStream != null) {
                            try {
                                clientStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void closeSystemDialogs(String reason) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchCloseSystemDialogs(reason);
            }
        }

        @Override
        public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep,
                                             boolean sync) {
            if (sync) {
                try {
                    mWindowSession.wallpaperOffsetsComplete(asBinder());
                } catch (RemoteException e) {
                }
            }
        }

        @Override
        public void dispatchWallpaperCommand(String action, int x, int y,
                                             int z, Bundle extras, boolean sync) {
            if (sync) {
                try {
                    mWindowSession.wallpaperCommandComplete(asBinder(), null);
                } catch (RemoteException e) {
                }
            }
        }

        /* Drag/drop */
        @Override
        public void dispatchDragEvent(DragEvent event) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchDragEvent(event);
            }
        }

        @Override
        public void updatePointerIcon(float x, float y) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.updatePointerIcon(x, y);
            }
        }

        @Override
        public void dispatchSystemUiVisibilityChanged(int seq, int globalVisibility,
                                                      int localValue, int localChanges) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchSystemUiVisibilityChanged(seq, globalVisibility,
                        localValue, localChanges);
            }
        }

        @Override
        public void dispatchWindowShown() {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchWindowShown();
            }
        }

        @Override
        public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
            ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchRequestKeyboardShortcuts(receiver, deviceId);
            }
        }

        @Override
        public void dispatchPointerCaptureChanged(boolean hasCapture) {
            final ViewRootImpl viewAncestor = mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchPointerCaptureChanged(hasCapture);
            }
        }

    }
    final class ViewRootHandler extends Handler {
        @Override
        public String getMessageName(Message message) {
            switch (message.what) {
                case MSG_INVALIDATE:
                    return "MSG_INVALIDATE";
                case MSG_INVALIDATE_RECT:
                    return "MSG_INVALIDATE_RECT";
                case MSG_DIE:
                    return "MSG_DIE";
                case MSG_RESIZED:
                    return "MSG_RESIZED";
                case MSG_RESIZED_REPORT:
                    return "MSG_RESIZED_REPORT";
                case MSG_WINDOW_FOCUS_CHANGED:
                    return "MSG_WINDOW_FOCUS_CHANGED";
                case MSG_DISPATCH_INPUT_EVENT:
                    return "MSG_DISPATCH_INPUT_EVENT";
                case MSG_DISPATCH_APP_VISIBILITY:
                    return "MSG_DISPATCH_APP_VISIBILITY";
                case MSG_DISPATCH_GET_NEW_SURFACE:
                    return "MSG_DISPATCH_GET_NEW_SURFACE";
                case MSG_DISPATCH_KEY_FROM_IME:
                    return "MSG_DISPATCH_KEY_FROM_IME";
                case MSG_CHECK_FOCUS:
                    return "MSG_CHECK_FOCUS";
                case MSG_CLOSE_SYSTEM_DIALOGS:
                    return "MSG_CLOSE_SYSTEM_DIALOGS";
                case MSG_DISPATCH_DRAG_EVENT:
                    return "MSG_DISPATCH_DRAG_EVENT";
                case MSG_DISPATCH_DRAG_LOCATION_EVENT:
                    return "MSG_DISPATCH_DRAG_LOCATION_EVENT";
                case MSG_DISPATCH_SYSTEM_UI_VISIBILITY:
                    return "MSG_DISPATCH_SYSTEM_UI_VISIBILITY";
                case MSG_UPDATE_CONFIGURATION:
                    return "MSG_UPDATE_CONFIGURATION";
                case MSG_PROCESS_INPUT_EVENTS:
                    return "MSG_PROCESS_INPUT_EVENTS";
                case MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST:
                    return "MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST";
                case MSG_WINDOW_MOVED:
                    return "MSG_WINDOW_MOVED";
                case MSG_SYNTHESIZE_INPUT_EVENT:
                    return "MSG_SYNTHESIZE_INPUT_EVENT";
                case MSG_DISPATCH_WINDOW_SHOWN:
                    return "MSG_DISPATCH_WINDOW_SHOWN";
                case MSG_UPDATE_POINTER_ICON:
                    return "MSG_UPDATE_POINTER_ICON";
                case MSG_POINTER_CAPTURE_CHANGED:
                    return "MSG_POINTER_CAPTURE_CHANGED";
                case MSG_DRAW_FINISHED:
                    return "MSG_DRAW_FINISHED";
            }
            return super.getMessageName(message);
        }

        @Override
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            if (msg.what == MSG_REQUEST_KEYBOARD_SHORTCUTS && msg.obj == null) {
                // Debugging for b/27963013
                throw new NullPointerException(
                        "Attempted to call MSG_REQUEST_KEYBOARD_SHORTCUTS with null receiver:");
            }
            return super.sendMessageAtTime(msg, uptimeMillis);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INVALIDATE:
                    ((View) msg.obj).invalidate();
                    break;
                case MSG_INVALIDATE_RECT:
                    final View.AttachInfo.InvalidateInfo info = (View.AttachInfo.InvalidateInfo) msg.obj;
                    info.target.invalidate(info.left, info.top, info.right, info.bottom);
                    info.recycle();
                    break;
                case MSG_PROCESS_INPUT_EVENTS:
                    mProcessInputEventsScheduled = false;
                    doProcessInputEvents();
                    break;
                case MSG_DISPATCH_APP_VISIBILITY:
                    handleAppVisibility(msg.arg1 != 0);
                    break;
                case MSG_DISPATCH_GET_NEW_SURFACE:
                    handleGetNewSurface();
                    break;
                case MSG_RESIZED: {
                    // Recycled in the fall through...
                    SomeArgs args = (SomeArgs) msg.obj;
                    if (mWinFrame.equals(args.arg1)
                            && mPendingOverscanInsets.equals(args.arg5)
                            && mPendingContentInsets.equals(args.arg2)
                            && mPendingStableInsets.equals(args.arg6)
                            && mPendingVisibleInsets.equals(args.arg3)
                            && mPendingOutsets.equals(args.arg7)
                            && mPendingBackDropFrame.equals(args.arg8)
                            && args.arg4 == null
                            && args.argi1 == 0
                            && mDisplay.getDisplayId() == args.argi3) {
                        break;
                    }
                } // fall through...
                case MSG_RESIZED_REPORT:
                    if (mAdded) {
                        SomeArgs args = (SomeArgs) msg.obj;

                        final int displayId = args.argi3;
                        final MergedConfiguration mergedConfiguration = (MergedConfiguration) args.arg4;
                        final boolean displayChanged = mDisplay.getDisplayId() != displayId;

                        if (mergedConfiguration != null) {
                            // If configuration changed - notify about that and, maybe, about move to
                            // display.
                            performConfigurationChange(mergedConfiguration, false /* force */,
                                    displayChanged ? displayId : INVALID_DISPLAY /* same display */);
                        } else if (displayChanged) {
                            // Moved to display without config change - report last applied one.
                            onMovedToDisplay(displayId, mLastConfigurationFromResources);
                        }

                        final boolean framesChanged = !mWinFrame.equals(args.arg1)
                                || !mPendingOverscanInsets.equals(args.arg5)
                                || !mPendingContentInsets.equals(args.arg2)
                                || !mPendingStableInsets.equals(args.arg6)
                                || !mPendingVisibleInsets.equals(args.arg3)
                                || !mPendingOutsets.equals(args.arg7);

                        mWinFrame.set((Rect) args.arg1);
                        mPendingOverscanInsets.set((Rect) args.arg5);
                        mPendingContentInsets.set((Rect) args.arg2);
                        mPendingStableInsets.set((Rect) args.arg6);
                        mPendingVisibleInsets.set((Rect) args.arg3);
                        mPendingOutsets.set((Rect) args.arg7);
                        mPendingBackDropFrame.set((Rect) args.arg8);
                        mForceNextWindowRelayout = args.argi1 != 0;
                        mPendingAlwaysConsumeNavBar = args.argi2 != 0;

                        args.recycle();

                        if (msg.what == MSG_RESIZED_REPORT) {
                            reportNextDraw();
                        }

                        if (mView != null && framesChanged) {
                            forceLayout(mView);
                        }
                        requestLayout();
                    }
                    break;
                case MSG_WINDOW_MOVED:
                    if (mAdded) {
                        final int w = mWinFrame.width();
                        final int h = mWinFrame.height();
                        final int l = msg.arg1;
                        final int t = msg.arg2;
                        mWinFrame.left = l;
                        mWinFrame.right = l + w;
                        mWinFrame.top = t;
                        mWinFrame.bottom = t + h;

                        mPendingBackDropFrame.set(mWinFrame);
                        maybeHandleWindowMove(mWinFrame);
                    }
                    break;
                case MSG_WINDOW_FOCUS_CHANGED: {
                    if (mAdded) {
                        boolean hasWindowFocus = msg.arg1 != 0;
                        mAttachInfo.mHasWindowFocus = hasWindowFocus;

                        profileRendering(hasWindowFocus);

                        if (hasWindowFocus) {
                            boolean inTouchMode = msg.arg2 != 0;
                            ensureTouchModeLocally(inTouchMode);

                            if (mAttachInfo.mThreadedRenderer != null && mSurface.isValid()){
                                mFullRedrawNeeded = true;
                                try {
                                    final WindowManager.LayoutParams lp = mWindowAttributes;
                                    final Rect surfaceInsets = lp != null ? lp.surfaceInsets : null;
                                    mAttachInfo.mThreadedRenderer.initializeIfNeeded(
                                            mWidth, mHeight, mAttachInfo, mSurface, surfaceInsets);
                                } catch (Surface.OutOfResourcesException e) {
                                    try {
                                        if (!mWindowSession.outOfMemory(mWindow)) {
                                            Process.killProcess(Process.myPid());
                                        }
                                    } catch (RemoteException ex) {
                                    }
                                    // Retry in a bit.
                                    sendMessageDelayed(obtainMessage(msg.what, msg.arg1, msg.arg2), 500);
                                    return;
                                }
                            }
                        }

                        mLastWasImTarget = WindowManager.LayoutParams
                                .mayUseInputMethod(mWindowAttributes.flags);

                        InputMethodManager imm = InputMethodManager.peekInstance();
                        if (imm != null && mLastWasImTarget && !isInLocalFocusMode()) {
                            imm.onPreWindowFocus(mView, hasWindowFocus);
                        }
                        if (mView != null) {
                            mAttachInfo.mKeyDispatchState.reset();
                            mView.dispatchWindowFocusChanged(hasWindowFocus);
                            mAttachInfo.mTreeObserver.dispatchOnWindowFocusChange(hasWindowFocus);

                            if (mAttachInfo.mTooltipHost != null) {
                                mAttachInfo.mTooltipHost.hideTooltip();
                            }
                        }

                        // Note: must be done after the focus change callbacks,
                        // so all of the view state is set up correctly.
                        if (hasWindowFocus) {
                            if (imm != null && mLastWasImTarget && !isInLocalFocusMode()) {
                                imm.onPostWindowFocus(mView, mView.findFocus(),
                                        mWindowAttributes.softInputMode,
                                        !mHasHadWindowFocus, mWindowAttributes.flags);
                            }
                            // Clear the forward bit.  We can just do this directly, since
                            // the window manager doesn't care about it.
                            mWindowAttributes.softInputMode &=
                                    ~WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION;
                            ((WindowManager.LayoutParams)mView.getLayoutParams())
                                    .softInputMode &=
                                    ~WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION;
                            mHasHadWindowFocus = true;
                        } else {
                            if (mPointerCapture) {
                                handlePointerCaptureChanged(false);
                            }
                        }
                    }
                } break;
                case MSG_DIE:
                    doDie();
                    break;
                case MSG_DISPATCH_INPUT_EVENT: {
                    SomeArgs args = (SomeArgs)msg.obj;
                    InputEvent event = (InputEvent)args.arg1;
                    InputEventReceiver receiver = (InputEventReceiver)args.arg2;
                    enqueueInputEvent(event, receiver, 0, true);
                    args.recycle();
                } break;
                case MSG_SYNTHESIZE_INPUT_EVENT: {
                    InputEvent event = (InputEvent)msg.obj;
                    enqueueInputEvent(event, null, QueuedInputEvent.FLAG_UNHANDLED, true);
                } break;
                case MSG_DISPATCH_KEY_FROM_IME: {
                    KeyEvent event = (KeyEvent)msg.obj;
                    if ((event.getFlags()&KeyEvent.FLAG_FROM_SYSTEM) != 0) {
                        // The IME is trying to say this event is from the
                        // system!  Bad bad bad!
                        //noinspection UnusedAssignment
                        event = KeyEvent.changeFlags(event, event.getFlags() &
                                ~KeyEvent.FLAG_FROM_SYSTEM);
                    }
                    enqueueInputEvent(event, null, QueuedInputEvent.FLAG_DELIVER_POST_IME, true);
                } break;
                case MSG_CHECK_FOCUS: {
                    InputMethodManager imm = InputMethodManager.peekInstance();
                    if (imm != null) {
                        imm.checkFocus();
                    }
                } break;
                case MSG_CLOSE_SYSTEM_DIALOGS: {
                    if (mView != null) {
                        mView.onCloseSystemDialogs((String)msg.obj);
                    }
                } break;
                case MSG_DISPATCH_DRAG_EVENT:
                case MSG_DISPATCH_DRAG_LOCATION_EVENT: {
                    DragEvent event = (DragEvent)msg.obj;
                    event.mLocalState = mLocalDragState;    // only present when this app called startDrag()
                    handleDragEvent(event);
                } break;
                case MSG_DISPATCH_SYSTEM_UI_VISIBILITY: {
                    handleDispatchSystemUiVisibilityChanged((SystemUiVisibilityInfo) msg.obj);
                } break;
                case MSG_UPDATE_CONFIGURATION: {
                    Configuration config = (Configuration) msg.obj;
                    if (config.isOtherSeqNewer(
                            mLastReportedMergedConfiguration.getMergedConfiguration())) {
                        // If we already have a newer merged config applied - use its global part.
                        config = mLastReportedMergedConfiguration.getGlobalConfiguration();
                    }

                    // Use the newer global config and last reported override config.
                    mPendingMergedConfiguration.setConfiguration(config,
                            mLastReportedMergedConfiguration.getOverrideConfiguration());

                    performConfigurationChange(mPendingMergedConfiguration, false /* force */,
                            INVALID_DISPLAY /* same display */);
                } break;
                case MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST: {
                    setAccessibilityFocus(null, null);
                } break;
                case MSG_INVALIDATE_WORLD: {
                    if (mView != null) {
                        invalidateWorld(mView);
                    }
                } break;
                case MSG_DISPATCH_WINDOW_SHOWN: {
                    handleDispatchWindowShown();
                } break;
                case MSG_REQUEST_KEYBOARD_SHORTCUTS: {
                    final IResultReceiver receiver = (IResultReceiver) msg.obj;
                    final int deviceId = msg.arg1;
                    handleRequestKeyboardShortcuts(receiver, deviceId);
                } break;
                case MSG_UPDATE_POINTER_ICON: {
                    MotionEvent event = (MotionEvent) msg.obj;
                    resetPointerIcon(event);
                } break;
                case MSG_POINTER_CAPTURE_CHANGED: {
                    final boolean hasCapture = msg.arg1 != 0;
                    handlePointerCaptureChanged(hasCapture);
                } break;
                case MSG_DRAW_FINISHED: {
                    pendingDrawFinished();
                } break;
            }
        }
    }
}
