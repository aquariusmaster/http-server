package com.anderb.webserver.server.response;

import com.anderb.webserver.server.HttpStatus;
import lombok.SneakyThrows;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.anderb.webserver.server.Headers.CONTENT_TYPE;

public class HttpResponse {
    private HttpStatus status = HttpStatus.OK;
    private final Map<String, String> headers = new HashMap<>();
    private final OutputStream outputStream = new ByteArrayOutputStream();
    private PrintWriter writer;
    private boolean usingOutputStream;
    private String charset = "UTF-8";

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public Set<String> getHeaders() {
        return headers.keySet();
    }

    public void setContentType(String type) {
        headers.put(CONTENT_TYPE, type);
    }

    public OutputStream getOutputStream() {
        if (writer != null) {
            throw new IllegalStateException("Illegal to call getOutputStream() after getWriter() has been called");
        }
        usingOutputStream = true;
        return outputStream;
    }

    @SneakyThrows
    public PrintWriter getWriter() {
        if (usingOutputStream) {
            throw new IllegalStateException("Illegal to call getWriter() after getOutputStream() has been called");
        }

        if (writer == null) {
            OutputStreamWriter w = new OutputStreamWriter(outputStream, getCharacterEncoding());
            writer = new PrintWriter(w);
        }

        return writer;
    }

    public void writeAsString(String value) {
        if (value == null) return;
        getWriter().println(value);
    }

    private String getCharacterEncoding() {
        return charset;
    }

    public void setCharacterEncoding(String charset) {
        this.charset = charset;
    }

    public int getContentLength() {
        return ((ByteArrayOutputStream) outputStream).size();
    }

    protected byte[] getBodyInBytes() {
        return ((ByteArrayOutputStream) outputStream).toByteArray();
    }

    public boolean isUsingOutputStream() {
        return usingOutputStream;
    }

    public static HttpResponse badRequest(String message) {
        HttpResponse response = new HttpResponse();
        response.setStatus(HttpStatus.BAD_REQUEST);
        response.getWriter().println(message);
        return response;
    }

    public static HttpResponse serverError(String message) {
        HttpResponse response = new HttpResponse();
        response.setStatus(HttpStatus.SERVER_ERROR);
        response.getWriter().println(message);
        return response;
    }
}
