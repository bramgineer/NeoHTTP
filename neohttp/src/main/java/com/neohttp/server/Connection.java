package com.neohttp.server;

import com.neohttp.util.HttpParser;
import com.neohttp.handler.RequestHandler;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Connection {
    private static final Logger logger = Logger.getLogger(Connection.class.getName());
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
            logger.fine("Connection initialized with buffer size: " + BUFFER_SIZE);
        } catch (IllegalArgumentException e) {
            // Handle the case where BUFFER_SIZE is negative
            this.lastException = new RuntimeException("Failed to allocate request buffer", e);
            this.state = ConnectionState.CLOSED;
            logger.log(Level.SEVERE, "Failed to allocate request buffer", e);
        } catch (OutOfMemoryError e) {
            // Handle the case where there's not enough memory to allocate the buffer
            this.lastException = new RuntimeException("Insufficient memory to allocate request buffer", e);
            this.state = ConnectionState.CLOSED;
            logger.log(Level.SEVERE, "Insufficient memory to allocate request buffer", e);
        }
    }

    public void read(SocketChannel channel) throws IOException {
        /*
         * Read the request from the socket channel into the request buffer.
         */
        try {
            int bytesRead = channel.read(requestBuffer);
            logger.fine("Read " + bytesRead + " bytes from channel");
            if (bytesRead == -1) {
                state = ConnectionState.CLOSED;
                logger.info("End of stream reached, closing connection");
                return;
            }
            if (isRequestComplete()) {
                state = ConnectionState.PROCESSING;
                logger.fine("Request complete, moving to PROCESSING state");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading from channel", e);
            handleException(e);
        }
    }

    public boolean isRequestComplete() {
        /*
         * Check if the request is complete by checking if the request buffer contains the CRLF CRLF sequence.
         */
        String requestStr = new String(requestBuffer.array(), 0, requestBuffer.position(), StandardCharsets.UTF_8);
        boolean isComplete = requestStr.contains("\r\n\r\n");
        logger.fine("Request complete: " + isComplete);
        return isComplete;
    }

    public void processRequest(RequestHandler handler) {
        /*
         * Parse the request from the request buffer into a HttpRequest object.
         */
        try {
            requestBuffer.flip();
            request = HttpParser.parseRequest(requestBuffer);
            keepAlive = HttpParser.isKeepAlive(request);
            logger.fine("Request parsed, keep-alive: " + keepAlive);
            response = handler.handle(request);
            state = ConnectionState.WRITING;
            prepareResponse();
            logger.fine("Response prepared, moving to WRITING state");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing request", e);
            handleException(e);
        }
    }

    private void prepareResponse() {
        /*
         * Convert the response to a string and wrap it in a ByteBuffer.
         */
        String responseStr = response.toString();
        responseBuffer = ByteBuffer.wrap(responseStr.getBytes(StandardCharsets.UTF_8));
        logger.fine("Response prepared with " + responseBuffer.remaining() + " bytes");
    }

    public void write(SocketChannel channel) throws IOException {
        /*
         * Write the response to the socket channel.
         */
        try {
            int bytesWritten = channel.write(responseBuffer);
            logger.fine("Wrote " + bytesWritten + " bytes to channel");
            if (!responseBuffer.hasRemaining()) {
                if (keepAlive) {
                    reset();
                    logger.fine("Keep-alive connection reset");
                } else {
                    state = ConnectionState.CLOSED;
                    logger.info("Response fully written, closing connection");
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing to channel", e);
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        lastException = e;
        state = ConnectionState.CLOSED;
        logger.log(Level.SEVERE, "Connection closed due to exception", e);
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
            logger.fine("Connection reset, ready for next request");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error resetting connection", e);
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
        boolean ready = state == ConnectionState.WRITING && responseBuffer != null;
        logger.fine("Connection ready to write: " + ready);
        return ready;
    }

    public boolean isClosed() {
        /*
         * Check if the connection is closed by checking if the connection state is CLOSED.
         */
        return state == ConnectionState.CLOSED;
    }
}