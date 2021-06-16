package com.anderb.webserver;

import com.anderb.webserver.server.request.BasicHttpRequestParser;
import com.anderb.webserver.server.response.BasicHttpResponseWriter;
import com.anderb.webserver.server.HttpServer;

public class HttpServerApplication {
    public static void main(String[] args) {
        new HttpServer(new BasicHttpRequestParser(), new BasicHttpResponseWriter(), null).run();
    }
}
