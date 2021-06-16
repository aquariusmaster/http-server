package com.anderb.webserver.server.request;

import java.net.Socket;

public interface HttpRequestParser {
    HttpRequest parseRequest(Socket socket);
}
