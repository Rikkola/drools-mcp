package org.drools.agentic.example.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Utility class for implementing retry logic with exponential backoff.
 */
public class RetryUtils {
    private static final Logger logger = LoggerFactory.getLogger(RetryUtils.class);
    
    /**
     * Execute an operation with retry logic.
     */
    public static <T> T withRetry(Supplier<T> operation, AgentRetryConfig config, String operationName) {
        Exception lastException = null;
        
        for (int attempt = 0; attempt < config.getMaxRetries(); attempt++) {
            try {
                if (attempt > 0) {
                    logger.info("🔄 Retry attempt {} for operation: {}", attempt + 1, operationName);
                }
                
                return operation.get();
                
            } catch (Exception e) {
                lastException = e;
                
                if (!config.shouldRetry(e, attempt)) {
                    logger.warn("❌ Operation {} failed permanently after {} attempts: {}", 
                        operationName, attempt + 1, e.getMessage());
                    break;
                }
                
                if (attempt < config.getMaxRetries() - 1) {
                    var delay = config.getBackoffDelay(attempt);
                    logger.warn("⏳ Operation {} failed (attempt {}), retrying in {}ms: {}", 
                        operationName, attempt + 1, delay.toMillis(), e.getMessage());
                    
                    try {
                        Thread.sleep(delay.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                } else {
                    logger.error("❌ Operation {} failed after {} attempts: {}", 
                        operationName, attempt + 1, e.getMessage());
                }
            }
        }
        
        // If we get here, all retries failed
        throw new RuntimeException(
            String.format("Operation '%s' failed after %d attempts", operationName, config.getMaxRetries()), 
            lastException
        );
    }
    
    /**
     * Execute a callable operation with retry logic.
     */
    public static <T> T withRetryCallable(Callable<T> operation, AgentRetryConfig config, String operationName) {
        return withRetry(() -> {
            try {
                return operation.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException("Operation failed", e);
            }
        }, config, operationName);
    }
    
    /**
     * Execute a runnable operation with retry logic.
     */
    public static void withRetryRunnable(Runnable operation, AgentRetryConfig config, String operationName) {
        withRetry(() -> {
            operation.run();
            return null;
        }, config, operationName);
    }
}