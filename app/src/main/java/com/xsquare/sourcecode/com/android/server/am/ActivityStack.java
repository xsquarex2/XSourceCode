package com.xsquare.sourcecode.com.android.server.am;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.UserHandle;
import android.util.EventLog;

import java.util.ArrayList;
import java.util.Objects;

import static android.view.Display.DEFAULT_DISPLAY;

/**
 * 是一个栈式管理结构，每个ActivityStack可能包含一个或多个TaskRecord，
 * 栈顶的TaskRecord表示当前用户可见的任务。
 *
 * ActivityStack的职责是管理多个任务栈TaskRecord。
 * Created by xsquare on 2018/3/28.
 */

public class ActivityStack {
    //每一个ActivityStack都有一个编号，从0开始递增。
    // 编号为0，表示桌面(Launcher)所在的ActivityStack，叫做Home Stack
    final int mStackId;
    //ActivityStack栈就是通过这个数组实现的
    private final ArrayList<TaskRecord> mTaskHistory = new ArrayList<>();
    //在发生Activity切换时，正处于Pausing状态的Activity
    ActivityRecord mPausingActivity = null;
    //当前处于Resumed状态的ActivityRecord
    ActivityRecord mResumedActivity = null;
    //ActivityStack会绑定到一个显示设备上，譬如手机屏幕、投影仪等，在AMS中，
    // 通过ActivityDisplay这个类来抽象表示一个显示设备，ActivityDisplay.mStacks表示当前已经绑定到显示设备
    // 的所有ActivityStack。当执行一次绑定操作时，就会将ActivityStack.mStacks这个属性赋值成
    // ActivityDisplay.mStacks，否则，ActivityStack.mStacks就为null。
    // 简而言之，当mStacks不为null时， 表示当前ActivityStack已经绑定到了一个显示设备。
    ArrayList<ActivityStack> mStacks;
    enum ActivityState {
        INITIALIZING,//初始化
        RESUMED,//已显示
        PAUSING,//暂停中
        PAUSED,//已暂停
        STOPPING,//停止中
        STOPPED,//已停止
        FINISHING,//结束中
        DESTROYING,//销毁中
        DESTROYED//已销毁
    }

    /**
     * 该函数的功能是找到目标ActivityRecord(target)所在的任务栈(TaskRecord)，
     * 如果找到，则返回栈顶的ActivityRecord，否则，返回null
     */
    void findTaskLocked(ActivityRecord target, FindTaskResult result) {
        Intent intent = target.intent;
        ActivityInfo info = target.info;
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }
        final int userId = UserHandle.getUserId(info.applicationInfo.uid);
        boolean isDocument = intent != null & intent.isDocument();
        // If documentData is non-null then it must match the existing task data.
        Uri documentData = isDocument ? intent.getData() : null;
        for (int taskNdx = mTaskHistory.size() - 1; taskNdx >= 0; --taskNdx) {
            final TaskRecord task = mTaskHistory.get(taskNdx);
            if (task.voiceSession != null) {
                // We never match voice sessions; those always run independently.
                continue;
            }
            if (task.userId != userId) {
                // Looking for a different task.
                continue;
            }
            final ActivityRecord r = task.getTopActivity();
            if (r == null || r.finishing || r.userId != userId ||
                    r.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
                continue;
            }
            if (r.mActivityType != target.mActivityType) {
                continue;
            }
            final Intent taskIntent = task.intent;
            final Intent affinityIntent = task.affinityIntent;
            final boolean taskIsDocument;
            final Uri taskDocumentData;
            if (taskIntent != null && taskIntent.isDocument()) {
                taskIsDocument = true;
                taskDocumentData = taskIntent.getData();
            } else if (affinityIntent != null && affinityIntent.isDocument()) {
                taskIsDocument = true;
                taskDocumentData = affinityIntent.getData();
            } else {
                taskIsDocument = false;
                taskDocumentData = null;
            }
            // TODO Refactor to remove duplications. Check if logic can be simplified.
            if (taskIntent != null && taskIntent.getComponent() != null &&
                    taskIntent.getComponent().compareTo(cls) == 0 &&
                    Objects.equals(documentData, taskDocumentData)) {
                result.r = r;
                result.matchedByRootAffinity = false;
                break;
            } else if (affinityIntent != null && affinityIntent.getComponent() != null &&
                    affinityIntent.getComponent().compareTo(cls) == 0 &&
                    Objects.equals(documentData, taskDocumentData)) {
                result.r = r;
                result.matchedByRootAffinity = false;
                break;
            } else if (!isDocument && !taskIsDocument
                    && result.r == null && task.rootAffinity != null) {
                if (task.rootAffinity.equals(target.taskAffinity)) {
                    // It is possible for multiple tasks to have the same root affinity especially
                    // if they are in separate stacks. We save off this candidate, but keep looking
                    // to see if there is a better candidate.
                    result.r = r;
                    result.matchedByRootAffinity = true;
                }
            } else if (DEBUG_TASKS);
        }
    }

    /**
     * 根据Intent和ActivityInfo这两个参数可以获取一个Activity的包名，
     * 该函数会从栈顶至栈底遍历ActivityStack中的所有Activity，如果包名匹配成功，就返回
     */
    ActivityRecord findActivityLocked(Intent intent, ActivityInfo info,
                                      boolean compareIntentFilters) {
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }
        final int userId = UserHandle.getUserId(info.applicationInfo.uid);

        for (int taskNdx = mTaskHistory.size() - 1; taskNdx >= 0; --taskNdx) {
            final TaskRecord task = mTaskHistory.get(taskNdx);
            final ArrayList<ActivityRecord> activities = task.mActivities;

            for (int activityNdx = activities.size() - 1; activityNdx >= 0; --activityNdx) {
                ActivityRecord r = activities.get(activityNdx);
                if (!r.okToShowLocked()) {
                    continue;
                }
                if (!r.finishing && r.userId == userId) {
                    if (compareIntentFilters) {
                        if (r.intent.filterEquals(intent)) {
                            return r;
                        }
                    } else {
                        if (r.intent.getComponent().equals(cls)) {
                            return r;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * 该函数用于将当前的ActivityStack挪到前台，执行时，调用ActivityStack中的其他一些判定函数
     */
    void moveToFront(String reason) {
        moveToFront(reason, null);
    }
    /**
     * @param reason The reason for moving the stack to the front.
     * @param task If non-null, the task will be moved to the top of the stack.
     * */
    void moveToFront(String reason, TaskRecord task) {
        if (!isAttached()) {
            return;
        }
        mStacks.remove(this);
        mStacks.add(findStackInsertIndex(ON_TOP), this);
        mStackSupervisor.setFocusStackUnchecked(reason, this);
        if (task != null) {
            insertTaskAtTop(task, null);
            return;
        }
        task = topTask();
        if (task != null) {
            mWindowContainerController.positionChildAtTop(task.getWindowContainerController(),
                    true /* includingParents */);
        }
    }

    /**
     * 用于判定当前ActivityStack是否已经绑定到显示设备
     */
    final boolean isAttached() {
        return mStacks != null;
    }

    /**
     * 用于判定当前是否为默认的显示设备(Display.DEFAULT_DISPLAY)，通常，默认的显示设备就是手机屏幕
     */
    final boolean isOnHomeDisplay() {
        return isAttached() &&
                mActivityContainer.mActivityDisplay.mDisplayId == DEFAULT_DISPLAY;
    }

    /**
     * 用于判定当前ActivityStack是否为Home Stack，即判定当前显示的是否为桌面(Launcher)
     */
    final boolean isHomeStack() {
        return mStackId == HOME_STACK_ID;
    }

    /**
     * 该函数用于将指定的任务栈挪到当前ActivityStack的最前面。
     * 在Activity状态变化时，需要对已有的ActivityStack中的任务栈进行调整，待显示Activity的宿主任务需要挪到前台
     */
    final void moveTaskToFrontLocked(TaskRecord tr, boolean noAnimation, ActivityOptions options,
                                     AppTimeTracker timeTracker, String reason) {
        final ActivityStack topStack = getTopStackOnDisplay();
        final ActivityRecord topActivity = topStack != null ? topStack.topActivity() : null;
        final int numTasks = mTaskHistory.size();
        final int index = mTaskHistory.indexOf(tr);
        if (numTasks == 0 || index < 0)  {
            // nothing to do!
            if (noAnimation) {
                ActivityOptions.abort(options);
            } else {
                updateTransitLocked(TRANSIT_TASK_TO_FRONT, options);
            }
            return;
        }
        if (timeTracker != null) {
            // The caller wants a time tracker associated with this task.
            for (int i = tr.mActivities.size() - 1; i >= 0; i--) {
                tr.mActivities.get(i).appTimeTracker = timeTracker;
            }
        }
        // Shift all activities with this task up to the top
        // of the stack, keeping them in the same internal order.
        insertTaskAtTop(tr, null);
        // Don't refocus if invisible to current user
        final ActivityRecord top = tr.getTopActivity();
        if (top == null || !top.okToShowLocked()) {
            addRecentActivityLocked(top);
            ActivityOptions.abort(options);
            return;
        }
        // Set focus to the top running activity of this stack.
        final ActivityRecord r = topRunningActivityLocked();
        mStackSupervisor.moveFocusableActivityStackToFrontLocked(r, reason);

        if (DEBUG_TRANSITION) Slog.v(TAG_TRANSITION, "Prepare to front transition: task=" + tr);
        if (noAnimation) {
            mWindowManager.prepareAppTransition(TRANSIT_NONE, false);
            if (r != null) {
                mNoAnimActivities.add(r);
            }
            ActivityOptions.abort(options);
        } else {
            updateTransitLocked(TRANSIT_TASK_TO_FRONT, options);
        }
        // If a new task is moved to the front, then mark the existing top activity as supporting
        // picture-in-picture while paused
        if (topActivity != null && topActivity.getStack().getStackId() != PINNED_STACK_ID) {
            topActivity.supportsPictureInPictureWhilePausing = true;
        }
        mStackSupervisor.resumeFocusedStackTopActivityLocked();
        EventLog.writeEvent(EventLogTags.AM_TASK_TO_FRONT, tr.userId, tr.taskId);
        mService.mTaskChangeNotificationController.notifyTaskMovedToFront(tr.taskId);
    }

    /**
     * 将任务插入ActivityStack栈顶
     */
    private void insertTaskAtTop(TaskRecord task, ActivityRecord starting) {
        updateTaskReturnToForTopInsertion(task);
        // TODO: Better place to put all the code below...may be addTask...
        mTaskHistory.remove(task);
        // Now put task at top.
        final int position = getAdjustedPositionForTask(task, mTaskHistory.size(), starting);
        mTaskHistory.add(position, task);
        updateTaskMovement(task, true);
        mWindowContainerController.positionChildAtTop(task.getWindowContainerController(),
                true /* includingParents */);
    }
}
