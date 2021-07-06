package com.anderb.server.http.response;

import com.anderb.server.IOHelper;
import com.anderb.server.http.Headers;
import com.anderb.server.http.HttpStatus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;

@Slf4j
public class BaseHttpResponseWriter implements HttpResponseWriter {
    private static final String SERVER_VERSION = "HttpServer/0.0.1";
    private static final String DEFAULT_MIME_TYPE = "text/html";
    private static final String DELIM = ": ";

    public void writeResponse(Socket socket, HttpResponse response) {
        Objects.requireNonNull(socket, "Socket cannot be null!");
        Objects.requireNonNull(response, "response cannot be null!");

        try {
            OutputStream outputStream = socket.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outputStream));

            writeStatusLine(response.getStatus(), out);
            writeHeaders(response, out);
            writeBody(response, outputStream);
            out.flush();
            outputStream.flush();
            log.info("Connection closed: {}", socket.isClosed());
        } catch (Exception e) {
            throw new HttpResponseWriteException(e);
        } finally {
            IOHelper.closeQuietly(
                    response.isUsingOutputStream() ?
                        response.getOutputStream() :
                        response.getWriter()
            );
        }
        log.info("Connection closed: {}", socket.isClosed());
    }

    private void writeBody(HttpResponse response, OutputStream out) throws IOException {
        if (response.getContentLength() <= 0) return;
        out.write(response.getBodyInBytes());
    }

    private void writeStatusLine(HttpStatus status, BufferedWriter out) throws IOException {
        out.write("HTTP/1.0 " + status.getCode() + " " + status.getMessage() + "\r\n");
    }

    private void writeHeaders(HttpResponse response, BufferedWriter out) throws IOException {
        writeHeader(Headers.DATE, new Date(), out);
        writeHeader("Server", SERVER_VERSION, out);
        writeHeader(Headers.CONTENT_LENGTH, response.getContentLength(), out);
        response.getHeaders().forEach(name -> writeHeader(name, response.getHeader(name), out));
        if (!response.getHeaders().contains(Headers.CONTENT_TYPE)) {
            writeHeader(Headers.CONTENT_TYPE,  DEFAULT_MIME_TYPE, out);
        }
        out.write("\r\n");
        out.flush();
    }

    @SneakyThrows
    private void writeHeader(String name, Object value, BufferedWriter out) {
        out.write(name + DELIM + value + "\r\n");
    }
}
