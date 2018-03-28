package com.xsquare.sourcecode.android.app;

import android.os.RemoteException;
import android.os.UserHandle;

/**
 * ActivityManager.StackId： 描述组件栈ID信息
 * ActivityManager.StackInfo： 描述组件栈信息，可以利用StackInfo去系统中检索某个栈。
 * ActivityManager.MemoryInfo： 系统可用内存信息
 * ActivityManager.RecentTaskInfo： 最近的任务信息
 * ActivityManager.RunningAppProcessInfo： 正在运行的进程信息
 * ActivityManager.RunningServiceInfo： 正在运行的服务信息
 * ActivityManager.RunningTaskInfo： 正在运行的任务信息
 * ActivityManager.AppTask： 描述应用任务信息
 *
 * 1、进程（Process）：Android系统进行资源调度和分配的基本单位，
 * 需要注意的是同一个栈的Activity可以运行在不同的进程里。
 * 2、任务（Task）：Task是一组以栈的形式聚集在一起的Activity的集合，这个任务栈就是一个Task。
 * Created by xsquare on 2018/3/28.
 */

public class ActivityManager {
    /**
     * 判断应用是否运行在一个低内存的Android设备上。
     */
    public boolean isLowRamDevice() {
        return isLowRamDeviceStatic();
    }

    /**
     * 重置app里的用户数据
     */
    public boolean clearApplicationUserData(String packageName, IPackageDataObserver observer) {
        try {
            return getService().clearApplicationUserData(packageName,
                    observer, UserHandle.myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
