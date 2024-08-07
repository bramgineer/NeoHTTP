package com.neohttp.handler;

import com.neohttp.server.HttpRequest;
import com.neohttp.server.HttpResponse;

public interface RequestHandler {
    HttpResponse handle(HttpRequest request);
}