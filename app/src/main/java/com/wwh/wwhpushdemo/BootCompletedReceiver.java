package com.wwh.wwhpushdemo;


import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class BootCompletedReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DemoLog","get in");
        PushService.startForWeakLock(context, intent);
    }
}
