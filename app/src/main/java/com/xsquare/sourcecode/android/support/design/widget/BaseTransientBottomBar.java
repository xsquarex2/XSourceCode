package com.xsquare.sourcecode.android.support.design.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RestrictTo;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.xsquare.sourcecode.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by xsquare on 2018/4/27.
 */

public class BaseTransientBottomBar<B extends BaseTransientBottomBar<B>> {
    public abstract static class BaseCallback<B> {
        public static final int DISMISS_EVENT_SWIPE = 0;
        public static final int DISMISS_EVENT_ACTION = 1;
        public static final int DISMISS_EVENT_TIMEOUT = 2;
        public static final int DISMISS_EVENT_MANUAL = 3;
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;
        @RestrictTo(LIBRARY_GROUP)
        @IntDef({DISMISS_EVENT_SWIPE, DISMISS_EVENT_ACTION, DISMISS_EVENT_TIMEOUT,
                DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface DismissEvent {}
        public void onDismissed(B transientBottomBar, @DismissEvent int event) {
        }
        public void onShown(B transientBottomBar) {
        }
    }
    public interface ContentViewCallback {
        void animateContentIn(int delay, int duration);
        void animateContentOut(int delay, int duration);
    }
    @RestrictTo(LIBRARY_GROUP)
    @IntDef({LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG})
    @IntRange(from = 1)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {}
    public static final int LENGTH_INDEFINITE = -2;
    public static final int LENGTH_SHORT = -1;
    public static final int LENGTH_LONG = 0;
    static final int ANIMATION_DURATION = 250;
    static final int ANIMATION_FADE_DURATION = 180;
    static final Handler sHandler;
    static final int MSG_SHOW = 0;
    static final int MSG_DISMISS = 1;
    private static final boolean USE_OFFSET_API = (Build.VERSION.SDK_INT >= 16)
            && (Build.VERSION.SDK_INT <= 19);
    static {
        sHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_SHOW:
                        ((BaseTransientBottomBar) message.obj).showView();
                        return true;
                    case MSG_DISMISS:
                        ((BaseTransientBottomBar) message.obj).hideView(message.arg1);
                        return true;
                }
                return false;
            }
        });
    }
    private final ViewGroup mTargetParent;
    private final Context mContext;
    final SnackbarBaseLayout mView;
    private final ContentViewCallback mContentViewCallback;
    private int mDuration;
    private List<BaseCallback<B>> mCallbacks;
    private final AccessibilityManager mAccessibilityManager;
    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    interface OnLayoutChangeListener {
        void onLayoutChange(View view, int left, int top, int right, int bottom);
    }
    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View v);
        void onViewDetachedFromWindow(View v);
    }
    protected BaseTransientBottomBar(@NonNull ViewGroup parent, @NonNull View content,
                                     @NonNull ContentViewCallback contentViewCallback) {
        mTargetParent = parent;
        mContentViewCallback = contentViewCallback;
        mContext = parent.getContext();
        ThemeUtils.checkAppCompatTheme(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mView = (SnackbarBaseLayout) inflater.inflate(
                R.layout.design_layout_snackbar, mTargetParent, false);
        mView.addView(content);
        ViewCompat.setAccessibilityLiveRegion(mView,ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
        ViewCompat.setImportantForAccessibility(mView,ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
//        设置了fitSystemWindow
        ViewCompat.setFitsSystemWindows(mView, true);
        ViewCompat.setOnApplyWindowInsetsListener(mView,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                                v.getPaddingRight(), insets.getSystemWindowInsetBottom());
                        return insets;
                    }
                });
        mAccessibilityManager = (AccessibilityManager)
                mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    /**
     * 设置持续时间
     */
    @NonNull
    public B setDuration(@Duration int duration) {
        mDuration = duration;
        return (B) this;
    }

    /**
     * 返回持续时间
     */
    @Duration
    public int getDuration() {
        return mDuration;
    }
    /**
     * 获取 {@link BaseTransientBottomBar}'s context.
     */
    @NonNull
    public Context getContext() {
        return mContext;
    }
    /**
     * 获取mVview
     */
    @NonNull
    public View getView() {
        return mView;
    }
    /**
     * 显示
     */
    public void show() {
        SnackbarManager.getInstance().show(mDuration, mManagerCallback);
    }
    /**
     * 隐藏
     */
    public void dismiss() {
        dispatchDismiss(BaseCallback.DISMISS_EVENT_MANUAL);
    }
    void dispatchDismiss(@BaseCallback.DismissEvent int event) {
        SnackbarManager.getInstance().dismiss(mManagerCallback, event);
    }

    /**
     * 添加回调
     */
    @NonNull
    public B addCallback(@NonNull BaseCallback<B> callback) {
        if (callback == null) {
            return (B) this;
        }
        if (mCallbacks == null) {
            mCallbacks = new ArrayList<BaseCallback<B>>();
        }
        mCallbacks.add(callback);
        return (B) this;
    }
    /**
     * 移除回调
     */
    @NonNull
    public B removeCallback(@NonNull BaseCallback<B> callback) {
        if (callback == null) {
            return (B) this;
        }
        if (mCallbacks == null) {
            return (B) this;
        }
        mCallbacks.remove(callback);
        return (B) this;
    }
    /**
     * 是否显示
     */
    public boolean isShown() {
        return SnackbarManager.getInstance().isCurrent(mManagerCallback);
    }
    /**
     * 是否正在被显示或者已加入显示队列中
     */
    public boolean isShownOrQueued() {
        return SnackbarManager.getInstance().isCurrentOrNext(mManagerCallback);
    }

    final SnackbarManager.Callback mManagerCallback = new SnackbarManager.Callback() {
        @Override
        public void show() {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, BaseTransientBottomBar.this));
        }
        @Override
        public void dismiss(int event) {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0,
                    BaseTransientBottomBar.this));
        }
    };

    final void showView() {
        if (mView.getParent() == null) {
            final ViewGroup.LayoutParams lp = mView.getLayoutParams();
            if (lp instanceof CoordinatorLayout.LayoutParams) {
                //如果父view为coordinatorLayout,我们设置behavior
                final CoordinatorLayout.LayoutParams clp = (CoordinatorLayout.LayoutParams) lp;
                final Behavior behavior = new Behavior();
                behavior.setStartAlphaSwipeDistance(0.1f);
                behavior.setEndAlphaSwipeDistance(0.6f);
                behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
                behavior.setListener(new SwipeDismissBehavior.OnDismissListener() {
                    @Override
                    public void onDismiss(View view) {
                        view.setVisibility(View.GONE);
                        dispatchDismiss(BaseCallback.DISMISS_EVENT_SWIPE);
                    }
                    @Override
                    public void onDragStateChanged(int state) {
                        switch (state) {
                            case SwipeDismissBehavior.STATE_DRAGGING:
                            case SwipeDismissBehavior.STATE_SETTLING:
                                SnackbarManager.getInstance().pauseTimeout(mManagerCallback);
                                break;
                            case SwipeDismissBehavior.STATE_IDLE:
                                SnackbarManager.getInstance()
                                        .restoreTimeoutIfPaused(mManagerCallback);
                                break;
                        }
                    }
                });
                clp.setBehavior(behavior);
                clp.insetEdge = Gravity.BOTTOM;
            }
            mTargetParent.addView(mView);
        }
        mView.setOnAttachStateChangeListener(
                new OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {}
                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        if (isShownOrQueued()) {
                            sHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onViewHidden(BaseCallback.DISMISS_EVENT_MANUAL);
                                }
                            });
                        }
                    }
                });
        if (ViewCompat.isLaidOut(mView)) {
            if (shouldAnimate()) {
                animateViewIn();
            } else {
                onViewShown();
            }
        } else {
            mView.setOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    mView.setOnLayoutChangeListener(null);
                    if (shouldAnimate()) {
                        animateViewIn();
                    } else {
                        onViewShown();
                    }
                }
            });
        }
    }
    void animateViewIn() {
        if (Build.VERSION.SDK_INT >= 12) {
            final int viewHeight = mView.getHeight();
            if (USE_OFFSET_API) {
                ViewCompat.offsetTopAndBottom(mView, viewHeight);
            } else {
                mView.setTranslationY(viewHeight);
            }
            final ValueAnimator animator = new ValueAnimator();
            animator.setIntValues(viewHeight, 0);
            animator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            animator.setDuration(ANIMATION_DURATION);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mContentViewCallback.animateContentIn(
                            ANIMATION_DURATION - ANIMATION_FADE_DURATION,
                            ANIMATION_FADE_DURATION);
                }
                @Override
                public void onAnimationEnd(Animator animator) {
                    onViewShown();
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                private int mPreviousAnimatedIntValue = viewHeight;
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    int currentAnimatedIntValue = (int) animator.getAnimatedValue();
                    if (USE_OFFSET_API) {
                        ViewCompat.offsetTopAndBottom(mView,
                                currentAnimatedIntValue - mPreviousAnimatedIntValue);
                    } else {
                        mView.setTranslationY(currentAnimatedIntValue);
                    }
                    mPreviousAnimatedIntValue = currentAnimatedIntValue;
                }
            });
            animator.start();
        } else {
            final Animation anim = AnimationUtils.loadAnimation(mView.getContext(),
                    R.anim.design_snackbar_in);
            anim.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            anim.setDuration(ANIMATION_DURATION);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    onViewShown();
                }
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            mView.startAnimation(anim);
        }
    }

    /**
     * 移除动画
     */
    private void animateViewOut(final int event) {
        if (Build.VERSION.SDK_INT >= 12) {
            final ValueAnimator animator = new ValueAnimator();
            animator.setIntValues(0, mView.getHeight());
            animator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            animator.setDuration(ANIMATION_DURATION);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mContentViewCallback.animateContentOut(0, ANIMATION_FADE_DURATION);
                }
                @Override
                public void onAnimationEnd(Animator animator) {
                    onViewHidden(event);//
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                private int mPreviousAnimatedIntValue = 0;

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    int currentAnimatedIntValue = (int) animator.getAnimatedValue();
                    if (USE_OFFSET_API) {
                        ViewCompat.offsetTopAndBottom(mView,
                                currentAnimatedIntValue - mPreviousAnimatedIntValue);
                    } else {
                        mView.setTranslationY(currentAnimatedIntValue);
                    }
                    mPreviousAnimatedIntValue = currentAnimatedIntValue;
                }
            });
            animator.start();
        } else {
            final Animation anim = AnimationUtils.loadAnimation(mView.getContext(),
                    R.anim.design_snackbar_out);
            anim.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            anim.setDuration(ANIMATION_DURATION);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    onViewHidden(event);
                }

                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            mView.startAnimation(anim);
        }
    }

    final void hideView(@BaseCallback.DismissEvent final int event) {
        if (shouldAnimate() && mView.getVisibility() == View.VISIBLE) {
            animateViewOut(event);
        } else {
            //如果不能加载动画，或者view未显示，直接回调
            onViewHidden(event);
        }
    }

    void onViewShown() {
        //告诉snackbarManager view显示了
        SnackbarManager.getInstance().onShown(mManagerCallback);
        //处理回调
        if (mCallbacks != null) {
            int callbackCount = mCallbacks.size();
            for (int i = callbackCount - 1; i >= 0; i--) {
                mCallbacks.get(i).onShown((B) this);
            }
        }
    }

    void onViewHidden(int event) {
        //告诉snackbarManager view被移除了
        SnackbarManager.getInstance().onDismissed(mManagerCallback);
        //处理移除回调
        if (mCallbacks != null) {
            int callbackCount = mCallbacks.size();
            for (int i = callbackCount - 1; i >= 0; i--) {
                mCallbacks.get(i).onDismissed((B) this, event);
            }
        }
        if (Build.VERSION.SDK_INT < 11) {
            mView.setVisibility(View.GONE);
        }
        //移除view
        final ViewParent parent = mView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mView);
        }
    }
    //是否可加载动画
    boolean shouldAnimate() {
        return !mAccessibilityManager.isEnabled();
    }

    /**
     * SnackbarBaseLayout
     */
    @RestrictTo(LIBRARY_GROUP)
    static class SnackbarBaseLayout extends FrameLayout {
        private BaseTransientBottomBar.OnLayoutChangeListener mOnLayoutChangeListener;
        private BaseTransientBottomBar.OnAttachStateChangeListener mOnAttachStateChangeListener;
        SnackbarBaseLayout(Context context) {
            this(context, null);
        }
        SnackbarBaseLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
            if (a.hasValue(R.styleable.SnackbarLayout_elevation)) {
                ViewCompat.setElevation(this, a.getDimensionPixelSize(
                        R.styleable.SnackbarLayout_elevation, 0));
            }
            a.recycle();
            setClickable(true);
        }
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (mOnLayoutChangeListener != null) {
                mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }
        }
        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewAttachedToWindow(this);
            }
            ViewCompat.requestApplyInsets(this);
        }
        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewDetachedFromWindow(this);
            }
        }
        void setOnLayoutChangeListener(
                BaseTransientBottomBar.OnLayoutChangeListener onLayoutChangeListener) {
            mOnLayoutChangeListener = onLayoutChangeListener;
        }
        void setOnAttachStateChangeListener(
                BaseTransientBottomBar.OnAttachStateChangeListener listener) {
            mOnAttachStateChangeListener = listener;
        }
    }

    /**
     * Behavior
     */
    final class Behavior extends SwipeDismissBehavior<SnackbarBaseLayout> {
        @Override
        public boolean canSwipeDismissView(View child) {
            return child instanceof SnackbarBaseLayout;
        }
        @Override
        public boolean onInterceptTouchEvent(CoordinatorLayout parent, SnackbarBaseLayout child,
                                             MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    //如果用户当前正在触摸Snackbar，我们希望确保禁用任何Snackbar超时。我们在完成时恢复超时
                    if (parent.isPointInChildBounds(child, (int) event.getX(),
                            (int) event.getY())) {
                        SnackbarManager.getInstance().pauseTimeout(mManagerCallback);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    SnackbarManager.getInstance().restoreTimeoutIfPaused(mManagerCallback);
                    break;
            }
            return super.onInterceptTouchEvent(parent, child, event);
        }
    }
}
