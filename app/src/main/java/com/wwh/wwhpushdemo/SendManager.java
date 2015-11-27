package com.wwh.wwhpushdemo;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Administrator on 2015/11/11.
 */
public class SendManager {
    public static final int MSG_READY_SEND = 1;
    private OutputStream outStream = null;
    private HandlerThread mThread;
    private Handler mHandler;
    private PushManager mPushManager;

    public boolean isAlive() {
        return mThread != null && mThread.isAlive() && outStream != null;
    }

    public void close() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
        }
        if (mThread.getLooper() != null) {
            mThread.getLooper().quit();
        }
        try {
            if (null != outStream) {
                outStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outStream = null;
        }

    }

    public SendManager(PushManager pushManager, OutputStream outputStream) {
        outStream = outputStream;
        mPushManager = pushManager;
        mThread = new HandlerThread("SEND_THREAD");
        mThread.start();
        mHandler = new Handler(mThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    switch (msg.what) {
                        case MSG_READY_SEND:
                            if (isAlive()) {
                                if (msg.obj instanceof HeartRequest) {
                                    try {
                                        outStream.write(((PushRequest) msg.obj).toByteArray());
                                    } catch (Exception e) {
                                        Log.d("DemoLog", "error" + e.toString());
                                    }
                                    Log.d("DemoLog", "send heart");
                                    mPushManager.mHandler.removeMessages(PushManager.MGS_CHECK_HEART);
                                    mPushManager.mHandler.sendEmptyMessageDelayed(PushManager.MGS_CHECK_HEART, PushManager.HEART_TIMEOUT);
                                } else if (msg.obj instanceof PushRequest) {
                                    try {
                                        outStream.write(((PushRequest) msg.obj).toByteArray());
                                    } catch (IOException e) {

                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }

    public void sendRequest(PushRequest pushRequest) {
        Message msg = new Message();
        msg.what = MSG_READY_SEND;
        msg.obj = pushRequest;
        mHandler.sendMessage(msg);
    }
}
