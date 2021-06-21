package com.anderb.webserver.server.handler;

import com.anderb.webserver.server.request.HttpRequest;
import com.anderb.webserver.server.response.HttpResponse;

public class BaseGetHandler extends HttpHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws HttpHandleException {
        if (!"GET".equals(request.getMethod())) {
            nextHandler.handle(request, response);
            return;
        }
        try {
            response.getWriter().println("Get handler works!");
            response.getWriter().flush();
            response.getWriter().close();
        } catch (Exception e) {
            throw new HttpHandleException(e);
        }
    }
}
