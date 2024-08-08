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

    public HttpRequest(String method, String path, String version, Map<String, String> headers, String body) {
        // Validate method
        if (method == null || method.trim().isEmpty()) {
            this.method = "";
        } else if (!isValidHttpMethod(method)) {
            this.method = "";
        } else {
            this.method = method.toUpperCase();
        }

        // Validate path
        if (path == null) {
            this.path = "";
        } else if (!isValidPath(path)) {
            this.path = "";
        } else {
            this.path = path;
        }

        // Validate version
        if (version == null) {
            this.version = "";
        } else if (!version.startsWith("HTTP/")) {
            this.version = "";
        } else {
            this.version = version;
        }

        // Validate and copy headers
        if (headers == null) {
            this.headers = Collections.emptyMap();
        } else {
            Map<String, String> validatedHeaders = new HashMap<>();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey() != null && !entry.getKey().trim().isEmpty() && entry.getValue() != null) {
                    validatedHeaders.put(entry.getKey(), entry.getValue());
                }
            }
            this.headers = Collections.unmodifiableMap(validatedHeaders);
        }

        // Body can be null for GET requests, so we don't validate it
        this.body = body;
    }

    private boolean isValidHttpMethod(String method) {
        // Add more HTTP methods as needed
        return Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH").contains(method.toUpperCase());
    }

    private boolean isValidPath(String path) {
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