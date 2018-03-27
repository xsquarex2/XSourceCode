package com.xsquare.sourcecode.android.view;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Objects;

/**
 * 系统服务类
 * 用于应用程序与窗口管理器通信
 * 要获取不同显示的 WindowManager，请使用Context＃createDisplayContext}为此获取Context
 * 然后使用 Context.getSystemService（Context.WINDOW_SERVICE）获取WindowManager。
 * 在另一个显示器上显示窗口可以创建一个Presentation。
 * Created by xsquare on 2018/3/25.
 */
//@SystemService(Context.WINDOW_SERVICE)
public interface WindowManager extends ViewManager{
    /**
     * 返回windowManager正管理的window的display信息
     * @return Display
     */
    public Display getDefaultDisplay();

    /**
     * {@link #removeView}的特殊变体
     * 在返回之前会立即调用View的视图层次结构中{@link View#onDetachedFromWindow()
     * View.onDetachedFromWindow()}
     * @param view 将被移除的view
     */
    public void removeViewImmediate(android.view.View view);

    /**
     尝试添加WindowManager.LayoutParams令牌无效的视图时抛出异常。
     */
    public static class BadTokenException extends RuntimeException {
        public BadTokenException() {
        }

        public BadTokenException(String name) {
            super(name);
        }
    }
    /**
     * 调用addView（View，ViewGroup.LayoutParams）到无法找到的辅助显示器时抛出的异常。
     * 有关辅助显示的详细信息，请参阅{@link android.app.Presentation}。
     */
    public static class InvalidDisplayException extends RuntimeException {
        public InvalidDisplayException() {
        }

        public InvalidDisplayException(String name) {
            super(name);
        }
    }

    /**
     * 用于从焦点窗口异步请求键盘快捷键。
     */
    public interface KeyboardShortcutsReceiver {
        /**
         当对焦窗口键盘快捷键准备好显示时使用回调。

         参数 result ：
         要显示的键盘快捷键。
         */
        void onKeyboardShortcutsReceived(List<KeyboardShortcutGroup> result);
    }
    public void requestAppKeyboardShortcuts(final KeyboardShortcutsReceiver receiver, int deviceId);



    public static class LayoutParams extends ViewGroup.LayoutParams implements Parcelable {
        /**
         * 此窗口的X位置。 使用默认gravity，它被忽略。使用Gravity＃LEFT或Gravity＃START或Gravity＃RIGHT或
         * Gravity＃END它提供了从给定边缘的偏移量。
         */
        @ViewDebug.ExportedProperty
        public int x;

        /**
         * 这个窗口的Y位置。 使用默认Gravity，它被忽略。
         * 使用Gravity＃TOP或Gravity＃BOTTOM时提供与给定边缘的偏移。
         */
        @ViewDebug.ExportedProperty
        public int y;
        /**
         * 指示将水平分配给与这些LayoutParams相关联的视图的多少空间。 如果视图不应该被拉伸，则指定0。
         * 否则，在重量大于0的所有视图中，额外的像素将被评估。
         */
        @ViewDebug.ExportedProperty
        public float horizontalWeight;

        /**
         * 指示垂直分配给与这些LayoutParams相关联的视图的额外空间量。 如果视图不应该被拉伸，则指定0。
         * 否则，在重量大于0的所有视图中，额外的像素将被评估。
         */
        @ViewDebug.ExportedProperty
        public float verticalWeight;

        /**
         * 窗口的一般类型。窗口类型有三大类：
         *
         * 应用程序窗口（从 FIRST_APPLICATION_WINDOW 至 LAST_APPLICATION_WINDOW）是正常的顶级应用程序视窗。
         * 对于这些类型的窗口，token必须是设置为他们是其中一部分的活动的标志
         * （这将会 如果token为空，通常可以为您完成）。
         *
         * 子窗口（从FIRST_SUB_WINDOW}至LAST_SUB_WINDOW}与另一个顶级别相关联窗口。
         * 对于这些类型的窗口，token必须是它附加到的窗口的标记。
         *
         * 系统窗口（从FIRST_SYSTEM_WINDOW至LAST_SYSTEM_WINDOW）是特殊类型的窗口由系统用于特定目的。
         * 他们不应该正常由应用程序使用，需要特殊许可使用它们。
         *
         * @see #TYPE_BASE_APPLICATION
         * @see #TYPE_APPLICATION
         * @see #TYPE_APPLICATION_STARTING
         * @see #TYPE_DRAWN_APPLICATION
         * @see #TYPE_APPLICATION_PANEL
         * @see #TYPE_APPLICATION_MEDIA
         * @see #TYPE_APPLICATION_SUB_PANEL
         * @see #TYPE_APPLICATION_ABOVE_SUB_PANEL
         * @see #TYPE_APPLICATION_ATTACHED_DIALOG
         * @see #TYPE_STATUS_BAR
         * @see #TYPE_SEARCH_BAR
         * @see #TYPE_PHONE
         * @see #TYPE_SYSTEM_ALERT
         * @see #TYPE_TOAST
         * @see #TYPE_SYSTEM_OVERLAY
         * @see #TYPE_PRIORITY_PHONE
         * @see #TYPE_STATUS_BAR_PANEL
         * @see #TYPE_SYSTEM_DIALOG
         * @see #TYPE_KEYGUARD_DIALOG
         * @see #TYPE_SYSTEM_ERROR
         * @see #TYPE_INPUT_METHOD
         * @see #TYPE_INPUT_METHOD_DIALOG
         */
        @ViewDebug.ExportedProperty(mapping = {
                @ViewDebug.IntToString(from = TYPE_BASE_APPLICATION,
                        to = "TYPE_BASE_APPLICATION"),
                @ViewDebug.IntToString(from = TYPE_APPLICATION,
                        to = "TYPE_APPLICATION"),
                @ViewDebug.IntToString(from = TYPE_APPLICATION_STARTING,
                        to = "TYPE_APPLICATION_STARTING"),
                @ViewDebug.IntToString(from = TYPE_DRAWN_APPLICATION,
                        to = "TYPE_DRAWN_APPLICATION"),
                @ViewDebug.IntToString(from = TYPE_APPLICATION_PANEL,
                        to = "TYPE_APPLICATION_PANEL"),
                @ViewDebug.IntToString(from = TYPE_APPLICATION_MEDIA,
                        to = "TYPE_APPLICATION_MEDIA"),
                @ViewDebug.IntToString(from = TYPE_APPLICATION_SUB_PANEL,
                        to = "TYPE_APPLICATION_SUB_PANEL"),
                @ViewDebug.IntToString(from = TYPE_APPLICATION_ABOVE_SUB_PANEL,
                        to = "TYPE_APPLICATION_ABOVE_SUB_PANEL"),
                @ViewDebug.IntToString(from = TYPE_APPLICATION_ATTACHED_DIALOG,
                        to = "TYPE_APPLICATION_ATTACHED_DIALOG"),
                @ViewDebug.IntToString(from = TYPE_APPLICATION_MEDIA_OVERLAY,
                        to = "TYPE_APPLICATION_MEDIA_OVERLAY"),
                @ViewDebug.IntToString(from = TYPE_STATUS_BAR,
                        to = "TYPE_STATUS_BAR"),
                @ViewDebug.IntToString(from = TYPE_SEARCH_BAR,
                        to = "TYPE_SEARCH_BAR"),
                @ViewDebug.IntToString(from = TYPE_PHONE,
                        to = "TYPE_PHONE"),
                @ViewDebug.IntToString(from = TYPE_SYSTEM_ALERT,
                        to = "TYPE_SYSTEM_ALERT"),
                @ViewDebug.IntToString(from = TYPE_TOAST,
                        to = "TYPE_TOAST"),
                @ViewDebug.IntToString(from = TYPE_SYSTEM_OVERLAY,
                        to = "TYPE_SYSTEM_OVERLAY"),
                @ViewDebug.IntToString(from = TYPE_PRIORITY_PHONE,
                        to = "TYPE_PRIORITY_PHONE"),
                @ViewDebug.IntToString(from = TYPE_SYSTEM_DIALOG,
                        to = "TYPE_SYSTEM_DIALOG"),
                @ViewDebug.IntToString(from = TYPE_KEYGUARD_DIALOG,
                        to = "TYPE_KEYGUARD_DIALOG"),
                @ViewDebug.IntToString(from = TYPE_SYSTEM_ERROR,
                        to = "TYPE_SYSTEM_ERROR"),
                @ViewDebug.IntToString(from = TYPE_INPUT_METHOD,
                        to = "TYPE_INPUT_METHOD"),
                @ViewDebug.IntToString(from = TYPE_INPUT_METHOD_DIALOG,
                        to = "TYPE_INPUT_METHOD_DIALOG"),
                @ViewDebug.IntToString(from = TYPE_WALLPAPER,
                        to = "TYPE_WALLPAPER"),
                @ViewDebug.IntToString(from = TYPE_STATUS_BAR_PANEL,
                        to = "TYPE_STATUS_BAR_PANEL"),
                @ViewDebug.IntToString(from = TYPE_SECURE_SYSTEM_OVERLAY,
                        to = "TYPE_SECURE_SYSTEM_OVERLAY"),
                @ViewDebug.IntToString(from = TYPE_DRAG,
                        to = "TYPE_DRAG"),
                @ViewDebug.IntToString(from = TYPE_STATUS_BAR_SUB_PANEL,
                        to = "TYPE_STATUS_BAR_SUB_PANEL"),
                @ViewDebug.IntToString(from = TYPE_POINTER,
                        to = "TYPE_POINTER"),
                @ViewDebug.IntToString(from = TYPE_NAVIGATION_BAR,
                        to = "TYPE_NAVIGATION_BAR"),
                @ViewDebug.IntToString(from = TYPE_VOLUME_OVERLAY,
                        to = "TYPE_VOLUME_OVERLAY"),
                @ViewDebug.IntToString(from = TYPE_BOOT_PROGRESS,
                        to = "TYPE_BOOT_PROGRESS"),
                @ViewDebug.IntToString(from = TYPE_INPUT_CONSUMER,
                        to = "TYPE_INPUT_CONSUMER"),
                @ViewDebug.IntToString(from = TYPE_DREAM,
                        to = "TYPE_DREAM"),
                @ViewDebug.IntToString(from = TYPE_NAVIGATION_BAR_PANEL,
                        to = "TYPE_NAVIGATION_BAR_PANEL"),
                @ViewDebug.IntToString(from = TYPE_DISPLAY_OVERLAY,
                        to = "TYPE_DISPLAY_OVERLAY"),
                @ViewDebug.IntToString(from = TYPE_MAGNIFICATION_OVERLAY,
                        to = "TYPE_MAGNIFICATION_OVERLAY"),
                @ViewDebug.IntToString(from = TYPE_PRESENTATION,
                        to = "TYPE_PRESENTATION"),
                @ViewDebug.IntToString(from = TYPE_PRIVATE_PRESENTATION,
                        to = "TYPE_PRIVATE_PRESENTATION"),
                @ViewDebug.IntToString(from = TYPE_VOICE_INTERACTION,
                        to = "TYPE_VOICE_INTERACTION"),
                @ViewDebug.IntToString(from = TYPE_VOICE_INTERACTION_STARTING,
                        to = "TYPE_VOICE_INTERACTION_STARTING"),
                @ViewDebug.IntToString(from = TYPE_DOCK_DIVIDER,
                        to = "TYPE_DOCK_DIVIDER"),
                @ViewDebug.IntToString(from = TYPE_QS_DIALOG,
                        to = "TYPE_QS_DIALOG"),
                @ViewDebug.IntToString(from = TYPE_SCREENSHOT,
                        to = "TYPE_SCREENSHOT"),
                @ViewDebug.IntToString(from = TYPE_APPLICATION_OVERLAY,
                        to = "TYPE_APPLICATION_OVERLAY")
        })
        public int type;

        /**
         * 开始表示正常应用程序窗口的窗口类型。
         */
        public static final int FIRST_APPLICATION_WINDOW = 1;

        /**
         * 窗口类型：作为整个应用程序的“基本”窗口的应用程序窗口;
         * 所有其他应用程序窗口都将显示在其上。 在多用户系统中，仅显示拥有用户的窗口。
         */
        public static final int TYPE_BASE_APPLICATION   = 1;

        /**
         * 窗口类型：正常应用程序窗口。 token必须是一个活动标记，用于标识该窗口属于哪个人。
         * 在多用户系统中，仅显示拥有用户的窗口。
         */
        public static final int TYPE_APPLICATION        = 2;

        /**
         * 窗口类型：应用程序启动时显示的特殊应用程序窗口。
         * 不适用于应用程序本身; 这被系统用来显示内容，直到应用程序显示自己的窗口。
         * 在多用户系统中显示所有用户的窗口。
         */
        public static final int TYPE_APPLICATION_STARTING = 3;

        /**
         * 窗口类型：TYPE APPLICATION的变体，确保窗口管理器等待在显示应用程序之前绘制该窗口。
         * 在多用户系统中，仅显示拥有用户的窗口。
         */
        public static final int TYPE_DRAWN_APPLICATION = 4;

        /**
         * 应用程序窗口类型的结束。
         */
        public static final int LAST_APPLICATION_WINDOW = 99;

        /**
         * 启动子窗口的类型。
         * 这些窗口的token必须设置为它们附加到的窗口。
         * 这些类型的窗口以Z顺序保持在其附加的窗口旁边，并且它们的坐标空间相对于它们附接的窗口。
         */
        public static final int FIRST_SUB_WINDOW = 1000;

        /**
         * 窗口类型：应用程序窗口顶部的面板。 这些窗口出现在其附近窗口的顶部。
         */
        public static final int TYPE_APPLICATION_PANEL = FIRST_SUB_WINDOW;

        /**
         * 窗口类型：显示媒体的窗口（如视频）。 这些窗口显示在其附近的窗口后面。
         */
        public static final int TYPE_APPLICATION_MEDIA = FIRST_SUB_WINDOW + 1;

        /**
         * 窗口类型：应用程序窗口顶部的子面板。 这些窗口显示在其附近的窗口和任何TYPE_APPLICATION_PANEL面板上。
         */
        public static final int TYPE_APPLICATION_SUB_PANEL = FIRST_SUB_WINDOW + 2;

        /**
         * 窗口类型：像TYPE_APPLICATION_PANEL，但窗口的布局与顶级窗口的布局不同，而不是其容器的小孩。
         */
        public static final int TYPE_APPLICATION_ATTACHED_DIALOG = FIRST_SUB_WINDOW + 3;

        /**
         * 窗口类型：在媒体窗口顶部显示覆盖的窗口。
         * 这些窗口显示在TYPE_APPLICATION_MEDIA和应用程序窗口之间。
         * 它们应该是半透明的，以便有用。 这是一个很丑的黑客：
         */
        public static final int TYPE_APPLICATION_MEDIA_OVERLAY  = FIRST_SUB_WINDOW + 4;

        /**
         * 窗口类型：应用程序窗口顶部的上一个子面板，它是子窗口窗口。
         * 这些窗口显示在所附窗口和任何TYPE_APPLICATION_SUB_PANEL面板的顶部。
         * @hide
         */
        public static final int TYPE_APPLICATION_ABOVE_SUB_PANEL = FIRST_SUB_WINDOW + 5;

        /**
         * 子窗口类型的结束。
         */
        public static final int LAST_SUB_WINDOW = 1999;

        /**
         * 启动系统特定的窗口类型。 这些通常不是由应用程序创建的。
         */
        public static final int FIRST_SYSTEM_WINDOW     = 2000;

        /**
         * 窗口类型：状态栏。 只能有一个状态栏窗口; 它被放置在屏幕的顶部，所有其他窗口都向下移动，所以它们在它的下方。
         * 在多用户系统中显示所有用户的窗口。
         */
        public static final int TYPE_STATUS_BAR         = FIRST_SYSTEM_WINDOW;

        /**
         * 窗口类型：搜索栏。 只能有一个搜索栏窗口; 它被放置在屏幕的顶部。 在多用户系统中显示所有用户的窗口。
         */
        public static final int TYPE_SEARCH_BAR         = FIRST_SYSTEM_WINDOW+1;

        /**
         * 窗口类型：手机。
         * 这些是提供用户与电话（特别是来电）的交互的非应用程序窗口。
         * 这些窗口通常位于所有应用程序之上，但位于状态栏的后面。在多用户系统中显示所有用户的窗口。
         */
        @Deprecated
        public static final int TYPE_PHONE              = FIRST_SYSTEM_WINDOW+2;

        /**
         * 窗口类型：系统窗口，如低功率警报。 这些窗口始终位于应用程序窗口之上。
         * 在多用户系统中只显示所有用户的窗口。
         * @deprecated for non-system apps. Use {@link #TYPE_APPLICATION_OVERLAY} instead.
         */
        @Deprecated
        public static final int TYPE_SYSTEM_ALERT       = FIRST_SYSTEM_WINDOW+3;

        /**
         * 窗口类型：键盘保护窗口。 在多用户系统中显示所有用户的窗口。
         * @removed
         */
        public static final int TYPE_KEYGUARD           = FIRST_SYSTEM_WINDOW+4;

        /**
         * 窗口类型：瞬态通知。 在多用户系统中，仅显示拥有用户的窗口。
         * @deprecated for non-system apps. Use {@link #TYPE_APPLICATION_OVERLAY} instead.
         */
        @Deprecated
        public static final int TYPE_TOAST              = FIRST_SYSTEM_WINDOW+5;

        /**
         * 窗口类型：系统覆盖窗口，需要显示在其他的顶部。
         * 这些窗口不能采取输入焦点，否则会干扰键盘保护。 在多用户系统中，仅显示拥有用户的窗口。
         * @deprecated for non-system apps. Use {@link #TYPE_APPLICATION_OVERLAY} instead.
         */
        @Deprecated
        public static final int TYPE_SYSTEM_OVERLAY     = FIRST_SYSTEM_WINDOW+6;

        /**
         * 窗口类型：优先手机UI，即使键盘保持活动，也需要显示。
         * 这些窗口不能采取输入焦点，否则会干扰键盘保护。 在多用户系统中显示所有用户的窗口。
         * @deprecated for non-system apps. Use {@link #TYPE_APPLICATION_OVERLAY} instead.
         */
        @Deprecated
        public static final int TYPE_PRIORITY_PHONE     = FIRST_SYSTEM_WINDOW+7;

        /**
         * 窗口类型：从状态栏滑出的面板在多用户系统中显示所有用户的窗口。
         */
        public static final int TYPE_SYSTEM_DIALOG      = FIRST_SYSTEM_WINDOW+8;

        /**
         * 窗口类型：键盘显示的对话框在多用户系统中显示所有用户的窗口。
         */
        public static final int TYPE_KEYGUARD_DIALOG    = FIRST_SYSTEM_WINDOW+9;

        /**
         * 窗口类型：内部系统错误窗口，出现在他们可以做的一切之上。
         * 在多用户系统中，仅显示拥有用户的窗口。
         * @deprecated for non-system apps. Use {@link #TYPE_APPLICATION_OVERLAY} instead.
         */
        @Deprecated
        public static final int TYPE_SYSTEM_ERROR       = FIRST_SYSTEM_WINDOW+10;

        /**
         * 窗口类型：内部输入法窗口，它们出现在普通UI之上。
         * 应用程序窗口可以调整大小或平移，以便在显示此窗口时保持输入焦点可见。
         * 在多用户系统中，仅显示拥有用户的窗口。
         */
        public static final int TYPE_INPUT_METHOD       = FIRST_SYSTEM_WINDOW+11;

        /**
         * 窗口类型：内部输入法对话框窗口，显示在当前输入法窗口上方。 在多用户系统中只显示拥有用户的窗口。
         */
        public static final int TYPE_INPUT_METHOD_DIALOG= FIRST_SYSTEM_WINDOW+12;

        /**
         * 窗口类型：壁纸窗口，放置在任何希望坐在壁纸顶部的窗口之后。 在多用户系统中，仅显示拥有用户的窗口。
         */
        public static final int TYPE_WALLPAPER          = FIRST_SYSTEM_WINDOW+13;

        /**
         * 窗口类型：面板，从在状态栏在多用户系统中滑出显示了所有用户的窗口。
         */
        public static final int TYPE_STATUS_BAR_PANEL   = FIRST_SYSTEM_WINDOW+14;

        /**
         * 窗口类型：安全系统覆盖窗口，需要显示在其他的顶部。 这些窗口不能采取输入焦点，否则会干扰键盘保护。
         * 这与TYPE_SYSTEM_OVERLAY完全相同，只有系统本身才允许创建这些叠加层。 应用程序无法获取创建安全系统覆盖的权限。
         * 在多用户系统中，仅显示拥有用户的窗口。
         * @hide
         */
        public static final int TYPE_SECURE_SYSTEM_OVERLAY = FIRST_SYSTEM_WINDOW+15;

        /**
         * 窗口类型：拖放伪窗口。 只有一个拖动层（最多），它被放置在所有其他窗口的顶部。
         * 在多用户系统中只显示拥有用户的窗口。
         * @hide
         */
        public static final int TYPE_DRAG               = FIRST_SYSTEM_WINDOW+16;

        /**
         * 窗口类型：从状态栏下方滑出的面板在多用户系统中显示所有用户的窗口。
         * @hide
         */
        public static final int TYPE_STATUS_BAR_SUB_PANEL = FIRST_SYSTEM_WINDOW+17;

        /**
         * 窗口类型：（鼠标）指针在多用户系统中显示所有用户的窗口。
         * @hide
         */
        public static final int TYPE_POINTER = FIRST_SYSTEM_WINDOW+18;

        /**
         * 窗口类型：导航栏（与状态栏不同）在多用户系统中，在所有用户的窗口中都会显示。
         * @hide
         */
        public static final int TYPE_NAVIGATION_BAR = FIRST_SYSTEM_WINDOW+19;

        /**
         * 窗口类型：当用户更改系统卷时显示的音量级别覆盖/对话框。 在多用户系统中显示所有用户的窗口。
         * @hide
         */
        public static final int TYPE_VOLUME_OVERLAY = FIRST_SYSTEM_WINDOW+20;

        /**
         * 窗口类型：引导进度对话框，位于世界各地。 在多用户系统中显示所有用户的窗口。
         * @hide
         */
        public static final int TYPE_BOOT_PROGRESS = FIRST_SYSTEM_WINDOW+21;

        /**
         * 窗口类型在系统UI栏被隐藏时消耗输入事件。 在多用户系统中显示所有用户的窗口。
         * @hide
         */
        public static final int TYPE_INPUT_CONSUMER = FIRST_SYSTEM_WINDOW+22;

        /**
         * 窗口类型：Dreams（屏幕保护程序）窗口，就在键盘上方。
         * 在多用户系统中，仅显示拥有用户的窗口。
         * @hide
         */
        public static final int TYPE_DREAM = FIRST_SYSTEM_WINDOW+23;

        /**
         * 窗口类型：导航栏面板（导航栏与状态栏不同）在多用户系统中，所有用户的窗口都将显示。
         * @hide
         */
        public static final int TYPE_NAVIGATION_BAR_PANEL = FIRST_SYSTEM_WINDOW+24;

        /**
         * 窗口类型：显示覆盖窗口。 用于模拟二次显示设备。
         * 在多用户系统中显示所有用户的窗口。
         * @hide
         */
        public static final int TYPE_DISPLAY_OVERLAY = FIRST_SYSTEM_WINDOW+26;

        /**
         * 窗口类型：放大倍数窗口。 用于突出显示器的放大部分，当启用可访问性缩放时。
         * 在多用户系统中显示所有用户的窗口。
         * @hide
         */
        public static final int TYPE_MAGNIFICATION_OVERLAY = FIRST_SYSTEM_WINDOW+27;

        /**
         * 窗口类型：用于在私有虚拟显示器上显示的窗口。
         */
        public static final int TYPE_PRIVATE_PRESENTATION = FIRST_SYSTEM_WINDOW+30;

        /**
         * 窗口类型：Windows中的语音交互层。
         * @hide
         */
        public static final int TYPE_VOICE_INTERACTION = FIRST_SYSTEM_WINDOW+31;

        /**
         * 窗口类型：Windows仅由连接的{@link android.accessibilityservice.AccessibilityService}覆盖，
         * 用于拦截用户交互，而无需更改辅助功能服务可以内省的窗口。
         * 特别地，可访问性服务可以仅仅检视目标用户可以与哪些窗口进行交互，这些窗口可以触摸这些窗口，或者可以键入这些窗口。
         * 例如，如果存在可触摸屏幕的全屏辅助功能重叠，则即使它们被可触摸窗口覆盖，其下方的窗口也将由辅助功能服务内省。
         */
        public static final int TYPE_ACCESSIBILITY_OVERLAY = FIRST_SYSTEM_WINDOW+32;

        /**
         * 窗口类型：语音交互层的启动窗口。
         * @hide
         */
        public static final int TYPE_VOICE_INTERACTION_STARTING = FIRST_SYSTEM_WINDOW+33;

        /**
         * 用于显示用于调整停靠堆栈大小的句柄的窗口。 该窗口由系统进程所有。
         * @hide
         */
        public static final int TYPE_DOCK_DIVIDER = FIRST_SYSTEM_WINDOW+34;

        /**
         * 窗口类型：像TYPE_APPLICATION_ATTACHED_DIALOG，但由快速设置瓷砖使用。
         * @hide
         */
        public static final int TYPE_QS_DIALOG = FIRST_SYSTEM_WINDOW+35;

        /**
         * 窗口类型：与TYPE_DREAM共享类似的特征。 该图层保留用于截图区域选择。 这些窗口不能采取输入焦点。
         * @hide
         */
        public static final int TYPE_SCREENSHOT = FIRST_SYSTEM_WINDOW + 36;

        /**
         * Window type: Window for Presentation on an external display.
         * @see android.app.Presentation
         * @hide
         */
        public static final int TYPE_PRESENTATION = FIRST_SYSTEM_WINDOW + 37;

        /**
         * Window type: Application overlay windows are displayed above all activity windows
         * (types between {@link #FIRST_APPLICATION_WINDOW} and {@link #LAST_APPLICATION_WINDOW})
         * but below critical system windows like the status bar or IME.
         * <p>
         * The system may change the position, size, or visibility of these windows at anytime
         * to reduce visual clutter to the user and also manage resources.
         * <p>
         * Requires {@link android.Manifest.permission#SYSTEM_ALERT_WINDOW} permission.
         * <p>
         * The system will adjust the importance of processes with this window type to reduce the
         * chance of the low-memory-killer killing them.
         * <p>
         * In multi-user systems shows only on the owning user's screen.
         */
        public static final int TYPE_APPLICATION_OVERLAY = FIRST_SYSTEM_WINDOW + 38;

        /**
         * 系统窗口类型结束。
         */
        public static final int LAST_SYSTEM_WINDOW      = 2999;

        /**
         * @hide
         * Used internally when there is no suitable type available.
         */
        public static final int INVALID_WINDOW_TYPE = -1;

        /**
         * Return true if the window type is an alert window.
         *
         * @param type The window type.
         * @return If the window type is an alert window.
         * @hide
         */
        public static boolean isSystemAlertWindowType(int type) {
            switch (type) {
                case TYPE_PHONE:
                case TYPE_PRIORITY_PHONE:
                case TYPE_SYSTEM_ALERT:
                case TYPE_SYSTEM_ERROR:
                case TYPE_SYSTEM_OVERLAY:
                case TYPE_APPLICATION_OVERLAY:
                    return true;
            }
            return false;
        }

        /** @deprecated this is ignored, this value is set automatically when needed. */
        @Deprecated
        public static final int MEMORY_TYPE_NORMAL = 0;
        /** @deprecated this is ignored, this value is set automatically when needed. */
        @Deprecated
        public static final int MEMORY_TYPE_HARDWARE = 1;
        /** @deprecated this is ignored, this value is set automatically when needed. */
        @Deprecated
        public static final int MEMORY_TYPE_GPU = 2;
        /** @deprecated this is ignored, this value is set automatically when needed. */
        @Deprecated
        public static final int MEMORY_TYPE_PUSH_BUFFERS = 3;

        /**
         * @deprecated this is ignored
         */
        @Deprecated
        public int memoryType;

        /**
         * 窗口标志：只要此窗口对用户可见，允许在屏幕打开时激活锁定屏幕。
         * 这可以独立使用，也可以与FLAG_KEEP_SCREEN_ON和/或FLAG_SHOW_WHEN_LOCKED结合使用
         *  {@link #FLAG_KEEP_SCREEN_ON} and/or {@link #FLAG_SHOW_WHEN_LOCKED} */
        public static final int FLAG_ALLOW_LOCK_WHILE_SCREEN_ON     = 0x00000001;

        /**
         * 窗口标志：这个窗口后面的一切都会变暗。使用 #dimAmount来控制昏暗的数量。
         * */
        public static final int FLAG_DIM_BEHIND        = 0x00000002;

        /**
         * 窗口标志：模糊此窗口后面的所有内容。
         * @已弃用 不再支持模糊。
         */
        @Deprecated
        public static final int FLAG_BLUR_BEHIND        = 0x00000004;

        /**
         * 窗口标志：此窗口不会获得关键输入焦点，因此用户无法向其发送键或其他按钮事件。
         * 那些会改变它背后的任何可关注的窗口。
         * 此标志还将启用#FLAG_NOT_TOUCH_MODAL是否显式设置。
         * 设置此标志也意味着该窗口不需要与软输入法进行交互，
         * 因此它将被Z-排序并且与任何活动输入法无关（通常这意味着它在输入法之上得到Z-排序，所以它可以使用全屏的内容，如果需要的话可以覆盖输入法。
         * 可以使用{@link #FLAG_ALT_FOCUSABLE_IM}来修改这个行为。
         * can use {@link #FLAG_ALT_FOCUSABLE_IM} to modify this behavior. */
        public static final int FLAG_NOT_FOCUSABLE      = 0x00000008;

        /** 窗口标志：此窗口永远不会接收触摸事件。 */
        public static final int FLAG_NOT_TOUCHABLE      = 0x00000010;

        /**
         * 窗口标志：即使该窗口是可对焦的（其#FLAG_NOT_FOCUSABLE未设置），
         * 允许窗口外的任何指针事件发送到其后面的窗口。
         * 否则它将消耗所有指针事件本身，而不管它们是否在窗口内。
         */
        public static final int FLAG_NOT_TOUCH_MODAL    = 0x00000020;

        /**
         * 窗口标志：设置时，如果设备在按下触摸屏时处于睡眠状态，则会收到此第一次触摸事件。
         * 通常，第一触摸事件由系统消耗，因为用户看不到它们正在按下什么。
         * @deprecated This flag has no effect.
         */
        @Deprecated
        public static final int FLAG_TOUCHABLE_WHEN_WAKING = 0x00000040;

        /**
        * 窗口标志：只要该窗口对用户可见，请保持设备的屏幕亮起。
        * */
        public static final int FLAG_KEEP_SCREEN_ON     = 0x00000080;

        /**
         * 窗口标志：将窗口放在整个屏幕内，忽略边框周围的装饰（如状态栏）。
         * 窗口必须正确定位其内容以考虑屏幕装饰。通常按照Window＃setFlags中的描述，为Window设置此标志。
         */
        public static final int FLAG_LAYOUT_IN_SCREEN   = 0x00000100;

        /** 窗口标志：允许窗口延伸到屏幕外部。 */
        public static final int FLAG_LAYOUT_NO_LIMITS   = 0x00000200;

        /**
         * 窗口标志：在显示此窗口时隐藏所有屏幕装饰（如状态栏）。
         * 这允许窗口自己使用整个显示空间 - 当顶层设有这个标志的应用程序窗口时，状态栏将被隐藏。
         * 全屏窗口将忽略窗口的#softInputMode字段的#SOFT_INPUT_ADJUST_RESIZE值; 窗口将保持全屏，不会调整大小。
         * 该标志可以通过android.R.attr＃windowFullscreen属性控制在主题中。
         * 此属性将在标准全屏主题中为您自动设置
         * <p>This flag can be controlled in your theme through the
         * {@link android.R.attr#windowFullscreen} attribute; this attribute
         * is automatically set for you in the standard fullscreen themes
         * such as {@link android.R.style#Theme_NoTitleBar_Fullscreen},
         * {@link android.R.style#Theme_Black_NoTitleBar_Fullscreen},
         * {@link android.R.style#Theme_Light_NoTitleBar_Fullscreen},
         * {@link android.R.style#Theme_Holo_NoActionBar_Fullscreen},
         * {@link android.R.style#Theme_Holo_Light_NoActionBar_Fullscreen},
         * {@link android.R.style#Theme_DeviceDefault_NoActionBar_Fullscreen}, and
         * {@link android.R.style#Theme_DeviceDefault_Light_NoActionBar_Fullscreen}.</p>
         */
        public static final int FLAG_FULLSCREEN      = 0x00000400;

        /** 窗口标志：覆盖 #FLAG_FULLSCREEN 并强制显示屏幕装饰（如状态栏）。 */
        public static final int FLAG_FORCE_NOT_FULLSCREEN   = 0x00000800;

        /**
         * 窗口标志：将此窗口合成屏幕时，打开抖动。
         * @deprecated This flag is no longer used. */
        @Deprecated
        public static final int FLAG_DITHER             = 0x00001000;

        /**
         * 窗口标志：将窗口的内容视为安全的，防止窗口显示在屏幕截图中或在非安全显示器上查看。
         * 有关安全表面和安全显示的更多详细信息，请参阅android.view.Display＃FLAG_SECURE。
         * <p>See {@link android.view.Display#FLAG_SECURE} for more details about
         * secure surfaces and secure displays.
         */
        public static final int FLAG_SECURE             = 0x00002000;

        /** 窗口标志：一种特殊模式，其中布局参数用于在合成到屏幕时执行曲面的缩放。*/
        public static final int FLAG_SCALED             = 0x00004000;

        /**
         * 窗口标志：用于当用户将屏幕对准脸部时经常使用的窗口，它会积极地过滤事件流，
         * 以防止在这种情况下的意外按压，这种情况可能不是特定窗口的期望，当这样的事件检测到流，
         * 应用程序将接收到一个CANCEL运动事件来指示这一点，
         * 所以应用程序可以通过对事件执行任何操作直到手指被释放来处理。 */
        public static final int FLAG_IGNORE_CHEEK_PRESSES    = 0x00008000;

        /**
         * 窗口标志：仅与 #FLAG_LAYOUT_IN_SCREEN结合使用的特殊选项。
         * 当在屏幕中请求布局时，您的窗口可能会出现在屏幕装饰之上或之后，例如状态栏。
         * 还包括这个标志，窗口管理器将报告所需的插入矩形，以确保您的内容不被屏幕装饰覆盖。
         * 通常按照 Window＃setFlags中的描述，为Window设置此标志。
         * This flag is normally set for you by Window as described in {@link android.view.Window#setFlags}.*/
        public static final int FLAG_LAYOUT_INSET_DECOR = 0x00010000;

        /**
         * 窗口标志：相对于当前窗口与当前方法的交互方式，反转#FLAG_NOT_FOCUSABLE的状态。
         * 也就是说，如果FLAG_NOT_FOCUSABLE被设置并且该标志被设置，
         * 则该窗口将表现为如果它需要与输入方法交互并因此被放置在/远离它;
         * 如果未设置FLAG_NOT_FOCUSABLE，并且该标志被设置，则该窗口将表现为不需要与输入方法交互，
         * 并且可以被放置以使用更多空间并覆盖输入方法。
         */
        public static final int FLAG_ALT_FOCUSABLE_IM = 0x00020000;

        /**
         * 窗口标志：如果您设置了 #FLAG_NOT_TOUCH_MODAL，则可以将此标志设置为接收一个特殊的MotionEvent，
         * 并在窗口之外发生的触发动作为MotionEvent＃ACTION_OUTSIDE MotionEvent.ACTION_OUTSIDE。
         * 请注意，您不会收到完整的向下/向上/向上手势，仅将第一个位置作为ACTION_OUTSIDE。
         */
        public static final int FLAG_WATCH_OUTSIDE_TOUCH = 0x00040000;

        /**
         * 窗口标志：屏幕锁定时显示窗口的特殊标志。这将使应用程序窗口优先于关键防护装置或任何其他锁定屏幕。
         * 可以使用#FLAG_KEEP_SCREEN_ON打开屏幕，直接显示窗口，然后再显示屏幕保护窗口。
         * 可以与#FLAG_DISMISS_KEYGUARD一起使用，以自动完全关闭非安全的密钥保护。
         * 此标志仅适用于最高级的全屏窗口。
         */
        public static final int FLAG_SHOW_WHEN_LOCKED = 0x00080000;

        /**
         * 窗口标志：请求您的窗口后面显示系统壁纸。
         * 窗户表面必须是半透明的，以便能够实际看到它后面的壁纸;这个标志只是确保如果这个窗口实际上具有半透明区域，墙纸表面将会在那里。
         * 该标志可以通过android.R.attr＃windowShowWallpaper属性控制在主题中。
         * 此属性将在标准壁纸主题中为您自动设置
         * <p>This flag can be controlled in your theme through the
         * {@link android.R.attr#windowShowWallpaper} attribute; this attribute
         * is automatically set for you in the standard wallpaper themes
         * such as {@link android.R.style#Theme_Wallpaper},
         * {@link android.R.style#Theme_Wallpaper_NoTitleBar},
         * {@link android.R.style#Theme_Wallpaper_NoTitleBar_Fullscreen},
         * {@link android.R.style#Theme_Holo_Wallpaper},
         * {@link android.R.style#Theme_Holo_Wallpaper_NoTitleBar},
         * {@link android.R.style#Theme_DeviceDefault_Wallpaper}, and
         * {@link android.R.style#Theme_DeviceDefault_Wallpaper_NoTitleBar}.</p>
         */
        public static final int FLAG_SHOW_WALLPAPER = 0x00100000;

        /**
         * 窗口标志：当设置为窗口被添加或显示时，一旦显示窗口，
         * 系统将会戳出电源管理器的用户活动（就好像用户已唤醒设备）打开屏幕。 */
        public static final int FLAG_TURN_SCREEN_ON = 0x00200000;

        /**
         * 窗口标志：当设置窗口将导致键盘保护被关闭，只有当它不是安全的锁定键盘保护。
         * 因为安全性不需要这样一个键盘保护，所以如果用户导航到另一个窗口（与 #FLAG_SHOW_WHEN_LOCKED}相反，
         * 这只会暂时隐藏安全和非安全的键盘保护程序，但确保重新出现） 当用户移动到不隐藏它们的另一个UI时）。
         * 如果keyguard当前处于活动状态并且是安全的（需要一个解锁模式），
         * 除非用户仍然需要在看到此窗口之前进行确认，否则 #FLAG_SHOW_WHEN_LOCKED也被设置。
         * @deprecated Use {@link #FLAG_SHOW_WHEN_LOCKED} or {@link KeyguardManager#dismissKeyguard}
         * instead. Since keyguard was dismissed all the time as long as an activity with this flag
         * on its window was focused, keyguard couldn't guard against unintentional touches on the
         * screen, which isn't desired.
         */
        @Deprecated
        public static final int FLAG_DISMISS_KEYGUARD = 0x00400000;

        /**
         * 窗口标志：当设置窗口将接受触摸事件超出其界限，以发送到也支持拆分触摸的其他窗口。
         * 当这个标志没有被设置时，第一个指向下一个的指针决定所有后续触发的窗口，直到所有指针向上。
         * 当设置此标志时，每个指针（不一定是第一个）将决定该指针的所有后续触摸将到达的窗口，
         * 直到该指针向上移动，从而允许多个指针的触摸跨多个窗口分割。
         */
        public static final int FLAG_SPLIT_TOUCH = 0x00800000;

        /**
         * 指示此窗口是否应该硬件加速。 要求硬件加速不能保证会发生。
         * 该标志只能通过程序控制才能启用硬件加速。要以编程方式为给定窗口启用硬件加速，请执行以下操作：
         * Window w = activity.getWindow(); // in Activity's onCreate() for instance
         * w.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
         * WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
         * 重要的是要记住，在设置活动或对话框的内容视图之前，必须设置此标志。
         * 使用 android.R.attr＃hardwareAccelerated在清单中启用硬件加速功能后，无法使用此标志。
         * 如果您需要选择性地并以编程方式禁用硬件加速（例如用于自动测试），请确保在清单中已关闭，
         * 并在使用上述方法时，在活动或对话框上启用它。
         * 如果活动或应用程序上的android.R.attr＃hardwareAccelerated android：hardwareAccelerated XML属性设置为true，
         * 系统会自动设置此标志。
         */
        public static final int FLAG_HARDWARE_ACCELERATED = 0x01000000;

        /**
         * 窗口标志：允许窗口内容扩展到屏幕过扫描区域，如果有的话。
         * 窗口应该仍然正确定位其内容以考虑过扫描区域。
         * 该标志可以通过{@link android.R.attr＃windowOverscan}属性控制在您的主题中;
         * 此属性将在标准过扫描主题中为您自动设置
         * {@link android.R.style#Theme_Holo_NoActionBar_Overscan},
         * {@link android.R.style#Theme_Holo_Light_NoActionBar_Overscan},
         * {@link android.R.style#Theme_DeviceDefault_NoActionBar_Overscan}, and
         * {@link android.R.style#Theme_DeviceDefault_Light_NoActionBar_Overscan}.</p>
         * 当该窗口被启用时，其正常内容可能在某种程度上被显示器的过扫描区域遮蔽。
         * 为了确保该内容的关键部分对用户可见，
         * 您可以使用 View＃setFitsSystemWindows（boolean）View.setFitsSystemWindows（boolean）
         * 来设置应用应用偏移量的视图层次结构中的点。
         * （这可以通过直接调用此函数，使用视图层次结构中的 android.R.attr＃fitsSystemWindows属性来实现，
         * 或者实现您自己的 View＃fitSystemWindows（android.graphics.Rect）View.fitSystemWindows（Rect）方法）。
         * 定位内容元素的这种机制与其与布局和
         * View＃setSystemUiVisibility（int）View.setSystemUiVisibility（int）的等效使用相同。
         * 这里是一个示例布局，将正确定位其UI元素，并设置此过扫描标志：
         * 例子 development/samples/ApiDemos/res/layout/overscan_activity.xml complete
         */
        public static final int FLAG_LAYOUT_IN_OVERSCAN = 0x02000000;

        /**
         * 窗口标志：请求半透明状态栏，并提供最少的系统提供的背景保护。
         * 这个标志可以通过{@link android.R.attr＃windowTranslucentStatus}属性控制在主题中。
         * 此属性在标准的半透明装饰主题中为您自动设置
         * {@link android.R.style#Theme_Holo_NoActionBar_TranslucentDecor},
         * {@link android.R.style#Theme_Holo_Light_NoActionBar_TranslucentDecor},
         * {@link android.R.style#Theme_DeviceDefault_NoActionBar_TranslucentDecor}, and
         * {@link android.R.style#Theme_DeviceDefault_Light_NoActionBar_TranslucentDecor}.</p>
         * 当窗口启用此标志时，它将自动设置系统UI可见性标志
         * {@link View＃SYSTEM_UI_FLAG_LAYOUT_STABLE}和{@link View＃SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN}。
         */
        public static final int FLAG_TRANSLUCENT_STATUS = 0x04000000;

        /**
         * 窗口标志：请求半透明的导航栏，系统提供最少的背景保护。
         * 该标志可以通过android.R.attr＃windowTranslucentNavigation属性控制在您的主题中;
         * 此属性在标准的半透明装饰主题中为您自动设置
         * {@link android.R.style#Theme_Holo_NoActionBar_TranslucentDecor},
         * {@link android.R.style#Theme_Holo_Light_NoActionBar_TranslucentDecor},
         * {@link android.R.style#Theme_DeviceDefault_NoActionBar_TranslucentDecor}, and
         * {@link android.R.style#Theme_DeviceDefault_Light_NoActionBar_TranslucentDecor}.</p>
         * 当窗口启用此标志时，它将自动设置系统UI可见性标记
         * {@link View＃SYSTEM_UI_FLAG_LAYOUT_STABLE}和{@link View＃SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION}。
         */
        public static final int FLAG_TRANSLUCENT_NAVIGATION = 0x08000000;

        /**
         * 在本地对焦模式下的窗口的标志。
         * 局部对焦模式下的窗口可以使用{@link Window＃setLocalFocus（boolean，boolean）}来控制与窗口管理器无关的焦点。
         * 通常在此模式下窗口不会从窗口管理器获取触摸/键事件，
         * 但只能通过使用{@link Window＃injectInputEvent（InputEvent）}的本地注入获取事件。
         */
        public static final int FLAG_LOCAL_FOCUS_MODE = 0x10000000;

        /**
         * 窗口标志：启用触摸将窗口滑出到中间手势中的相邻窗口中，而不是在手势持续时间内捕获。
         * 此标志仅改变此窗口的触摸焦点的行为。
         * 触摸可以从窗口滑出，但不一定会滑回（除非具有触摸焦点的其他窗口允许）。
         * {@hide}
         */
        public static final int FLAG_SLIPPERY = 0x20000000;

        /**
         * 窗口标志：当使用附加的窗口请求布局时，附加的窗口可能与父窗口的屏幕装饰重叠，例如导航栏。
         * 通过包括这个标志，窗口管理器将在父窗口的装饰框架内将附加的窗口布局，使得它不与屏幕装饰重叠。
         */
        public static final int FLAG_LAYOUT_ATTACHED_IN_DECOR = 0x40000000;

        /**
         * 表示该窗口负责绘制系统栏的背景的标志。
         * 如果设置，系统栏将以透明背景绘制，并且此窗口中的相应区域将填充指定的颜色
         * Window＃getStatusBarColor（）和 Window＃getNavigationBarColor（）。
         */
        public static final int FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS = 0x80000000;

        /**
         * 各种行为选项/标志。 默认为无。
         *
         * @see #FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
         * @see #FLAG_DIM_BEHIND
         * @see #FLAG_NOT_FOCUSABLE
         * @see #FLAG_NOT_TOUCHABLE
         * @see #FLAG_NOT_TOUCH_MODAL
         * @see #FLAG_TOUCHABLE_WHEN_WAKING
         * @see #FLAG_KEEP_SCREEN_ON
         * @see #FLAG_LAYOUT_IN_SCREEN
         * @see #FLAG_LAYOUT_NO_LIMITS
         * @see #FLAG_FULLSCREEN
         * @see #FLAG_FORCE_NOT_FULLSCREEN
         * @see #FLAG_SECURE
         * @see #FLAG_SCALED
         * @see #FLAG_IGNORE_CHEEK_PRESSES
         * @see #FLAG_LAYOUT_INSET_DECOR
         * @see #FLAG_ALT_FOCUSABLE_IM
         * @see #FLAG_WATCH_OUTSIDE_TOUCH
         * @see #FLAG_SHOW_WHEN_LOCKED
         * @see #FLAG_SHOW_WALLPAPER
         * @see #FLAG_TURN_SCREEN_ON
         * @see #FLAG_DISMISS_KEYGUARD
         * @see #FLAG_SPLIT_TOUCH
         * @see #FLAG_HARDWARE_ACCELERATED
         * @see #FLAG_LOCAL_FOCUS_MODE
         * @see #FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
         */
        @ViewDebug.ExportedProperty(flagMapping = {
                @ViewDebug.FlagToString(mask = FLAG_ALLOW_LOCK_WHILE_SCREEN_ON, equals = FLAG_ALLOW_LOCK_WHILE_SCREEN_ON,
                        name = "FLAG_ALLOW_LOCK_WHILE_SCREEN_ON"),
                @ViewDebug.FlagToString(mask = FLAG_DIM_BEHIND, equals = FLAG_DIM_BEHIND,
                        name = "FLAG_DIM_BEHIND"),
                @ViewDebug.FlagToString(mask = FLAG_BLUR_BEHIND, equals = FLAG_BLUR_BEHIND,
                        name = "FLAG_BLUR_BEHIND"),
                @ViewDebug.FlagToString(mask = FLAG_NOT_FOCUSABLE, equals = FLAG_NOT_FOCUSABLE,
                        name = "FLAG_NOT_FOCUSABLE"),
                @ViewDebug.FlagToString(mask = FLAG_NOT_TOUCHABLE, equals = FLAG_NOT_TOUCHABLE,
                        name = "FLAG_NOT_TOUCHABLE"),
                @ViewDebug.FlagToString(mask = FLAG_NOT_TOUCH_MODAL, equals = FLAG_NOT_TOUCH_MODAL,
                        name = "FLAG_NOT_TOUCH_MODAL"),
                @ViewDebug.FlagToString(mask = FLAG_TOUCHABLE_WHEN_WAKING, equals = FLAG_TOUCHABLE_WHEN_WAKING,
                        name = "FLAG_TOUCHABLE_WHEN_WAKING"),
                @ViewDebug.FlagToString(mask = FLAG_KEEP_SCREEN_ON, equals = FLAG_KEEP_SCREEN_ON,
                        name = "FLAG_KEEP_SCREEN_ON"),
                @ViewDebug.FlagToString(mask = FLAG_LAYOUT_IN_SCREEN, equals = FLAG_LAYOUT_IN_SCREEN,
                        name = "FLAG_LAYOUT_IN_SCREEN"),
                @ViewDebug.FlagToString(mask = FLAG_LAYOUT_NO_LIMITS, equals = FLAG_LAYOUT_NO_LIMITS,
                        name = "FLAG_LAYOUT_NO_LIMITS"),
                @ViewDebug.FlagToString(mask = FLAG_FULLSCREEN, equals = FLAG_FULLSCREEN,
                        name = "FLAG_FULLSCREEN"),
                @ViewDebug.FlagToString(mask = FLAG_FORCE_NOT_FULLSCREEN, equals = FLAG_FORCE_NOT_FULLSCREEN,
                        name = "FLAG_FORCE_NOT_FULLSCREEN"),
                @ViewDebug.FlagToString(mask = FLAG_DITHER, equals = FLAG_DITHER,
                        name = "FLAG_DITHER"),
                @ViewDebug.FlagToString(mask = FLAG_SECURE, equals = FLAG_SECURE,
                        name = "FLAG_SECURE"),
                @ViewDebug.FlagToString(mask = FLAG_SCALED, equals = FLAG_SCALED,
                        name = "FLAG_SCALED"),
                @ViewDebug.FlagToString(mask = FLAG_IGNORE_CHEEK_PRESSES, equals = FLAG_IGNORE_CHEEK_PRESSES,
                        name = "FLAG_IGNORE_CHEEK_PRESSES"),
                @ViewDebug.FlagToString(mask = FLAG_LAYOUT_INSET_DECOR, equals = FLAG_LAYOUT_INSET_DECOR,
                        name = "FLAG_LAYOUT_INSET_DECOR"),
                @ViewDebug.FlagToString(mask = FLAG_ALT_FOCUSABLE_IM, equals = FLAG_ALT_FOCUSABLE_IM,
                        name = "FLAG_ALT_FOCUSABLE_IM"),
                @ViewDebug.FlagToString(mask = FLAG_WATCH_OUTSIDE_TOUCH, equals = FLAG_WATCH_OUTSIDE_TOUCH,
                        name = "FLAG_WATCH_OUTSIDE_TOUCH"),
                @ViewDebug.FlagToString(mask = FLAG_SHOW_WHEN_LOCKED, equals = FLAG_SHOW_WHEN_LOCKED,
                        name = "FLAG_SHOW_WHEN_LOCKED"),
                @ViewDebug.FlagToString(mask = FLAG_SHOW_WALLPAPER, equals = FLAG_SHOW_WALLPAPER,
                        name = "FLAG_SHOW_WALLPAPER"),
                @ViewDebug.FlagToString(mask = FLAG_TURN_SCREEN_ON, equals = FLAG_TURN_SCREEN_ON,
                        name = "FLAG_TURN_SCREEN_ON"),
                @ViewDebug.FlagToString(mask = FLAG_DISMISS_KEYGUARD, equals = FLAG_DISMISS_KEYGUARD,
                        name = "FLAG_DISMISS_KEYGUARD"),
                @ViewDebug.FlagToString(mask = FLAG_SPLIT_TOUCH, equals = FLAG_SPLIT_TOUCH,
                        name = "FLAG_SPLIT_TOUCH"),
                @ViewDebug.FlagToString(mask = FLAG_HARDWARE_ACCELERATED, equals = FLAG_HARDWARE_ACCELERATED,
                        name = "FLAG_HARDWARE_ACCELERATED"),
                @ViewDebug.FlagToString(mask = FLAG_LOCAL_FOCUS_MODE, equals = FLAG_LOCAL_FOCUS_MODE,
                        name = "FLAG_LOCAL_FOCUS_MODE"),
                @ViewDebug.FlagToString(mask = FLAG_TRANSLUCENT_STATUS, equals = FLAG_TRANSLUCENT_STATUS,
                        name = "FLAG_TRANSLUCENT_STATUS"),
                @ViewDebug.FlagToString(mask = FLAG_TRANSLUCENT_NAVIGATION, equals = FLAG_TRANSLUCENT_NAVIGATION,
                        name = "FLAG_TRANSLUCENT_NAVIGATION"),
                @ViewDebug.FlagToString(mask = FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS, equals = FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                        name = "FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS")
        }, formatToHexString = true)
        public int flags;

        /**
         * 如果窗口已经请求硬件加速，但是在进程中不允许这样做，那么它仍然像硬件加速一样呈现。
         * 这用于系统进程中的启动预览窗口，这不需要具有硬件加速的开销（它们只是静态渲染），
         * 但是应该将其渲染为与应用程序的实际窗口匹配，即使 它是硬件加速。
         * 即使窗口不是硬件加速，仍然执行它的渲染就好像是。 像{@link #FLAG_HARDWARE_ACCELERATED}，
         * 除了需要硬件加速的可信系统窗口（例如LockScreen），通常禁用硬件加速。
         * 除了 #FLAG_HARDWARE_ACCELERATED之外，还必须指定此标志，以启用系统窗口的硬件加速。
         * @hide
         */
        public static final int PRIVATE_FLAG_FAKE_HARDWARE_ACCELERATED = 0x00000001;

        /**
         * 在系统过程中，我们全局不使用硬件加速，因为有很多线程在做UI，并且它们冲突。
         * 如果UI的某些部分真的想要使用硬件加速，这个标志可以设置为强制它。
         * 这基本上是用于锁屏。 任何人使用它，你可能是错的。
         * @hide
         */
        public static final int PRIVATE_FLAG_FORCE_HARDWARE_ACCELERATED = 0x00000002;

        /**
         * 默认情况下，壁纸滚动时会发送新的偏移量。
         * 如果他们没有做任何有用的事情（他们不影响壁纸滚动操作），可以通过调用来跳过这些通知
         * {@link
         * android.service.wallpaper.WallpaperService.Engine#setOffsetNotificationsEnabled(boolean)}.
         *
         * @hide
         */
        public static final int PRIVATE_FLAG_WANTS_OFFSET_NOTIFICATIONS = 0x00000004;

        /**
         * 在多用户系统中，如果此标志已设置且所有者为系统进程，则此窗口将显示在所有用户屏幕上。
         * 这将覆盖通常仅在拥有用户的屏幕上显示的窗口类型的默认行为。请参阅每个窗口类型以确定其默认行为。
         * {@hide} */
        public static final int PRIVATE_FLAG_SHOW_FOR_ALL_USERS = 0x00000010;

        /**
         * 永远不要动画窗口的位置变化。
         * {@hide}
         */
        @TestApi
        public static final int PRIVATE_FLAG_NO_MOVE_ANIMATION = 0x00000040;

        /**
         * 窗口标志：将窗口的大小限制为原始大小（[320x480] x密度）的特殊标志。
         * 用于创建在兼容模式下运行的应用程序的窗口。
         * {@hide} */
        public static final int PRIVATE_FLAG_COMPATIBLE_WINDOW = 0x00000080;

        /**
         * 窗口标志：用于系统对话框的特殊选项。当设置此标志时，窗口将在创建时无条件地要求对焦。
         * {@hide} */
        public static final int PRIVATE_FLAG_SYSTEM_ERROR = 0x00000100;

        /**
         * 窗口标志：当窗口变为最顶层时，保持以前的半透明装饰状态。
         * {@hide} */
        public static final int PRIVATE_FLAG_INHERIT_TRANSLUCENT_DECOR = 0x00000200;

        /**
         * 标记当前窗口是否为键盘窗口，这意味着它将隐藏其后的所有其他窗口，
         * 除了具有{@link #FLAG_SHOW_WHEN_LOCKED}标志的窗口。
         * 此外，这只能由{@link LayoutParams＃TYPE_STATUS_BAR}设置。
         * {@hide}
         */
        public static final int PRIVATE_FLAG_KEYGUARD = 0x00000400;

        /**
         * 标记防止当前窗口背后的壁纸接收触摸事件。
         * {@hide}
         */
        public static final int PRIVATE_FLAG_DISABLE_WALLPAPER_TOUCH_EVENTS = 0x00000800;

        /**
         * 标志强制状态栏窗口始终可见。如果设置此标志时，该栏被隐藏，将再次显示该栏，并且该栏将具有透明背景。
         * 这只能由{@link LayoutParams＃TYPE_STATUS_BAR}设置。
         * {@hide}
         */
        public static final int PRIVATE_FLAG_FORCE_STATUS_BAR_VISIBLE_TRANSPARENT = 0x00001000;

        /**
         * 指示x，y，width和height成员应该被忽略（并因此保留其以前的值）的标志。
         * 例如因为他们是通过重新定位来外部管理的。
         * {@hide}
         */
        public static final int PRIVATE_FLAG_PRESERVE_GEOMETRY = 0x00002000;

        /**
         * 标志会使窗口忽略应用程序可见性，而仅依赖于装饰视图可见性来确定窗口可见性。
         * 这被recents用于在启动应用程序后继续绘制。
         * @hide
         */
        public static final int PRIVATE_FLAG_FORCE_DECOR_VIEW_VISIBILITY = 0x00004000;

        /**
         * 标记以指示不会在配置更改触发的活动重新启动期间替换此窗口。
         * 一般来说，WindowManager希望在重新启动之后更换Windows，
         * 因此它将保留其表面，直到替换准备显示为了防止视觉故障。
         * 然而，一些窗口，如PopupWindows期望在配置更改之间被清除，因此应该提示WindowManager它不应该等待更换。
         * @hide
         */
        public static final int PRIVATE_FLAG_WILL_NOT_REPLACE_ON_RELAUNCH = 0x00008000;

        /**
         * 标记以指示该子窗口应始终布置在父框架中，而不管当前的窗口模式配置如何。
         * @hide
         */
        public static final int PRIVATE_FLAG_LAYOUT_CHILD_WINDOW_IN_PARENT_FRAME = 0x00010000;

        /**
         * 标记表示此窗口总是绘制状态栏背景，无论其他标志是什么。
         * @hide
         */
        public static final int PRIVATE_FLAG_FORCE_DRAW_STATUS_BAR_BACKGROUND = 0x00020000;

        /**
         * 标志表示该窗口需要持续性能模式（如果设备支持）.
         * @hide
         */
        public static final int PRIVATE_FLAG_SUSTAINED_PERFORMANCE_MODE = 0x00040000;

        /**
         * Flag to indicate that this window is used as a task snapshot window. A task snapshot
         * window is a starting window that gets shown with a screenshot from the previous state
         * that is active until the app has drawn its first frame.
         *
         * <p>If this flag is set, SystemUI flags are ignored such that the real window behind can
         * set the SystemUI flags.
         * @hide
         */
        public static final int PRIVATE_FLAG_TASK_SNAPSHOT = 0x00080000;

        /**
         * 控制平台私有的标志。
         * @hide
         */
        @TestApi
        public int privateFlags;

        /**
         * 如果需要{@link #NEEDS_MENU_SET_TRUE}或不需要{@link #NEEDS_MENU_SET_FALSE}菜单键，
         * 那么尚未明确指定的窗口的值为{@link #needsMenuKey}。
         * 对于这种情况，我们应该看看它后面的窗口来确定适当的值。
         * @hide
         */
        public static final int NEEDS_MENU_UNSET = 0;

        /**
         * 显示指定窗口的{@link #needsMenuKey}的值需要一个菜单键。
         * @hide
         */
        public static final int NEEDS_MENU_SET_TRUE = 1;

        /**
         *  对于明确指定的窗口，{@link #needsMenuKey}的值不需要菜单键。
         * @hide
         */
        public static final int NEEDS_MENU_SET_FALSE = 2;

        /**
         * 属于响应{@link KeyEvent＃KEYCODE_MENU}的活动的窗口的状态变量，因此需要一个菜单键。
         * 对于菜单是物理按钮的设备，该变量将被忽略，但是在菜单键以软件绘制的设备上可能会被隐藏
         * 除非该变量设置为{@link #NEEDS_MENU_SET_TRUE}。
         * （请注意，如果可用，操作栏是提供其他功能的首选方式，否则通过选项菜单访问。）
         * {@hide}
         */
        public int needsMenuKey = NEEDS_MENU_UNSET;

        /**
         * 给定一组特定的窗口管理器标志，确定这样一个窗口当它具有焦点时可能是输入法的目标。
         * 特别地，这检查{@link #FLAG_NOT_FOCUSABLE}和{@link #FLAG_ALT_FOCUSABLE_IM}标志，
         * 如果两者的组合对应于需要在输入法后面的窗口，则返回true，以便用户可以键入。
         * 参数 flags ：当前窗口管理器标志。
         * 返回：如果这样的窗口应该在输入法后面/交互时返回true，否则返回false。
         */
        public static boolean mayUseInputMethod(int flags) {
            switch (flags&(FLAG_NOT_FOCUSABLE|FLAG_ALT_FOCUSABLE_IM)) {
                case 0:
                case FLAG_NOT_FOCUSABLE|FLAG_ALT_FOCUSABLE_IM:
                    return true;
            }
            return false;
        }

        /**
         * 用于确定该窗口的软输入区域的所需可见性状态的位的 #softInputMode的掩码。
         */
        public static final int SOFT_INPUT_MASK_STATE = 0x0f;

        /**
         * softInputMode的可见性状态：未指定任何状态。
         */
        public static final int SOFT_INPUT_STATE_UNSPECIFIED = 0;

        /**
         * softInputMode的可见性状态：请勿更改软输入区域的状态。
         */
        public static final int SOFT_INPUT_STATE_UNCHANGED = 1;

        /**
         * softInputMode的可见性状态：通常适当时（当用户向前导航到您的窗口时），请隐藏任何软输入区域。
         */
        public static final int SOFT_INPUT_STATE_HIDDEN = 2;

        /**
         * softInputMode的可见性状态：当此窗口接收到焦点时，请始终隐藏任何软输入区域。
         */
        public static final int SOFT_INPUT_STATE_ALWAYS_HIDDEN = 3;

        /**
         * #softInputMode的可见性状态：当此窗口接收到焦点时，请始终隐藏任何软输入区域。
         * #softInputMode的可见性状态：正常情况下（当用户向前浏览窗口时）显示软输入区域。
         */
        public static final int SOFT_INPUT_STATE_VISIBLE = 4;

        /**
         * VsoftInputMode的可见性状态：当此窗口接收输入焦点时，请始终使软输入区域可见。
         */
        public static final int SOFT_INPUT_STATE_ALWAYS_VISIBLE = 5;

        /**
         * 确定窗口应调整以适应软输入窗口的方式的{@link #softInputMode}的掩码。
         */
        public static final int SOFT_INPUT_MASK_ADJUST = 0xf0;

        /**
         * #softInputMode的调整选项：没有指定。根据窗口的内容，系统将尝试选择一个或另一个。
         */
        public static final int SOFT_INPUT_ADJUST_UNSPECIFIED = 0x00;

        /**
         * #softInputMode的调整选项：设置为允许在显示输入法时调整窗口大小，以使其内容不被输入法覆盖。
         * 这不能与#SOFT_INPUT_ADJUST_PAN组合;如果这两个都没有设置，那么系统将尝试根据窗口的内容选择一个或另一个。
         * 如果窗口的布局参数标志包括 #FLAG_FULLSCREEN，#softInputMode的值将被忽略;
         * 窗口不会调整大小，但将保持全屏。
         */
        public static final int SOFT_INPUT_ADJUST_RESIZE = 0x10;

        /**
         * #softInputMode的调整选项：设置为在显示输入法时具有窗口平移，
         * 因此不需要处理调整大小，而只需要通过框架进行调整，以确保当前输入焦点可见。
         * 这不能与 #SOFT_INPUT_ADJUST_RESIZE组合;
         * 如果这两个都没有设置，那么系统将尝试根据窗口的内容选择一个或另一个。
         */
        public static final int SOFT_INPUT_ADJUST_PAN = 0x20;

        /**
         * {@link #softInputMode}的调整选项：设置为不显示输入法的窗口。
         * 窗口不会被调整大小，并且不会将其焦点可见。
         */
        public static final int SOFT_INPUT_ADJUST_NOTHING = 0x30;

        /**
         * {@link #softInputMode}的位：当用户向前浏览窗口时设置。
         * 这通常由系统自动为您设置，尽管您可能希望在某些情况下自己设置窗口。
         * 窗口显示后，此标志将自动清除。
         */
        public static final int SOFT_INPUT_IS_FORWARD_NAVIGATION = 0x100;

        /**
         * An internal annotation for flags that can be specified to {@link #softInputMode}.
         *
         * @hide
         */
        @Retention(RetentionPolicy.SOURCE)
        @IntDef(flag = true, value = {
                SOFT_INPUT_STATE_UNSPECIFIED,
                SOFT_INPUT_STATE_UNCHANGED,
                SOFT_INPUT_STATE_HIDDEN,
                SOFT_INPUT_STATE_ALWAYS_HIDDEN,
                SOFT_INPUT_STATE_VISIBLE,
                SOFT_INPUT_STATE_ALWAYS_VISIBLE,
                SOFT_INPUT_ADJUST_UNSPECIFIED,
                SOFT_INPUT_ADJUST_RESIZE,
                SOFT_INPUT_ADJUST_PAN,
                SOFT_INPUT_ADJUST_NOTHING,
                SOFT_INPUT_IS_FORWARD_NAVIGATION,
        })
        public @interface SoftInputModeFlags {}

        /**
         * 任何软输入区域的所需工作模式。 可以是以下任何组合：
         * 其中一个可见性状态
         * {@link #SOFT_INPUT_STATE_UNSPECIFIED}, {@link #SOFT_INPUT_STATE_UNCHANGED},
         * {@link #SOFT_INPUT_STATE_HIDDEN}, {@link #SOFT_INPUT_STATE_ALWAYS_VISIBLE}, or
         * {@link #SOFT_INPUT_STATE_VISIBLE}.
         * <li> One of the adjustment options
         * {@link #SOFT_INPUT_ADJUST_UNSPECIFIED},
         * {@link #SOFT_INPUT_ADJUST_RESIZE}, or
         * {@link #SOFT_INPUT_ADJUST_PAN}.
         * 该标志可以通过 android.R.attr＃windowSoftInputMode属性控制在主题中。
         */
        @SoftInputModeFlags
        public int softInputMode;

        /**
         * 根据 Gravity在屏幕中放置窗口。
         * Gravity＃apply（int，int，int，android.graphics.Rect，int，int，android.graphics.Rect）Gravity.apply和
         * Gravity＃applyDisplay（int，android.graphics.Rect， android.graphics.Rect）Gravity.applyDisplay
         * 在窗口布局期间使用，该值作为所需的重力给定。
         * 例如，您可以指定 Gravity＃DISPLAY_CLIP_HORIZONTAL Gravity.DISPLAY_CLIP_HORIZONTAL和
         * Gravity＃DISPLAY_CLIP_VERTICAL Gravity.DISPLAY_CLIP_VERTICAL这里来控制
         * Gravity＃applyDisplay（int，android.graphics.Rect，android.graphics.Rect）Gravity.applyDisplay的行为。
         *
         * @see Gravity
         */
        public int gravity;

        /**
         * 容器和窗口小部件之间的水平边距（占容器宽度的百分比）。
         * 请参阅 Gravity＃apply（int，int，int，android.graphics.Rect，int，int，android.graphics.Rect）Gravity.apply。
         * 此字段添加了 #x以提供xAdj参数。
         */
        public float horizontalMargin;

        /**
         * 容器和小部件之间的垂直边距（以容器的高度的百分比表示）。
         * 请参阅 Gravity＃apply（int，int，int，android.graphics.Rect，int，int，android.graphics.Rect）Gravity.apply。
         * 此字段添加了#y以提供yAdj参数。
         */
        public float verticalMargin;

        /**
         * 绘图面和窗口内容之间的正面插图。
         * @hide
         */
        public final Rect surfaceInsets = new Rect();

        /**
         * 表面昆虫是否已手动设定。
         * 当设置为{@code false}时，视图根将自动确定适当的表面插入。
         * @see #surfaceInsets
         * @hide
         */
        public boolean hasManualSurfaceInsets;

        /**
         * 是否应该使用以前的表面昆虫，而不是现在设置的。
         * 当设置为true时，视图根将忽略此对象中的表面插入，并使用它当前具有的内容。
         * @see #surfaceInsets
         * @hide
         */
        public boolean preservePreviousSurfaceInsets = true;

        /**
         * 所需的位图格式。 可能是android.graphics.PixelFormat中的常量之一。 默认为OPAQUE。
         */
        public int format;

        /**
         * 定义用于此窗口的动画的样式资源。
         * 这必须是系统资源; 它不能是应用程序资源，因为窗口管理器无法访问应用程序。
         */
        public int windowAnimations;

        /**
         * 应用于整个窗口的Alpha值。
         * 1.0的alpha值意味着完全不透明，0.0表示完全透明
         */
        public float alpha = 1.0f;

        /**
         * 设置 #FLAG_DIM_BEHIND时，这是要应用的调光量。
         * 范围为1.0，完全不透明至0.0，不变暗。
         */
        public float dimAmount = 1.0f;

        /**
         * 使用#screenBrightness和 #buttonBrightness的默认值，
         * 表示该窗口的亮度值不会被覆盖，并且应使用正常亮度策略。
         */
        public static final float BRIGHTNESS_OVERRIDE_NONE = -1.0f;

        /**
         * 值为 #screenBrightness和 #buttonBrightness，
         * 表示当该窗口在前面时，屏幕或按钮背光亮度应设置为最低值。
         */
        public static final float BRIGHTNESS_OVERRIDE_OFF = 0.0f;

        /**
         * 值为 #screenBrightness和 #buttonBrightness，
         * 表示当该窗口在前面时，应将屏幕或按钮背光亮度设置为最高值。
         */
        public static final float BRIGHTNESS_OVERRIDE_FULL = 1.0f;

        /**
         * 这可以用来覆盖用户首选的屏幕亮度。值小于0，默认值是指使用首选屏幕亮度。
         * 0至1将亮度从黑暗调整为全亮。
         */
        public float screenBrightness = BRIGHTNESS_OVERRIDE_NONE;

        /**
         * 这可以用来覆盖按钮和键盘背光灯的标准行为。
         * 值小于0，默认值是指使用标准的背光行为。0至1将亮度从黑暗调整为全亮。
         */
        public float buttonBrightness = BRIGHTNESS_OVERRIDE_NONE;

        /**
         * rotationAnimation的值用于定义用于指定此窗口将在旋转后旋转或旋转的动画。
         */
        public static final int ROTATION_ANIMATION_ROTATE = 0;

        /**
         * V #rotationAnimation定义用于指定此窗口将在旋转后淡入或淡出的动画的值。
         */
        public static final int ROTATION_ANIMATION_CROSSFADE = 1;

        /**
         * #rotationAnimation定义用于指定此窗口将立即消失或出现的动画的值.
         */
        public static final int ROTATION_ANIMATION_JUMPCUT = 2;

        /**
         * #rotationAnimation指定无缝旋转模式的值。
         * 这样做就像JUMPCUT一样，但是如果在暂停屏幕的情况下无法应用旋转，则会退回到CROSSFADE。
         */
        public static final int ROTATION_ANIMATION_SEAMLESS = 3;

        /**
         * 定义旋转设备时在此窗口中使用的退出和输入动画。
         * 这只有一个影响，如果进入和离开的最上方的不透明窗口设置#FLAG_FULLSCREEN位，并且不被其他窗口覆盖。
         * 所有其他情况都将默认为 #ROTATION_ANIMATION_ROTATE行为。
         *
         * @see #ROTATION_ANIMATION_ROTATE
         * @see #ROTATION_ANIMATION_CROSSFADE
         * @see #ROTATION_ANIMATION_JUMPCUT
         */
        public int rotationAnimation = ROTATION_ANIMATION_ROTATE;

        /**
         * 此窗口的标识符。 这通常会为你填写。
         */
        public IBinder token = null;

        /**
         * 拥有此窗口的软件包的名称。
         */
        public String packageName = null;

        /**
         * 窗口的具体方向值。可以是{@link android.content.pm.ActivityInfo＃screenOrientation}允许的任何相同的值。
         * 如果未设置，将使用默认值{@link android.content.pm.ActivityInfo＃SCREEN_ORIENTATION_UNSPECIFIED}。
         */
        public int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

        /**
         * 窗口的首选刷新率。
         * 这必须是窗口所显示的支持的刷新率之一。 所选的刷新率将应用于显示器的默认模式。
         * 如果设置了{@link #preferredDisplayModeId}，则忽略此值。
         * 参阅 Display#getSupportedRefreshRates()
         该方法已经被丢弃：但是可以用#preferredDisplayModeId替代
         */
        @Deprecated
        public float preferredRefreshRate;

        /**
         * 窗口首选显示模式的ID。
         * 这必须是为显示器获得的支持的模式之一窗口打开。 值0表示没有偏好。
         * @see Display#getSupportedModes()
         * @see Display.Mode#getModeId()
         */
        public int preferredDisplayModeId;

        /**
         * 控制状态栏的可见性。
         *
         * @see View#STATUS_BAR_VISIBLE
         * @see View#STATUS_BAR_HIDDEN
         */
        public int systemUiVisibility;

        /**
         * @hide
         * 该层次结构中的视图请求的ui可见性。 组合值应为systemUi Visibility | 子树SystemUi可见性。
         */
        public int subtreeSystemUiVisibility;

        /**
         * G获取有关系统可见性更改的回调。
         * TODO: Maybe there should be a bitfield of optional callbacks that we need.
         * @hide
         */
        public boolean hasSystemUiListeners;

        /**
         * 当此窗口具有焦点时，禁用触摸板指针手势处理。
         * 窗口将从触摸板接收原始位置更新，而不是指针移动和合成触摸事件。
         *
         * @hide
         */
        public static final int INPUT_FEATURE_DISABLE_POINTER_GESTURES = 0x00000001;

        /**
         * 不构建此窗口的输入通道。因此，该频道将无法接收输入。
         * @hide
         */
        public static final int INPUT_FEATURE_NO_INPUT_CHANNEL = 0x00000002;

        /**
         * 当此窗口具有焦点时，不会为所有输入事件调用用户活动，因此应用程序将不得不自己执行。
         * 只能由键盘保护和电话应用程序使用。
         * @hide
         */
        public static final int INPUT_FEATURE_DISABLE_USER_ACTIVITY = 0x00000004;

        /**
         * 控制输入子系统的特殊功能。
         * @see #INPUT_FEATURE_DISABLE_POINTER_GESTURES
         * @see #INPUT_FEATURE_NO_INPUT_CHANNEL
         * @see #INPUT_FEATURE_DISABLE_USER_ACTIVITY
         * @hide
         */
        public int inputFeatures;

        /**
         * 设置当此窗口具有焦点时，用户活动超时发生之前的毫秒数。
         * 值-1使用标准超时。 值为0使用最小支持显示超时。
         * 此属性只能用于减少用户指定的显示超时; 它永远不会超出通常会超时的时间。
         * 只能由键盘保护和电话应用程序使用。
         * @hide
         */
        public long userActivityTimeout = -1;

        /**
         * 对于具有锚点（例如PopupWindow）的窗口，可以跟踪锚定窗口的视图。
         * @hide
         */
        public int accessibilityIdOfAnchor = -1;

        /**
         * 窗口标题不与标题栏中显示的标题保持同步，因此我们单独跟踪当前显示的标题以提供可访问性。
         * @hide
         */
        @TestApi
        public CharSequence accessibilityTitle;

        /**
         * 设置窗口管理器将隐藏窗口的超时时间（以毫秒为单位）。
         * 对于瞬时通知（如吐司）有用，因此我们不必依靠客户端合作来确保窗口被隐藏。必须在窗口创建时指定。
         * 请注意，应用程序不准备处理他们的窗口被删除没有他们明确的请求，并可能尝试与删除的窗口进行交互导致未定义的行为和崩溃。
         * 因此，我们会隐藏这样的窗口，以防止它们覆盖其他应用程序。
         * @hide
         */
        public long hideTimeoutMilliseconds = -1;

        /**
         * The color mode requested by this window. The target display may
         * not be able to honor the request. When the color mode is not set
         * to {@link ActivityInfo#COLOR_MODE_DEFAULT}, it might override the
         * pixel format specified in {@link #format}.
         *
         * @hide
         */
        @ActivityInfo.ColorMode
        private int mColorMode = ActivityInfo.COLOR_MODE_DEFAULT;

        public LayoutParams() {
            super(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT);
            type = TYPE_APPLICATION;
            format = PixelFormat.OPAQUE;
        }

        public LayoutParams(int _type) {
            super(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT);
            type = _type;
            format = PixelFormat.OPAQUE;
        }

        public LayoutParams(int _type, int _flags) {
            super(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT);
            type = _type;
            flags = _flags;
            format = PixelFormat.OPAQUE;
        }

        public LayoutParams(int _type, int _flags, int _format) {
            super(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT);
            type = _type;
            flags = _flags;
            format = _format;
        }

        public LayoutParams(int w, int h, int _type, int _flags, int _format) {
            super(w, h);
            type = _type;
            flags = _flags;
            format = _format;
        }

        public LayoutParams(int w, int h, int xpos, int ypos, int _type,
                            int _flags, int _format) {
            super(w, h);
            x = xpos;
            y = ypos;
            type = _type;
            flags = _flags;
            format = _format;
        }

        public final void setTitle(CharSequence title) {
            if (null == title)
                title = "";

            mTitle = TextUtils.stringOrSpannedString(title);
        }

        public final CharSequence getTitle() {
            return mTitle != null ? mTitle : "";
        }

        /**
         * 根据输入视图的高程（视觉z位置）设置曲面插值。
         * @hide
         */
        public final void setSurfaceInsets(View view, boolean manual, boolean preservePrevious) {
            final int surfaceInset = (int) Math.ceil(view.getZ() * 2);
            // Partial workaround for b/28318973. Every inset change causes a freeform window
            // to jump a little for a few frames. If we never allow surface insets to decrease,
            // they will stabilize quickly (often from the very beginning, as most windows start
            // as focused).
            // TODO(b/22668382) to fix this properly.
            if (surfaceInset == 0) {
                // OK to have 0 (this is the case for non-freeform windows).
                surfaceInsets.set(0, 0, 0, 0);
            } else {
                surfaceInsets.set(
                        Math.max(surfaceInset, surfaceInsets.left),
                        Math.max(surfaceInset, surfaceInsets.top),
                        Math.max(surfaceInset, surfaceInsets.right),
                        Math.max(surfaceInset, surfaceInsets.bottom));
            }
            hasManualSurfaceInsets = manual;
            preservePreviousSurfaceInsets = preservePrevious;
        }

        /**
         * <p>Set the color mode of the window. Setting the color mode might
         * override the window's pixel {@link android.view.WindowManager.LayoutParams#format format}.</p>
         *
         * <p>The color mode must be one of {@link ActivityInfo#COLOR_MODE_DEFAULT},
         * {@link ActivityInfo#COLOR_MODE_WIDE_COLOR_GAMUT} or
         * {@link ActivityInfo#COLOR_MODE_HDR}.</p>
         *
         * @see #getColorMode()
         */
        public void setColorMode(@ActivityInfo.ColorMode int colorMode) {
            mColorMode = colorMode;
        }

        /**
         * Returns the color mode of the window, one of {@link ActivityInfo#COLOR_MODE_DEFAULT},
         * {@link ActivityInfo#COLOR_MODE_WIDE_COLOR_GAMUT} or {@link ActivityInfo#COLOR_MODE_HDR}.
         *
         * @see #setColorMode(int)
         */
        @ActivityInfo.ColorMode
        public int getColorMode() {
            return mColorMode;
        }

        /** @hide */
        @SystemApi
        public final void setUserActivityTimeout(long timeout) {
            userActivityTimeout = timeout;
        }

        /** @hide */
        @SystemApi
        public final long getUserActivityTimeout() {
            return userActivityTimeout;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int parcelableFlags) {
            out.writeInt(width);
            out.writeInt(height);
            out.writeInt(x);
            out.writeInt(y);
            out.writeInt(type);
            out.writeInt(flags);
            out.writeInt(privateFlags);
            out.writeInt(softInputMode);
            out.writeInt(gravity);
            out.writeFloat(horizontalMargin);
            out.writeFloat(verticalMargin);
            out.writeInt(format);
            out.writeInt(windowAnimations);
            out.writeFloat(alpha);
            out.writeFloat(dimAmount);
            out.writeFloat(screenBrightness);
            out.writeFloat(buttonBrightness);
            out.writeInt(rotationAnimation);
            out.writeStrongBinder(token);
            out.writeString(packageName);
            TextUtils.writeToParcel(mTitle, out, parcelableFlags);
            out.writeInt(screenOrientation);
            out.writeFloat(preferredRefreshRate);
            out.writeInt(preferredDisplayModeId);
            out.writeInt(systemUiVisibility);
            out.writeInt(subtreeSystemUiVisibility);
            out.writeInt(hasSystemUiListeners ? 1 : 0);
            out.writeInt(inputFeatures);
            out.writeLong(userActivityTimeout);
            out.writeInt(surfaceInsets.left);
            out.writeInt(surfaceInsets.top);
            out.writeInt(surfaceInsets.right);
            out.writeInt(surfaceInsets.bottom);
            out.writeInt(hasManualSurfaceInsets ? 1 : 0);
            out.writeInt(preservePreviousSurfaceInsets ? 1 : 0);
            out.writeInt(needsMenuKey);
            out.writeInt(accessibilityIdOfAnchor);
            TextUtils.writeToParcel(accessibilityTitle, out, parcelableFlags);
            out.writeLong(hideTimeoutMilliseconds);
        }

        public static final Parcelable.Creator<android.view.WindowManager.LayoutParams> CREATOR
                = new Parcelable.Creator<android.view.WindowManager.LayoutParams>() {
            public android.view.WindowManager.LayoutParams createFromParcel(Parcel in) {
                return new android.view.WindowManager.LayoutParams(in);
            }

            public android.view.WindowManager.LayoutParams[] newArray(int size) {
                return new android.view.WindowManager.LayoutParams[size];
            }
        };


        public LayoutParams(Parcel in) {
            width = in.readInt();
            height = in.readInt();
            x = in.readInt();
            y = in.readInt();
            type = in.readInt();
            flags = in.readInt();
            privateFlags = in.readInt();
            softInputMode = in.readInt();
            gravity = in.readInt();
            horizontalMargin = in.readFloat();
            verticalMargin = in.readFloat();
            format = in.readInt();
            windowAnimations = in.readInt();
            alpha = in.readFloat();
            dimAmount = in.readFloat();
            screenBrightness = in.readFloat();
            buttonBrightness = in.readFloat();
            rotationAnimation = in.readInt();
            token = in.readStrongBinder();
            packageName = in.readString();
            mTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            screenOrientation = in.readInt();
            preferredRefreshRate = in.readFloat();
            preferredDisplayModeId = in.readInt();
            systemUiVisibility = in.readInt();
            subtreeSystemUiVisibility = in.readInt();
            hasSystemUiListeners = in.readInt() != 0;
            inputFeatures = in.readInt();
            userActivityTimeout = in.readLong();
            surfaceInsets.left = in.readInt();
            surfaceInsets.top = in.readInt();
            surfaceInsets.right = in.readInt();
            surfaceInsets.bottom = in.readInt();
            hasManualSurfaceInsets = in.readInt() != 0;
            preservePreviousSurfaceInsets = in.readInt() != 0;
            needsMenuKey = in.readInt();
            accessibilityIdOfAnchor = in.readInt();
            accessibilityTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            hideTimeoutMilliseconds = in.readLong();
        }

        @SuppressWarnings({"PointlessBitwiseExpression"})
        public static final int LAYOUT_CHANGED = 1<<0;
        public static final int TYPE_CHANGED = 1<<1;
        public static final int FLAGS_CHANGED = 1<<2;
        public static final int FORMAT_CHANGED = 1<<3;
        public static final int ANIMATION_CHANGED = 1<<4;
        public static final int DIM_AMOUNT_CHANGED = 1<<5;
        public static final int TITLE_CHANGED = 1<<6;
        public static final int ALPHA_CHANGED = 1<<7;
        public static final int MEMORY_TYPE_CHANGED = 1<<8;
        public static final int SOFT_INPUT_MODE_CHANGED = 1<<9;
        public static final int SCREEN_ORIENTATION_CHANGED = 1<<10;
        public static final int SCREEN_BRIGHTNESS_CHANGED = 1<<11;
        public static final int ROTATION_ANIMATION_CHANGED = 1<<12;
        /** {@hide} */
        public static final int BUTTON_BRIGHTNESS_CHANGED = 1<<13;
        /** {@hide} */
        public static final int SYSTEM_UI_VISIBILITY_CHANGED = 1<<14;
        /** {@hide} */
        public static final int SYSTEM_UI_LISTENER_CHANGED = 1<<15;
        /** {@hide} */
        public static final int INPUT_FEATURES_CHANGED = 1<<16;
        /** {@hide} */
        public static final int PRIVATE_FLAGS_CHANGED = 1<<17;
        /** {@hide} */
        public static final int USER_ACTIVITY_TIMEOUT_CHANGED = 1<<18;
        /** {@hide} */
        public static final int TRANSLUCENT_FLAGS_CHANGED = 1<<19;
        /** {@hide} */
        public static final int SURFACE_INSETS_CHANGED = 1<<20;
        /** {@hide} */
        public static final int PREFERRED_REFRESH_RATE_CHANGED = 1 << 21;
        /** {@hide} */
        public static final int NEEDS_MENU_KEY_CHANGED = 1 << 22;
        /** {@hide} */
        public static final int PREFERRED_DISPLAY_MODE_ID = 1 << 23;
        /** {@hide} */
        public static final int ACCESSIBILITY_ANCHOR_CHANGED = 1 << 24;
        /** {@hide} */
        @TestApi
        public static final int ACCESSIBILITY_TITLE_CHANGED = 1 << 25;
        /** {@hide} */
        public static final int EVERYTHING_CHANGED = 0xffffffff;

        // internal buffer to backup/restore parameters under compatibility mode.
        private int[] mCompatibilityParamsBackup = null;

        public final int copyFrom(android.view.WindowManager.LayoutParams o) {
            int changes = 0;

            if (width != o.width) {
                width = o.width;
                changes |= LAYOUT_CHANGED;
            }
            if (height != o.height) {
                height = o.height;
                changes |= LAYOUT_CHANGED;
            }
            if (x != o.x) {
                x = o.x;
                changes |= LAYOUT_CHANGED;
            }
            if (y != o.y) {
                y = o.y;
                changes |= LAYOUT_CHANGED;
            }
            if (horizontalWeight != o.horizontalWeight) {
                horizontalWeight = o.horizontalWeight;
                changes |= LAYOUT_CHANGED;
            }
            if (verticalWeight != o.verticalWeight) {
                verticalWeight = o.verticalWeight;
                changes |= LAYOUT_CHANGED;
            }
            if (horizontalMargin != o.horizontalMargin) {
                horizontalMargin = o.horizontalMargin;
                changes |= LAYOUT_CHANGED;
            }
            if (verticalMargin != o.verticalMargin) {
                verticalMargin = o.verticalMargin;
                changes |= LAYOUT_CHANGED;
            }
            if (type != o.type) {
                type = o.type;
                changes |= TYPE_CHANGED;
            }
            if (flags != o.flags) {
                final int diff = flags ^ o.flags;
                if ((diff & (FLAG_TRANSLUCENT_STATUS | FLAG_TRANSLUCENT_NAVIGATION)) != 0) {
                    changes |= TRANSLUCENT_FLAGS_CHANGED;
                }
                flags = o.flags;
                changes |= FLAGS_CHANGED;
            }
            if (privateFlags != o.privateFlags) {
                privateFlags = o.privateFlags;
                changes |= PRIVATE_FLAGS_CHANGED;
            }
            if (softInputMode != o.softInputMode) {
                softInputMode = o.softInputMode;
                changes |= SOFT_INPUT_MODE_CHANGED;
            }
            if (gravity != o.gravity) {
                gravity = o.gravity;
                changes |= LAYOUT_CHANGED;
            }
            if (format != o.format) {
                format = o.format;
                changes |= FORMAT_CHANGED;
            }
            if (windowAnimations != o.windowAnimations) {
                windowAnimations = o.windowAnimations;
                changes |= ANIMATION_CHANGED;
            }
            if (token == null) {
                // NOTE: token only copied if the recipient doesn't
                // already have one.
                token = o.token;
            }
            if (packageName == null) {
                // NOTE: packageName only copied if the recipient doesn't
                // already have one.
                packageName = o.packageName;
            }
            if (!Objects.equals(mTitle, o.mTitle) && o.mTitle != null) {
                // NOTE: mTitle only copied if the originator set one.
                mTitle = o.mTitle;
                changes |= TITLE_CHANGED;
            }
            if (alpha != o.alpha) {
                alpha = o.alpha;
                changes |= ALPHA_CHANGED;
            }
            if (dimAmount != o.dimAmount) {
                dimAmount = o.dimAmount;
                changes |= DIM_AMOUNT_CHANGED;
            }
            if (screenBrightness != o.screenBrightness) {
                screenBrightness = o.screenBrightness;
                changes |= SCREEN_BRIGHTNESS_CHANGED;
            }
            if (buttonBrightness != o.buttonBrightness) {
                buttonBrightness = o.buttonBrightness;
                changes |= BUTTON_BRIGHTNESS_CHANGED;
            }
            if (rotationAnimation != o.rotationAnimation) {
                rotationAnimation = o.rotationAnimation;
                changes |= ROTATION_ANIMATION_CHANGED;
            }

            if (screenOrientation != o.screenOrientation) {
                screenOrientation = o.screenOrientation;
                changes |= SCREEN_ORIENTATION_CHANGED;
            }

            if (preferredRefreshRate != o.preferredRefreshRate) {
                preferredRefreshRate = o.preferredRefreshRate;
                changes |= PREFERRED_REFRESH_RATE_CHANGED;
            }

            if (preferredDisplayModeId != o.preferredDisplayModeId) {
                preferredDisplayModeId = o.preferredDisplayModeId;
                changes |= PREFERRED_DISPLAY_MODE_ID;
            }

            if (systemUiVisibility != o.systemUiVisibility
                    || subtreeSystemUiVisibility != o.subtreeSystemUiVisibility) {
                systemUiVisibility = o.systemUiVisibility;
                subtreeSystemUiVisibility = o.subtreeSystemUiVisibility;
                changes |= SYSTEM_UI_VISIBILITY_CHANGED;
            }

            if (hasSystemUiListeners != o.hasSystemUiListeners) {
                hasSystemUiListeners = o.hasSystemUiListeners;
                changes |= SYSTEM_UI_LISTENER_CHANGED;
            }

            if (inputFeatures != o.inputFeatures) {
                inputFeatures = o.inputFeatures;
                changes |= INPUT_FEATURES_CHANGED;
            }

            if (userActivityTimeout != o.userActivityTimeout) {
                userActivityTimeout = o.userActivityTimeout;
                changes |= USER_ACTIVITY_TIMEOUT_CHANGED;
            }

            if (!surfaceInsets.equals(o.surfaceInsets)) {
                surfaceInsets.set(o.surfaceInsets);
                changes |= SURFACE_INSETS_CHANGED;
            }

            if (hasManualSurfaceInsets != o.hasManualSurfaceInsets) {
                hasManualSurfaceInsets = o.hasManualSurfaceInsets;
                changes |= SURFACE_INSETS_CHANGED;
            }

            if (preservePreviousSurfaceInsets != o.preservePreviousSurfaceInsets) {
                preservePreviousSurfaceInsets = o.preservePreviousSurfaceInsets;
                changes |= SURFACE_INSETS_CHANGED;
            }

            if (needsMenuKey != o.needsMenuKey) {
                needsMenuKey = o.needsMenuKey;
                changes |= NEEDS_MENU_KEY_CHANGED;
            }

            if (accessibilityIdOfAnchor != o.accessibilityIdOfAnchor) {
                accessibilityIdOfAnchor = o.accessibilityIdOfAnchor;
                changes |= ACCESSIBILITY_ANCHOR_CHANGED;
            }

            if (!Objects.equals(accessibilityTitle, o.accessibilityTitle)
                    && o.accessibilityTitle != null) {
                // NOTE: accessibilityTitle only copied if the originator set one.
                accessibilityTitle = o.accessibilityTitle;
                changes |= ACCESSIBILITY_TITLE_CHANGED;
            }

            // This can't change, it's only set at window creation time.
            hideTimeoutMilliseconds = o.hideTimeoutMilliseconds;

            return changes;
        }

        @Override
        public String debug(String output) {
            output += "Contents of " + this + ":";
            Log.d("Debug", output);
            output = super.debug("");
            Log.d("Debug", output);
            Log.d("Debug", "");
            Log.d("Debug", "WindowManager.LayoutParams={title=" + mTitle + "}");
            return "";
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(256);
            sb.append("WM.LayoutParams{");
            sb.append("(");
            sb.append(x);
            sb.append(',');
            sb.append(y);
            sb.append(")(");
            sb.append((width == MATCH_PARENT ? "fill" : (width == WRAP_CONTENT
                    ? "wrap" : String.valueOf(width))));
            sb.append('x');
            sb.append((height == MATCH_PARENT ? "fill" : (height == WRAP_CONTENT
                    ? "wrap" : String.valueOf(height))));
            sb.append(")");
            if (horizontalMargin != 0) {
                sb.append(" hm=");
                sb.append(horizontalMargin);
            }
            if (verticalMargin != 0) {
                sb.append(" vm=");
                sb.append(verticalMargin);
            }
            if (gravity != 0) {
                sb.append(" gr=#");
                sb.append(Integer.toHexString(gravity));
            }
            if (softInputMode != 0) {
                sb.append(" sim=#");
                sb.append(Integer.toHexString(softInputMode));
            }
            sb.append(" ty=");
            sb.append(type);
            sb.append(" fl=#");
            sb.append(Integer.toHexString(flags));
            if (privateFlags != 0) {
                if ((privateFlags & PRIVATE_FLAG_COMPATIBLE_WINDOW) != 0) {
                    sb.append(" compatible=true");
                }
                sb.append(" pfl=0x").append(Integer.toHexString(privateFlags));
            }
            if (format != PixelFormat.OPAQUE) {
                sb.append(" fmt=");
                sb.append(format);
            }
            if (windowAnimations != 0) {
                sb.append(" wanim=0x");
                sb.append(Integer.toHexString(windowAnimations));
            }
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                sb.append(" or=");
                sb.append(screenOrientation);
            }
            if (alpha != 1.0f) {
                sb.append(" alpha=");
                sb.append(alpha);
            }
            if (screenBrightness != BRIGHTNESS_OVERRIDE_NONE) {
                sb.append(" sbrt=");
                sb.append(screenBrightness);
            }
            if (buttonBrightness != BRIGHTNESS_OVERRIDE_NONE) {
                sb.append(" bbrt=");
                sb.append(buttonBrightness);
            }
            if (rotationAnimation != ROTATION_ANIMATION_ROTATE) {
                sb.append(" rotAnim=");
                sb.append(rotationAnimation);
            }
            if (preferredRefreshRate != 0) {
                sb.append(" preferredRefreshRate=");
                sb.append(preferredRefreshRate);
            }
            if (preferredDisplayModeId != 0) {
                sb.append(" preferredDisplayMode=");
                sb.append(preferredDisplayModeId);
            }
            if (systemUiVisibility != 0) {
                sb.append(" sysui=0x");
                sb.append(Integer.toHexString(systemUiVisibility));
            }
            if (subtreeSystemUiVisibility != 0) {
                sb.append(" vsysui=0x");
                sb.append(Integer.toHexString(subtreeSystemUiVisibility));
            }
            if (hasSystemUiListeners) {
                sb.append(" sysuil=");
                sb.append(hasSystemUiListeners);
            }
            if (inputFeatures != 0) {
                sb.append(" if=0x").append(Integer.toHexString(inputFeatures));
            }
            if (userActivityTimeout >= 0) {
                sb.append(" userActivityTimeout=").append(userActivityTimeout);
            }
            if (surfaceInsets.left != 0 || surfaceInsets.top != 0 || surfaceInsets.right != 0 ||
                    surfaceInsets.bottom != 0 || hasManualSurfaceInsets
                    || !preservePreviousSurfaceInsets) {
                sb.append(" surfaceInsets=").append(surfaceInsets);
                if (hasManualSurfaceInsets) {
                    sb.append(" (manual)");
                }
                if (!preservePreviousSurfaceInsets) {
                    sb.append(" (!preservePreviousSurfaceInsets)");
                }
            }
            if (needsMenuKey != NEEDS_MENU_UNSET) {
                sb.append(" needsMenuKey=");
                sb.append(needsMenuKey);
            }
            sb.append(" colorMode=").append(mColorMode);
            sb.append('}');
            return sb.toString();
        }

        /**
         * 缩放布局参数的坐标和大小。
         * @hide
         */
        public void scale(float scale) {
            x = (int) (x * scale + 0.5f);
            y = (int) (y * scale + 0.5f);
            if (width > 0) {
                width = (int) (width * scale + 0.5f);
            }
            if (height > 0) {
                height = (int) (height * scale + 0.5f);
            }
        }

        /**
         * 备份在兼容模式下使用的布局参数。
         * @see android.view.WindowManager.LayoutParams#restore()
         */
        void backup() {
            int[] backup = mCompatibilityParamsBackup;
            if (backup == null) {
                // we backup 4 elements, x, y, width, height
                backup = mCompatibilityParamsBackup = new int[4];
            }
            backup[0] = x;
            backup[1] = y;
            backup[2] = width;
            backup[3] = height;
        }

        /**
         * 恢复布局参数的坐标，大小和 gravity
         * @see android.view.WindowManager.LayoutParams#backup()
         */
        void restore() {
            int[] backup = mCompatibilityParamsBackup;
            if (backup != null) {
                x = backup[0];
                y = backup[1];
                width = backup[2];
                height = backup[3];
            }
        }

        private CharSequence mTitle = null;

        /** @hide */
        @Override
        protected void encodeProperties(@NonNull ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);

            encoder.addProperty("x", x);
            encoder.addProperty("y", y);
            encoder.addProperty("horizontalWeight", horizontalWeight);
            encoder.addProperty("verticalWeight", verticalWeight);
            encoder.addProperty("type", type);
            encoder.addProperty("flags", flags);
        }
    }
}
