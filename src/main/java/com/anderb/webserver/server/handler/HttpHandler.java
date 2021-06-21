package com.anderb.webserver.server.handler;

import com.anderb.webserver.server.request.HttpRequest;
import com.anderb.webserver.server.response.HttpResponse;

public abstract class HttpHandler {

    protected HttpHandler nextHandler;

    public void nextHandler(HttpHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
    public abstract void handle(HttpRequest request, HttpResponse response) throws HttpHandleException;

    public static HttpHandler of(HttpHandler ...handlers) {
        if (handlers == null || handlers.length == 0 ||
                (handlers.length == 1 && handlers[0] instanceof NotFoundHttpHandler)) {
            return new NotFoundHttpHandler();
        }
        if (handlers.length == 1) {
            handlers[0].nextHandler(new NotFoundHttpHandler());
            return handlers[0];
        }
        HttpHandler prev = handlers[0];
        for (int i = 1; i < handlers.length; i++) {
            prev.nextHandler(handlers[i]);
            prev = handlers[i];
        }
        if (!(handlers[handlers.length - 1] instanceof NotFoundHttpHandler)) {
            prev.nextHandler(new NotFoundHttpHandler());
        }
        return handlers[0];
    }

}
//                    HttpResponse response = new HttpResponse();
//                    response.setStatus(HttpStatus.OK);
//                    response.getWriter().write(request.getRequestBody() != null ? request.getRequestBody() : "Hello");
//
//                    Path path = new File(request.getRequestBody()).toPath();
//                    Files.copy(path, response.getOutputStream());
//                    response.getOutputStream().flush();
//                    response.getOutputStream().close();
//                    response.addHeader("Content-Type", "image/jpeg");
//                    response.addHeader("Content-Disposition", "attachment; filename=test.jpeg");
//                    URLConnection.guessContentTypeFromName(new File("log4j.properties").getName());
//                    response.getWriter().flush();
//                    response.getWriter().close();
