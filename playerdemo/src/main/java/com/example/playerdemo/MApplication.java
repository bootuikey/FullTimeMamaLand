package com.example.playerdemo;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;

import com.example.playerdemo.handler.CrashHandler;
import com.lecloud.config.LeCloudPlayerConfig;
import com.letv.proxy.LeCloudProxy;

import java.util.List;

public class MApplication extends Application {


    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps != null) {
            for (RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    return procInfo.processName;
                }
            }
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String processName = getProcessName(this, android.os.Process.myPid());
        if (getApplicationInfo().packageName.equals(processName)) {
            //TODO CrashHandler是一个抓取崩溃log的工具类（可选）
            CrashHandler.getInstance(this);
            LeCloudProxy.init(getApplicationContext());
            LeCloudPlayerConfig.getInstance().setDeveloperMode(true).setIsApp().setUseLiveToVod(true);
        }
    }
}
