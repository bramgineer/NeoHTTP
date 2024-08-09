package com.neohttp.server;

public interface RequestHandler {
    HttpResponse handle(HttpRequest request);
}
