package com.neohttp.server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class NonBlockingHttpServer {
    private static final int PORT = 8080;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private ConcurrentHashMap<SocketChannel, Connection> connections = new ConcurrentHashMap<>();
   // private static final Logger logger = Logger.getLogger(NonBlockingHttpServer.class.getName());

    public void start() throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(PORT));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (!key.isValid()) continue;

                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        connections.put(clientChannel, new Connection());
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Connection conn = connections.get(clientChannel);
        conn.read(clientChannel);

        if (conn.isRequestComplete()) {
            conn.prepareResponse();
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Connection conn = connections.get(clientChannel);
        conn.write(clientChannel);

        if (conn.isResponseComplete()) {
            if (conn.isKeepAlive()) {
                key.interestOps(SelectionKey.OP_READ);
                conn.reset();
            } else {
                connections.remove(clientChannel);
                clientChannel.close();
                key.cancel();
            }
        }
    }

    private class Connection {
        private ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
        private ByteBuffer responseBuffer;
        private boolean keepAlive = false;

        void read(SocketChannel channel) throws IOException {
            channel.read(requestBuffer);
        }

        boolean isRequestComplete() {
            // Implement logic to check if the full request has been received
            return requestBuffer.position() > 0 && new String(requestBuffer.array()).contains("\r\n\r\n");
        }

        void prepareResponse() {
            // Parse request and prepare response
            String request = new String(requestBuffer.array()).trim();
            String response;
            if (request.startsWith("GET")) {
                response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<html><body><h1>Hello, World!</h1></body></html>";
            } else {
                response = "HTTP/1.1 400 Bad Request\r\n\r\n";
            }
            responseBuffer = ByteBuffer.wrap(response.getBytes());
            
            // Check for keep-alive
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
    }
}