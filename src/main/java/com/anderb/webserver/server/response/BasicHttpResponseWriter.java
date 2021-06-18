package com.anderb.webserver.server.response;

import com.anderb.webserver.server.HttpStatus;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;

import static com.anderb.webserver.server.Headers.*;

public class BasicHttpResponseWriter implements HttpResponseWriter {
    private static final String SERVER_VERSION = "HttpServer/0.0.1";
    private static final String DEFAULT_MIME_TYPE = "text/html";
    private static final String DELIM = ": ";

    public void writeResponse(Socket socket, HttpResponse response) {
        Objects.requireNonNull(socket, "Socket cannot be null!");
        Objects.requireNonNull(response, "response cannot be null!");

        try (OutputStream outputStream = socket.getOutputStream();
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writeStatus(response.getStatus(), out);
            writeHeaders(response, out);
            writeBody(response, outputStream);
            out.flush();
            outputStream.flush();
        } catch (Exception e) {
            throw new HttpResponseWriteException(e);
        }
    }

    private void writeBody(HttpResponse response, OutputStream out) throws IOException {
        if (response.getContentLength() <= 0) return;
        out.write(response.getBodyInBytes());
    }

    private void writeStatus(HttpStatus status, BufferedWriter out) throws IOException {
        out.write("HTTP/1.0 " + status.getCode() + " " + status.getMsg() + "\r\n");
    }

    private void writeHeaders(HttpResponse response, BufferedWriter out) throws IOException {
        out.write(DATE + DELIM + new Date() + "\r\n");
        out.write("Server: " + SERVER_VERSION + "\r\n");
        out.write(CONTENT_TYPE + DELIM + DEFAULT_MIME_TYPE + "\r\n");
        out.write(String.format(CONTENT_LENGTH + DELIM + "%d\r\n", response.getContentLength()));
        out.write("\r\n");
        out.flush();
    }
}
