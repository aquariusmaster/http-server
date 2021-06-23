package com.anderb.httpserver.server.handler;

import com.anderb.httpserver.server.HttpStatus;
import com.anderb.httpserver.server.request.HttpRequest;
import com.anderb.httpserver.server.response.HttpResponse;

public class NotFoundHttpHandler extends HttpHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws HttpHandleException {

        response.setStatus(HttpStatus.NOT_FOUND);
        response.getWriter().println("The resource you requested is not found");
        response.getWriter().flush();
        response.getWriter().close();
    }
}
