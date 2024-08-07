package com.neohttp.server;

import java.util.Map;
import java.util.Collections;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final String body;

    public HttpRequest(String method, String path, String version, Map<String, String> headers, String body) {
        // Validate method
        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("HTTP method cannot be null or empty");
        }
        this.method = method.toUpperCase();

        // Validate path
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        this.path = path;

        // Validate version
        if (version == null || !version.startsWith("HTTP/")) {
            throw new IllegalArgumentException("Invalid HTTP version");
        }
        this.version = version;

        // Validate and copy headers
        this.headers = headers != null ? Collections.unmodifiableMap(headers) : Collections.emptyMap();

        // Body can be null for GET requests, so we don't validate it
        this.body = body;
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