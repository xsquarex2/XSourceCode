package com.xsquare.sourcecode.android.view;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ViewTree观察者
 * Created by xsquare on 2018/3/27.
 */
public class ViewTreeObserver {
    private CopyOnWriteArrayList<android.view.ViewTreeObserver.OnWindowAttachListener> mOnWindowAttachListeners;

    /**
     * 通知已注册的监听器该窗口已被attached/detached。
     */
    final void dispatchOnWindowAttachedChange(boolean attached) {
        // 注意：由于使用CopyOnWriteArrayList，我们必须使用迭代器来执行调度。
        // 迭代器对侦听器是一种安全警戒，可以通过调用各种添加/删除方法来改变列表。
        // 这可以防止在我们迭代它时修改数组。
        final CopyOnWriteArrayList<android.view.ViewTreeObserver.OnWindowAttachListener> listeners
                = mOnWindowAttachListeners;
        if (listeners != null && listeners.size() > 0) {
            for (android.view.ViewTreeObserver.OnWindowAttachListener listener : listeners) {
                if (attached) listener.onWindowAttached();
                else listener.onWindowDetached();
            }
        }
    }
}
