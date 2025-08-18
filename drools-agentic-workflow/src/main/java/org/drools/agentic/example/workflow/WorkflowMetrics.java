package org.drools.agentic.example.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance analytics and metrics collection for workflow execution.
 */
public class WorkflowMetrics {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowMetrics.class);
    
    private final Map<String, List<Duration>> phaseDurations = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> toolUsageCount = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> errorCount = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> totalPhaseTime = new ConcurrentHashMap<>();
    private final AtomicInteger totalWorkflows = new AtomicInteger();
    private final AtomicInteger successfulWorkflows = new AtomicInteger();
    private final AtomicInteger failedWorkflows = new AtomicInteger();
    private final Map<String, AtomicInteger> retryCount = new ConcurrentHashMap<>();
    
    public void recordWorkflowStart() {
        totalWorkflows.incrementAndGet();
    }
    
    public void recordWorkflowSuccess() {
        successfulWorkflows.incrementAndGet();
    }
    
    public void recordWorkflowFailure() {
        failedWorkflows.incrementAndGet();
    }
    
    public void recordPhaseDuration(String phase, Duration duration) {
        phaseDurations.computeIfAbsent(phase, k -> Collections.synchronizedList(new ArrayList<>()))
                     .add(duration);
        totalPhaseTime.computeIfAbsent(phase, k -> new AtomicLong())
                     .addAndGet(duration.toMillis());
        
        logger.debug("📊 Phase Duration: {} took {}ms", phase, duration.toMillis());
    }
    
    public void recordToolUsage(String toolName) {
        toolUsageCount.computeIfAbsent(toolName, k -> new AtomicInteger()).incrementAndGet();
        logger.debug("🔧 Tool Usage: {} used", toolName);
    }
    
    public void recordError(String phase, String errorType) {
        String key = phase + ":" + errorType;
        errorCount.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
        logger.debug("❌ Error Recorded: {} in {}", errorType, phase);
    }
    
    public void recordRetry(String operation) {
        retryCount.computeIfAbsent(operation, k -> new AtomicInteger()).incrementAndGet();
        logger.debug("🔄 Retry Recorded: {}", operation);
    }
    
    public void generateReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 WORKFLOW PERFORMANCE REPORT");
        System.out.println("=".repeat(60));
        
        // Workflow Summary
        int total = totalWorkflows.get();
        int successful = successfulWorkflows.get();
        int failed = failedWorkflows.get();
        double successRate = total > 0 ? (successful * 100.0) / total : 0.0;
        
        System.out.println("🎯 WORKFLOW SUMMARY:");
        System.out.printf("   Total Workflows: %d%n", total);
        System.out.printf("   Successful: %d (%.1f%%)%n", successful, successRate);
        System.out.printf("   Failed: %d (%.1f%%)%n", failed, 100.0 - successRate);
        
        // Phase Performance
        if (!phaseDurations.isEmpty()) {
            System.out.println("\n⏱️  PHASE PERFORMANCE:");
            phaseDurations.entrySet().stream()
                .sorted(Map.Entry.<String, List<Duration>>comparingByKey())
                .forEach(entry -> {
                    String phase = entry.getKey();
                    List<Duration> durations = entry.getValue();
                    
                    double avgMs = durations.stream()
                        .mapToLong(Duration::toMillis)
                        .average()
                        .orElse(0.0);
                    
                    long minMs = durations.stream()
                        .mapToLong(Duration::toMillis)
                        .min()
                        .orElse(0);
                    
                    long maxMs = durations.stream()
                        .mapToLong(Duration::toMillis)
                        .max()
                        .orElse(0);
                    
                    System.out.printf("   %-20s: avg %.1fms, min %dms, max %dms (%d runs)%n", 
                        phase, avgMs, minMs, maxMs, durations.size());
                });
        }
        
        // Tool Usage
        if (!toolUsageCount.isEmpty()) {
            System.out.println("\n🔧 TOOL USAGE SUMMARY:");
            toolUsageCount.entrySet().stream()
                .sorted(Map.Entry.<String, AtomicInteger>comparingByValue(
                    (a, b) -> b.get() - a.get()))
                .limit(10) // Top 10 most used tools
                .forEach(entry -> 
                    System.out.printf("   %-25s: %d times%n", 
                        entry.getKey(), entry.getValue().get()));
        }
        
        // Error Analysis
        if (!errorCount.isEmpty()) {
            System.out.println("\n❌ ERROR ANALYSIS:");
            errorCount.entrySet().stream()
                .sorted(Map.Entry.<String, AtomicInteger>comparingByValue(
                    (a, b) -> b.get() - a.get()))
                .forEach(entry -> 
                    System.out.printf("   %-30s: %d occurrences%n", 
                        entry.getKey(), entry.getValue().get()));
        }
        
        // Retry Statistics
        if (!retryCount.isEmpty()) {
            System.out.println("\n🔄 RETRY STATISTICS:");
            retryCount.entrySet().stream()
                .sorted(Map.Entry.<String, AtomicInteger>comparingByValue(
                    (a, b) -> b.get() - a.get()))
                .forEach(entry -> 
                    System.out.printf("   %-25s: %d retries%n", 
                        entry.getKey(), entry.getValue().get()));
        }
        
        System.out.println("=".repeat(60));
        
        // Log summary metrics
        logger.info("📊 Metrics Summary - Workflows: {}, Success Rate: {:.1f}%, Avg Phase Time: {}ms", 
            total, successRate, calculateOverallAverage());
    }
    
    private double calculateOverallAverage() {
        return phaseDurations.values().stream()
            .flatMap(List::stream)
            .mapToLong(Duration::toMillis)
            .average()
            .orElse(0.0);
    }
    
    public Map<String, Double> getPhaseAverages() {
        Map<String, Double> averages = new HashMap<>();
        phaseDurations.forEach((phase, durations) -> {
            double avg = durations.stream()
                .mapToLong(Duration::toMillis)
                .average()
                .orElse(0.0);
            averages.put(phase, avg);
        });
        return averages;
    }
    
    public double getSuccessRate() {
        int total = totalWorkflows.get();
        return total > 0 ? (successfulWorkflows.get() * 100.0) / total : 0.0;
    }
    
    public void reset() {
        phaseDurations.clear();
        toolUsageCount.clear();
        errorCount.clear();
        totalPhaseTime.clear();
        retryCount.clear();
        totalWorkflows.set(0);
        successfulWorkflows.set(0);
        failedWorkflows.set(0);
    }
}