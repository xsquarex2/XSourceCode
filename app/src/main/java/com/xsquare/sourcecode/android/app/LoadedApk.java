package com.xsquare.sourcecode.android.app;

import android.app.Application;
import android.app.Instrumentation;
import android.os.Trace;
import android.util.SparseArray;

/**
 * Created by xsquare on 2018/3/28.
 */

public class LoadedApk {
    /**
     * 创建application流程
     * 1、创建加载Application的ClassLoader对象。
     * 2、创建ContextImpl对象。
     * 3、创建Application对象。
     * 4、将Application对象设置给ContextImpl。
     * 5、将Application对象添加到ActivityThread的Application列表中。
     * 6、执行Application的回调方法onCreate()。
     * @param forceDefaultAppClass
     * @param instrumentation
     * @return
     */
    public Application makeApplication(boolean forceDefaultAppClass,
                                       Instrumentation instrumentation) {
        // Application只会创建一次，如果Application对象已经存在则不再创建，一个APK对应一个
        // LoadedApk对象，一个LoadedApk对象对应一个Application对象。
        if (mApplication != null) {
            return mApplication;
        }

        Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "makeApplication");

        Application app = null;

        String appClass = mApplicationInfo.className;
        if (forceDefaultAppClass || (appClass == null)) {
            appClass = "android.app.Application";
        }

        try {
            // 1. 创建加载Application的ClassLoader对象。
            java.lang.ClassLoader cl = getClassLoader();
            if (!mPackageName.equals("android")) {
                Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER,
                        "initializeJavaContextClassLoader");
                initializeJavaContextClassLoader();
                Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            }
            // 2. 创建ContextImpl对象。
            ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);
            // 3. 创建Application对象。
            app = mActivityThread.mInstrumentation.newApplication(
                    cl, appClass, appContext);
            // 4. 将Application对象设置给ContextImpl。
            appContext.setOuterContext(app);
        } catch (Exception e) {
            if (!mActivityThread.mInstrumentation.onException(app, e)) {
                Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                throw new RuntimeException(
                        "Unable to instantiate application " + appClass
                                + ": " + e.toString(), e);
            }
        }
        // 5. 将Application对象添加到ActivityThread的Application列表中。
        mActivityThread.mAllApplications.add(app);
        mApplication = app;

        if (instrumentation != null) {
            try {
                // 6. 执行Application的回调方法onCreate()。
                instrumentation.callApplicationOnCreate(app);
            } catch (Exception e) {
                if (!instrumentation.onException(app, e)) {
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                    throw new RuntimeException(
                            "Unable to create application " + app.getClass().getName()
                                    + ": " + e.toString(), e);
                }
            }
        }

        // Rewrite the R 'constants' for all library apks.
        SparseArray<String> packageIdentifiers = getAssets().getAssignedPackageIdentifiers();
        final int N = packageIdentifiers.size();
        for (int i = 0; i < N; i++) {
            final int id = packageIdentifiers.keyAt(i);
            if (id == 0x01 || id == 0x7f) {
                continue;
            }

            rewriteRValues(getClassLoader(), packageIdentifiers.valueAt(i), id);
        }

        Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);

        return app;
    }
}
