package com.neohttp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerConfig {
    public static void configure() {
        // Set the default logging level
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");
        
        // Configure the date-time format for logging
        System.setProperty(org.slf4j.impl.SimpleLogger.DATE_TIME_FORMAT_KEY, "yyyy-MM-dd HH:mm:ss:SSS");
        
        // Show logger name in output
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_LOG_NAME_KEY, "true");
        
        // Show thread name in output
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY, "true");
        
        // Configure log output to include the calling class and line number
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
        
        // Create a logger instance (optional, for demonstration)
        Logger logger = LoggerFactory.getLogger(LoggerConfig.class);
        logger.info("SLF4J logging configured successfully");
    }
}