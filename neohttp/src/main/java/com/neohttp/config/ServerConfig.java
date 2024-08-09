package com.neohttp.config;

public class ServerConfig {
    private int port = 8080; // Default port
    private int threadPoolSize = 10; // Default thread pool size

    // Getters and setters
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public int getThreadPoolSize() { return threadPoolSize; }
    public void setThreadPoolSize(int threadPoolSize) { this.threadPoolSize = threadPoolSize; }

    private String webRoot = "./www"; // Default web root directory

    public String getWebRoot() { return webRoot; }
    public void setWebRoot(String webRoot) { this.webRoot = webRoot; }
}