package ru.toxuin.vitapsnbypass.library;

import com.github.jacopofar.navigationinspector.BasicNavigationManipulator;
import com.github.jacopofar.navigationinspector.HttpRequestWrapper;
import com.github.jacopofar.navigationinspector.HttpResponseWrapper;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import ru.toxuin.vitapsnbypass.BackgroundService;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class UpdateXmlManipulator implements BasicNavigationManipulator {

    public boolean isFiltered(HttpRequest originalRequest) {
        return originalRequest.getUri().contains(".psp2.update.playstation.net/update/psp2/list/");
    }

    public HttpObject onResponse(HttpResponseWrapper httpResponseWrapper, long requestNumber) {
        return parseResponse(httpResponseWrapper.getOriginalHttpObject());
    }

    public HttpObject onBigResponse(HttpObject httpObject, long requestNumber) {
        return parseResponse(httpObject);
    }

    private HttpObject parseResponse(HttpObject httpObject) {
        if (httpObject instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) httpObject;
            HttpResponseWrapper container = new HttpResponseWrapper(response);

            File file = BackgroundService.getFWFile();
            if (!file.exists()) return container.getOriginalHttpObject();

            byte[] fileData = new byte[(int) file.length()];
            DataInputStream dis;
            try {
                dis = new DataInputStream(new FileInputStream(file));
                dis.readFully(fileData);
                dis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            container.setContent(fileData);
            return container.getOriginalHttpObject();
        }

        return httpObject;
    }



    public int getMaximumRequestBufferSizeInBytes() {
        return 1024*1024*4;
    }

    public int getMaximumResponseBufferSizeInBytes() {
        return 1024*1024*4;
    }



    public HttpResponse onRequest(HttpRequestWrapper httpRequestWrapper, long requestNumber) {
        // NOT USED
        return null;
    }

    public HttpResponse onBigRequest(HttpObject httpObject, long requestNumber) {
        // NOT USED
        return null;
    }

}
