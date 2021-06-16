package com.anderb.webserver.server.response;

import com.anderb.webserver.server.HttpStatus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;

public class BasicHttpResponseWriter implements HttpResponseWriter {
    private static final String SERVER_VERSION = "HttpServer/0.0.1";
    private static final String DEFAULT_MIME_TYPE = "text/html";

    public void writeResponse(Socket socket, HttpResponse response) {
        Objects.requireNonNull(socket, "Socket cannot be null!");
        Objects.requireNonNull(response, "response cannot be null!");

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            writeStatus(response.getStatus(), out);
            writeHeaders(response, out);
            writeBody(response.getBody(), out);
            out.flush();
        } catch (Exception e) {
            throw new HttpResponseWriteException(e);
        }
    }

    private void writeBody(String body, BufferedWriter out) throws IOException {
        if (body != null) {
            out.write(body);
        }
    }

    private void writeStatus(HttpStatus status, BufferedWriter out) throws IOException {
        out.write("HTTP/1.0 " + status.getCode() + " " + status.getMsg() + "\r\n");
    }

    private void writeHeaders(HttpResponse response, BufferedWriter out) throws IOException {
        out.write("Date: " + new Date() + "\r\n");
        out.write("Server: " + SERVER_VERSION + "\r\n");
        out.write("Content-Type: " + DEFAULT_MIME_TYPE + "\r\n");
        out.write(String.format("Content-Length: %d\r\n", response.getBody() != null ? response.getBody().length() : 0));
        out.write("\r\n");
    }
}
