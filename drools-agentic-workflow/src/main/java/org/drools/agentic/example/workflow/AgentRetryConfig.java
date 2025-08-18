package org.drools.agentic.example.workflow;

import java.time.Duration;
import java.util.Set;

/**
 * Configuration for agent retry logic and error handling.
 */
public class AgentRetryConfig {
    private int maxRetries = 3;
    private Duration backoffDelay = Duration.ofSeconds(2);
    private Set<Class<? extends Exception>> retryableExceptions = Set.of(
        RuntimeException.class,
        IllegalStateException.class
        // Add more retryable exceptions as needed
    );
    private boolean exponentialBackoff = true;
    private Duration maxBackoffDelay = Duration.ofMinutes(1);

    public AgentRetryConfig() {}

    public AgentRetryConfig(int maxRetries, Duration backoffDelay) {
        this.maxRetries = maxRetries;
        this.backoffDelay = backoffDelay;
    }

    public boolean shouldRetry(Exception exception, int attemptNumber) {
        if (attemptNumber >= maxRetries) {
            return false;
        }
        
        return retryableExceptions.stream()
            .anyMatch(retryableClass -> retryableClass.isInstance(exception));
    }

    public Duration getBackoffDelay(int attemptNumber) {
        if (!exponentialBackoff) {
            return backoffDelay;
        }
        
        Duration delay = backoffDelay.multipliedBy((long) Math.pow(2, attemptNumber));
        return delay.compareTo(maxBackoffDelay) > 0 ? maxBackoffDelay : delay;
    }

    // Getters and setters
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public Duration getBackoffDelay() { return backoffDelay; }
    public void setBackoffDelay(Duration backoffDelay) { this.backoffDelay = backoffDelay; }

    public Set<Class<? extends Exception>> getRetryableExceptions() { return retryableExceptions; }
    public void setRetryableExceptions(Set<Class<? extends Exception>> retryableExceptions) { 
        this.retryableExceptions = retryableExceptions; 
    }

    public boolean isExponentialBackoff() { return exponentialBackoff; }
    public void setExponentialBackoff(boolean exponentialBackoff) { this.exponentialBackoff = exponentialBackoff; }

    public Duration getMaxBackoffDelay() { return maxBackoffDelay; }
    public void setMaxBackoffDelay(Duration maxBackoffDelay) { this.maxBackoffDelay = maxBackoffDelay; }
}