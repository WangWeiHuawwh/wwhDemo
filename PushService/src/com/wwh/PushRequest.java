package com.wwh;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2015/11/12.
 */
public class PushRequest {
	public static final int CMD_HEART = 1;
	public static final int CMD_OK = 2;
	public PushHead mPushHead = new PushHead();
	public byte[] body;

	public PushRequest(int sql, int i) {
		mPushHead.sequence = sql;
		mPushHead.cmd = i;
		mPushHead.dataLength = 20;
		mPushHead.bodyLength = 0;
	}

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
		return byteBuffer.array();
	}
}
