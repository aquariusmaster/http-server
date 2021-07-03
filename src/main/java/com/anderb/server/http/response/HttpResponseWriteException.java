package com.anderb.server.http.response;

public class HttpResponseWriteException extends RuntimeException {

    public HttpResponseWriteException(String message) {
        super(message);
    }

    public HttpResponseWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpResponseWriteException(Throwable cause) {
        super(cause);
    }

}
