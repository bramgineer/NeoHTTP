package com.neohttp;
import com.neohttp.config.ServerConfig;
import com.neohttp.server.NonBlockingHttpServer;
import com.neohttp.server.Router;
import java.io.IOException;
public class Main {
    public static void main(String[] args) throws IOException {
        ServerConfig config = new ServerConfig();
        config.setPort(8080); // Can be set from command line args or config file
        config.setThreadPoolSize(20);
        config.setWebRoot("./www");
        Router router = new Router();
        NonBlockingHttpServer server = new NonBlockingHttpServer(config, router);
        server.start();
    }
}