package com.neohttp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Router {
    private List<Route> routes = new ArrayList<>();
    private RequestHandler defaultHandler = new NotFoundHandler();
    public void addRoute(String path, RequestHandler handler) {
        routes.add(new Route(path, handler));
    }

    public RequestHandler getHandler(String path) {
        for (Route route : routes) {
            if (route.matches(path)) {
                return route.handler;
            }
        }
        return defaultHandler != null ? defaultHandler : new NotFoundHandler();
    }

    private static class Route {
        private final Pattern pattern;
        private final RequestHandler handler;

        public Route(String path, RequestHandler handler) {
            this.pattern = Pattern.compile("^" + path.replaceAll("\\{\\w+\\}", "([^/]+)") + "$");
            this.handler = handler;
        }

        public boolean matches(String path) {
            return pattern.matcher(path).matches();
        }
    }

    public void setDefaultHandler(RequestHandler handler) {
        this.defaultHandler = handler;
    }

    private static class NotFoundHandler implements RequestHandler {
        @Override
        public HttpResponse handle(HttpRequest request) {
            return new HttpResponse.Builder()
                .setStatusCode(404)
                .setStatusMessage("Not Found")
                .setContentType("text/plain")
                .setBody("404 Not Found")
                .build();
        }
    }
}