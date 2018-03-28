package com.xsquare.sourcecode.com.android.server.am;

import android.app.ActivityOptions;
import android.app.PictureInPictureParams;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.PersistableBundle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * 描述栈里的Activity相关信息，对应着一个用户界面，是Activity管理的最小单位。
 * Created by xsquare on 2018/3/28.
 */

public class ActivityRecord {
    final ActivityManagerService service;
    //当前ActivityRecord的标识
    final IApplicationToken.Stub appToken;
    AppWindowContainerController mWindowContainerController;
    //从标签中解析出来的信息，包含launchMode，permission，taskAffinity等
    final ActivityInfo info;
    final ApplicationInfo appInfo; // information about activity's app
    final int launchedFromPid; // always the pid who started the activity.
    //启动当前Activity的UID，即发起者的UID
    final int launchedFromUid;
    //启动当前Activity的包名，即发起者的包名
    final String launchedFromPackage;
    final int userId;          // Which user is this running for?
    //启动当前Activity的Intent
    final Intent intent;
    final ComponentName realActivity;  // the intent component, or target of an alias.
    final String shortComponentName; // the short component name of the intent
    final String resolvedType; // as per original caller;
    //当前所属的包名，这是由静态定义的
    final String packageName;
    //当前所属的进程名，大部分情况都是由静态定义的，但也有例外
    final String processName;
    //相同taskAffinity的Activity会被分配到同一个任务栈中
    final String taskAffinity;
    final boolean stateNotNeeded; // As per ActivityInfo.flags
    boolean fullscreen; // covers the full screen?
    final boolean noDisplay;  // activity is not displayed?
    private final boolean componentSpecified;  // did caller specify an explicit component?
    final boolean rootVoiceInteraction;  // was this the root activity of a voice interaction?

    static final int APPLICATION_ACTIVITY_TYPE = 0;
    static final int HOME_ACTIVITY_TYPE = 1;
    static final int RECENTS_ACTIVITY_TYPE = 2;
    static final int ASSISTANT_ACTIVITY_TYPE = 3;
    //Activity的类型有三种：APPLICATION_ACTIVITY_TYPE(应用)、HOME_ACTIVITY_TYPE(桌面)、RECENTS_ACTIVITY_TYPE(最近使用)
    int mActivityType;

    private CharSequence nonLocalizedLabel;  // the label information from the package mgr.
    private int labelRes;           // the label information from the package mgr.
    private int icon;               // resource identifier of activity's icon.
    private int logo;               // resource identifier of activity's logo.
    private int theme;              // resource identifier of activity's theme.
    private int realTheme;          // actual theme resource we will use, never 0.
    private int windowFlags;        // custom window flags for preview window.
    //ActivityRecord的宿主任务
    private TaskRecord task;
    private long createTime = System.currentTimeMillis();
    long displayStartTime;  // when we started launching this activity
    long fullyDrawnStartTime; // when we started launching this activity
    private long startTime;         // last time this activity was started
    long lastVisibleTime;   // last time this activity became visible
    long cpuTimeAtResume;   // the cpu time of host process at the time of resuming activity
    long pauseTime;         // last time we started pausing the activity
    long launchTickTime;    // base time for launch tick messages
    // Last configuration reported to the activity in the client process.
    private MergedConfiguration mLastReportedConfiguration;
    private int mLastReportedDisplayId;
    private boolean mLastReportedMultiWindowMode;
    private boolean mLastReportedPictureInPictureMode;
    CompatibilityInfo compat;// last used compatibility mode
    //在当前ActivityRecord看来，resultTo表示上一个启动它的ActivityRecord
    //当需要启动另一个ActivityRecord，会把自己作为resultTo，传递给下一个ActivityRecord
    ActivityRecord resultTo;
    final String resultWho; // additional identifier for use by resultTo.
    final int requestCode;  // code given by requester (resultTo)
    ArrayList<ResultInfo> results; // pending ActivityResult objs we have received
    HashSet<WeakReference<PendingIntentRecord>> pendingResults; // all pending intents for this act
    //Intent数组，用于暂存还没有调度到应用进程Activity的Intent
    ArrayList<ReferrerIntent> newIntents;
    ActivityOptions pendingOptions; // most recently given options
    ActivityOptions returningOptions; // options that are coming back via convertToTranslucent
    AppTimeTracker appTimeTracker; // set if we are tracking the time in this app/task/activity
    HashSet<ConnectionRecord> connections; // All ConnectionRecord we hold
    UriPermissionOwner uriPermissions; // current special URI access perms.
    //ActivityRecord的宿主进程
    ProcessRecord app;
    //ActivityRecord所处的状态，初始值是ActivityState.INITIALIZING
    ActivityState state;
    Bundle icicle;         // last saved activity state
    PersistableBundle persistentState; // last persistently saved activity state
    //标识当前的ActivityRecord是否处于任务栈的根部，即是否为进入任务栈的第一个ActivityRecord
    boolean frontOfTask;
    boolean launchFailed;   // set if a launched failed, to abort on 2nd try
    boolean haveState;      // have we gotten the last activity state?
    boolean stopped;        // is activity pause finished?
    boolean delayedResume;  // not yet resumed because of stopped app switches?
    boolean finishing;      // activity in pending finish list?
    boolean deferRelaunchUntilPaused;   // relaunch of activity is being deferred until pause is
    // completed
    boolean preserveWindowOnDeferredRelaunch; // activity windows are preserved on deferred relaunch
    int configChangeFlags;  // which config values have changed
    private boolean keysPaused;     // has key dispatching been paused for it?
    int launchMode;         // the launch mode activity attribute.
    boolean visible;        // does this activity's window need to be shown?
    boolean visibleIgnoringKeyguard; // is this activity visible, ignoring the fact that Keyguard
    // might hide this activity?
    private boolean mDeferHidingClient; // If true we told WM to defer reporting to the client
    // process that it is hidden.
    boolean sleeping;       // have we told the activity to sleep?
    boolean nowVisible;     // is this activity's window visible?
    boolean idle;           // has the activity gone idle?
    boolean hasBeenLaunched;// has this activity ever been launched?
    boolean frozenBeforeDestroy;// has been frozen but not yet destroyed.
    boolean immersive;      // immersive mode (don't interrupt if possible)
    boolean forceNewConfig; // force re-create with new config next time
    boolean supportsPictureInPictureWhilePausing;  // This flag is set by the system to indicate
    // that the activity can enter picture in picture while pausing (ie. only when another
    // task is brought to front or started)
    PictureInPictureParams pictureInPictureArgs = new PictureInPictureParams.Builder().build();
    // The PiP params used when deferring the entering of picture-in-picture.
    int launchCount;        // count of launches since last state
    long lastLaunchTime;    // time of last launch of this activity
    ComponentName requestedVrComponent; // the requested component for handling VR mode.
    ArrayList<ActivityContainer> mChildContainers = new ArrayList<>();

    String stringName;      // for caching of toString().
    //标识当前的ActivityRecord是否已经置入任务栈中
    private boolean inHistory;
}
