package com.anderb.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

@Slf4j
public class SocketTemplate {
    private final Handler requestHandler;
    private final BiConsumer<Exception, Socket> errorHandler;
    private final ExecutorService pool;
    private final int port;
    private final long keepAliveTime;

    private ServerSocket server;

    public SocketTemplate(Handler handler,
                          BiConsumer<Exception, Socket> errorHandler,
                          ExecutorService pool,
                          int port,
                          long keepAliveTime) {
        this.port = port;
        this.requestHandler = handler;
        this.pool = pool;
        this.errorHandler = errorHandler;
        this.keepAliveTime = keepAliveTime;
        //Stop server gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public static SocketTemplateBuilder builder() {
        return new SocketTemplateBuilder();
    }

    public void run() {
        try {
            server = new ServerSocket(port);
            log.info("Server is running on {}", server.getLocalSocketAddress());
            while (!server.isClosed()) {
                try {
                    Socket socket = server.accept();
                    log.debug("Connection established: {}", socket);
                    pool.execute(new ConnectionHandler(socket, requestHandler, errorHandler, keepAliveTime));
                } catch (Exception e) {
                    if (!server.isClosed()) {
                        log.error("Error during working with socket", e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Server error", e);
        } finally {
            IOHelper.closeQuietly(server);
        }
        log.info("Server is stopped");
    }

    @SneakyThrows
    public void stop() {
        log.info("Stop server signal received");
        if (server != null) {
            pool.shutdown();
            server.close();
        }
    }

    public static class SocketTemplateBuilder {
        private Handler requestHandler;
        private BiConsumer<Exception, Socket> errorHandler;
        private ExecutorService pool;
        private Integer port;
        private Long keepAliveTime;
        private ServerSocket server;

        SocketTemplateBuilder() {
        }

        public SocketTemplateBuilder requestHandler(Handler requestHandler) {
            this.requestHandler = requestHandler;
            return this;
        }

        public SocketTemplateBuilder errorHandler(BiConsumer<Exception, Socket> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public SocketTemplateBuilder pool(ExecutorService pool) {
            this.pool = pool;
            return this;
        }

        public SocketTemplateBuilder port(int port) {
            this.port = port;
            return this;
        }

        public SocketTemplateBuilder keepAliveTime(long idleTtlMillis) {
            this.keepAliveTime = idleTtlMillis;
            return this;
        }

        public SocketTemplateBuilder server(ServerSocket server) {
            this.server = server;
            return this;
        }

        public SocketTemplate build() {
            return new SocketTemplate(requestHandler, errorHandler, pool, port, keepAliveTime);
        }

        public String toString() {
            return "SocketTemplate.SocketTemplateBuilder(requestHandler=" + this.requestHandler + ", errorHandler=" + this.errorHandler + ", pool=" + this.pool + ", port=" + this.port + ", server=" + this.server + ")";
        }
    }
}

