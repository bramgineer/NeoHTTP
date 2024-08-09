package com.neohttp.server;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.io.IOException;

public class Connection {
    private ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer responseBuffer;
    //private StringBuilder requestBuilder = new StringBuilder();
    private boolean keepAlive = false;

    int read(SocketChannel channel) throws IOException {
        int totalBytesRead = 0;
        int bytesRead;
        do {
            bytesRead = channel.read(requestBuffer);
            if (bytesRead > 0) {
                totalBytesRead += bytesRead;
            }
        } while (bytesRead > 0);

        if (totalBytesRead == 0 && !requestBuffer.hasRemaining()) {
            // Buffer is full, resize it
            ByteBuffer newBuffer = ByteBuffer.allocate(requestBuffer.capacity() * 2);
            requestBuffer.flip();
            newBuffer.put(requestBuffer);
            requestBuffer = newBuffer;
        }

        return totalBytesRead;
    }

    boolean isRequestComplete() {
        requestBuffer.flip();
        String content = new String(requestBuffer.array(), 0, requestBuffer.limit());
        requestBuffer.compact();
        return content.contains("\r\n\r\n");
    }

    void prepareResponse(Router router) {
        requestBuffer.flip();
        String requestString = new String(requestBuffer.array(), 0, requestBuffer.limit());
        HttpRequest request = HttpRequest.parse(requestString.trim());
        RequestHandler handler = router.getHandler(request.getPath());
        HttpResponse response = handler.handle(request);
        responseBuffer = ByteBuffer.wrap(response.toString().getBytes());
        
        keepAlive = requestString.toLowerCase().contains("connection: keep-alive");
    }

    void write(SocketChannel channel) throws IOException {
        channel.write(responseBuffer);
    }

    boolean isResponseComplete() {
        return responseBuffer != null && !responseBuffer.hasRemaining();
    }

    boolean isKeepAlive() {
        return keepAlive;
    }
    private StringBuilder requestBuilder = new StringBuilder();

    public StringBuilder getRequestBuilder() {
        return requestBuilder;
    }
    
    void reset() {
        requestBuffer.clear();
        responseBuffer = null;
    }

    public void close() {
        try {
            if (requestBuffer != null) {
                requestBuffer.clear();
                requestBuffer = null;
            }
            if (responseBuffer != null) {
                responseBuffer.clear();
                responseBuffer = null;
            }
        } catch (Exception e) {
            // Log the exception or handle it as appropriate for your application
            System.err.println("Error while closing connection: " + e.getMessage());
        } finally {
            // Ensure all resources are nullified even if an exception occurs
            requestBuffer = null;
            responseBuffer = null;
        }
    }
}
