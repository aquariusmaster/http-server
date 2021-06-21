package com.anderb.webserver;

import com.anderb.webserver.server.handler.BaseGetHandler;
import com.anderb.webserver.server.HttpServer;

public class HttpServerApplication {
    public static void main(String[] args) {
        HttpServer.create().handler(new BaseGetHandler()).build().run();
    }
}
