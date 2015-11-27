package com.wwh.watch;

import android.content.Context;
import android.content.SharedPreferences;

public class WatchDog extends Subprocess {
    private final Object mSync = new Object();

    private ProcessWatcher mProcessWatcher;

    @Override
    public void runOnSubprocess() {
        killPreviousProcess();
        regWatchers(getParentPid());
        holdMainThread();
        unregWatchers();
        //System.exit(0);
    }

    private void killPreviousProcess() {
        try {
            final String KEY = "previous_pid";
            final SharedPreferences spf = getContext().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
            final int pid = spf.getInt(KEY, 0);
            if (pid != 0) {
                android.os.Process.killProcess(pid);
            }
            spf.edit().putInt(KEY, android.os.Process.myPid()).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void regWatchers(int parentPid) {
        if (mProcessWatcher == null) {
            mProcessWatcher = new ProcessWatcher(parentPid, this);
        } else {
            mProcessWatcher.stop();
        }
        mProcessWatcher.start();

    }

    private void unregWatchers() {
        if (mProcessWatcher != null) {
            mProcessWatcher.stop();
        }
    }

    private void holdMainThread() {
        try {
            synchronized (mSync) {
                mSync.wait();
            }
        } catch (InterruptedException e) {
        }
    }

    public void exit() {
        try {
            mSync.notify();
        } catch (Exception e) {
        }
    }
}
