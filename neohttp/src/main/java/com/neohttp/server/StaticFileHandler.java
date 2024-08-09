package com.neohttp.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler implements RequestHandler {
    private final String webRoot;

    public StaticFileHandler(String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String path = request.getPath();
        if ("/".equals(path)) {
            path = "/index.html";
        }

        Path filePath = Paths.get(webRoot, path);
        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            try {
                byte[] content = Files.readAllBytes(filePath);
                String contentType = getContentType(path);
                return new HttpResponse.Builder()
                    .setStatusCode(200)
                    .setStatusMessage("OK")
                    .setContentType(contentType)
                    .setBody(new String(content))
                    .build();
            } catch (IOException e) {
                return new HttpResponse.Builder()
                    .setStatusCode(500)
                    .setStatusMessage("Internal Server Error")
                    .setContentType("text/plain")
                    .setBody("Error reading file")
                    .build();
            }
        } else {
            return new HttpResponse.Builder()
                .setStatusCode(404)
                .setStatusMessage("Not Found")
                .setContentType("text/plain")
                .setBody("404 Not Found")
                .build();
        }
    }

    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        return "application/octet-stream";
    }
}