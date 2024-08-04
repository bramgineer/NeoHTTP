package com.neohttp.server;

import com.neohttp.util.LoggerConfig;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private static final Logger logger = LogManager.getLogger(HttpServer.class);
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;
    private final ExecutorService threadPool;

    public HttpServer() {
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void start() {
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.bind(new InetSocketAddress(PORT));
            logger.info("Server started on port {}", PORT);

            while (true) {
                SocketChannel clientChannel = serverChannel.accept();
                threadPool.submit(new HttpRequestHandler(clientChannel));
            }
        } catch (IOException e) {
            logger.error("Server error: ", e);
        }
    }

    public static void main(String[] args) {
        LoggerConfig.configure();
        new HttpServer().start();
    }
}