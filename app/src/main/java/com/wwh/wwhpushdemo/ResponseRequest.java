package com.wwh.wwhpushdemo;

import com.wwh.Commen;

/**
 * Created by Administrator on 2015/11/12.
 */
public class ResponseRequest extends PushRequest {
    public ResponseRequest(int id) {
        mPushHead.sequence = id;
        mPushHead.cmd = CMD_OK;
        mPushHead.bodyLength = 0;
        mPushHead.dataLength = mPushHead.HEAD_LENGTH;
        mPushHead.version = Commen.version;
    }
}
