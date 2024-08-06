package com.neohttp.server;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class HttpRequestHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(HttpRequestHandler.class);
    private final SocketChannel clientChannel;

    public HttpRequestHandler(SocketChannel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            clientChannel.read(buffer);
            String request = new String(buffer.array(), StandardCharsets.UTF_8).trim();
            logger.info("Received request: {}", request);

            HttpResponse response = handleRequest(request);
            clientChannel.write(ByteBuffer.wrap(response.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            logger.error("Request handling error: ", e);
        } finally {
            try {
                if (clientChannel != null) {
                    clientChannel.close();
                }
            } catch (IOException e) {
                logger.error("Error closing client channel: ", e);
            }
        }
    }

    private HttpResponse handleRequest(String request) {
        HttpResponse response = new HttpResponse();
        if (request.startsWith("GET")) {
            response.setStatusCode(200);
            response.setBody("<html><body><h1>GET request received</h1></body></html>");
        } else if (request.startsWith("POST")) {
            response.setStatusCode(200);
            response.setBody("<html><body><h1>POST request received</h1></body></html>");
        } else if (request.startsWith("PUT")) {
            response.setStatusCode(200);
            response.setBody("<html><body><h1>PUT request received</h1></body></html>");
        } else if (request.startsWith("DELETE")) {
            response.setStatusCode(200);
            response.setBody("<html><body><h1>DELETE request received</h1></body></html>");
        } else {
            response.setStatusCode(400);
            response.setBody("<html><body><h1>Bad Request</h1></body></html>");
        }
        return response;
    }
}