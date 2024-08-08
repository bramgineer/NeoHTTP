package com.neohttp.util;

import java.util.logging.*;
import java.text.SimpleDateFormat;

public class LoggerConfig {
    public static void configure() {
        // Get the root logger
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);

        // Remove existing handlers
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Create and set a new ConsoleHandler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);

        // Create and set a custom formatter
        consoleHandler.setFormatter(new Formatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

            @Override
            public String format(LogRecord record) {
                StringBuilder sb = new StringBuilder();
                sb.append(dateFormat.format(record.getMillis())).append(" ");
                sb.append("[").append(record.getLongThreadID()).append("] ");
                sb.append(record.getLoggerName()).append(" ");
                sb.append(record.getLevel().getName()).append(": ");
                sb.append(formatMessage(record)).append("\n");
                return sb.toString();
            }
        });

        rootLogger.addHandler(consoleHandler);

        // Create a logger instance (optional, for demonstration)
        Logger logger = Logger.getLogger(LoggerConfig.class.getName());
        logger.info("Java Util Logging configured successfully");
    }
}