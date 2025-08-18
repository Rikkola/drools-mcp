package org.drools.agentic.example.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Comprehensive reporter for workflow execution with structured logging.
 */
public class WorkflowReporter {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowReporter.class);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    public void reportWorkflowStart(String workflowName, String input) {
        logger.info("🚀 Workflow Started: {} | Input length: {} chars", 
            workflowName, input != null ? input.length() : 0);
        System.out.printf("🚀 [%s] Starting workflow: %s%n", 
            Instant.now().atZone(java.time.ZoneId.systemDefault()).format(TIME_FORMAT), 
            workflowName);
        System.out.printf("📝 Input: %s%n", truncateInput(input, 100));
    }
    
    public void reportPhaseStart(String phaseName, String input) {
        logger.info("📍 Phase Started: {} | Input: {}", phaseName, truncateInput(input, 50));
        System.out.printf("📍 [%s] Starting phase: %s%n", 
            Instant.now().atZone(java.time.ZoneId.systemDefault()).format(TIME_FORMAT), 
            phaseName);
    }
    
    public void reportPhaseComplete(String phaseName, Object result, Duration duration) {
        logger.info("✅ Phase Completed: {} | Duration: {}ms | Result Type: {}", 
            phaseName, duration.toMillis(), result != null ? result.getClass().getSimpleName() : "null");
        System.out.printf("✅ [%s] Completed: %s (took %dms)%n", 
            Instant.now().atZone(java.time.ZoneId.systemDefault()).format(TIME_FORMAT), 
            phaseName, duration.toMillis());
        
        if (result instanceof String) {
            String resultStr = (String) result;
            if (resultStr.length() > 200) {
                System.out.printf("📄 Result preview: %s...%n", resultStr.substring(0, 200));
            } else {
                System.out.printf("📄 Result: %s%n", resultStr);
            }
        }
    }
    
    public void reportAgentAction(String agentName, String action, Object details) {
        logger.debug("🔧 Agent Action: {} | Action: {} | Details: {}", 
            agentName, action, details);
        System.out.printf("   ↳ %s: %s%n", agentName, action);
        
        if (details != null && !details.toString().trim().isEmpty()) {
            System.out.printf("     Details: %s%n", truncateInput(details.toString(), 100));
        }
    }
    
    public void reportWorkflowComplete(WorkflowState state) {
        logger.info("🎯 Workflow Completed: {} | Total Duration: {}ms | Phases: {} | Errors: {} | Warnings: {}", 
            state.getStatus(), state.getTotalDuration().toMillis(), 
            state.getCompletedPhases().size(), state.getErrors().size(), state.getWarnings().size());
        
        System.out.println("=".repeat(60));
        System.out.printf("🎯 Workflow Status: %s%n", state.getStatus());
        System.out.printf("⏱️  Total Duration: %dms%n", state.getTotalDuration().toMillis());
        System.out.printf("✅ Completed Phases: %s%n", String.join(" → ", state.getCompletedPhases()));
        
        if (!state.getWarnings().isEmpty()) {
            System.out.printf("⚠️  Warnings (%d):%n", state.getWarnings().size());
            state.getWarnings().forEach(warning -> System.out.printf("   • %s%n", warning));
        }
        
        if (!state.getErrors().isEmpty()) {
            System.out.printf("❌ Errors (%d):%n", state.getErrors().size());
            state.getErrors().forEach(error -> System.out.printf("   • %s%n", error));
        }
        
        System.out.println("=".repeat(60));
    }
    
    private String truncateInput(String input, int maxLength) {
        if (input == null) return "null";
        if (input.length() <= maxLength) return input;
        return input.substring(0, maxLength) + "...";
    }
}