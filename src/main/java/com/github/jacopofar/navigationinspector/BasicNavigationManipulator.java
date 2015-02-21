package com.github.jacopofar.navigationinspector;

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public interface BasicNavigationManipulator {

    /**
     * Returns the size of the request buffer, in bytes.
     * Requests smaller than this size (headers included) will cause onRequest to be triggered and
     * receive the whole request. Requests of a greater size will trigger onBigRequest different times with different chunks
     * */
    int getMaximumRequestBufferSizeInBytes();
    /**
     * Returns the size of the response buffer, in bytes.
     * Responses smaller than this size (headers included) will cause onResponse to be triggered and
     * receive the whole response. Responses of a greater size will trigger onBigResponses different times with different chunks
     * */
    int getMaximumResponseBufferSizeInBytes();
    /**
     * Returns true when the request has to be managed by the proxy.
     * When false, the request and the response will not trigger the other proxy method
     * */
    boolean isFiltered(HttpRequest originalRequest);
    /**
     * Called before any request smaller than the buffer size
     * @param requestNumber a number identifying the single request
     * @return the HTTP response, or null in case the request has to be forwarded untouched
     * */
    HttpResponse onRequest(HttpRequestWrapper httpRequestWrapper, long requestNumber);
    /**
     * Called for each request chunk when the request is bigger than the buffer
     * @param requestNumber a number identifying the single request
     * @return the HTTP response (can be given before all the chunks have been received) or null in case the request has to be forwarded untouched
     * */
    HttpResponse onBigRequest(HttpObject httpObject, long requestNumber);
    /**
     * Called for each server response smaller than the buffer size
     * @param requestNumber a number identifying the single response
     * @return the response, possibly modified by the method, null forces a disconnect
     * */
    HttpObject onResponse(HttpResponseWrapper httpResponseWrapper, long requestNumber);
    /**
     * Called for each chunk of the server response when bigger than the buffer size
     * @param requestNumber a number identifying the single response
     * @return the response chunk, possibly modified by the method, null forces a disconnect
     * */
    HttpObject onBigResponse(HttpObject httpObject, long requestNumber);


}