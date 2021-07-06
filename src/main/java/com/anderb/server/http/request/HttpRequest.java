package com.anderb.server.http.request;

import lombok.Data;

import java.util.Map;

@Data
public class HttpRequest {
    private String method;
    private String path;
    private Map<String, String> headers;
    private String requestBody;

    public String getHeader(String name) {
        return headers.get(name);
    }
}
