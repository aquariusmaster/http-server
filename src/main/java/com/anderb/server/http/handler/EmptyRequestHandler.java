package com.anderb.server.http.handler;

import com.anderb.server.http.HttpStatus;
import com.anderb.server.http.response.HttpResponse;
import com.anderb.server.http.request.HttpRequest;

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
