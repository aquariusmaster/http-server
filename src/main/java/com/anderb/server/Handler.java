package com.anderb.server;

import java.net.Socket;

public interface Handler {
    void handle(Socket socket);
}
