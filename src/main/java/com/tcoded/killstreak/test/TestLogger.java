package com.tcoded.killstreak.test;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Utility for logging test events during self-test execution.
 */
public class TestLogger {

    private static final Logger logger = Logger.getLogger("KillStreakTest");
    private static FileWriter fileWriter;
    private static boolean enabled = false;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /**
     * Enable test logging to both console and file.
     *
     * @param logFilePath path to the log file
     * @return true if successfully enabled, false otherwise
     */
    public static synchronized boolean enable(String logFilePath) {
        try {
            fileWriter = new FileWriter(logFilePath, false);
            enabled = true;
            logTest("TEST LOGGER ENABLED");
            return true;
        } catch (IOException e) {
            logger.severe("Failed to enable test logger: " + e.getMessage());
            return false;
        }
    }

    /**
     * Disable test logging and close the file.
     */
    public static synchronized void disable() {
        if (fileWriter != null) {
            try {
                logTest("TEST LOGGER DISABLED");
                fileWriter.close();
                enabled = false;
            } catch (IOException e) {
                logger.severe("Failed to disable test logger: " + e.getMessage());
            }
        }
    }

    /**
     * Log a message to both console and file.
     *
     * @param message the message to log
     */
    public static synchronized void logTest(String message) {
        if (!enabled) {
            return;
        }
        log(message);
    }

    private static void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = "[" + timestamp + "] " + message;

        logger.info(logMessage);

        if (fileWriter != null) {
            try {
                fileWriter.write(logMessage + "\n");
                fileWriter.flush();
            } catch (IOException e) {
                logger.severe("Failed to write to test log file: " + e.getMessage());
            }
        }
    }

    /**
     * Check if test logging is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Log a message only when in test environment (-Dtestenv=true).
     * Will be logged to console if TestLogger is enabled, otherwise will be silently ignored.
     *
     * @param message the message to log
     */
    public static void logTestEnv(String message) {
        if ("true".equalsIgnoreCase(System.getProperty("testenv"))) {
            log(message);
        }
    }
}
