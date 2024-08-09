package com.neohttp.server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.neohttp.config.ServerConfig;

public class NonBlockingHttpServer {

    private final ServerConfig config;
    protected Selector selector;
    private ServerSocketChannel serverChannel;
    
    ConcurrentHashMap<SocketChannel, Connection> connections = new ConcurrentHashMap<>();
    private Logger logger = Logger.getLogger(NonBlockingHttpServer.class.getName());
    private final Router router;

    public NonBlockingHttpServer(ServerConfig config, Router router) {
        this.config = config;
        this.router = router;
        this.router.setDefaultHandler(new StaticFileHandler(config.getWebRoot()));
    }

    public void start() throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(config.getPort()));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Add shutdown hook for clean resource management
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        logger.info("Server started on port " + config.getPort());

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

    void setLogger(Logger logger) {
        this.logger = logger;
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

    public void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        connections.put(clientChannel, new Connection());
        logger.info("New connection accepted from: " + clientChannel.getRemoteAddress());
    }

    public void read(SelectionKey key) throws IOException {
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
                conn.prepareResponse(router);
                key.interestOps(SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading from channel", e);
            handleChannelError(key);
        }
    }

    public void write(SelectionKey key) throws IOException {
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

    
}