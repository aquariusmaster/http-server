package com.anderb.webserver.server;

import com.anderb.webserver.server.handler.EmptyRequestHandler;
import com.anderb.webserver.server.handler.Endpoint;
import com.anderb.webserver.server.handler.HttpHandler;
import com.anderb.webserver.server.handler.NotFoundHttpHandler;
import com.anderb.webserver.server.request.BaseHttpRequestParser;
import com.anderb.webserver.server.request.HttpRequest;
import com.anderb.webserver.server.request.HttpRequestParseException;
import com.anderb.webserver.server.request.HttpRequestParser;
import com.anderb.webserver.server.response.BaseHttpResponseWriter;
import com.anderb.webserver.server.response.HttpResponse;
import com.anderb.webserver.server.response.HttpResponseWriteException;
import com.anderb.webserver.server.response.HttpResponseWriter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Properties;

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
            port = (int) properties.getOrDefault("port", 8080);
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
                    HttpRequest request = requestParser.parseRequest(socket);
                    log.debug("Request: {}", request);

                    //Processing
                    HttpResponse response = new HttpResponse();
                    httpHandler.handle(request, response);

                    //Send response
                    responseWriter.writeResponse(socket, response);
                } catch (HttpRequestParseException e) {
                    log.error("Error parsing request", e);
                    responseWriter.writeResponse(socket, HttpResponse.badRequest(e.getMessage()));
                } catch (HttpResponseWriteException e) {
                    log.error("Error writing response", e);
                    responseWriter.writeResponse(socket, HttpResponse.serverError(e.getMessage()));
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

    public static HttpServerBuilder createDefault() {
        return new HttpServerBuilder();
    }

    public static class HttpServerBuilder {
        private HttpRequestParser parser;
        private LinkedList<HttpHandler> handlers = new LinkedList<>();
        private LinkedList<Endpoint> endpoints = new LinkedList<>();
        private HttpResponseWriter writer;

        HttpServerBuilder() {
        }

        public HttpServerBuilder requestParser(HttpRequestParser requestParser) {
            this.parser = requestParser;
            return this;
        }

        public HttpServerBuilder handler(HttpHandler httpHandler) {
            if (handlers == null) {
                handlers = new LinkedList<>();
            }
            handlers.add(httpHandler);
            return this;
        }

        public HttpServerBuilder endpoint(Endpoint endpoint) {
            if (endpoints == null) {
                endpoints = new LinkedList<>();
            }
            endpoints.add(endpoint);
            return this;
        }


        public HttpServerBuilder responseWriter(HttpResponseWriter responseWriter) {
            this.writer = responseWriter;
            return this;
        }

        public HttpServer build() {
            if (parser == null) {
                parser = new BaseHttpRequestParser();
            }
            if (writer == null) {
                writer = new BaseHttpResponseWriter();
            }
            handlers.addAll(endpoints);
            handlers.addFirst(new EmptyRequestHandler());
            handlers.addLast(new NotFoundHttpHandler());

            return new HttpServer(parser, HttpHandler.of(handlers), writer, null);
        }

    }

}
