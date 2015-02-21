package com.github.jacopofar.navigationinspector;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;

public class HttpRequestWrapper{

    private DefaultFullHttpRequest httpObject;

    public HttpRequestWrapper(DefaultFullHttpRequest httpObject) {
        this.httpObject=httpObject;
    }

    public String getUri() {
        return this.httpObject.getUri();
    }

    public HttpObject getOriginalHttpObject() {
        return this.httpObject;
    }

    public HttpHeaders getHeaders() {
        return httpObject.headers();
    }

}