package com.anderb.webserver.server;

import lombok.Data;

import java.util.Map;

@Data
public class HttpRequest {
    private String method;
    private String path;
    private Map<String, String> headers;
    private String requestBody;

//    public HttpRequest() {
//    }
//
//    public String getMethod() {
//        return this.method;
//    }
//
//    public String getPath() {
//        return this.path;
//    }
//
//    public Map<String, String> getHeaders() {
//        return this.headers;
//    }
//
//    public String getRequestBody() {
//        return this.requestBody;
//    }
//
//    public void setMethod(String method) {
//        this.method = method;
//    }
//
//    public void setPath(String path) {
//        this.path = path;
//    }
//
//    public void setHeaders(Map<String, String> headers) {
//        this.headers = headers;
//    }
//
//    public void setRequestBody(String requestBody) {
//        this.requestBody = requestBody;
//    }
//
//    public boolean equals(final Object o) {
//        if (o == this) return true;
//        if (!(o instanceof HttpRequest)) return false;
//        final HttpRequest other = (HttpRequest) o;
//        if (!other.canEqual((Object) this)) return false;
//        final Object this$method = this.getMethod();
//        final Object other$method = other.getMethod();
//        if (this$method == null ? other$method != null : !this$method.equals(other$method)) return false;
//        final Object this$path = this.getPath();
//        final Object other$path = other.getPath();
//        if (this$path == null ? other$path != null : !this$path.equals(other$path)) return false;
//        final Object this$headers = this.getHeaders();
//        final Object other$headers = other.getHeaders();
//        if (this$headers == null ? other$headers != null : !this$headers.equals(other$headers)) return false;
//        final Object this$requestBody = this.getRequestBody();
//        final Object other$requestBody = other.getRequestBody();
//        if (this$requestBody == null ? other$requestBody != null : !this$requestBody.equals(other$requestBody))
//            return false;
//        return true;
//    }
//
//    protected boolean canEqual(final Object other) {
//        return other instanceof HttpRequest;
//    }
//
//    public int hashCode() {
//        final int PRIME = 59;
//        int result = 1;
//        final Object $method = this.getMethod();
//        result = result * PRIME + ($method == null ? 43 : $method.hashCode());
//        final Object $path = this.getPath();
//        result = result * PRIME + ($path == null ? 43 : $path.hashCode());
//        final Object $headers = this.getHeaders();
//        result = result * PRIME + ($headers == null ? 43 : $headers.hashCode());
//        final Object $requestBody = this.getRequestBody();
//        result = result * PRIME + ($requestBody == null ? 43 : $requestBody.hashCode());
//        return result;
//    }
//
//    public String toString() {
//        return "HttpRequest(method=" + this.getMethod() + ", path=" + this.getPath() + ", headers=" + this.getHeaders() + ", requestBody=" + this.getRequestBody() + ")";
//    }
}
