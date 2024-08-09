package com.neohttp.server;

import com.neohttp.config.ServerConfig;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.hc.core5.http.ParseException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NonBlockingHttpServerFunctionalTest {

    private static NonBlockingHttpServer server;
    private static ExecutorService executorService;
    private static final int PORT = 8080;
    private static final String WEB_ROOT = "./www";

    @BeforeAll
    static void setUp() throws IOException {
        // Create a temporary directory for web root
        Path tempDir = Files.createTempDirectory("neohttp-test");
        Files.writeString(tempDir.resolve("index.html"), "<html><body>Hello, World!</body></html>");

        ServerConfig config = new ServerConfig();
        config.setPort(PORT);
        config.setWebRoot(tempDir.toString());

        Router router = new Router();
        server = new NonBlockingHttpServer(config, router);

        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Give the server some time to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void tearDown() throws IOException {
        executorService.shutdownNow();
        server.close();
    }

    @Test
    void testGetIndexHtml() throws IOException, ParseException   {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://localhost:" + PORT + "/index.html");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                assertEquals(200, response.getCode());
                String responseBody = EntityUtils.toString(response.getEntity());
                assertTrue(responseBody.contains("Hello, World!"));
            }
        }
}

    @Test
    void testGet404() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://localhost:" + PORT + "/nonexistent.html");
            HttpResponse response = httpClient.execute(request);

            assertEquals(404, response.getCode());
        }
    }
}