package com.neohttp.server;

import java.util.Map;
import java.util.HashMap;
//import java.util.stream.Collectors;

public class HttpResponse {
    private int statusCode;
    private String statusMessage;
    private Map<String, String> headers;
    private String body;

    // Private constructor for use with Builder
    private HttpResponse(Builder builder) {
        this.statusCode = builder.statusCode;
        this.statusMessage = builder.statusMessage;
        this.headers = builder.headers;
        this.body = builder.body;
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
        return new Builder()
            .setStatusCode(200)
            .setStatusMessage("OK")
            .setBody(body)
            .build();
    }

    public static HttpResponse error(int statusCode, String statusMessage, String errorMessage) {
        return new Builder()
            .setStatusCode(statusCode)
            .setStatusMessage(statusMessage)
            .setContentType("text/plain")
            .setBody(statusCode + " " + statusMessage + "\n" + errorMessage)
            .build();
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

    public static class Builder {
        private int statusCode = 200;
        private String statusMessage = "OK";
        private Map<String, String> headers = new HashMap<>();
        private String body = "";

        public Builder setStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public Builder setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder setContentType(String contentType) {
            this.headers.put("Content-Type", contentType);
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(this);
        }
    }
}