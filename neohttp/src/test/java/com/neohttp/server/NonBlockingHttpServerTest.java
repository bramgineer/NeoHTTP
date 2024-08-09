package com.neohttp.server;

import com.neohttp.config.ServerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.logging.Logger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class NonBlockingHttpServerTest {

    private NonBlockingHttpServer server;
    private ServerConfig config;
    @Mock
    private Router router;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = new ServerConfig();
        config.setPort(8080);
        server = new NonBlockingHttpServer(config, router);
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void tearDown() throws Exception {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        server.close();
    }

    @Test
    void testServerStart() throws Exception {
        executor.submit(() -> {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(100); // Give server time to start

        try (SocketChannel client = SocketChannel.open()) {
            assertTrue(client.connect(new InetSocketAddress("localhost", config.getPort())));
        }
    }

    @Test
void testAccept() throws IOException {
    // Mocks
        ServerConfig mockConfig = mock(ServerConfig.class);
        Router mockRouter = mock(Router.class);
        Logger mockLogger = mock(Logger.class);
        NonBlockingHttpServer server = new NonBlockingHttpServer(mockConfig, mockRouter);
        server.setLogger(mockLogger); // Inject the mock logger

        SelectionKey mockKey = mock(SelectionKey.class);
        ServerSocketChannel mockServerChannel = mock(ServerSocketChannel.class);
        SocketChannel mockClientChannel = mock(SocketChannel.class);
        Selector mockSelector = mock(Selector.class);

        // Setup
        when(mockKey.channel()).thenReturn(mockServerChannel);
        when(mockServerChannel.accept()).thenReturn(mockClientChannel);
        when(mockClientChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 12345));
        server.selector = mockSelector;

        // Execute
        server.accept(mockKey);

        // Verify
        verify(mockClientChannel).configureBlocking(false);
        verify(mockClientChannel).register(mockSelector, SelectionKey.OP_READ);
        verify(mockLogger).info(contains("New connection accepted from:"));
    }

    @Test
    void testRead() throws Exception {
        
    }

    @Test
    void testWrite() throws Exception {
        SocketChannel clientChannel = mock(SocketChannel.class);
        Connection connection = spy(new Connection());
        server.connections.put(clientChannel, connection);
        SelectionKey key = mock(SelectionKey.class);

        when(key.channel()).thenReturn(clientChannel);
        when(connection.isResponseComplete()).thenReturn(true);
        when(connection.isKeepAlive()).thenReturn(true);

        server.write(key);

        verify(connection).write(clientChannel);
        verify(key).interestOps(SelectionKey.OP_READ);
        verify(connection).reset();
    }
}