package com.anderb.server.http.request;

public class HttpRequestParseException extends RuntimeException {

    public HttpRequestParseException(String message) {
        super(message);
    }

    public HttpRequestParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpRequestParseException(Throwable cause) {
        super(cause);
    }

}
