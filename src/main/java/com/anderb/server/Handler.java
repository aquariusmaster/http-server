package com.anderb.server;

import java.net.Socket;

public interface Handler {
     Status handle(Socket socket);

     enum Status {
         FINISHED,
         IDLE
     }
}
