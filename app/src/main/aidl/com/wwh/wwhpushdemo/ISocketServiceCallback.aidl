
package com.wwh.wwhpushdemo;

oneway interface ISocketServiceCallback 
{
    void response(inout Bundle mBundle);
    void connected();
    void disconnect();
}
