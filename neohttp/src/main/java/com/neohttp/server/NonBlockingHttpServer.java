package com.neohttp.server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

public class NonBlockingHttpServer {
    private static final int PORT = 8080;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private ConcurrentHashMap<SocketChannel, Connection> connections = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger(NonBlockingHttpServer.class.getName());

    public void start() throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(PORT));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Add shutdown hook for clean resource management
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        logger.info("Server started on port " + PORT);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;

                    try {
                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isReadable()) {
                            read(key);
                        } else if (key.isWritable()) {
                            write(key);
                        }
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Error handling channel operation", e);
                        handleChannelError(key);
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error in main server loop", e);
            }
        }
    }

    public void close() throws IOException {
        logger.info("Closing the server...");
        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
                logger.info("Selector closed successfully");
            }
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
                logger.info("Server channel closed successfully");
            }
            for (Connection conn : connections.values()) {
                conn.close();
            }
            connections.clear();
            logger.info("All connections closed");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while closing the server", e);
            throw e; // Re-throw the exception to inform the caller about the failure
        }
        logger.info("Server closed successfully");
    }

    private void shutdown() {
        try {
            selector.close();
            serverChannel.close();
            for (Connection conn : connections.values()) {
                conn.close();
            }
            logger.info("Server shut down gracefully");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error during server shutdown", e);
        }
    }

    private void handleChannelError(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        Connection conn = connections.remove(channel);
        if (conn != null) {
            conn.close();
        }
        key.cancel();
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        connections.put(clientChannel, new Connection());
        logger.info("New connection accepted: " + clientChannel.getRemoteAddress());
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Connection conn = connections.get(clientChannel);
        try {
            if (conn.read(clientChannel) == -1) {
                // Client closed connection
                logger.info("Client closed connection: " + clientChannel.getRemoteAddress());
                handleChannelError(key);
                return;
            }

            if (conn.isRequestComplete()) {
                conn.prepareResponse();
                key.interestOps(SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading from channel", e);
            handleChannelError(key);
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Connection conn = connections.get(clientChannel);
        try {
            conn.write(clientChannel);

            if (conn.isResponseComplete()) {
                if (conn.isKeepAlive()) {
                    key.interestOps(SelectionKey.OP_READ);
                    conn.reset();
                } else {
                    logger.info("Closing connection after response: " + clientChannel.getRemoteAddress());
                    handleChannelError(key);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error writing to channel", e);
            handleChannelError(key);
        }
    }

    private class Connection {
        private ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
        private ByteBuffer responseBuffer;
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
            return requestBuffer.position() > 0 && new String(requestBuffer.array(), 0, requestBuffer.position()).contains("\r\n\r\n");
        }

        void prepareResponse() {
            requestBuffer.flip();
            String request = new String(requestBuffer.array(), 0, requestBuffer.limit()).trim();
            String response;
            if (request.startsWith("GET")) {
                response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<html><body><h1>Hello, World!</h1></body></html>";
            } else {
                response = "HTTP/1.1 400 Bad Request\r\n\r\n";
            }
            responseBuffer = ByteBuffer.wrap(response.getBytes());
            
            keepAlive = request.toLowerCase().contains("connection: keep-alive");
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
}