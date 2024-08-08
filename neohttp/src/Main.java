public class Main {
    public static void main(String[] args) throws IOException {
        ServerConfig config = new ServerConfig();
        config.setPort(8080); // Can be set from command line args or config file
        config.setThreadPoolSize(20);
        
        NonBlockingHttpServer server = new NonBlockingHttpServer(config);
        server.start();
    }
}