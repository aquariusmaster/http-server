package com.anderb.server.http;

import com.anderb.server.Handler;
import com.anderb.server.IOHelper;
import com.anderb.server.SocketTemplate;
import com.anderb.server.http.handler.*;
import com.anderb.server.http.request.BaseHttpRequestParser;
import com.anderb.server.http.request.HttpRequest;
import com.anderb.server.http.request.HttpRequestParseException;
import com.anderb.server.http.request.HttpRequestParser;
import com.anderb.server.http.response.BaseHttpResponseWriter;
import com.anderb.server.http.response.HttpResponse;
import com.anderb.server.http.response.HttpResponseWriteException;
import com.anderb.server.http.response.HttpResponseWriter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketException;
import java.util.LinkedList;
import java.util.concurrent.Executors;

@Slf4j
public class HttpServer {
    private final HttpRequestParser requestParser;
    private final HttpHandler httpHandler;
    private final HttpResponseWriter responseWriter;
    private final SocketTemplate socketTemplate;

    public HttpServer(HttpRequestParser requestParser,
                      HttpHandler httpHandler,
                      HttpResponseWriter responseWriter,
                      int port,
                      int threadsNumber) {

        this.requestParser = requestParser;
        this.httpHandler = httpHandler;
        this.responseWriter = responseWriter;

        socketTemplate = buildServer(port, threadsNumber);
    }

    private SocketTemplate buildServer(int port, int threadsNumber) {
        return SocketTemplate.builder()
                .port(port)
                .requestHandler(socket -> {
                    try {
                        socket.setKeepAlive(true);

                        //Parsing http request
                        HttpRequest request = requestParser.parseRequest(socket);
                        log.debug("Http request: {}", request);
                        if (request == null) {
                            return Handler.Status.IDLE;
                        }

                        //Processing
                        HttpResponse response = new HttpResponse();
                        httpHandler.handle(request, response);

                        //Send response
                        responseWriter.writeResponse(socket, response);

                        log.info("Connection: {}", request.getHeader("Connection"));
                        log.info("Socket keep-alive: {}", socket.getKeepAlive());

                        if (!socket.getKeepAlive() &&
                                request.getHeader("Connection") == null &&
                                !"keep-alive".equalsIgnoreCase(request.getHeader("Connection"))) {
                            IOHelper.closeQuietly(socket);
                        }

                    } catch (HttpRequestParseException e) {
                        log.error("Error parsing the request", e);
                        responseWriter.writeResponse(socket, HttpResponse.badRequest(e.getMessage()));
                    } catch (HttpHandleException e) {
                        log.error("Error handling the request", e);
                        responseWriter.writeResponse(socket, HttpResponse.serverError(e.getMessage()));
                    } catch (HttpResponseWriteException e) {
                        log.error("Error writing the response", e);
                        responseWriter.writeResponse(socket, HttpResponse.serverError(e.getMessage()));
                    } catch (SocketException e) {
                        log.error("Error working with socket", e);
                    }
                    return Handler.Status.FINISHED;
                })
                .errorHandler((e, socket) -> {
                    if (!socket.isClosed()) {
                        responseWriter.writeResponse(socket, HttpResponse.serverError(e.getMessage()));
                    }
                }
                )
                .pool(Executors.newFixedThreadPool(threadsNumber))
                .build();
    }

    public void run() {
        socketTemplate.run();
    }

    public void stop() {
        socketTemplate.stop();
    }

    public static HttpServerBuilder create() {
        return new HttpServerBuilder();
    }

    public static class HttpServerBuilder {
        private HttpRequestParser parser;
        private LinkedList<HttpHandler> handlers = new LinkedList<>();
        private LinkedList<Endpoint> endpoints = new LinkedList<>();
        private HttpResponseWriter writer;
        private Integer port;
        private Integer threadsNumber;

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

        public HttpServerBuilder port(int port) {
            this.port = port;
            return this;
        }

        public HttpServerBuilder threadsNumber(int threadsNumber) {
            this.threadsNumber = threadsNumber;
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

            endpoints.forEach(endpoint -> log.info("Registering {}", endpoint));
            if (port == null) {
                port = 8080;
            }
            if (threadsNumber == null) {
                threadsNumber = 1;
            }
            return new HttpServer(parser, HttpHandler.of(handlers), writer, port, threadsNumber);
        }

    }

}
