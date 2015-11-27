package com.wwh.wwhpushdemo;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/11.
 */
public class PushManager implements ReceiveManager.SocketReceiveListener {
    private final static int STATE_OPEN = 1;//socket打开
    private final static int STATE_CLOSE = 1 << 1;//socket关闭
    private final static int STATE_CONNECT_START = 1 << 2;//开始连接server
    private final static int STATE_CONNECT_SUCCESS = 1 << 3;//连接成功
    public final static int STATE_CONNECT_FAILED = 1 << 4;//连接失败
    private final int STATE_CONNECT_WAIT = 1 << 5;//等待连接
    public static int connectTimes = 0;
    public final static int MSG_CONNECT = 1;//连接handler
    public final static int MSG_SEND_OK = 2;//发送回包
    public final static int MSG_SEND_HEART = 3;//发送心跳包
    public final static int MGS_CHECK_HEART = 4;//处理心跳回包
    public final static int MGS_RECONNECTION = 5;//重连
    public final static int HEART_POST = 0;
    public final static int HEART_WAIT = 1;
    public final static int HEART_TIME = 1000 * 60 * 1;
    public final static int HEART_TIMEOUT = 1000 * 30;
    private Context mContext;
    private final String IP = "10.0.2.2";
    //"10.0.2.2";
    private final int PORT = 12345;
    public volatile int state = STATE_CONNECT_START;
    private Socket socket = null;
    private Thread connectThread;
    private SendManager mSendManager;
    private ReceiveManager mReceiveManager;
    private SocketListener mSocketListener;
    //private int isHeart = HEART_POST;//0标识收到回包，1标识等待回包中
    private List<PushRequest> heartArray = new ArrayList<PushRequest>();
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECT:
                    break;
                case MSG_SEND_OK:
                    if (msg.obj instanceof PushResponse) {
                        ResponseRequest responsePush = new ResponseRequest(((PushResponse) msg.obj).mPushHead.sequence);
                        mSendManager.sendRequest(responsePush);
                    }
                    break;
                case MSG_SEND_HEART:
                    if (mSendManager != null) {
                        HeartRequest heartPush = new HeartRequest();
                        mSendManager.sendRequest(heartPush);
                        heartArray.add(heartPush);
                        //isHeart = HEART_POST;
                        mHandler.removeMessages(MSG_SEND_HEART);
                        mHandler.sendEmptyMessageDelayed(MSG_SEND_HEART, HEART_TIME);
                    }
                    break;
                case MGS_CHECK_HEART:
                    //收到心跳回包
                    if (hashHeart()) {
                        //未收到回包
                        Log.d("DemoLog", "no heart response");
                        reconnect();
                    }
                    break;
                case MGS_RECONNECTION:
                    reconnect();
                    break;
            }
        }
    };

    private boolean hashHeart() {
        if (heartArray.size() > 0) {
            return true;
        }
        return false;
    }

    public PushManager(Context context, SocketListener socketListener) {
        mContext = context;
        mSocketListener = socketListener;
    }

    public boolean reconnect() {
        Log.d("DemoLog", "rest connect");
        closeAll();
        //isHeart = HEART_POST;
        mHandler.removeCallbacksAndMessages(null);
        heartArray.clear();
        connectThread = new Thread(connectRunnable);
        connectThread.start();
        return true;
    }

    public void layout() {
        Log.d("DemoLog", "layout");
        closeAll();
        mHandler.removeCallbacksAndMessages(null);
        state = STATE_CONNECT_FAILED;
        heartArray.clear();
    }

    public void closeAll() {
        if (state != STATE_CLOSE) {
            if (mSendManager != null && mReceiveManager != null) {
                mSendManager.close();
                mReceiveManager.close();
            }
            try {
                if (null != connectThread && connectThread.isAlive()) {
                    connectThread.interrupt();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connectThread = null;
            }
            state = STATE_CLOSE;
        }

    }

    private Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("DemoLog", "Conn :Start");
            state = STATE_OPEN;
            try {
                while (state != STATE_CLOSE) {
                    try {
                        state = STATE_CONNECT_START;
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(IP, PORT), 20 * 1000);
                        socket.setKeepAlive(true);
                        socket.setTcpNoDelay(true);
                        socket.setSendBufferSize(1024 * 1024);
                        socket.setReceiveBufferSize(1024 * 1024);
                        state = STATE_CONNECT_SUCCESS;
                        Log.d("DemoLog", "Conn :success");
                    } catch (Exception e) {
                        e.printStackTrace();
                        state = STATE_CONNECT_FAILED;
                        Log.d("DemoLog", e.toString());
                    }
                    if (state == STATE_CONNECT_SUCCESS) {
                        try {
                            connectTimes = 0;
                            mSendManager = new SendManager(PushManager.this, socket.getOutputStream());
                            mReceiveManager = new ReceiveManager(PushManager.this, socket.getInputStream(), PushManager.this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mHandler.removeMessages(MSG_SEND_HEART);
                        mHandler.sendEmptyMessage(MSG_SEND_HEART);
                        mSocketListener.onConnect(true);
                        break;
                    } else {
                        //state = STATE_CONNECT_WAIT;
                        //如果有网络没有连接上，则定时取连接，没有网络则直接退出
                        if (NetworkUtil.isNetworkAvailable(mContext)) {
                            try {
                                if (addConnectTimes()) {
                                    Thread.sleep(20 * 1000);
                                    doReconnection();
                                } else {
                                    layout();
                                }
                                break;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                        } else {
                            mSocketListener.onConnect(false);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    public boolean addConnectTimes() {
        synchronized (this) {
            if (connectTimes < 5) {
                connectTimes++;
                return true;
            } else {
                connectTimes = 0;
                return false;
            }
        }
    }

    public boolean isAlive() {
        return mSendManager.isAlive() && mReceiveManager.isAlive() && socket.isConnected() && !socket.isClosed();
    }

    public boolean isConnected() {
        return isAlive() && state != STATE_CLOSE && state == STATE_CONNECT_SUCCESS;
    }

    @Override
    public void onResponse(PushResponse pushResponse) {
        Log.d("DemoTest", "response=" + pushResponse.mPushHead.toString());
        if (pushResponse.mPushHead.cmd == PushRequest.CMD_RES_OK) {
            //收到push消息
            Log.d("DemoLog", "onResponse push");
            Message msg = new Message();
            msg.what = MSG_SEND_OK;
            msg.obj = pushResponse;
            mHandler.removeMessages(MSG_SEND_OK);
            mHandler.sendMessage(msg);
            //            byte[] body = AESEncrypt.desEncrypt(Commen.key,
            //                    pushResponse.body, pushResponse.mPushHead.bodyLength);
            mSocketListener.onResponse(pushResponse.body);
        } else if (pushResponse.mPushHead.cmd == PushRequest.CMD_RES_HEART) {
            Log.d("DemoLog", "onResponse heart");
            //isHeart = HEART_POST;
            for (PushRequest tempPushRequest : heartArray) {
                if (tempPushRequest.mPushHead.sequence == pushResponse.mPushHead.sequence) {
                    heartArray.remove(tempPushRequest);
                    break;
                }
            }
        }
    }

    @Override
    public void onReceiveError() {
        Log.d("DemoLog", "onReceive error");
        reconnect();
    }

    public void doReconnection() {
        if (state == STATE_CONNECT_SUCCESS || state == STATE_CONNECT_FAILED) {
            mHandler.removeMessages(MGS_RECONNECTION);
            mHandler.sendEmptyMessage(MGS_RECONNECTION);
        } else {
            Log.d("DemoPush", "already connection...");
        }
    }

}
