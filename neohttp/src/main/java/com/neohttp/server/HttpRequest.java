package com.neohttp.server;

import java.util.Map;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final String body;

    private HttpRequest(String method, String path, String version, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest parse(String requestString) throws IllegalArgumentException {
        try {
            String[] lines = requestString.split("\r\n");
            String[] requestLine = lines[0].split(" ");
            if (requestLine.length != 3) {
                throw new IllegalArgumentException("Invalid request line");
            }

            String method = validateMethod(requestLine[0]);
            String path = validatePath(requestLine[1]);
            String version = validateVersion(requestLine[2]);

            Map<String, String> headers = new HashMap<>();
            int i;
            for (i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.isEmpty()) break;
                String[] parts = line.split(": ", 2);
                if (parts.length == 2) {
                    headers.put(parts[0], parts[1]);
                }
            }

            StringBuilder bodyBuilder = new StringBuilder();
            for (i++; i < lines.length; i++) {
                bodyBuilder.append(lines[i]).append("\r\n");
            }
            String body = bodyBuilder.toString().trim();

            return new HttpRequest(method, path, version, Collections.unmodifiableMap(headers), body);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing HTTP request: " + e.getMessage(), e);
        }
    }

    private static String validateMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("Method cannot be null or empty");
        }
        String upperMethod = method.toUpperCase();
        if (!isValidHttpMethod(upperMethod)) {
            throw new IllegalArgumentException("Invalid HTTP method: " + method);
        }
        return upperMethod;
    }

    private static String validatePath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (!isValidPath(path)) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        return path;
    }

    private static String validateVersion(String version) {
        if (version == null) {
            throw new IllegalArgumentException("Version cannot be null");
        }
        if (!version.startsWith("HTTP/")) {
            throw new IllegalArgumentException("Invalid HTTP version: " + version);
        }
        return version;
    }

    private static boolean isValidHttpMethod(String method) {
        // Add more HTTP methods as needed
        return Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH").contains(method);
    }

    private static boolean isValidPath(String path) {
        // Basic path validation, can be extended for more specific requirements
        return path.startsWith("/") && !path.contains("..") && !path.contains("//");
    }

    // Getters
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public String getVersion() { return version; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }

    // Utility method to check if a specific header exists
    public boolean hasHeader(String headerName) {
        return headers.containsKey(headerName);
    }

    // Utility method to get a header value
    public String getHeader(String headerName) {
        return headers.get(headerName);
    }
}