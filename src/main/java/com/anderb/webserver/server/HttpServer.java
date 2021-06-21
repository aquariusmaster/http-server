package com.anderb.webserver.server;

import com.anderb.webserver.server.handler.HttpHandler;
import com.anderb.webserver.server.handler.NotFoundHttpHandler;
import com.anderb.webserver.server.request.BasicHttpRequestParser;
import com.anderb.webserver.server.request.HttpRequest;
import com.anderb.webserver.server.request.HttpRequestParseException;
import com.anderb.webserver.server.request.HttpRequestParser;
import com.anderb.webserver.server.response.BasicHttpResponseWriter;
import com.anderb.webserver.server.response.HttpResponse;
import com.anderb.webserver.server.response.HttpResponseWriteException;
import com.anderb.webserver.server.response.HttpResponseWriter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class HttpServer {
    private final HttpRequestParser requestParser;
    private final HttpHandler httpHandler;
    private final HttpResponseWriter responseWriter;
    private int port = 8080;

    private static boolean finished = false;
    private ServerSocket server;

    public HttpServer(HttpRequestParser requestParser, HttpHandler httpHandler, HttpResponseWriter responseWriter, Properties properties) {
        this.requestParser = requestParser;
        this.httpHandler = httpHandler;
        this.responseWriter = responseWriter;
        if (properties != null) {
            port = properties.get("port") != null ? (int) properties.get("port") : 8080;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void run() {

        try {
            server = new ServerSocket(port);
            log.info("Server is running");
            while (!finished) {
                Socket socket = null;
                try {
                    socket = server.accept();
                    //Parsing
//                    log.info("@@@@@@@@@@@@@@@@");
//                    log.info("Socket: {}", socket);
//                    log.info("Inet: {}", socket.getInetAddress());
//                    log.info("Port: {}", socket.getPort());
//                    log.info("Buff size: {}", socket.getReceiveBufferSize());
//                    log.info("Opts: {}", socket.supportedOptions());
//                    log.info("Timeout: {}", socket.getSoTimeout());
//                    log.info("Remote: {}", socket.getRemoteSocketAddress());
//                    log.info("getTcpNoDelay: {}", socket.getTcpNoDelay());
//                    log.info("isConnected: {}", socket.isConnected());
//                    log.info("getLocalSocketAddress: {}", socket.getLocalSocketAddress());
//                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    log.info("Content: {}", bufferedReader.lines().collect(Collectors.toList()));
//                    log.info("@@@@@@@@@@@@@@@@");
                    HttpRequest request = requestParser.parseRequest(socket);
                    log.debug("Request: {}", request);
                    //Processing
                    HttpResponse response = new HttpResponse();
                    httpHandler.handle(request, response);

                    //Send response
                    responseWriter.writeResponse(socket, response);
                } catch (HttpRequestParseException e) {
                    log.error("Error parsing request", e);
                    HttpResponse response = new HttpResponse();
                    response.setStatus(HttpStatus.BAD_REQUEST);
                    response.getWriter().println(e.getMessage());
                    responseWriter.writeResponse(socket, response);
                } catch (HttpResponseWriteException e) {
                    log.error("Error writing response", e);
                } catch (Exception e) {
                    if (!finished) { //suppress exception during finishing
                        log.error("Error during working with socket", e);
                    }
                } finally {
                    closeSilently(socket);
                }
            }

        } catch (IOException e) {
            log.error("Server error", e);
        } finally {
            closeSilently(server);
        }
        log.info("Server was stopped");
    }

    @SneakyThrows
    public void stop() {
        log.info("Stopping server");
        finished = true;
        server.close();
    }

    private static void closeSilently(Closeable resourceToClose) {
        if (resourceToClose == null) return;
        try {
            resourceToClose.close();
        } catch (IOException e) {
            log.warn("Exception while closing server");
        }
    }

    public static HttpServerBuilder create() {
        return new HttpServerBuilder();
    }

    public static class HttpServerBuilder {
        private HttpRequestParser parser;
        private HttpHandler handler;
        private HttpResponseWriter writer;

        HttpServerBuilder() {
        }

        public HttpServerBuilder requestParser(HttpRequestParser requestParser) {
            this.parser = requestParser;
            return this;
        }

        public HttpServerBuilder handler(HttpHandler httpHandler) {
            if (handler == null) {
                handler = httpHandler;
                return this;
            }
            this.handler.nextHandler(httpHandler);
            handler = httpHandler;
            return this;
        }

        public HttpServerBuilder responseWriter(HttpResponseWriter responseWriter) {
            this.writer = responseWriter;
            return this;
        }

        public HttpServer build() {
            if (parser == null) {
                parser = new BasicHttpRequestParser();
            }
            if (writer == null) {
                writer = new BasicHttpResponseWriter();
            }
            if (handler != null) {
                handler.nextHandler(new NotFoundHttpHandler());
            } else {
                handler = new NotFoundHttpHandler();
            }

            return new HttpServer(parser, handler, writer, null);
        }

    }

}
