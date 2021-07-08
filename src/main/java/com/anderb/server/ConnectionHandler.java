package com.anderb.server;

import lombok.extern.slf4j.Slf4j;

import java.net.Socket;
import java.util.Objects;
import java.util.function.BiConsumer;

import static com.anderb.server.Handler.Status.IDLE;

@Slf4j
public class ConnectionHandler implements Runnable {

    private final Socket socket;
    private final Handler requestHandler;
    private final BiConsumer<Exception, Socket> errorHandler;
    private final long keepAliveTime;
    private long idleDeadline = -1;

    public ConnectionHandler(Socket socket, Handler requestHandler, BiConsumer<Exception, Socket> errorHandler, long keepAliveTime) {
        Objects.requireNonNull(socket);
        Objects.requireNonNull(requestHandler);
        Objects.requireNonNull(errorHandler);

        this.socket = socket;
        this.requestHandler = requestHandler;
        this.errorHandler = errorHandler;
        this.keepAliveTime = keepAliveTime;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                Handler.Status status = requestHandler.handle(socket);
                if (status != IDLE) continue;
                if (idleDeadline == -1) {
                    idleDeadline = System.currentTimeMillis() + keepAliveTime;
                } else if (idleDeadline < System.currentTimeMillis()) {
                    log.debug("Closing idle socket");
                    socket.close();
                }
                Thread.sleep(10); //TODO probably must be a better way to wait for data from the socket
            } catch (Exception e) {
                errorHandler.accept(e, socket);
            }
        }
    }
}
