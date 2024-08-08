package com.neohttp.server;

import java.util.Map;
import java.util.HashMap;
//import java.util.stream.Collectors;

public class HttpResponse {
    private int statusCode;
    private String statusMessage;
    private Map<String, String> headers;
    private String body;

    // Constructors
    public HttpResponse() {
        this(200, "OK", new HashMap<>(), "");
    }

    public HttpResponse(int statusCode, String statusMessage, Map<String, String> headers, String body) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.headers = headers;
        this.body = body;
    }

    // Getters
    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    // Setters
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    // Utility methods
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    @Override
    public String toString() {
        try {
            StringBuilder response = new StringBuilder();
            response.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");
            
            // Add Content-Length header if not present
            if (!headers.containsKey("Content-Length")) {
                headers.put("Content-Length", String.valueOf(body != null ? body.length() : 0));
            }
            
            // Add headers
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    response.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
                }
            }
            
            // Add an empty line to separate headers from body
            response.append("\r\n");
            
            // Add body
            if (body != null) {
                response.append(body);
            }
            
            return response.toString();
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error generating HTTP response: " + e.getMessage());
            // Return a basic error response
            return "HTTP/1.1 500 Internal Server Error\r\n\r\nError generating response";
        }
    }

    public static HttpResponse ok(String body) {
        return new HttpResponse(200, "OK", new HashMap<>(), body);
    }

    public static HttpResponse error(int statusCode, String statusMessage, String errorMessage) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        String body = statusCode + " " + statusMessage + "\n" + errorMessage;
        return new HttpResponse(statusCode, statusMessage, headers, body);
    }

    public static HttpResponse badRequest(String errorMessage) {
        return error(400, "Bad Request", errorMessage);
    }

    public static HttpResponse notFound(String errorMessage) {
        return error(404, "Not Found", errorMessage);
    }

    public static HttpResponse methodNotAllowed(String errorMessage) {
        return error(405, "Method Not Allowed", errorMessage);
    }

    public static HttpResponse internalServerError(String errorMessage) {
        return error(500, "Internal Server Error", errorMessage);
    }

    public static HttpResponse fromException(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return badRequest(e.getMessage());
        } else if (e instanceof UnsupportedOperationException) {
            return methodNotAllowed(e.getMessage());
        } else {
            return internalServerError(e.getMessage());
        }
    }
}