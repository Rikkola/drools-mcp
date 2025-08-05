package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Granite-optimized wrapper for SupervisorAgent that provides enhanced prompting
 * and error handling specifically designed for Granite code models.
 * 
 * This wrapper addresses issues where Granite models tend to hallucinate agent names
 * instead of using the provided ones from the agent list.
 */
public class GraniteOptimizedSupervisorAgent {
    private final SupervisorAgent delegate;
    private static final int MAX_RETRIES = 2;
    
    // Pattern to extract the user request from error scenarios
    private static final Pattern REQUEST_PATTERN = Pattern.compile("Create a ([^.]+)\\.");
    
    public GraniteOptimizedSupervisorAgent(SupervisorAgent delegate) {
        this.delegate = delegate;
    }
    
    /**
     * Invokes the supervisor with Granite-optimized prompting and retry logic.
     * 
     * @param request The user request
     * @return The supervisor's response
     */
    public String invoke(String request) {
        // First attempt with enhanced prompting
        String enhancedRequest = buildGraniteOptimizedPrompt(request);
        
        try {
            return delegate.invoke(enhancedRequest);
        } catch (Exception e) {
            System.err.println("⚠️  First attempt failed with Granite model: " + e.getMessage());
            
            // Check if it's an invalid agent name error
            if (e.getMessage().contains("No agent found with name:")) {
                return retryWithExplicitPrompt(request);
            }
            
            // Check if it's a JSON parsing error (common with Granite)
            if (e.getMessage().contains("Failed to parse") && e.getMessage().contains("into")) {
                return retryWithJsonFixPrompt(request);
            }
            
            // If it's a different error, just rethrow
            throw e;
        }
    }
    
    /**
     * Builds a Granite-optimized prompt with explicit instructions and examples.
     */
    private String buildGraniteOptimizedPrompt(String request) {
        return String.format("""
            🎯 IMPORTANT INSTRUCTIONS FOR GRANITE MODEL:
            
            You are helping with a Drools DRL task. You have ONE agent available: "handleRequest"
            
            🚨 CRITICAL RULES:
            1. The ONLY valid agent name is: "handleRequest" 
            2. DO NOT create new agent names like "createType", "generateCode", "validateRules"
            3. USE EXACTLY "handleRequest" - copy it exactly as written
            
            ✅ CORRECT response format:
            {"agentName": "handleRequest", "arguments": {"request": "your request here"}}
            
            ❌ WRONG examples (DO NOT USE):
            - "createType" 
            - "generateCode"
            - "validateRules"
            - "createPersonType"
            
            User's request: %s
            
            Remember: Use "handleRequest" as the agent name!
            """, request);
    }
    
    /**
     * Retry mechanism for JSON formatting issues common with Granite models.
     */
    private String retryWithJsonFixPrompt(String request) {
        System.out.println("🔧 Retrying with JSON format correction for Granite...");
        
        String jsonFixRequest = String.format("""
            GRANITE MODEL - JSON FORMAT ERROR DETECTED
            
            📋 TASK: Select "handleRequest" for this request: %s
            
            🚨 CRITICAL: Your previous response had invalid JSON format.
            
            📝 REQUIRED JSON FORMAT (EXACT STRUCTURE):
            {
              "agentName": "handleRequest",
              "arguments": {
                "request": "%s"
              }
            }
            
            ⚠️  IMPORTANT FORMATTING RULES:
            1. Start with opening brace {
            2. End with closing brace }
            3. Use double quotes for all strings
            4. Don't break the JSON across lines incorrectly
            5. Ensure proper comma placement
            
            Return ONLY the JSON object - no additional text!
            """, request, request);
        
        try {
            return delegate.invoke(jsonFixRequest);
        } catch (Exception retryException) {
            System.err.println("🔧 JSON fix retry also failed: " + retryException.getMessage());
            return retryWithExplicitPrompt(request);
        }
    }
    
    /**
     * Retry mechanism with even more explicit prompting when the first attempt fails.
     */
    private String retryWithExplicitPrompt(String request) {
        System.out.println("🔄 Retrying with explicit Granite prompting...");
        
        String ultraExplicitRequest = String.format("""
            GRANITE MODEL ALERT: Previous attempt failed because you used wrong agent name.
            
            📋 AVAILABLE AGENTS: handleRequest (ONLY THIS ONE EXISTS)
            
            🎯 YOUR TASK: Select "handleRequest" for this request: %s
            
            📝 REQUIRED JSON FORMAT:
            {
              "agentName": "handleRequest",
              "arguments": {
                "request": "%s"
              }
            }
            
            ⚠️  IMPORTANT: Copy "handleRequest" exactly - no variations, no creativity!
            """, request, request);
        
        try {
            return delegate.invoke(ultraExplicitRequest);
        } catch (Exception retryException) {
            System.err.println("❌ Retry also failed: " + retryException.getMessage());
            
            // Last resort: return a direct response bypassing the supervisor
            return handleDirectResponse(request);
        }
    }
    
    /**
     * Last resort: provide a direct response when supervisor fails completely.
     */
    private String handleDirectResponse(String request) {
        System.out.println("🚨 Supervisor failed completely, providing direct response...");
        
        // Extract the essence of the request for a helpful fallback response
        String taskType = extractTaskType(request);
        
        return String.format("""
            I apologize, but I encountered an issue with the agent selection system. 
            However, I understand you want me to %s.
            
            Let me help you directly with creating DRL code for your request:
            
            Your request: %s
            
            To properly handle this, I would need to:
            1. Create the Person type declaration with name, age, and adult fields
            2. Create rules to determine if a person is an adult (age >= 18)
            3. Validate the DRL code structure
            4. Test it with sample data
            
            Would you like me to proceed with generating the DRL code for this request?
            """, taskType, request);
    }
    
    /**
     * Extracts the task type from the user request for fallback responses.
     */
    private String extractTaskType(String request) {
        Matcher matcher = REQUEST_PATTERN.matcher(request);
        if (matcher.find()) {
            return "create a " + matcher.group(1).toLowerCase();
        }
        return "help with your DRL authoring task";
    }
    
    /**
     * Factory method to create a Granite-optimized supervisor agent.
     */
    public static GraniteOptimizedSupervisorAgent create(SupervisorAgent supervisor) {
        return new GraniteOptimizedSupervisorAgent(supervisor);
    }
}