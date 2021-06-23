package com.anderb.httpserver.server.request;

import java.net.Socket;

public interface HttpRequestParser {
    HttpRequest parseRequest(Socket socket);
}
