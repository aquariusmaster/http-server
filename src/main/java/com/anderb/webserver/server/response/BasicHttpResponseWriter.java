package com.anderb.webserver.server.response;

import com.anderb.webserver.server.HttpStatus;
import lombok.SneakyThrows;

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
        writeHeader(DATE, new Date(), out);
        writeHeader("Server", SERVER_VERSION, out);
        writeHeader(CONTENT_LENGTH, response.getContentLength(), out);
        response.getHeaders().forEach(name -> writeHeader(name, response.getHeader(name), out));
        if (!response.getHeaders().contains(CONTENT_TYPE)) {
            writeHeader(CONTENT_TYPE,  DEFAULT_MIME_TYPE, out);
        }
        out.write("\r\n");
        out.flush();
    }

    @SneakyThrows
    private void writeHeader(String name, Object value, BufferedWriter out) {
        out.write(name + DELIM + value + "\r\n");
    }
}
