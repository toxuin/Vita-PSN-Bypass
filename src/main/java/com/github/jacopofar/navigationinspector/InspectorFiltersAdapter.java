package com.github.jacopofar.navigationinspector;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;

import org.littleshoot.proxy.HttpFilters;

import java.net.InetSocketAddress;

public class InspectorFiltersAdapter implements HttpFilters {

    private BasicNavigationManipulator navigationManipulator;
    private long requestNumber;

    public InspectorFiltersAdapter(BasicNavigationManipulator nm, long requestNumber) {
        this.navigationManipulator = nm;
        this.requestNumber = requestNumber;
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        /*
        if (httpObject instanceof DefaultFullHttpRequest) {
            return navigationManipulator.onRequest(new HttpRequestWrapper((DefaultFullHttpRequest) httpObject), requestNumber);
        } else {
            return navigationManipulator.onBigRequest(httpObject, requestNumber);
        }
        */
        return null;
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if (httpObject instanceof DefaultFullHttpResponse) {
            return navigationManipulator.onResponse(new HttpResponseWrapper((DefaultFullHttpResponse)httpObject),requestNumber);
        } else return navigationManipulator.onBigResponse(httpObject,requestNumber);
        //return httpObject;
    }

    @Override
    public HttpObject proxyToClientResponse(HttpObject httpObject) {
        return httpObject;
    }





    @Override
    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
        return null;
    }

    @Override
    public void proxyToServerRequestSending() {
    }

    @Override
    public void proxyToServerRequestSent() {
    }

    @Override
    public void serverToProxyResponseReceiving() {
    }

    @Override
    public void serverToProxyResponseReceived() {
    }

    @Override
    public void proxyToServerConnectionQueued() {
    }

    @Override
    public InetSocketAddress proxyToServerResolutionStarted(String resolvingServerHostAndPort) {
        return null;
    }

    @Override
    public void proxyToServerResolutionSucceeded(String serverHostAndPort, InetSocketAddress resolvedRemoteAddress) {
    }

    @Override
    public void proxyToServerConnectionStarted() {
    }

    @Override
    public void proxyToServerConnectionSSLHandshakeStarted() {
    }

    @Override
    public void proxyToServerConnectionFailed() {
    }

    @Override
    public void proxyToServerConnectionSucceeded() {
    }


}