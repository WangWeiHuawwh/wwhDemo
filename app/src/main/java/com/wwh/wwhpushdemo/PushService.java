package com.wwh.wwhpushdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.wwh.watch.Subprocess;
import com.wwh.watch.WatchDog;

/**
 * Created by Administrator on 2015/11/11.
 */
public class PushService extends Service implements SocketListener {
    private ISocketServiceCallback mISocketServiceCallback = null;
    private static PushManager mPushManager = null;
    private ConnectionChangeReceiver mConnectionChangeReceiver = new ConnectionChangeReceiver();
    private ISocketService.Stub mBinder = new ISocketService.Stub() {

        @Override
        public int request(Bundle mBundle) throws RemoteException {
            return 0;
        }

        @Override
        public void registerCallback(Bundle mBundle, ISocketServiceCallback iSocketServiceCallback)
                throws RemoteException {
            if (iSocketServiceCallback != null) {
                mISocketServiceCallback = iSocketServiceCallback;
                Log.d("DemoLog", "registerCallback isRegistered");

            }
        }

        @Override
        public void unregisterCallback(Bundle mBundle, ISocketServiceCallback iSocketServiceCallback)
                throws RemoteException {
            if (iSocketServiceCallback != null) {

            }
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("DemoLog", "service create pid=" + android.os.Process.myPid());
        Subprocess.create(this, WatchDog.class);
        mPushManager = new PushManager(getApplication(), this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionChangeReceiver, filter);
    }

    public static void connection() {
        if (mPushManager != null) {
            mPushManager.reconnect();
        }
    }

    public static void close() {
        if (mPushManager != null) {
            mPushManager.closeAll();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(0, new Notification());
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mPushManager != null) {
            if (mPushManager.state == PushManager.STATE_CONNECT_FAILED && !mPushManager.isAlive()) {
                Log.d("DemoPush", "bind connection");
                mPushManager.doReconnection();
            }
        }
        Log.d("DemoPush", "onbind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        unregisterReceiver(mConnectionChangeReceiver);
        super.onDestroy();
    }

    @Override
    public void onConnect(Boolean success) {
        //成功连接
        if (mISocketServiceCallback != null) {
            try {
                mISocketServiceCallback.connected();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResponse(byte[] response) {
        showNotification(1, response.toString());
        Bundle bundle = new Bundle();
        bundle.putString("string", "success");
        try {
            if (mISocketServiceCallback != null) {
                mISocketServiceCallback.response(bundle);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void showNotification(int i, String s) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("My notification")
                        .setContentText("!" + s);
        //mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setTicker("My notification");
        mBuilder.setAutoCancel(true);
        Class<?> clazz = null;
        try {
            clazz = Class.forName("com.wwh.wwhpushdemo.NotificationActivity");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, clazz);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(NotificationActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(i, mBuilder.build());
    }

    public void showLock() {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            if (pm.isInteractive()) {//isInteractive() api20
                Log.d("DemoPush", "screen lock");
                Intent alarmIntent = new Intent(this, LockToastActivity.class);
                alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(alarmIntent);
            } else {
                Log.d("DemoPush", "screen dislock");
            }
        } else {
            if (!pm.isScreenOn()) {
                Log.d("DemoPush", "screen lock");
                Intent alarmIntent = new Intent(this, LockToastActivity.class);
                alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(alarmIntent);
            } else {
                Log.d("DemoPush", "screen dislock");
            }
        }
    }
}
