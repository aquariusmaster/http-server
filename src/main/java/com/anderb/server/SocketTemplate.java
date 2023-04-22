package com.anderb.server;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLServerSocketFactory;
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

    @Builder(builderMethodName = "create")
    public SocketTemplate(Handler requestHandler,
                          BiConsumer<Exception, Socket> errorHandler,
                          ExecutorService pool,
                          int port,
                          long keepAliveTime) {
        this.port = port;
        this.requestHandler = requestHandler;
        this.pool = pool;
        this.errorHandler = errorHandler;
        this.keepAliveTime = keepAliveTime;
        //Stop server gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void run() {
        try {
            server = createServerSocket(port);
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

    private ServerSocket createServerSocket(int port) throws IOException {
        var keyStore = System.getProperty("javax.net.ssl.keyStore");
        var keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
        if (keyStore == null || keyStore.isEmpty() || keyStorePassword == null || keyStorePassword.isEmpty()) {
            return new ServerSocket(port);
        }
        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        return sslServerSocketFactory.createServerSocket(port);
    }

}
