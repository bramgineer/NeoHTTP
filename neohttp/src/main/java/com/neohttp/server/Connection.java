package com.neohttp.server;

import com.neohttp.util.HttpParser;
import com.neohttp.handler.RequestHandler;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Connection {
    private static final int BUFFER_SIZE = 4096;
    private ByteBuffer requestBuffer;
    private ByteBuffer responseBuffer;
    private HttpRequest request;
    private HttpResponse response;
    private boolean keepAlive;
    private ConnectionState state;

    private enum ConnectionState {
        READING, PROCESSING, WRITING, CLOSED
    }

    private Exception lastException;

    public Connection() {
        /*
         * Initialize the connection with a request buffer of size BUFFER_SIZE.
         */
        try {
            this.requestBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            this.keepAlive = false;
            this.state = ConnectionState.READING;
        } catch (IllegalArgumentException e) {
            // Handle the case where BUFFER_SIZE is negative
            this.lastException = new RuntimeException("Failed to allocate request buffer", e);
            this.state = ConnectionState.CLOSED;
        } catch (OutOfMemoryError e) {
            // Handle the case where there's not enough memory to allocate the buffer
            this.lastException = new RuntimeException("Insufficient memory to allocate request buffer", e);
            this.state = ConnectionState.CLOSED;
        }
    }

    public void read(SocketChannel channel) throws IOException {
        /*
         * Read the request from the socket channel into the request buffer.
         */
        try {
            int bytesRead = channel.read(requestBuffer);
            if (bytesRead == -1) {
                state = ConnectionState.CLOSED;
                return;
            }
            if (isRequestComplete()) {
                state = ConnectionState.PROCESSING;
            }
        } catch (IOException e) {
            handleException(e);
        }
    }

    public boolean isRequestComplete() {
        /*
         * Check if the request is complete by checking if the request buffer contains the CRLF CRLF sequence.
         */
        String requestStr = new String(requestBuffer.array(), 0, requestBuffer.position(), StandardCharsets.UTF_8);
        return requestStr.contains("\r\n\r\n");
    }

    public void processRequest(RequestHandler handler) {
        /*
         * Parse the request from the request buffer into a HttpRequest object.
         */
        try {
            requestBuffer.flip();
            request = HttpParser.parseRequest(requestBuffer);
            keepAlive = HttpParser.isKeepAlive(request);
            response = handler.handle(request);
            state = ConnectionState.WRITING;
            prepareResponse();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void prepareResponse() {
        /*
         * Convert the response to a string and wrap it in a ByteBuffer.
         */
        String responseStr = response.toString();
        responseBuffer = ByteBuffer.wrap(responseStr.getBytes(StandardCharsets.UTF_8));
    }

    public void write(SocketChannel channel) throws IOException {
        /*
         * Write the response to the socket channel.
         */
        try {
            channel.write(responseBuffer);
            if (!responseBuffer.hasRemaining()) {
                if (keepAlive) {
                    reset();
                } else {
                    state = ConnectionState.CLOSED;
                }
            }
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        lastException = e;
        state = ConnectionState.CLOSED;
        // Log the exception or perform any other necessary error handling
    }

    public Exception getLastException() {
        return lastException;
    }
    
    public void reset() {
        /*
         * Reset the connection state.
         */
        try {
            requestBuffer.clear();
            responseBuffer = null;
            request = null;
            response = null;
            state = ConnectionState.READING;
        } catch (Exception e) {
            handleException(e);
            // If an exception occurs during reset, we should ensure the connection is closed
            state = ConnectionState.CLOSED;
        }
    }

    public boolean isKeepAlive() {
        /*
         * Check if the connection is keep-alive.
         */
        return keepAlive;
    }

    public ConnectionState getState() {
        /*
         * Get the current connection state.
         */
        return state;
    }

    public boolean isReadyToWrite() {
        /*
         * Check if the connection is ready to write by checking if the connection state is WRITING and the response buffer is not null.
         */
        return state == ConnectionState.WRITING && responseBuffer != null;
    }

    public boolean isClosed() {
        /*
         * Check if the connection is closed by checking if the connection state is CLOSED.
         */
        return state == ConnectionState.CLOSED;
    }
}