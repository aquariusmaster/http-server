package com.anderb.server.http.response;

import com.anderb.server.IOHelper;
import com.anderb.server.http.HttpStatus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;

import static com.anderb.server.http.Constants.*;

@Slf4j
public class BaseHttpResponseWriter implements HttpResponseWriter {

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
        } catch (Exception e) {
            throw new HttpResponseWriteException(e);
        } finally {
            IOHelper.closeQuietly(
                    response.isUsingOutputStream() ?
                        response.getOutputStream() :
                        response.getWriter()
            );
        }
    }

    private void writeBody(HttpResponse response, OutputStream out) throws IOException {
        if (response.getContentLength() <= 0) return;
        out.write(response.getBodyInBytes());
    }

    private void writeStatusLine(HttpStatus status, BufferedWriter out) throws IOException {
        out.write("HTTP/1.1 " + status.getCode() + " " + status.getMessage() + END_LINE);
    }

    private void writeHeaders(HttpResponse response, BufferedWriter out) throws IOException {
        writeHeader(DATE, new Date(), out);
        writeHeader(SERVER, SERVER_VERSION, out);
        writeHeader(CONTENT_LENGTH, response.getContentLength(), out);
        response.getHeaders().forEach(name -> writeHeader(name, response.getHeader(name), out));
        if (!response.getHeaders().contains(CONTENT_TYPE)) {
            writeHeader(CONTENT_TYPE, DEFAULT_MIME_TYPE, out);
        }
        out.write(END_LINE);
        out.flush();
    }

    @SneakyThrows
    private void writeHeader(String name, Object value, BufferedWriter out) {
        out.write(name + DELIM + value + END_LINE);
    }
}
