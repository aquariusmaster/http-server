package com.anderb.webserver.server;

import com.anderb.webserver.server.request.HttpRequest;
import com.anderb.webserver.server.request.HttpRequestParser;
import com.anderb.webserver.server.response.HttpResponse;
import com.anderb.webserver.server.response.HttpResponseWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

@Slf4j
public class HttpServer {
    private final HttpRequestParser requestParser;
    private final HttpResponseWriter responseWriter;
    private int port = 8080;

    private static volatile boolean finished = false;

    public HttpServer(HttpRequestParser requestParser, HttpResponseWriter responseWriter, Properties properties) {
        this.requestParser = requestParser;
        this.responseWriter = responseWriter;
        if (properties != null) {
            port = properties.get("port") != null ? (int) properties.get("port") : 8080;
        }
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(port)){
            log.info("Server is running");
            while (!finished) {
                try (Socket socket = server.accept()) {
                    HttpRequest httpRequest = requestParser.parseRequest(socket);
                    log.debug("Request: {}", httpRequest);
                    HttpResponse response = HttpResponse.builder().status(HttpStatus.OK).body("Success").build();
                    responseWriter.writeResponse(socket, response);
                } catch (Exception e) {
                    log.error("Error during working with socket", e);
                }
            }
        } catch (IOException e) {
            log.error("Server error", e);
        }
    }
}
