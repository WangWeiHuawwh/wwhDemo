package com.wwh.wwhpushdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Administrator on 2015/11/18.
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
    private ConnectivityManager connectivityManager;
    private static final Long SYNCTIME = 800L;
    private static final String LASTTIMESYNC = "LASTTIMEDATE";
    private static final String LSATTIMEDIS = "LASTTIMEDIS";
    SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        if (ni != null && ni.isConnected()) {
            if (System.currentTimeMillis() - sharedPreferences.getLong(LASTTIMESYNC, 0) >= SYNCTIME) {
                sharedPreferences.edit().putLong(LASTTIMESYNC, System.currentTimeMillis()).commit();
                Log.d("DemoLog", "connection");
                PushService.connection();
            }
        } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
            if (System.currentTimeMillis() - sharedPreferences.getLong(LSATTIMEDIS, 0) >= SYNCTIME) {
                sharedPreferences.edit().putLong(LSATTIMEDIS, System.currentTimeMillis()).commit();
                Log.d("DemoLog", "disconnection");
                PushService.close();
            }
        }
    }


    boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null) {
            Log.d("DemoLog", "!=null");
            try {
                //For 3G check
                boolean is3g = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                        .isConnectedOrConnecting();
                //For WiFi Check
                boolean isWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                        .isConnected();

                Log.d("DemoLog", "isWifi=" + isWifi);
                Log.d("DemoLog", "is3g=" + is3g);
                if (!isWifi) {

                    return false;
                } else {
                    return true;
                }

            } catch (Exception er) {
                return false;
            }

        } else {
            Log.d("DemoLog", "==null");
            return false;
        }
    }

}
