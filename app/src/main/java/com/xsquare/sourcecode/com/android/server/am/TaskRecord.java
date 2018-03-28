package com.xsquare.sourcecode.com.android.server.am;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Debug;

import java.util.ArrayList;

/**
 * 是一个栈式管理结构，每个TaskRecord可能包含一个或多个ActivityRecord，
 * 栈顶的ActivityRecord表示当前用户可见的页面。
 *
 * TaskRecord的职责就是管理ActivityRecord，事实上，我们平时说的任务栈指的就是TaskRecord
 * ，所有ActivityRecord都必须要有宿主任务，如果不存在则新建一个。
 *
 * TaskRecord是一个栈结构，它里面的函数当然也侧重栈的管理：增删改查。
 * 事实上，在内部TaskRecord是用ArrayList来实现的栈的操作。
 * Created by xsquare on 2018/3/28.
 */

public class TaskRecord {
    //TaskRecord的唯一标识
    final int taskId;
    //TaskRecord把Activity的affinity记录下来，后续启动Activity时，
    // 会从已有的任务栈中匹配affinity，如果匹配上了，则不需要新建TaskRecord
    String affinity;
    //记录任务栈中最底部Activity的affinity，一经设定后就不再改变
    String rootAffinity;
    final IVoiceInteractionSession voiceSession;    // Voice interaction session driving task
    final IVoiceInteractor voiceInteractor;         // Associated interactor to provide to app
    //在当前任务栈中启动的第一个Activity的Intent将会被记录下来，后续如果有相同的Intent时，
    // 会与已有任务栈的Intent进行匹配，如果匹配上了，就不需要再新建一个TaskRecord了
    Intent intent;
    Intent affinityIntent;  // Intent of affinity-moved activity that started this task.
    int effectiveUid;       // The current effective uid of the identity of this task.

    //启动任务栈的Activity，这两个属性是用包名(CompentName)表示的，
    // real和orig是为了区分Activity有无别名(alias)的情况，
    // 如果AndroidManifest.xml中定义的Activity是一个alias，
    // 则此处real表示Activity的别名，orig表示真实的Activity
    ComponentName origActivity;
    ComponentName realActivity;

    boolean realActivitySuspended; // True if the actual activity component that started the
    // task is suspended.
    long firstActiveTime;   // First time this task was active.
    long lastActiveTime;    // Last time this task was active, including sleep.
    boolean inRecents;      // Actually in the recents list?
    boolean isAvailable;    // Is the activity available to be launched?
    boolean rootWasReset;   // True if the intent at the root of the task had
    // the FLAG_ACTIVITY_RESET_TASK_IF_NEEDED flag.
    boolean autoRemoveRecents;  // If true, we should automatically remove the task from
    // recents when activity finishes
    boolean askedCompatMode;// Have asked the user about compat mode for this task.
    boolean hasBeenVisible; // Set if any activities in the task have been visible to the user.

    String stringName;      // caching of toString() result.
    int userId;             // user for which this task was created
    boolean mUserSetupComplete; // The user set-up is complete as of the last time the task activity
    // was changed.

    int numFullscreen;      // Number of fullscreen activities.

    int mResizeMode;        // The resize mode of this task and its activities.
    // Based on the {@link ActivityInfo#resizeMode} of the root activity.
    private boolean mSupportsPictureInPicture;  // Whether or not this task and its activities
    // support PiP. Based on the {@link ActivityInfo#FLAG_SUPPORTS_PICTURE_IN_PICTURE} flag
    // of the root activity.
    boolean mTemporarilyUnresizable; // Separate flag from mResizeMode used to suppress resize
    // changes on a temporary basis.
    private int mLockTaskMode;  // Which tasklock mode to launch this task in. One of
    // ActivityManager.LOCK_TASK_LAUNCH_MODE_*
    private boolean mPrivileged;    // The root activity application of this task holds
    //任务栈的类型，等同于ActivityRecord的类型，是由任务栈的第一个ActivityRecord决定的
    int taskType;
    // 这是TaskRecord最重要的一个属性，TaskRecord是一个栈结构，
    // 栈的元素是ActivityRecord，其内部实现是一个数组mActivities
    final ArrayList<ActivityRecord> mActivities;
    //当前TaskRecord所在的ActivityStack
    private ActivityStack mStack;

    ActivityRecord getRootActivity() {
        for (int i = 0; i < mActivities.size(); i++) {
            final ActivityRecord r = mActivities.get(i);
            if (r.finishing) {
                continue;
            }
            return r;
        }
        return null;
    }
    ActivityRecord getTopActivity() {
        for (int i = mActivities.size() - 1; i >= 0; --i) {
            final ActivityRecord r = mActivities.get(i);
            if (r.finishing) {
                continue;
            }
            return r;
        }
        return null;
    }

    /**
     * 虽然也是从顶至底对任务栈进行遍历获取顶部的ActivityRecord，但这个函数同getTopActivity()有区别：
     * 输入参数notTop，表示在遍历的过程中需要排除notTop这个ActivityRecord;
     * @return ActivityRecord
     */
    ActivityRecord topRunningActivityLocked() {
        if (mStack != null) {
            for (int activityNdx = mActivities.size() - 1; activityNdx >= 0; --activityNdx) {
                ActivityRecord r = mActivities.get(activityNdx);
                if (!r.finishing && r.okToShowLocked()) {
                    return r;
                }
            }
        }
        return null;
    }

    /**
     * 将ActivityRecord添加到任务栈的顶部或底部。
     */
    void addActivityToTop(ActivityRecord r) {
        addActivityAtIndex(mActivities.size(), r);
    }
    void addActivityAtBottom(ActivityRecord r) {
        addActivityAtIndex(0, r);
    }

    /**
     * 该函数将一个ActivityRecord移至TaskRecord的顶部，实现方法就是先删除已有的，
     * 再在栈顶添加一个新的，这个和Intent.FLAG_ACTIVITY_REORDER_TO_FRONT相对应。
     */
    final void moveActivityToFrontLocked(ActivityRecord newTop) {
        mActivities.remove(newTop);
        mActivities.add(newTop);
        updateEffectiveIntent();
        setFrontOfTask();
    }

    /**
     * ActivityRecord有一个属性是frontOfTask，表示ActivityRecord是否为TaskRecord的根Activity。
     * 该函数设置TaskRecord中所有ActivityRecord的frontOfTask属性，
     * 从栈底往上 开始遍历，第一个不处于finishing状态的ActivityRecord的frontOfTask属性置成true，
     * 其他都为false。
     */
    final void setFrontOfTask() {
        boolean foundFront = false;
        final int numActivities = mActivities.size();
        for (int activityNdx = 0; activityNdx < numActivities; ++activityNdx) {
            final ActivityRecord r = mActivities.get(activityNdx);
            if (foundFront || r.finishing) {
                r.frontOfTask = false;
            } else {
                r.frontOfTask = true;
                // Set frontOfTask false for every following activity.
                foundFront = true;
            }
        }
        if (!foundFront && numActivities > 0) {
            // All activities of this task are finishing. As we ought to have a frontOfTask
            // activity, make the bottom activity front.
            mActivities.get(0).frontOfTask = true;
        }
    }

    /**
     * 清除TaskRecord中的ActivityRecord。这个和Intent.FLAG_ACTIVITY_CLEAR_TOP相对应，
     * 当启动Activity时，设置了Intent.FLAG_ACTIVITY_CLEAR_TOP参数，
     * 那么在宿主 TaskRecord中，待启动ActivityRecord之上的其他ActivityRecord都会被清除。
     */
    final void performClearTaskLocked() {
        mReuseTask = true;
        performClearTaskAtIndexLocked(0, !PAUSE_IMMEDIATELY);
        mReuseTask = false;
    }
}
