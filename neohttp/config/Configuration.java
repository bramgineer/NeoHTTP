package com.neohttp.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Configuration {
    private final Config config;

    public Configuration() {
        config = ConfigFactory.load();
    }

    public int getPort() {
        return config.getInt("neohttp.port");
    }

    public int getThreadPoolSize() {
        return config.getInt("neohttp.threadPoolSize");
    }
}
