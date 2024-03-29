package com.anderb.server.http.request;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import static com.anderb.server.http.Constants.*;

@Slf4j
public class BaseHttpRequestParser implements HttpRequestParser {

    public HttpRequest parseRequest(Socket socket) {
        try {
            if (!socket.isConnected() && socket.isClosed()) {
                return null;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String firstLine = in.readLine();
            if (firstLine == null) {
                return null;
            }
            log.debug("Request line: {}", firstLine);
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.setMethod(parseMethod(firstLine));
            httpRequest.setPath(parsePath(firstLine));
            httpRequest.setHeaders(parseHeaders(in));
            if (httpRequest.getHeaders().get(CONTENT_LENGTH) != null) {
                int contentLength = Integer.parseInt(httpRequest.getHeaders().get(CONTENT_LENGTH));
                var body = parseContent(in, contentLength);
                log.debug("Body: {}", body);
                httpRequest.setRequestBody(body);
            }
            return httpRequest;
        } catch (HttpRequestParseException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            return null;
        } catch (Exception e) {
            throw new HttpRequestParseException(e);
        }
    }

    private String parseMethod(String input) {
        if (input == null || input.isEmpty()) {
            throw   new HttpRequestParseException("Cannot parse http method for input: '" + input + "'");
        }
        return input.substring(0, input.indexOf(SPACE));
    }

    private String parsePath(String input) {
        if (input == null || input.isEmpty()) {
            throw new HttpRequestParseException("Cannot parse http path");
        }
        int startIndex = input.indexOf(SPACE);
        return input.substring(startIndex + 1, input.indexOf(SPACE, startIndex + 1));
    }

    private Map<String, String> parseHeaders(BufferedReader in) throws IOException {
        HashMap<String, String> headers = new HashMap<>();
        String line;
        while((line = in.readLine()) != null && !line.isEmpty()) {
            log.debug("Header: {}", line);
            int index = line.indexOf(DELIM);
            headers.put(line.substring(0, index), line.substring(index + 2));
        }
        return headers;
    }

    private String parseContent(BufferedReader in, int contentLength) throws IOException {
        //TODO consider change buffer capacity for large content
        char[] buffer = new char[contentLength];
        int charsIn = in.read(buffer, 0, contentLength);
        return String.valueOf(buffer, 0, charsIn);
    }
}
