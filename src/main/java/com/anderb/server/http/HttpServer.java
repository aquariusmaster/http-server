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

import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HttpServer {
    private final HttpRequestParser requestParser;
    private final HttpHandler httpHandler;
    private final HttpResponseWriter responseWriter;
    private final SocketTemplate socketTemplate;

    public HttpServer(HttpRequestParser requestParser,
                      HttpHandler httpHandler,
                      HttpResponseWriter responseWriter,
                      int port, int threadsNumber, long keepAliveTime) {

        this.requestParser = requestParser;
        this.httpHandler = httpHandler;
        this.responseWriter = responseWriter;

        socketTemplate = buildHttpServer(port, threadsNumber, keepAliveTime);
    }

    private SocketTemplate buildHttpServer(int port, int threadsNumber, long keepAliveTime) {
        return SocketTemplate.builder()
                .port(port)
                .keepAliveTime(keepAliveTime)
                .requestHandler(socket -> {
                    try {
                        socket.setSoTimeout(1000 * 60 * 5);

                        //Parsing http request
                        HttpRequest request = requestParser.parseRequest(socket);
                        if (request == null) {
                            return Handler.Status.IDLE;
                        }
                        log.info("new {}", request);

                        //Processing
                        HttpResponse response = new HttpResponse();
                        httpHandler.handle(request, response);

                        //Send response
                        responseWriter.writeResponse(socket, response);

                        if (ifNeedToClose(socket, request)) {
                            log.debug("closing socket {}", socket);
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
                .pool(createPool(threadsNumber, port))
                .build();
    }

    private ExecutorService createPool(int threadsNumber, int port) {
        return Executors.newFixedThreadPool(threadsNumber, new DefaultThreadFactory(port));
    }

    public void run() {
        socketTemplate.run();
    }

    public void stop() {
        socketTemplate.stop();
    }

    private boolean ifNeedToClose(Socket socket, HttpRequest request) throws SocketException {
        return "close".equalsIgnoreCase(request.getHeader("Connection"));
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
        private Long keepAliveTime;

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

        public HttpServerBuilder keepAliveTime(long keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
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
            handlers.addLast(new NotFoundHttpHandler());

            endpoints.forEach(endpoint -> log.info("Registering {}", endpoint));

            return new HttpServer(parser, HttpHandler.of(handlers), writer, port, threadsNumber, keepAliveTime);
        }

    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(int port) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "http-" + port + "-exec-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}
