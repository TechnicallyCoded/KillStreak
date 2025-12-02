package com.tcoded.killstreak.test;

/**
 * Manages debug mode state during self-tests.
 * Debug mode enables extra logging and detailed information output.
 */
public class DebugMode {

    private static volatile boolean enabled = false;

    /**
     * Enable debug mode.
     */
    public static void enable() {
        enabled = true;
    }

    /**
     * Disable debug mode.
     */
    public static void disable() {
        enabled = false;
    }

    /**
     * Check if debug mode is enabled.
     *
     * @return true if debug mode is enabled, false otherwise
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Log a debug message if debug mode is enabled.
     *
     * @param message the message to log
     */
    public static void log(String message) {
        if (enabled) {
            TestLogger.log("[DEBUG] " + message);
        }
    }

    /**
     * Log a formatted debug message if debug mode is enabled.
     *
     * @param format the format string
     * @param args the format arguments
     */
    public static void log(String format, Object... args) {
        if (enabled) {
            TestLogger.log("[DEBUG] " + String.format(format, args));
        }
    }
}
