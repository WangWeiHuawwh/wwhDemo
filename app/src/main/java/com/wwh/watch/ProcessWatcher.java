package com.wwh.watch;

import android.os.Build;
import android.os.FileObserver;
import android.util.Log;

import java.io.File;

public class ProcessWatcher {
    private FileObserver mFileObserver;
    private final String mPath;
    private final File mFile;
    private final WatchDog mWatchDog;

    public ProcessWatcher(int pid, WatchDog watchDog) {
        mPath = "/proc/" + pid;
        mFile = new File(mPath);
        mWatchDog = watchDog;
    }

    public void start() {
        if (mFileObserver == null) {
            mFileObserver = new MyFileObserver(mPath, FileObserver.CLOSE_NOWRITE);
        }
        mFileObserver.startWatching();
    }

    public void stop() {
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
    }

    private void doSomething() {
        try {
            //Runtime.getRuntime().exec("am start --user 0 -n in.srain.cube.demos.uctoast/in.srain.cube.demos.uctoast.beginactivty");
            if (Build.VERSION.SDK_INT >= 17) {
                Runtime.getRuntime().exec("am startservice --user 0 -n com.wwh.wwhpushdemo/com.wwh.wwhpushdemo.PushService");
            } else {
                Runtime.getRuntime().exec("am startservice -n com.wwh.wwhpushdemo/com.wwh.wwhpushdemo.PushService");
            }
        } catch (Exception e) {
            Log.d("DemoLog", "error");
        }
        Log.d("DemoLog", "restart");
        //mWatchDog.getContext().startActivity(new Intent(mWatchDog.getContext(), in.srain.cube.demos.uctoast.MainActivity.class));
    }

    private final class MyFileObserver extends FileObserver {
        private final Object mWaiter = new Object();

        public MyFileObserver(String path, int mask) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            if ((event & FileObserver.CLOSE_NOWRITE) == FileObserver.CLOSE_NOWRITE) {
                try {
                    synchronized (mWaiter) {
                        mWaiter.wait(3000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!mFile.exists()) {
                    doSomething();
                    stopWatching();
                    mWatchDog.exit();
                }
            }
        }
    }
}