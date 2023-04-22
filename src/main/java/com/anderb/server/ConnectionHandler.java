package com.anderb.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.Socket;
import java.util.function.BiConsumer;

import static com.anderb.server.Handler.Status.IDLE;

@Slf4j
@RequiredArgsConstructor
public class ConnectionHandler implements Runnable {

    private final Socket socket;
    private final Handler requestHandler;
    private final BiConsumer<Exception, Socket> errorHandler;
    private final long keepAliveTime;
    private long idleDeadline = -1;

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                Handler.Status status = requestHandler.handle(socket);
                if (status != IDLE) continue;
                if (idleDeadline == -1) {
                    idleDeadline = System.nanoTime() / 1_000_000 + keepAliveTime;
                } else if (idleDeadline < System.nanoTime()) {
                    log.debug("Closing idle connection");
                    socket.close();
                    break;
                }
                Thread.sleep(10); //TODO probably must be a better way to wait for data from the socket
            } catch (Exception e) {
                errorHandler.accept(e, socket);
            }
        }
    }
}
