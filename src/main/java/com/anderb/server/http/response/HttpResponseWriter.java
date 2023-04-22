package com.anderb.server.http.response;

import java.net.Socket;

public interface HttpResponseWriter {
    void writeResponse(Socket socket, HttpResponse response);
}
