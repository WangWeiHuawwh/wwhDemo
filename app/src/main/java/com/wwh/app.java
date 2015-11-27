package com.wwh;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by Administrator on 2015/11/17.
 */
public class app extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String processName = getProcessName(this,
                android.os.Process.myPid());
        //Log.d("DemoLog", "进程名称+" + processName);
        if (processName != null) {
            boolean defaultProcess = processName
                    .equals("com.wwh.wwhpushdemo");
            if (defaultProcess) {
                //必要的初始化资源操作
                Log.d("DemoLog", "app create");
            }
        }

    }

    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }
}
