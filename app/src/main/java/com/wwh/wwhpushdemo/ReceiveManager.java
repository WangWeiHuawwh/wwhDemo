package com.wwh.wwhpushdemo;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2015/11/11.
 */
public class ReceiveManager {
    private InputStream mInputStream;
    private Thread mReceiveThread;
    private PushManager mPushManager;
    byte[] headerBuffer = new byte[PushHead.HEAD_LENGTH];
    private PushResponse mPushResponse = new PushResponse();
    private SocketReceiveListener mSocketReceiveListener;

    public interface SocketReceiveListener {
        public void onResponse(PushResponse pushResponse);

        public void onReceiveError();
    }

    public ReceiveManager(PushManager pushManager, InputStream inputStream, SocketReceiveListener listener) {
        mInputStream = inputStream;
        mPushManager = pushManager;
        mSocketReceiveListener = listener;
        mReceiveThread = new Thread(new RecRunnable());
        mReceiveThread.start();
    }

    public boolean isAlive() {
        return mReceiveThread != null && mReceiveThread.isAlive() && mInputStream != null;
    }

    public void close() {
        if (mReceiveThread != null && mReceiveThread.isAlive()) {
            mReceiveThread.interrupt();
        }
        try {
            if (null != mInputStream) {
                mInputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mInputStream = null;
        }
    }

    private class RecRunnable implements Runnable {
        public void run() {
            try {
                while (mPushManager.isConnected()) {
                    if (ReadHead()) {
                        if (ReadBody()) {
                            prepareForNextPacket();
                        } else {
                            //error
                            mSocketReceiveListener.onReceiveError();
                            break;
                        }
                    } else {
                        //error
                        mSocketReceiveListener.onReceiveError();
                        break;
                    }
                }
            } catch (Exception e) {
                Log.d("DemoLog", "e=" + e.toString());
                mSocketReceiveListener.onReceiveError();
                e.printStackTrace();
            }
        }

        public void prepareForNextPacket() {
            mPushResponse = new PushResponse();
        }

        private Boolean ReadBody() throws IOException {
            int totalRead = 0;
            int needRead = mPushResponse.mPushHead.bodyLength - PushHead.HEAD_LENGTH;
            while (true) {
                int read = 0;
                read = mInputStream.read(mPushResponse.body, totalRead,
                        needRead - totalRead);
                Log.d("DemoLog", "body_size=" + read);
                if (read == -1) {
                    //socket error
                    Log.d("DemoLog", "readbody false");
                    return false;
                }
                totalRead += read;
                if (totalRead == needRead)
                    break;
            }
            mSocketReceiveListener.onResponse(mPushResponse);
            return true;
        }

        private Boolean ReadHead() throws IOException {
            int totalRead = 0;
            int needRead = PushHead.HEAD_LENGTH;
            while (true) {
                int read = 0;
                read = mInputStream.read(headerBuffer, totalRead,
                        needRead - totalRead);
                Log.d("DemoLog", "head_size=" + read);
                if (read == -1) {
                    //socket error
                    Log.d("DemoLog", "readhead false");
                    return false;
                }
                totalRead += read;
                if (totalRead == needRead)
                    break;
            }
            mPushResponse.mPushHead.initFromBytes(headerBuffer);
            mPushResponse.body = new byte[mPushResponse.mPushHead.bodyLength];
            return true;
        }
    }
}
