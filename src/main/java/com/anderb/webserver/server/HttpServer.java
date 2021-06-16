package com.anderb.webserver.server;

import lombok.extern.log4j.Log4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Log4j
public class HttpServer {
    private final static int PORT = 8080;
    private final static int MAX_CONN = 256;
    private static HttpRequestParser requestParser = new HttpRequestParser();
    private static volatile boolean finished = false;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT, MAX_CONN)){
            log.info("Server is running");
            while (!finished) {
                try (Socket socket = server.accept()) {
                    HttpRequest httpRequest = requestParser.parseRequest(socket);
                    log.info("Request: " + httpRequest);
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    out.write("HTTP/1.0 200 OK\r\n");
                    out.write("Date: Fri, 31 Dec 1999 23:59:59 GMT\r\n");
                    out.write("Server: Apache/0.8.4\r\n");
                    out.write("Content-Type: text/html\r\n");
                    out.write("Content-Length: 6\r\n");
                    out.write("Expires: Sat, 01 Jan 2000 00:59:59 GMT\r\n");
                    out.write("Last-modified: Fri, 09 Aug 1996 14:21:40 GMT\r\n");
                    out.write("\r\n");
                    out.write("Hello!");

                    out.flush();
                    out.close();
                } catch (Exception e) {
                    log.error("Error during working with socket", e);
                }
            }
        } catch (IOException e) {
            log.error("Server error", e);
        }
    }
}
