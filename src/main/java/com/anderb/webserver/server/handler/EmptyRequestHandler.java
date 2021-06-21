package com.anderb.webserver.server.handler;

import com.anderb.webserver.server.HttpStatus;
import com.anderb.webserver.server.request.HttpRequest;
import com.anderb.webserver.server.response.HttpResponse;

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
