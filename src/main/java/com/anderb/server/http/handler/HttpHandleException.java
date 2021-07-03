package com.anderb.server.http.handler;

public class HttpHandleException extends RuntimeException {
    public HttpHandleException(String message) {
        super(message);
    }

    public HttpHandleException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpHandleException(Throwable cause) {
        super(cause);
    }
}