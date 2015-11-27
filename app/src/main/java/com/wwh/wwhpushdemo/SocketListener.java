package com.wwh.wwhpushdemo;

/**
 * Created by Administrator on 2015/11/12.
 */
public interface SocketListener {
    public void onConnect(Boolean success);

    public void onResponse(byte[] pushResponse);
}
