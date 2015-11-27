package com.wwh.wwhpushdemo;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2015/11/12.
 */
public class PushHead {
    public static final int HEAD_LENGTH = 4 + 4 + 4 + 4 + 4;
    public int sequence;//唯一标识
    public int cmd;//请求类型
    public int version;//客户端版本号
    public int dataLength;//加密后的长度
    public int bodyLength;//原本数据长度

    public void initFromBytes(byte[] bytes) {
        if (bytes.length < HEAD_LENGTH) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        sequence = buffer.getInt();
        cmd = buffer.getInt();
        version = buffer.getInt();
        bodyLength = buffer.getInt();
        dataLength = buffer.getInt();
        Log.d("DemoLog", "dataLength" + dataLength + "bodyLength=" + bodyLength);
    }

    public String toString() {
        return "sequence=" + sequence + ",cmd=" + cmd + ",version=" + version + ",dataLength=" + dataLength + ",bodyLength=" + bodyLength;
    }

    static private AtomicInteger mSequenceSeed = new AtomicInteger(0);

    public static int getNextSequence() {
        return mSequenceSeed.incrementAndGet();
    }
}
