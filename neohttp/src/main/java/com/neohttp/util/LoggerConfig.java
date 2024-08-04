package com.neohttp.util;

import org.apache.logging.log4j.core.config.Configurator;

public class LoggerConfig {
    public static void configure() {
        Configurator.initialize(null, "config/log4j2.xml");
    }
}