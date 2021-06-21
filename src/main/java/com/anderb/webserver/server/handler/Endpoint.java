package com.anderb.webserver.server.handler;

import com.anderb.webserver.server.request.HttpRequest;
import com.anderb.webserver.server.response.HttpResponse;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.function.BiConsumer;

@Slf4j
@Builder(builderMethodName = "create")
public class Endpoint extends HttpHandler {
    @NonNull
    private final String method;
    @NonNull
    private final String path;
    @NonNull
    private final BiConsumer<HttpRequest, HttpResponse> handler;

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws HttpHandleException {
        if (areMethodAndPathNotFit(request)) {
            if (nextHandler != null) {
                nextHandler.handle(request, response);
            }
            return;
        }
        try {
            handler.accept(request, response);
        } catch (Exception e) {
            throw new HttpHandleException(e);
        } finally {
            flushAndCloseInputStream(response);
        }
    }

    private boolean areMethodAndPathNotFit(HttpRequest request) {
        return !(method.equalsIgnoreCase(request.getMethod()) && path.equalsIgnoreCase(request.getPath()));
    }

    private void flushAndCloseInputStream(HttpResponse response) {
        try {
            if (response.isUsingOutputStream()) {
                response.getOutputStream().flush();
                response.getOutputStream().close();
            } else {
                response.getWriter().flush();
                response.getWriter().close();
            }
        } catch (IOException e) {
            log.error("Error closing input stream");
        }
    }

}
