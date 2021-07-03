package com.anderb.server.http.request;

import java.net.Socket;

public interface HttpRequestParser {
    HttpRequest parseRequest(Socket socket);
}
