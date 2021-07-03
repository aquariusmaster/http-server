package com.anderb.server.http.handler;

import com.anderb.server.http.HttpStatus;
import com.anderb.server.http.request.HttpRequest;
import com.anderb.server.http.response.HttpResponse;

public class NotFoundHttpHandler extends HttpHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws HttpHandleException {

        response.setStatus(HttpStatus.NOT_FOUND);
        response.getWriter().println("The resource you requested is not found");
        response.getWriter().flush();
        response.getWriter().close();
    }
}
