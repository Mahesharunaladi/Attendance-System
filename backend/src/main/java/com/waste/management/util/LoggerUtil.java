package com.waste.management.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging utility for consistent logging across application
 */
public class LoggerUtil {
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtil.class);

    /**
     * Log info message
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Log info with parameters
     */
    public static void info(String message, Object... params) {
        logger.info(message, params);
    }

    /**
     * Log debug message
     */
    public static void debug(String message) {
        logger.debug(message);
    }

    /**
     * Log debug with parameters
     */
    public static void debug(String message, Object... params) {
        logger.debug(message, params);
    }

    /**
     * Log warning message
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * Log warning with exception
     */
    public static void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    /**
     * Log error message
     */
    public static void error(String message) {
        logger.error(message);
    }

    /**
     * Log error with exception
     */
    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * Log error with parameters and exception
     */
    public static void error(String message, Throwable throwable, Object... params) {
        logger.error(message, params, throwable);
    }

    /**
     * Log critical operations
     */
    public static void logOperation(String operation, String status) {
        info("[OPERATION] {} - Status: {}", operation, status);
    }

    /**
     * Log performance metrics
     */
    public static void logPerformance(String operation, long duration) {
        info("[PERFORMANCE] {} completed in {} ms", operation, duration);
    }

    /**
     * Log database operations
     */
    public static void logDatabaseOperation(String operation, String entity) {
        debug("[DATABASE] {} operation on entity: {}", operation, entity);
    }

    /**
     * Log API calls
     */
    public static void logApiCall(String endpoint, String method, int statusCode) {
        info("[API] {} {} - Status: {}", method, endpoint, statusCode);
    }
}
