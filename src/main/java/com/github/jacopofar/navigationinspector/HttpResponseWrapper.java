package com.github.jacopofar.navigationinspector;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;

import java.io.UnsupportedEncodingException;

public class HttpResponseWrapper {

    private FullHttpResponse httpObject;

    public HttpResponseWrapper(FullHttpResponse httpObject) {
        this.httpObject = httpObject;
    }

    /**
     * Returns the original Netty {@link FullHttpResponse} wrapped by this instance
     * */
    public HttpObject getOriginalHttpObject() {
        return this.httpObject;
    }
    /**
     * Returns the HTTP headers of this response
     * */
    public HttpHeaders getHeaders() {
        return this.httpObject.headers();
    }

    /**
     * Returns a String containing the response decoded with the charset given by getCharsetName()
     * */
    public String getStringContent() {
        try {
            return new String(getRawContent(), getCharsetName());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sets the raw content of this response, changing the Content-Length header accordingly
     * */
    public void setContent(byte[] content) {
        httpObject.content().capacity(content.length);
        httpObject.content().setIndex(httpObject.content().readerIndex(), httpObject.content().readerIndex()+content.length);
        httpObject.content().setBytes(httpObject.content().readerIndex(), content);
        if (httpObject.headers().contains("Content-Length"))
            httpObject.headers().set("Content-Length", content.length);
        if (HttpHeaders.isContentLengthSet(httpObject))
            HttpHeaders.setContentLength(httpObject, content.length);
    }

    /**
     * Returns a byte array containing this response
     * */
    public byte[] getRawContent() {
        byte[] ret = new byte[httpObject.content().readableBytes()];
        httpObject.content().getBytes(httpObject.content().readerIndex(), ret);
        return ret;
    }
    /**
     * Returns the name of the character set used by the page, as declared in Content-Type header, returning "UTF-8" when not present
     * */
    public String getCharsetName() {
        String charset="UTF-8";
        String contentType=httpObject.headers().get("Content-Type");
        if(contentType!=null && contentType.matches(".+charset=.+")){
            charset=contentType.replaceFirst(".+charset=", "").split("[^a-zA-Z0-9_\\-]")[0];
        }
        return charset;
    }


    public boolean isHTML() {
        //TODO use some heuristic?
        if (!httpObject.headers().contains("Content-Type"))
            return false;
        return httpObject.headers().get("Content-Type").contains("text/html");

    }

    /**
     * Return the document parsed with JSoup
     * */
    /*
    public Document parse() {
        return Jsoup.parse(this.getStringContent());
    }
    */
    /*
    public HttpResponseWrapper setDocument(Document parsedPage) {
        try {
            this.setContent(parsedPage.outerHtml().getBytes(this.getCharsetName()));
        } catch (UnsupportedEncodingException e) {
            //fall back to UTF-8
            System.err.println("The page uses the unknown encoding '"+this.getCharsetName()+"', using UTF-8 instead");
            try {
                this.setContent(parsedPage.outerHtml().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e1) {
                //should never happen
            }
        }
        return null;
    }
    */

    public int getStatus() {
        return httpObject.getStatus().code();
    }


}