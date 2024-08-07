package com.neohttp.util;

import com.neohttp.server.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpParser {
    public static HttpRequest parseRequest(ByteBuffer buffer) {
        String rawRequest = StandardCharsets.UTF_8.decode(buffer).toString();
        String[] lines = rawRequest.split("\r\n");
        
        if (lines.length == 0) {
            throw new IllegalArgumentException("Empty request");
        }

        // Parse request line
        String[] requestLine = lines[0].split(" ");
        if (requestLine.length != 3) {
            throw new IllegalArgumentException("Invalid request line");
        }

        String method = requestLine[0];
        String path = requestLine[1];
        String version = requestLine[2];

        // Parse headers
        Map<String, String> headers = new HashMap<>();
        int i = 1;
        for (; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                break; // End of headers
            }
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0].toLowerCase(), headerParts[1]);
            }
        }

        // Parse body
        StringBuilder body = new StringBuilder();
        for (i++; i < lines.length; i++) {
            body.append(lines[i]).append("\r\n");
        }

        return new HttpRequest(method, path, version, headers, body.toString().trim());
    }

    public static String getHeaderValue(Map<String, String> headers, String key) {
        return headers.get(key.toLowerCase());
    }

    public static boolean isKeepAlive(HttpRequest request) {
        String connection = getHeaderValue(request.getHeaders(), "Connection");
        if (connection != null && connection.equalsIgnoreCase("keep-alive")) {
            return true;
        }
        return "HTTP/1.1".equals(request.getVersion()) && 
               (connection == null || !connection.equalsIgnoreCase("close"));
    }

    public static int getContentLength(HttpRequest request) {
        String contentLength = getHeaderValue(request.getHeaders(), "Content-Length");
        return contentLength != null ? Integer.parseInt(contentLength) : 0;
    }
}