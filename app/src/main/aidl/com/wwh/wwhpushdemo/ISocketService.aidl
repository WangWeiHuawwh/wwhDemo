package com.wwh.wwhpushdemo;

import com.wwh.wwhpushdemo.ISocketServiceCallback;

interface ISocketService {

    void registerCallback(inout Bundle mBundle,ISocketServiceCallback cb);
    
    void unregisterCallback(inout Bundle mBundle, ISocketServiceCallback cb);
    
    int request(inout Bundle mBundle);
}
