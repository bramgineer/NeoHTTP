package com.neohttp.server;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HttpRequestHandlerTest {
    private HttpRequestHandler handler;
    private SocketChannel clientChannel;

    @Before
    public void setUp() throws Exception {
        clientChannel = mock(SocketChannel.class);
        handler = new HttpRequestHandler(clientChannel);
    }

    @Test
    public void testGetRequest() throws Exception {
        String request = "GET /index.html HTTP/1.1\r\n\r\n";
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(request.getBytes(StandardCharsets.UTF_8));
        buffer.flip(); // Prepare buffer for reading

        when(clientChannel.read(any(ByteBuffer.class))).thenAnswer(invocation -> {
            ByteBuffer buf = invocation.getArgument(0);
            buf.put(request.getBytes(StandardCharsets.UTF_8));
            return buf.position();
        });

        handler.run();

        String expectedResponse = "HTTP/1.1 200 OK\r\nContent-Length: 39\r\nContent-Type: text/html\r\n\r\n<html><body><h1>GET request received</h1></body></html>";

        // Capture the ByteBuffer argument
        ArgumentCaptor<ByteBuffer> captor = ArgumentCaptor.forClass(ByteBuffer.class);
        verify(clientChannel).write(captor.capture());

        ByteBuffer writtenBuffer = captor.getValue();
        writtenBuffer.flip(); // Prepare the buffer for reading

        byte[] writtenBytes = new byte[writtenBuffer.remaining()];
        writtenBuffer.get(writtenBytes);
        String writtenResponse = new String(writtenBytes, StandardCharsets.UTF_8);

        assertEquals(expectedResponse, writtenResponse);
    }
}