package com.wwh.wwhpushdemo;

import com.wwh.Commen;

/**
 * Created by Administrator on 2015/11/12.
 */
public class HeartRequest extends PushRequest {
    public HeartRequest() {
        mPushHead.sequence = PushHead.getNextSequence();
        mPushHead.cmd = CMD_HEART;
        mPushHead.bodyLength = 0;
        mPushHead.dataLength = mPushHead.HEAD_LENGTH;
        mPushHead.version = Commen.version;
    }
}
