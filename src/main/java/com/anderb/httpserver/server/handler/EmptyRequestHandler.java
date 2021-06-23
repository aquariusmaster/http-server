package com.anderb.httpserver.server.handler;

import com.anderb.httpserver.server.HttpStatus;
import com.anderb.httpserver.server.request.HttpRequest;
import com.anderb.httpserver.server.response.HttpResponse;

public class EmptyRequestHandler  extends HttpHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws HttpHandleException {
        if (request == null) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            return;
        }
        nextHandler.handle(request, response);
    }
}
