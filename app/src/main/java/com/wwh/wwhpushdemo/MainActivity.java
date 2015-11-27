package com.wwh.wwhpushdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {
    private ISocketService mService = null;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("DemoLog", "MainActivity pid=" + android.os.Process.myPid());
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(PushService.class.getName());
                mIntent.setClassName("com.wwh.wwhpushdemo", "com.wwh.wwhpushdemo.PushService");
                startService(mIntent);
                try {
                    bindService(mIntent, conn, Context.BIND_AUTO_CREATE);
                } catch (Exception e) {

                }

            }
        });
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("DemoLog", "onServiceDisconnected");
            try {
                unbindService(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("DemoLog", "onServiceConnected");
            if (service != null) {
                mService = ISocketService.Stub.asInterface(service);
                try {
                    mService.registerCallback(null, mCallBack);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private ISocketServiceCallback mCallBack = new ISocketServiceCallback.Stub() {
        @Override
        public void response(Bundle mBundle) throws RemoteException {
            if (mBundle != null) {
                Log.d("DemoLog", mBundle.getString("string"));
            }

        }

        @Override
        public void connected() throws RemoteException {
            Log.d("DemoLog", "MainActivity connected");
        }

        @Override
        public void disconnect() throws RemoteException {
            Log.d("DemoLog", "disconnect");
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
