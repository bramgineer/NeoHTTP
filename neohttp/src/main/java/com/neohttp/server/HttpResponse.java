package com.neohttp.server;

public class HttpResponse {
    private int statusCode;
    private String body;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "HTTP/1.1 " + statusCode + " OK\r\n" +
               "Content-Length: " + body.length() + "\r\n" +
               "Content-Type: text/html\r\n\r\n" + body;
    }
}