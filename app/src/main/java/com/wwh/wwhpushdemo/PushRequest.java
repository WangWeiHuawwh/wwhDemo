package com.wwh.wwhpushdemo;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2015/11/12.
 */
public class PushRequest {
    public static final int CMD_HEART = 1;
    public static final int CMD_OK = 100002;
    public static final int CMD_RES_HEART = 100001;
    public static final int CMD_RES_OK = 2;
    public PushHead mPushHead = new PushHead();
    public byte[] body;

    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(PushHead.HEAD_LENGTH
                + mPushHead.bodyLength);
        byteBuffer.putInt(mPushHead.sequence);
        byteBuffer.putInt(mPushHead.cmd);
        byteBuffer.putInt(mPushHead.version);
        byteBuffer.putInt(mPushHead.dataLength);
        byteBuffer.putInt(mPushHead.bodyLength);
        if (body != null && body.length > 0) {
            byteBuffer.put(body);
        }
        Log.d("DemoTest", "request=" + mPushHead.toString());
        return byteBuffer.array();
    }
}
