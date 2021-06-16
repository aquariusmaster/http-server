package com.anderb.webserver.server.response;

import java.net.Socket;

public interface HttpResponseWriter {
    void writeResponse(Socket socket, HttpResponse response);
}
