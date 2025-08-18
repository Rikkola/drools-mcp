package org.drools.agentic.example.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

/**
 * Comprehensive logging for agent interactions and tool usage.
 */
public class AgentInteractionLogger {
    private static final Logger logger = LoggerFactory.getLogger(AgentInteractionLogger.class);
    
    public void logAgentTransition(String fromAgent, String toAgent, Object data) {
        logger.info("🔄 Agent Transition: {} → {} | Data Type: {}", 
            fromAgent, toAgent, data != null ? data.getClass().getSimpleName() : "null");
        
        System.out.printf("🔄 %s ──────────────→ %s%n", fromAgent, toAgent);
        
        if (data instanceof String) {
            String dataStr = (String) data;
            if (dataStr.length() > 100) {
                System.out.printf("   📄 Data: %s...%n", dataStr.substring(0, 100));
            } else if (!dataStr.trim().isEmpty()) {
                System.out.printf("   📄 Data: %s%n", dataStr);
            }
        } else if (data != null) {
            System.out.printf("   📋 Data Type: %s%n", data.getClass().getSimpleName());
        }
    }
    
    public void logToolUsage(String agentName, String toolName, Map<String, Object> parameters) {
        logger.debug("🔨 Tool Usage: {} used {} with {} parameters", 
            agentName, toolName, parameters != null ? parameters.size() : 0);
        
        System.out.printf("   🔨 %s using %s%n", agentName, toolName);
        
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((key, value) -> {
                String valueStr = value != null ? value.toString() : "null";
                if (valueStr.length() > 50) {
                    valueStr = valueStr.substring(0, 50) + "...";
                }
                System.out.printf("      • %s: %s%n", key, valueStr);
            });
        }
    }
    
    public void logAgentStart(String agentName, String task) {
        logger.info("🤖 Agent Started: {} | Task: {}", agentName, task);
        System.out.printf("🤖 %s starting task: %s%n", agentName, task);
    }
    
    public void logAgentComplete(String agentName, String task, Object result, long durationMs) {
        logger.info("✅ Agent Completed: {} | Task: {} | Duration: {}ms | Result Type: {}", 
            agentName, task, durationMs, result != null ? result.getClass().getSimpleName() : "null");
        
        System.out.printf("✅ %s completed in %dms%n", agentName, durationMs);
        
        if (result instanceof String) {
            String resultStr = (String) result;
            if (resultStr.length() > 150) {
                System.out.printf("   📄 Result: %s...%n", resultStr.substring(0, 150));
            } else if (!resultStr.trim().isEmpty()) {
                System.out.printf("   📄 Result: %s%n", resultStr);
            }
        }
    }
    
    public void logAgentThinking(String agentName, String thought) {
        logger.debug("💭 Agent Thinking: {} | Thought: {}", agentName, thought);
        System.out.printf("   💭 %s: %s%n", agentName, truncateThought(thought));
    }
    
    public void logRetryAttempt(String agentName, String operation, int attemptNumber, Exception error) {
        logger.warn("🔄 Retry Attempt: {} | Operation: {} | Attempt: {} | Error: {}", 
            agentName, operation, attemptNumber, error.getMessage());
        
        System.out.printf("🔄 %s retrying %s (attempt %d): %s%n", 
            agentName, operation, attemptNumber, error.getMessage());
    }
    
    private String truncateThought(String thought) {
        if (thought == null) return "";
        if (thought.length() <= 80) return thought;
        return thought.substring(0, 80) + "...";
    }
}