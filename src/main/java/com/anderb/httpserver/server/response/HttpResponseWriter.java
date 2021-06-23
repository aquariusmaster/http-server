package com.anderb.httpserver.server.response;

import java.net.Socket;

public interface HttpResponseWriter {
    void writeResponse(Socket socket, HttpResponse response);
}
