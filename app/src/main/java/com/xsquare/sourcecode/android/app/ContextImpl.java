package com.xsquare.sourcecode.android.app;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.Process;
import android.os.Trace;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;

import com.xsquare.sourcecode.android.content.Context;

import java.io.File;
import java.util.Objects;

/**
 * ContextImpl对象的构建其实就是将ActivityThread、LoadedApk、activityToke、displayId、Configuration等重要信息
 * 传递给ContextImpl，这样ContextImpl就拥有了一个应用的所有信息
 * Created by xsquare on 2018/3/28.
 */

public class ContextImpl extends Context {
    private final ApplicationContentResolver mContentResolver;
    /**
     * 构造方法
     */
    private ContextImpl(@Nullable ContextImpl container, @NonNull ActivityThread mainThread,
                        @NonNull LoadedApk packageInfo, @Nullable String splitName,
                        @Nullable IBinder activityToken, @Nullable UserHandle user, int flags,
                        @Nullable ClassLoader classLoader) {
        mOuterContext = this;

        // If creator didn't specify which storage to use, use the default
        // location for application.
        // 1. 创建默认的应用目录/data/data/packageName。
        if ((flags & (android.content.Context.CONTEXT_CREDENTIAL_PROTECTED_STORAGE
                | android.content.Context.CONTEXT_DEVICE_PROTECTED_STORAGE)) == 0) {
            final File dataDir = packageInfo.getDataDirFile();
            if (Objects.equals(dataDir, packageInfo.getCredentialProtectedDataDirFile())) {
                flags |= android.content.Context.CONTEXT_CREDENTIAL_PROTECTED_STORAGE;
            } else if (Objects.equals(dataDir, packageInfo.getDeviceProtectedDataDirFile())) {
                flags |= android.content.Context.CONTEXT_DEVICE_PROTECTED_STORAGE;
            }
        }
        // 2. 复制mMainThread、mActivityToken、mPackageInfo、mResourcesManager等重要成员变量。
        mMainThread = mainThread;
        mActivityToken = activityToken;
        mFlags = flags;

        if (user == null) {
            user = Process.myUserHandle();
        }
        mUser = user;

        mPackageInfo = packageInfo;
        mSplitName = splitName;
        mClassLoader = classLoader;
        mResourcesManager = ResourcesManager.getInstance();

        if (container != null) {
            mBasePackageName = container.mBasePackageName;
            mOpPackageName = container.mOpPackageName;
            setResources(container.mResources);
            mDisplay = container.mDisplay;
        } else {
            mBasePackageName = packageInfo.mPackageName;
            ApplicationInfo ainfo = packageInfo.getApplicationInfo();
            if (ainfo.uid == Process.SYSTEM_UID && ainfo.uid != Process.myUid()) {
                // Special case: system components allow themselves to be loaded in to other
                // processes.  For purposes of app ops, we must then consider the context as
                // belonging to the package of this process, not the system itself, otherwise
                // the package+uid verifications in app ops will fail.
                mOpPackageName = ActivityThread.currentPackageName();
            } else {
                mOpPackageName = mBasePackageName;
            }
        }
        //创建ContentResolver对象。
        mContentResolver = new ApplicationContentResolver(this, mainThread, user);
    }



    final void setOuterContext(Context context) {
        mOuterContext = context;
    }

    /**
     * 创建Content Provider Context。
     *
     * @return
     * @throws PackageManager.NameNotFoundException
     */
    @Override
    public Context createPackageContext(String packageName, int flags)
            throws PackageManager.NameNotFoundException {
        return createPackageContextAsUser(packageName, flags,
                mUser != null ? mUser : Process.myUserHandle());
    }
    @Override
    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user)
            throws PackageManager.NameNotFoundException {
        if (packageName.equals("system") || packageName.equals("android")) {
            // The system resources are loaded in every application, so we can safely copy
            // the context without reloading Resources.
            return new ContextImpl(this, mMainThread, mPackageInfo, null, mActivityToken, user,
                    flags, null);
        }
        // 1. 获取LoadedApk对象。
        LoadedApk pi = mMainThread.getPackageInfo(packageName, mResources.getCompatibilityInfo(),
                flags | CONTEXT_REGISTER_PACKAGE, user.getIdentifier());
        if (pi != null) {
            // 2. 构造ContextImpl对象。
            ContextImpl c = new ContextImpl(this, mMainThread, pi, null, mActivityToken, user,
                    flags, null);

            final int displayId = mDisplay != null
                    ? mDisplay.getDisplayId() : Display.DEFAULT_DISPLAY;

            c.setResources(createResources(mActivityToken, pi, null, displayId, null,
                    getDisplayAdjustments(displayId).getCompatibilityInfo()));
            if (c.mResources != null) {
                return c;
            }
        }
        // Should be a better exception.
        throw new PackageManager.NameNotFoundException(
                "Application package " + packageName + " not found");
    }

    /**
     * 创建Service、app的Context
     * @return
     */
    static ContextImpl createAppContext(ActivityThread mainThread, LoadedApk packageInfo) {
        if (packageInfo == null) throw new IllegalArgumentException("packageInfo");
        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, null, null, null, 0,
                null);
        context.setResources(packageInfo.getResources());
        return context;
    }
    /**
     * 创建Activity的Context
     * @return
     */
    static ContextImpl createActivityContext(ActivityThread mainThread,
                                             LoadedApk packageInfo, ActivityInfo activityInfo, IBinder activityToken, int displayId,
                                             Configuration overrideConfiguration) {
        if (packageInfo == null) throw new IllegalArgumentException("packageInfo");

        String[] splitDirs = packageInfo.getSplitResDirs();
        ClassLoader classLoader = packageInfo.getClassLoader();

        if (packageInfo.getApplicationInfo().requestsIsolatedSplitLoading()) {
            Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, "SplitDependencies");
            try {
                classLoader = packageInfo.getSplitClassLoader(activityInfo.splitName);
                splitDirs = packageInfo.getSplitPaths(activityInfo.splitName);
            } catch (PackageManager.NameNotFoundException e) {
                // Nothing above us can handle a NameNotFoundException, better crash.
                throw new RuntimeException(e);
            } finally {
                Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
            }
        }

        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, activityInfo.splitName,
                activityToken, null, 0, classLoader);

        // Clamp display ID to DEFAULT_DISPLAY if it is INVALID_DISPLAY.
        displayId = (displayId != Display.INVALID_DISPLAY) ? displayId : Display.DEFAULT_DISPLAY;

        final CompatibilityInfo compatInfo = (displayId == Display.DEFAULT_DISPLAY)
                ? packageInfo.getCompatibilityInfo()
                : CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;

        final ResourcesManager resourcesManager = ResourcesManager.getInstance();

        // Create the base resources for which all configuration contexts for this Activity启动流程
        // will be rebased upon.
        context.setResources(resourcesManager.createBaseActivityResources(activityToken,
                packageInfo.getResDir(),
                splitDirs,
                packageInfo.getOverlayDirs(),
                packageInfo.getApplicationInfo().sharedLibraryFiles,
                displayId,
                overrideConfiguration,
                compatInfo,
                classLoader));
        context.mDisplay = resourcesManager.getAdjustedDisplay(displayId,
                context.getResources());
        return context;
    }
}
