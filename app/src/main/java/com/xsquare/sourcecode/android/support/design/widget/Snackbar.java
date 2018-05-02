package com.xsquare.sourcecode.android.support.design.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xsquare.sourcecode.R;

/**
 * Created by xsquare on 2018/4/25.
 */
public final class Snackbar {
    @NonNull
    public static Snackbar make(@NonNull View view, @NonNull CharSequence text,
                                @Duration int duration) {
        final ViewGroup parent = findSuitableParent(view);
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final SnackbarContentLayout content =
                (SnackbarContentLayout) inflater.inflate(
                        R.layout.design_layout_snackbar_include, parent, false);
        final Snackbar snackbar = new Snackbar(parent, content, content);
        snackbar.setText(text);
        snackbar.setDuration(duration);
        return snackbar;
    }
    @NonNull
    public static Snackbar make(@NonNull View view, @StringRes int resId, @Duration int duration) {
        return make(view, view.getResources().getText(resId), duration);
    }

    /**
     * 只会返回CoordinatorLayout或者android.R.id.content
     */
    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                return (ViewGroup) view;
            } else if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    return (ViewGroup) view;
                } else {
                    fallback = (ViewGroup) view;
                }
            }
            if (view != null) {
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);
        return fallback;
    }
    @NonNull
    public Snackbar setText(@NonNull CharSequence message) {
        final SnackbarContentLayout contentLayout = (SnackbarContentLayout) mView.getChildAt(0);
        final TextView tv = contentLayout.getMessageView();
        tv.setText(message);
        return this;
    }
    @NonNull
    public Snackbar setText(@StringRes int resId) {
        return setText(getContext().getText(resId));
    }
    @NonNull
    public Snackbar setAction(@StringRes int resId, View.OnClickListener listener) {
        return setAction(getContext().getText(resId), listener);
    }
    @NonNull
    public Snackbar setAction(CharSequence text, final View.OnClickListener listener) {
        final SnackbarContentLayout contentLayout = (SnackbarContentLayout) mView.getChildAt(0);
        final TextView tv = contentLayout.getActionView();

        if (TextUtils.isEmpty(text) || listener == null) {
            tv.setVisibility(View.GONE);
            tv.setOnClickListener(null);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view);
                    // Now dismiss the Snackbar
                    dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION);
                }
            });
        }
        return this;
    }
    @NonNull
    public Snackbar setActionTextColor(ColorStateList colors) {
        final SnackbarContentLayout contentLayout = (SnackbarContentLayout) mView.getChildAt(0);
        final TextView tv = contentLayout.getActionView();
        tv.setTextColor(colors);
        return this;
    }
    @NonNull
    public Snackbar setActionTextColor(@ColorInt int color) {
        final SnackbarContentLayout contentLayout = (SnackbarContentLayout) mView.getChildAt(0);
        final TextView tv = contentLayout.getActionView();
        tv.setTextColor(color);
        return this;
    }
    @Deprecated
    @NonNull
    public Snackbar setCallback(Callback callback) {
        if (mCallback != null) {
            removeCallback(mCallback);
        }
        if (callback != null) {
            addCallback(callback);
        }
        mCallback = callback;
        return this;
    }


    @RestrictTo(LIBRARY_GROUP)
    public static final class SnackbarLayout extends BaseTransientBottomBar.SnackbarBaseLayout {
        public SnackbarLayout(Context context) {
            super(context);
        }
        public SnackbarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int childCount = getChildCount();
            int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) {
                    child.measure(View.MeasureSpec.makeMeasureSpec(availableWidth, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(),
                                    View.MeasureSpec.EXACTLY));
                }
            }
        }
    }
}
