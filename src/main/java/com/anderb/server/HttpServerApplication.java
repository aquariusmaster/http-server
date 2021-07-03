package com.anderb.server;


import com.anderb.server.http.HttpServer;
import com.anderb.server.http.handler.Endpoint;

import static com.anderb.server.IOHelper.writeFileFromResource;

public class HttpServerApplication {
    public static void main(String[] args) {
        HttpServer.create()
                .port(8080)
                .endpoint(Endpoint
                        .create()
                        .method("GET")
                        .path("/hello")
                        .handler((request, response) -> response.writeAsString("Hello!"))
                        .build())
                .endpoint(Endpoint
                        .create()
                        .method("POST")
                        .path("/login")
                        .handler((request, response) -> response.writeAsString("Hello, " + request.getRequestBody()))
                        .build())
                .endpoint(Endpoint
                        .create()
                        .method("GET")
                        .path("/favicon.ico")
                        .handler((request, response) -> {
                            writeFileFromResource("static/favicon.ico", response.getOutputStream());
                            response.addHeader("Content-Type", "image/x-icon");
                            response.addHeader("Content-Disposition", "attachment; filename=favicon.ico");
                        })
                        .build())
                .endpoint(Endpoint
                        .create()
                        .method("GET")
                        .path("/")
                        .handler((request, response) -> {
                            writeFileFromResource("static/index.html", response.getOutputStream());
                            response.addHeader("Content-Type", "text/html;charset=utf-8");
                            response.addHeader("encoding", "utf-8");
                        })
                        .build())
                .build()
                .run();
    }
}
