package com.xsquare.sourcecode.android.view;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityEventSource;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import static java.lang.Math.max;

/**
 * @author xsquare
 * @date 2018/3/21
 */

public class View implements Drawable.Callback, KeyEvent.Callback,
        AccessibilityEventSource {

    /**
     * View分发事件
     * onTouch()先于onClick(),若onTouch()返回true即消费了事件，不执行onClick()
     * @param event 触摸事件
     * @return 当view处理了事件返回true
     */
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.isTargetAccessibilityFocus()) {
            if (!isAccessibilityFocusedViewOrHost()) {
                return false;
            }
            event.setTargetAccessibilityFocus(false);
        }
        boolean result = false;
        if (mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onTouchEvent(event, 0);
        }
        final int actionMasked = event.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            stopNestedScroll();
        }
        if (onFilterTouchEventForSecurity(event)) {
            if ((mViewFlags & ENABLED_MASK) == ENABLED && handleScrollBarDragging(event)) {
                result = true;
            }
            ListenerInfo li = mListenerInfo;
            //以下三个条件为真:
            //     1. mOnTouchListener != null
            //     2. (mViewFlags & ENABLED_MASK) == ENABLED （该条件是判断当前点击的控件是否enable）
            //     3. mOnTouchListener.onTouch(this, event)（即 回调控件注册Touch事件时的onTouch（））
            //     onTouch（）优先于onTouchEvent执行
            if (li != null && li.mOnTouchListener != null
                    && (mViewFlags & ENABLED_MASK) == ENABLED
                    && li.mOnTouchListener.onTouch(this, event)) {
                result = true;
            }
            if (!result && onTouchEvent(event)) {
                result = true;
            }
        }
        if (!result && mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onUnhandledEvent(event, 0);
        }
        if (actionMasked == MotionEvent.ACTION_UP ||
                actionMasked == MotionEvent.ACTION_CANCEL ||
                (actionMasked == MotionEvent.ACTION_DOWN && !result)) {
            stopNestedScroll();
        }
        return result;
    }

    /**
     * 当touch这个view的回调
     */
    public void setOnTouchListener(OnTouchListener l) {
        getListenerInfo().mOnTouchListener = l;
    }
    public interface OnTouchListener {
        boolean onTouch(android.view.View v, MotionEvent event);
    }


    public static class MeasureSpec {
        private static final int MODE_SHIFT = 30;
        private static final int MODE_MASK  = 0x3 << MODE_SHIFT;

        @IntDef({UNSPECIFIED, EXACTLY, AT_MOST})
        @Retention(RetentionPolicy.SOURCE)
        public @interface MeasureSpecMode {}

        public static final int UNSPECIFIED = 0 << MODE_SHIFT;

        public static final int EXACTLY     = 1 << MODE_SHIFT;

        public static final int AT_MOST     = 2 << MODE_SHIFT;

        public static int makeMeasureSpec(@IntRange(from = 0, to = (1 << android.view.View.MeasureSpec.MODE_SHIFT) - 1) int size,
                                          @MeasureSpecMode int mode) {
            if (sUseBrokenMakeMeasureSpec) {
                return size + mode;
            } else {
                return (size & ~MODE_MASK) | (mode & MODE_MASK);
            }
        }
        public static int makeSafeMeasureSpec(int size, int mode) {
            if (sUseZeroUnspecifiedMeasureSpec && mode == UNSPECIFIED) {
                return 0;
            }
            return makeMeasureSpec(size, mode);
        }
        @MeasureSpecMode
        public static int getMode(int measureSpec) {
            return (measureSpec & MODE_MASK);
        }
        public static int getSize(int measureSpec) {
            return (measureSpec & ~MODE_MASK);
        }
        static int adjust(int measureSpec, int delta) {
            final int mode = getMode(measureSpec);
            int size = getSize(measureSpec);
            if (mode == UNSPECIFIED) {
                return makeMeasureSpec(size, UNSPECIFIED);
            }
            size += delta;
            if (size < 0) {
                size = 0;
            }
            return makeMeasureSpec(size, mode);
        }
    }

    /**
     * 测量view
     * @param widthMeasureSpec  MeasureSpec
     * @param heightMeasureSpec MeasureSpec
     */
    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean optical = isLayoutModeOptical(this);
        if (optical != isLayoutModeOptical(mParent)) {
            Insets insets = getOpticalInsets();
            int oWidth  = insets.left + insets.right;
            int oHeight = insets.top  + insets.bottom;
            widthMeasureSpec  = android.view.View.MeasureSpec.adjust(widthMeasureSpec,  optical ? -oWidth  : oWidth);
            heightMeasureSpec = android.view.View.MeasureSpec.adjust(heightMeasureSpec, optical ? -oHeight : oHeight);
        }
        // Suppress sign extension for the low bytes
        long key = (long) widthMeasureSpec << 32 | (long) heightMeasureSpec & 0xffffffffL;
        if (mMeasureCache == null) mMeasureCache = new LongSparseLongArray(2);
        final boolean forceLayout = (mPrivateFlags & PFLAG_FORCE_LAYOUT) == PFLAG_FORCE_LAYOUT;
        // Optimize layout by avoiding an extra EXACTLY pass when the view is
        // already measured as the correct size. In API 23 and below, this
        // extra pass is required to make LinearLayout re-distribute weight.
        final boolean specChanged = widthMeasureSpec != mOldWidthMeasureSpec
                || heightMeasureSpec != mOldHeightMeasureSpec;
        final boolean isSpecExactly = android.view.View.MeasureSpec.getMode(widthMeasureSpec) == android.view.View.MeasureSpec.EXACTLY
                && android.view.View.MeasureSpec.getMode(heightMeasureSpec) == android.view.View.MeasureSpec.EXACTLY;
        final boolean matchesSpecSize = getMeasuredWidth() == android.view.View.MeasureSpec.getSize(widthMeasureSpec)
                && getMeasuredHeight() == android.view.View.MeasureSpec.getSize(heightMeasureSpec);
        final boolean needsLayout = specChanged
                && (sAlwaysRemeasureExactly || !isSpecExactly || !matchesSpecSize);
        if (forceLayout || needsLayout) {
            // first clears the measured dimension flag
            mPrivateFlags &= ~PFLAG_MEASURED_DIMENSION_SET;
            resolveRtlPropertiesIfNeeded();
            int cacheIndex = forceLayout ? -1 : mMeasureCache.indexOfKey(key);
            if (cacheIndex < 0 || sIgnoreMeasureCache) {
                // measure ourselves, this should set the measured dimension flag back
                onMeasure(widthMeasureSpec, heightMeasureSpec);
                mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
            } else {
                long value = mMeasureCache.valueAt(cacheIndex);
                // Casting a long to int drops the high 32 bits, no mask needed
                setMeasuredDimensionRaw((int) (value >> 32), (int) value);
                mPrivateFlags3 |= PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
            }
            // flag not set, setMeasuredDimension() was not invoked, we raise
            // an exception to warn the developer
            if ((mPrivateFlags & PFLAG_MEASURED_DIMENSION_SET) != PFLAG_MEASURED_DIMENSION_SET) {
                throw new IllegalStateException("View with id " + getId() + ": "
                        + getClass().getName() + "#onMeasure() did not set the"
                        + " measured dimension by calling"
                        + " setMeasuredDimension()");
            }
            mPrivateFlags |= PFLAG_LAYOUT_REQUIRED;
        }
        mOldWidthMeasureSpec = widthMeasureSpec;
        mOldHeightMeasureSpec = heightMeasureSpec;
        mMeasureCache.put(key, ((long) mMeasuredWidth) << 32 |
                (long) mMeasuredHeight & 0xffffffffL); // suppress sign extension
    }
    /**
     * 测量方法，由{@link #measure(int, int)}调用
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ////设置View宽高的测量值
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }
    protected final void setMeasuredDimension(int measuredWidth, int measuredHeight) {
        boolean optical = isLayoutModeOptical(this);
        if (optical != isLayoutModeOptical(mParent)) {
            Insets insets = getOpticalInsets();
            int opticalWidth  = insets.left + insets.right;
            int opticalHeight = insets.top  + insets.bottom;

            measuredWidth  += optical ? opticalWidth  : -opticalWidth;
            measuredHeight += optical ? opticalHeight : -opticalHeight;
        }
        setMeasuredDimensionRaw(measuredWidth, measuredHeight);
    }
    ////measureSpec指的是View测量后的大小
    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = android.view.View.MeasureSpec.getMode(measureSpec);
        int specSize = android.view.View.MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            //MeasureSpec.UNSPECIFIED一般用来系统的内部测量流程
            case android.view.View.MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            //我们主要关注着两种情况，它们返回的是View测量后的大小
            case android.view.View.MeasureSpec.AT_MOST:
            case android.view.View.MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }
    //如果View没有设置背景，那么返回android:minWidth这个属性的值，这个值可以为0
    //如果View设置了背景，那么返回android:minWidth和背景最小宽度两者中的最大值。
    protected int getSuggestedMinimumWidth() {
        return (mBackground == null) ? mMinWidth : max(mMinWidth, mBackground.getMinimumWidth());
    }
    protected int getSuggestedMinimumHeight() {
        return (mBackground == null) ? mMinHeight : max(mMinHeight, mBackground.getMinimumHeight());

    }


    /**
     * 确定自身的位置
     * @param l
     * @param t
     * @param r
     * @param b
     */
    public void layout(int l, int t, int r, int b) {
        //这里判断重新测量
        if ((mPrivateFlags3 & PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT) != 0) {
            onMeasure(mOldWidthMeasureSpec, mOldHeightMeasureSpec);
            mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
        }
        int oldL = mLeft;
        int oldT = mTop;
        int oldB = mBottom;
        int oldR = mRight;
        //调用setFrame()设置View四个顶点ed位置
        boolean changed = isLayoutModeOptical(mParent) ?
                setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);
        if (changed || (mPrivateFlags & PFLAG_LAYOUT_REQUIRED) == PFLAG_LAYOUT_REQUIRED) {
            //调用onLayout()确定View子元素的位置
            onLayout(changed, l, t, r, b);
            if (shouldDrawRoundScrollbar()) {
                if(mRoundScrollbarRenderer == null) {
                    mRoundScrollbarRenderer = new RoundScrollbarRenderer(this);
                }
            } else {
                mRoundScrollbarRenderer = null;
            }
            mPrivateFlags &= ~PFLAG_LAYOUT_REQUIRED;
            ListenerInfo li = mListenerInfo;
            if (li != null && li.mOnLayoutChangeListeners != null) {
                ArrayList<android.view.View.OnLayoutChangeListener> listenersCopy =
                        (ArrayList<android.view.View.OnLayoutChangeListener>)li.mOnLayoutChangeListeners.clone();
                int numListeners = listenersCopy.size();
                for (int i = 0; i < numListeners; ++i) {
                    listenersCopy.get(i).onLayoutChange(this, l, t, r, b, oldL, oldT, oldR, oldB);
                }
            }
        }
        mPrivateFlags &= ~PFLAG_FORCE_LAYOUT;
        mPrivateFlags3 |= PFLAG3_IS_LAID_OUT;
        if ((mPrivateFlags3 & PFLAG3_NOTIFY_AUTOFILL_ENTER_ON_LAYOUT) != 0) {
            mPrivateFlags3 &= ~PFLAG3_NOTIFY_AUTOFILL_ENTER_ON_LAYOUT;
            notifyEnterOrExitForAutoFillIfNeeded(true);
        }
    }

    /**
     * 有子View实现
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }
    /**
     * layout()中确定四个顶点的位置
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = false;
        if (mLeft != left || mRight != right || mTop != top || mBottom != bottom) {
            changed = true;
            // Remember our drawn bit
            int drawn = mPrivateFlags & PFLAG_DRAWN;
            int oldWidth = mRight - mLeft;
            int oldHeight = mBottom - mTop;
            int newWidth = right - left;
            int newHeight = bottom - top;
            boolean sizeChanged = (newWidth != oldWidth) || (newHeight != oldHeight);
            // Invalidate our old position
            invalidate(sizeChanged);
            mLeft = left;
            mTop = top;
            mRight = right;
            mBottom = bottom;
            mRenderNode.setLeftTopRightBottom(mLeft, mTop, mRight, mBottom);
            mPrivateFlags |= PFLAG_HAS_BOUNDS;
            if (sizeChanged) {
                sizeChange(newWidth, newHeight, oldWidth, oldHeight);
            }
            if ((mViewFlags & VISIBILITY_MASK) == VISIBLE || mGhostView != null) {
                // If we are visible, force the DRAWN bit to on so that
                // this invalidate will go through (at least to our parent).
                // This is because someone may have invalidated this view
                // before this call to setFrame came in, thereby clearing
                // the DRAWN bit.
                mPrivateFlags |= PFLAG_DRAWN;
                invalidate(sizeChanged);
                // parent display list may need to be recreated based on a change in the bounds
                // of any child
                invalidateParentCaches();
            }
            // Reset drawn bit to original value (invalidate turns it off)
            mPrivateFlags |= drawn;
            mBackgroundSizeChanged = true;
            mDefaultFocusHighlightSizeChanged = true;
            if (mForegroundInfo != null) {
                mForegroundInfo.mBoundsChanged = true;
            }
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
        return changed;
    }

    /**
     * 刷新View
     * @param invalidateCache
     */
    public void invalidate(boolean invalidateCache) {
        invalidateInternal(0, 0, mRight - mLeft, mBottom - mTop, invalidateCache, true);
    }
    void invalidateInternal(int l, int t, int r, int b, boolean invalidateCache,
                            boolean fullInvalidate) {
        if (mGhostView != null) {
            mGhostView.invalidate(true);
            return;
        }

        if (skipInvalidate()) {
            return;
        }

        if ((mPrivateFlags & (PFLAG_DRAWN | PFLAG_HAS_BOUNDS)) == (PFLAG_DRAWN | PFLAG_HAS_BOUNDS)
                || (invalidateCache && (mPrivateFlags & PFLAG_DRAWING_CACHE_VALID) == PFLAG_DRAWING_CACHE_VALID)
                || (mPrivateFlags & PFLAG_INVALIDATED) != PFLAG_INVALIDATED
                || (fullInvalidate && isOpaque() != mLastIsOpaque)) {
            if (fullInvalidate) {
                mLastIsOpaque = isOpaque();
                mPrivateFlags &= ~PFLAG_DRAWN;
            }

            mPrivateFlags |= PFLAG_DIRTY;

            if (invalidateCache) {
                mPrivateFlags |= PFLAG_INVALIDATED;
                mPrivateFlags &= ~PFLAG_DRAWING_CACHE_VALID;
            }

            // Propagate the damage rectangle to the parent view.
            final AttachInfo ai = mAttachInfo;
            final ViewParent p = mParent;
            if (p != null && ai != null && l < r && t < b) {
                final Rect damage = ai.mTmpInvalRect;
                damage.set(l, t, r, b);
                p.invalidateChild(this, damage);
            }

            // Damage the entire projection receiver, if necessary.
            if (mBackground != null && mBackground.isProjected()) {
                final android.view.View receiver = getProjectionReceiver();
                if (receiver != null) {
                    receiver.damageInParent();
                }
            }
        }
    }

    public void draw(Canvas canvas) {
        final int privateFlags = mPrivateFlags;
        //dirtyOpaque用来描述当前绘制，它有两种情况：1 检查DIRTY_OPAQUE为是否为1，如果是则说明当前视图某个子视图请求了一个不透明的UI绘制操作，此时当前
        //视图会被子视图覆盖 2 如果mAttachInfo.mIgnoreDirtyState = true则表示忽略该标志位
        final boolean dirtyOpaque = (privateFlags & PFLAG_DIRTY_MASK) == PFLAG_DIRTY_OPAQUE &&
                (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
        //将DIRTY_MASK与DRAWN置为1，表示开始绘制
        mPrivateFlags = (privateFlags & ~PFLAG_DIRTY_MASK) | PFLAG_DRAWN;
        /*
         * Draw traversal performs several drawing steps which must be executed
         * in the appropriate order:
         *
         *      1. Draw the background
         *      2. If necessary, save the canvas' layers to prepare for fading
         *      3. Draw view's content
         *      4. Draw children
         *      5. If necessary, draw the fading edges and restore layers
         *      6. Draw decorations (scrollbars for instance)
         */
        // Step 1, draw the background, if needed
        int saveCount;
        if (!dirtyOpaque) {
            //绘制当前视图的背景
            drawBackground(canvas);
        }
        //检查是否可以跳过第2步和第5步，也就是绘制变量，FADING_EDGE_HORIZONTAL == 1表示处于水平
        //滑动状态，则需要绘制水平边框渐变效果，FADING_EDGE_VERTICAL == 1表示处于垂直滑动状态，则
        //需要绘制垂直边框渐变效果。
        // skip step 2 & 5 if possible (common case)
        final int viewFlags = mViewFlags;
        boolean horizontalEdges = (viewFlags & FADING_EDGE_HORIZONTAL) != 0;
        boolean verticalEdges = (viewFlags & FADING_EDGE_VERTICAL) != 0;
        if (!verticalEdges && !horizontalEdges) {
            //窗口内容不透明才开始绘制，透明的时候就无需绘制了
            // Step 3, draw the content
            if (!dirtyOpaque) onDraw(canvas);
            // Step 4, draw the children
            dispatchDraw(canvas);
            drawAutofilledHighlight(canvas);
            // Overlay is part of the content and draws beneath Foreground
            if (mOverlay != null && !mOverlay.isEmpty()) {
                mOverlay.getOverlayView().dispatchDraw(canvas);
            }
            // Step 6, draw decorations (foreground, scrollbars)
            onDrawForeground(canvas);
            // Step 7, draw the default focus highlight
            drawDefaultFocusHighlight(canvas);
            if (debugDraw()) {
                debugDrawFocus(canvas);
            }
            // we're done...
            return;
        }
        //检查失修需要保存参数canvas所描述的一块画布的堆栈状态，并且创建额外的图层来绘制当前视图
        //在滑动时的边框渐变效果
        boolean drawTop = false;
        boolean drawBottom = false;
        boolean drawLeft = false;
        boolean drawRight = false;
        float topFadeStrength = 0.0f;
        float bottomFadeStrength = 0.0f;
        float leftFadeStrength = 0.0f;
        float rightFadeStrength = 0.0f;
        // Step 2, save the canvas' layers
        int paddingLeft = mPaddingLeft;
        final boolean offsetRequired = isPaddingOffsetRequired();
        if (offsetRequired) {
            paddingLeft += getLeftPaddingOffset();
        }
        //表示当前视图可以用来绘制的内容区域，这个区域已经将内置的和扩展的内边距排除之外
        int left = mScrollX + paddingLeft;
        int right = left + mRight - mLeft - mPaddingRight - paddingLeft;
        int top = mScrollY + getFadeTop(offsetRequired);
        int bottom = top + getFadeHeight(offsetRequired);
        if (offsetRequired) {
            right += getRightPaddingOffset();
            bottom += getBottomPaddingOffset();
        }
        final ScrollabilityCache scrollabilityCache = mScrollCache;
        final float fadeHeight = scrollabilityCache.fadingEdgeLength;
        int length = (int) fadeHeight;
        // clip the fade length if top and bottom fades overlap
        // overlapping fades produce odd-looking artifacts
        if (verticalEdges && (top + length > bottom - length)) {
            length = (bottom - top) / 2;
        }
        // also clip horizontal fades if necessary
        if (horizontalEdges && (left + length > right - length)) {
            length = (right - left) / 2;
        }
        if (verticalEdges) {
            topFadeStrength = Math.max(0.0f, Math.min(1.0f, getTopFadingEdgeStrength()));
            drawTop = topFadeStrength * fadeHeight > 1.0f;
            bottomFadeStrength = Math.max(0.0f, Math.min(1.0f, getBottomFadingEdgeStrength()));
            drawBottom = bottomFadeStrength * fadeHeight > 1.0f;
        }
        if (horizontalEdges) {
            leftFadeStrength = Math.max(0.0f, Math.min(1.0f, getLeftFadingEdgeStrength()));
            drawLeft = leftFadeStrength * fadeHeight > 1.0f;
            rightFadeStrength = Math.max(0.0f, Math.min(1.0f, getRightFadingEdgeStrength()));
            drawRight = rightFadeStrength * fadeHeight > 1.0f;
        }
        saveCount = canvas.getSaveCount();
        int solidColor = getSolidColor();
        if (solidColor == 0) {
            final int flags = Canvas.HAS_ALPHA_LAYER_SAVE_FLAG;
            if (drawTop) {
                canvas.saveLayer(left, top, right, top + length, null, flags);
            }
            if (drawBottom) {
                canvas.saveLayer(left, bottom - length, right, bottom, null, flags);
            }
            if (drawLeft) {
                canvas.saveLayer(left, top, left + length, bottom, null, flags);
            }
            if (drawRight) {
                canvas.saveLayer(right - length, top, right, bottom, null, flags);
            }
        } else {
            scrollabilityCache.setFadeColor(solidColor);
        }
        // Step 3, draw the content
        if (!dirtyOpaque) onDraw(canvas);
        // Step 4, draw the children
        dispatchDraw(canvas);
        //绘制当前视图的上下左右边框的渐变效果
        // Step 5, draw the fade effect and restore layers
        final Paint p = scrollabilityCache.paint;
        final Matrix matrix = scrollabilityCache.matrix;
        final Shader fade = scrollabilityCache.shader;
        if (drawTop) {
            matrix.setScale(1, fadeHeight * topFadeStrength);
            matrix.postTranslate(left, top);
            fade.setLocalMatrix(matrix);
            p.setShader(fade);
            canvas.drawRect(left, top, right, top + length, p);
        }
        if (drawBottom) {
            matrix.setScale(1, fadeHeight * bottomFadeStrength);
            matrix.postRotate(180);
            matrix.postTranslate(left, bottom);
            fade.setLocalMatrix(matrix);
            p.setShader(fade);
            canvas.drawRect(left, bottom - length, right, bottom, p);
        }
        if (drawLeft) {
            matrix.setScale(1, fadeHeight * leftFadeStrength);
            matrix.postRotate(-90);
            matrix.postTranslate(left, top);
            fade.setLocalMatrix(matrix);
            p.setShader(fade);
            canvas.drawRect(left, top, left + length, bottom, p);
        }
        if (drawRight) {
            matrix.setScale(1, fadeHeight * rightFadeStrength);
            matrix.postRotate(90);
            matrix.postTranslate(right, top);
            fade.setLocalMatrix(matrix);
            p.setShader(fade);
            canvas.drawRect(right - length, top, right, bottom, p);
        }
        canvas.restoreToCount(saveCount);
        drawAutofilledHighlight(canvas);
        // Overlay is part of the content and draws beneath Foreground
        if (mOverlay != null && !mOverlay.isEmpty()) {
            mOverlay.getOverlayView().dispatchDraw(canvas);
        }
        // Step 6, draw decorations (foreground, scrollbars)
        //绘制前景
        onDrawForeground(canvas);
        if (debugDraw()) {
            debugDrawFocus(canvas);
        }
    }


    @Override
    public void invalidateDrawable(@NonNull Drawable who) {

    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {

    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        return false;
    }

    @Override
    public void sendAccessibilityEvent(int eventType) {

    }

    @Override
    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {

    }
}
