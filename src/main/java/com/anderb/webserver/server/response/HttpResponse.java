package com.anderb.webserver.server.response;

import com.anderb.webserver.server.HttpStatus;

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

    public PrintWriter getWriter() throws UnsupportedEncodingException {
        if (usingOutputStream) {
            throw new IllegalStateException("Illegal to call getWriter() after getOutputStream() has been called");
        }

        if (writer == null) {
            OutputStreamWriter w = new OutputStreamWriter(outputStream, getCharacterEncoding());
            writer = new PrintWriter(w);
        }

        return writer;
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
}
