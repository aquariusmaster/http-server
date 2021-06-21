package com.anderb.webserver.server.handler;

import com.anderb.webserver.server.request.HttpRequest;
import com.anderb.webserver.server.response.HttpResponse;

import java.util.List;
import java.util.Objects;

public abstract class HttpHandler {

    protected HttpHandler nextHandler;

    public void nextHandler(HttpHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
    public abstract void handle(HttpRequest request, HttpResponse response) throws HttpHandleException;

    public static HttpHandler of(HttpHandler ...handlers) {
        Objects.requireNonNull(handlers);
        HttpHandler head = null;
        HttpHandler prev = null;
        for (HttpHandler handler : handlers) {
            if (prev == null) {
                head = prev = handler;
                continue;
            }
            prev.nextHandler(handler);
            prev = handler;
        }
        return head;
    }

    public static HttpHandler of(List<HttpHandler> handlers) {
        Objects.requireNonNull(handlers);
        HttpHandler prev = null;
        for (HttpHandler handler : handlers) {
            if (prev == null) {
                prev = handler;
                continue;
            }
            prev.nextHandler(handler);
            prev = handler;
        }
        return handlers.isEmpty() ? null : handlers.get(0);
    }

}
