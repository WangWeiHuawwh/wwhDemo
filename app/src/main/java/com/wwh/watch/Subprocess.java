package com.wwh.watch;
import android.content.Context;
import android.util.Log;

public abstract class Subprocess {
    private int mParentPid;
    private Context mContext;

    static {
        System.loadLibrary("subprocess");
    }

    public Subprocess() {
        Log.d("DemoLog", "mParentPid=" + mParentPid + ", mContext=" + (mContext == null) + "pid=" + android.os.Process.myPid());
    }

    public final int getParentPid() {
        return mParentPid;
    }

    public final Context getContext() {
        return mContext;
    }

    public abstract void runOnSubprocess();

    @SuppressWarnings("JniMissingFunction")
    public static native void create(Context ctx, Class<? extends Subprocess> clazz);
}
