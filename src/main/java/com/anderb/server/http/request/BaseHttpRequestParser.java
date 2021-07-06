package com.anderb.server.http.request;

import com.anderb.server.http.Headers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class BaseHttpRequestParser implements HttpRequestParser {

    public HttpRequest parseRequest(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            HttpRequest httpRequest = new HttpRequest();
//            int first = in.read();
//            System.out.println("int=" + first + ", char=" + (char) first);
            String firstLine = in.readLine();
            if (firstLine == null) {
                return null;
            }
            httpRequest.setMethod(parseMethod(firstLine));
            httpRequest.setPath(parsePath(firstLine));
            httpRequest.setHeaders(parseHeaders(in));
            if (httpRequest.getHeaders().get(Headers.CONTENT_LENGTH) != null) {
                int contentLength = Integer.parseInt(httpRequest.getHeaders().get(Headers.CONTENT_LENGTH));
                httpRequest.setRequestBody(parseContent(in, contentLength));
            }
            return httpRequest;
        } catch (HttpRequestParseException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpRequestParseException(e);
        }
    }

    private String parseMethod(String input) {
        if (input == null || input.isEmpty()) {
            throw   new HttpRequestParseException("Cannot parse http method for input: '" + input + "'");
        }
        return input.substring(0, input.indexOf(Headers.SPACE));
    }

    private String parsePath(String input) {
        if (input == null || input.isEmpty()) {
            throw new HttpRequestParseException("Cannot parse http path");
        }
        int startIndex = input.indexOf(Headers.SPACE);
        return input.substring(startIndex + 1, input.indexOf(Headers.SPACE, startIndex + 1));
    }

    private Map<String, String> parseHeaders(BufferedReader in) throws IOException {
        HashMap<String, String> headers = new HashMap<>();
        String line;
        while((line = in.readLine()) != null && !line.isEmpty()) {
            int index = line.indexOf(Headers.DELIM);
            headers.put(line.substring(0, index), line.substring(index + 2, line.length()));
        }
        return headers;
    }

    private String parseContent(BufferedReader in, int contentLength) throws IOException {
        char[] buffer = new char[contentLength];
        int charsIn = in.read(buffer, 0, contentLength);
        return String.valueOf(buffer, 0, charsIn);
    }
}
