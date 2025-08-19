package org.drools.agentic.example.workflow;

import java.time.Duration;

/**
 * Configuration-driven workflow settings for customizable execution.
 */
public class WorkflowConfiguration {
    private boolean enableProgressReporting = true;
    private boolean enableMetrics = true;
    private boolean enableDetailedLogging = true;
    private LogLevel logLevel = LogLevel.INFO;
    private Duration timeoutPerPhase = Duration.ofMinutes(2);
    private boolean enableRetry = true;
    private AgentRetryConfig retryConfig = new AgentRetryConfig();
    private boolean enableInteractionLogging = true;
    private boolean enableProgressBar = true;
    private boolean enableErrorRecovery = true;
    
    public enum LogLevel {
        ERROR, WARN, INFO, DEBUG, TRACE
    }
    
    // Default constructor with sensible defaults
    public WorkflowConfiguration() {}
    
    // Builder pattern for easy configuration
    public static WorkflowConfiguration builder() {
        return new WorkflowConfiguration();
    }
    
    public WorkflowConfiguration withProgressReporting(boolean enable) {
        this.enableProgressReporting = enable;
        return this;
    }
    
    public WorkflowConfiguration withMetrics(boolean enable) {
        this.enableMetrics = enable;
        return this;
    }
    
    public WorkflowConfiguration withDetailedLogging(boolean enable) {
        this.enableDetailedLogging = enable;
        return this;
    }
    
    public WorkflowConfiguration withLogLevel(LogLevel level) {
        this.logLevel = level;
        return this;
    }
    
    public WorkflowConfiguration withTimeout(Duration timeout) {
        this.timeoutPerPhase = timeout;
        return this;
    }
    
    public WorkflowConfiguration withRetry(boolean enable) {
        this.enableRetry = enable;
        return this;
    }
    
    public WorkflowConfiguration withRetryConfig(AgentRetryConfig config) {
        this.retryConfig = config;
        return this;
    }
    
    public WorkflowConfiguration withProgressBar(boolean enable) {
        this.enableProgressBar = enable;
        return this;
    }
    
    // Preset configurations for common scenarios
    public static WorkflowConfiguration production() {
        return new WorkflowConfiguration()
            .withLogLevel(LogLevel.WARN)
            .withProgressBar(false)
            .withDetailedLogging(false)
            .withMetrics(true);
    }
    
    public static WorkflowConfiguration development() {
        return new WorkflowConfiguration()
            .withLogLevel(LogLevel.DEBUG)
            .withProgressBar(true)
            .withDetailedLogging(true)
            .withMetrics(true);
    }
    
    public static WorkflowConfiguration quiet() {
        return new WorkflowConfiguration()
            .withLogLevel(LogLevel.ERROR)
            .withProgressBar(false)
            .withProgressReporting(false)
            .withDetailedLogging(false)
            .withMetrics(false);
    }
    
    // Getters
    public boolean isEnableProgressReporting() { return enableProgressReporting; }
    public boolean isEnableMetrics() { return enableMetrics; }
    public boolean isEnableDetailedLogging() { return enableDetailedLogging; }
    public LogLevel getLogLevel() { return logLevel; }
    public Duration getTimeoutPerPhase() { return timeoutPerPhase; }
    public boolean isEnableRetry() { return enableRetry; }
    public AgentRetryConfig getRetryConfig() { return retryConfig; }
    public boolean isEnableInteractionLogging() { return enableInteractionLogging; }
    public boolean isEnableProgressBar() { return enableProgressBar; }
    public boolean isEnableErrorRecovery() { return enableErrorRecovery; }
    
    // Setters for direct access
    public void setEnableProgressReporting(boolean enableProgressReporting) { 
        this.enableProgressReporting = enableProgressReporting; 
    }
    
    public void setEnableMetrics(boolean enableMetrics) { 
        this.enableMetrics = enableMetrics; 
    }
    
    public void setEnableDetailedLogging(boolean enableDetailedLogging) { 
        this.enableDetailedLogging = enableDetailedLogging; 
    }
    
    public void setLogLevel(LogLevel logLevel) { 
        this.logLevel = logLevel; 
    }
    
    public void setTimeoutPerPhase(Duration timeoutPerPhase) { 
        this.timeoutPerPhase = timeoutPerPhase; 
    }
    
    public void setEnableRetry(boolean enableRetry) { 
        this.enableRetry = enableRetry; 
    }
    
    public void setRetryConfig(AgentRetryConfig retryConfig) { 
        this.retryConfig = retryConfig; 
    }
    
    public void setEnableInteractionLogging(boolean enableInteractionLogging) { 
        this.enableInteractionLogging = enableInteractionLogging; 
    }
    
    public void setEnableProgressBar(boolean enableProgressBar) { 
        this.enableProgressBar = enableProgressBar; 
    }
    
    public void setEnableErrorRecovery(boolean enableErrorRecovery) { 
        this.enableErrorRecovery = enableErrorRecovery; 
    }
    
    @Override
    public String toString() {
        return String.format(
            "WorkflowConfiguration{logLevel=%s, progressReporting=%s, metrics=%s, retry=%s, timeout=%s}",
            logLevel, enableProgressReporting, enableMetrics, enableRetry, timeoutPerPhase
        );
    }
}