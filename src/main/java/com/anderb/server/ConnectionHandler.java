package com.anderb.server;

import lombok.SneakyThrows;
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
    private Long idleDeadline;
    private long period = 5000;

    public ConnectionHandler(Socket socket, Handler requestHandler, BiConsumer<Exception, Socket> errorHandler) {
        Objects.requireNonNull(socket);
        Objects.requireNonNull(requestHandler);
        Objects.requireNonNull(errorHandler);

        this.socket = socket;
        this.requestHandler = requestHandler;
        this.errorHandler = errorHandler;
    }

    @SneakyThrows
    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                Handler.Status status = requestHandler.handle(socket);
                if (status == IDLE) {
                    if (idleDeadline != null && idleDeadline < System.currentTimeMillis()) {
                        log.info("Closing idle socket");
                        socket.close();
                        return;
                    } else if (idleDeadline == null) {
                        idleDeadline = System.currentTimeMillis() + period;
                    }
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                errorHandler.accept(e, socket);
            }
        }
        log.info("Connection was closed: {}", socket);
    }
}
