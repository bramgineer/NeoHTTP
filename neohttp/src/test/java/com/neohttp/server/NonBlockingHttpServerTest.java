package com.neohttp.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class NonBlockingHttpServerTest {

    private NonBlockingHttpServer server;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        server = new NonBlockingHttpServer();
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        TimeUnit.SECONDS.sleep(1);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        try {
            server.close(); // Assuming the method is named 'close' instead of 'stop'
        } catch (IOException e) {
            e.printStackTrace();
        }
        executorService.shutdownNow();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void testGetRequest() throws IOException {
        URL url = new URL("http://localhost:8080");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        
        int status = con.getResponseCode();
        assertEquals(200, status);
        
        String contentType = con.getHeaderField("Content-Type");
        assertEquals("text/html", contentType);
        
        con.disconnect();
    }

    @Test
    void testInvalidRequest() throws IOException {
        URL url = URI.create("http://localhost:8080").toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        
        int status = con.getResponseCode();
        assertEquals(400, status);
        
        con.disconnect();
    }
}