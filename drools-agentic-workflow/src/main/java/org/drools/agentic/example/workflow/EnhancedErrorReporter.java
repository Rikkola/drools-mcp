package org.drools.agentic.example.workflow;

import org.drools.exception.DRLValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced error reporting with context and recovery suggestions.
 * Integrates with the DrlFaultFinder for precise DRL error reporting.
 */
public class EnhancedErrorReporter {
    private static final Logger logger = LoggerFactory.getLogger(EnhancedErrorReporter.class);
    
    public void reportAgentError(String agentName, String phase, Exception error, Object context) {
        logger.error("❌ Agent Error: {} in phase {} | Error: {} | Context: {}", 
            agentName, phase, error.getMessage(), 
            context != null ? context.getClass().getSimpleName() : "null", error);
        
        System.err.println("\n" + "=".repeat(60));
        System.err.printf("❌ ERROR in %s (%s phase)%n", agentName, phase);
        System.err.printf("🔍 Cause: %s%n", error.getMessage());
        
        // Enhanced DRL-specific error reporting using our DrlFaultFinder integration
        if (error instanceof DRLValidationException && context instanceof String) {
            reportDrlValidationError((DRLValidationException) error, (String) context);
        }
        
        // Add context information if available
        if (context != null) {
            reportErrorContext(context);
        }
        
        // Provide recovery suggestions
        suggestRecoveryActions(agentName, phase, error);
        
        // Add troubleshooting tips
        provideTroubleshootingTips(agentName, error);
        
        System.err.println("=".repeat(60));
    }
    
    private void reportDrlValidationError(DRLValidationException error, String drlContent) {
        System.err.println("\n🔍 DRL VALIDATION DETAILS:");
        
        String errorMessage = error.getMessage();
        
        // Check if our enhanced DrlFaultFinder provided detailed information
        if (errorMessage.contains("DRL validation failed at line")) {
            System.err.printf("   📍 %s%n", errorMessage);
        } else if (errorMessage.contains("line")) {
            // Extract line information if available
            System.err.printf("   📍 Error details: %s%n", errorMessage);
        } else {
            System.err.printf("   ⚠️  General DRL error: %s%n", errorMessage);
        }
        
        // Show relevant DRL snippet if available
        if (drlContent != null && !drlContent.trim().isEmpty()) {
            String[] lines = drlContent.split("\n");
            if (lines.length > 0) {
                System.err.println("\n📄 DRL Content Preview:");
                for (int i = 0; i < Math.min(lines.length, 10); i++) {
                    System.err.printf("   %2d: %s%n", i + 1, lines[i]);
                }
                if (lines.length > 10) {
                    System.err.printf("   ... (%d more lines)%n", lines.length - 10);
                }
            }
        }
    }
    
    private void reportErrorContext(Object context) {
        System.err.println("\n📋 ERROR CONTEXT:");
        
        if (context instanceof String) {
            String contextStr = (String) context;
            if (contextStr.length() > 200) {
                System.err.printf("   📄 Content: %s...%n", contextStr.substring(0, 200));
                System.err.printf("   📏 Total length: %d characters%n", contextStr.length());
            } else {
                System.err.printf("   📄 Content: %s%n", contextStr);
            }
        } else {
            System.err.printf("   📋 Context Type: %s%n", context.getClass().getSimpleName());
            System.err.printf("   📄 Context: %s%n", context.toString());
        }
    }
    
    private void suggestRecoveryActions(String agentName, String phase, Exception error) {
        System.err.println("\n💡 RECOVERY SUGGESTIONS:");
        
        String errorMsg = error.getMessage().toLowerCase();
        
        // DRL-specific suggestions
        if (agentName.contains("DRL") || errorMsg.contains("drl")) {
            if (errorMsg.contains("syntax") || errorMsg.contains("parse")) {
                System.err.println("   🔧 DRL Syntax Issues:");
                System.err.println("     • Check rule structure: rule \"name\" when ... then ... end");
                System.err.println("     • Verify parentheses and brackets are balanced");
                System.err.println("     • Ensure all statements end with semicolons");
                System.err.println("     • Try regenerating with simpler requirements");
            }
            
            if (errorMsg.contains("declare") || errorMsg.contains("fact")) {
                System.err.println("   🏗️  Fact Type Issues:");
                System.err.println("     • Ensure all fact types are declared with 'declare' blocks");
                System.err.println("     • Check field names and types are valid");
                System.err.println("     • Verify fact type names don't conflict");
            }
            
            if (errorMsg.contains("import") || errorMsg.contains("class")) {
                System.err.println("   📦 Import Issues:");
                System.err.println("     • Use only standard Java classes or declared fact types");
                System.err.println("     • Avoid importing non-existent custom classes");
                System.err.println("     • Check if required fact types are properly declared");
            }
        }
        
        // File storage suggestions
        if (agentName.contains("Storage") || agentName.contains("File")) {
            System.err.println("   📁 File Storage Issues:");
            System.err.println("     • Check file system permissions");
            System.err.println("     • Verify storage directory exists and is writable");
            System.err.println("     • Ensure sufficient disk space");
        }
        
        // Knowledge base suggestions
        if (agentName.contains("Knowledge") || errorMsg.contains("kie")) {
            System.err.println("   🧠 Knowledge Base Issues:");
            System.err.println("     • Verify DRL file is valid and complete");
            System.err.println("     • Check Drools runtime dependencies");
            System.err.println("     • Try rebuilding knowledge base from scratch");
        }
        
        // General retry suggestions
        if (errorMsg.contains("timeout") || errorMsg.contains("connection")) {
            System.err.println("   🔄 Connection/Timeout Issues:");
            System.err.println("     • Retry the operation after a short delay");
            System.err.println("     • Check network connectivity");
            System.err.println("     • Verify service endpoints are available");
        }
    }
    
    private void provideTroubleshootingTips(String agentName, Exception error) {
        System.err.println("\n🛠️  TROUBLESHOOTING TIPS:");
        
        System.err.println("   1. 📋 Check the error context and input data");
        System.err.println("   2. 🔍 Review the agent logs for detailed information");
        System.err.println("   3. 🧪 Try with simpler input to isolate the issue");
        System.err.println("   4. 🔄 Consider restarting the workflow from the beginning");
        
        if (agentName.contains("DRL")) {
            System.err.println("   5. 📚 Consult Drools documentation for DRL syntax");
            System.err.println("   6. 🎯 Test DRL fragments in isolation");
        }
        
        System.err.printf("   7. 🐛 Report persistent issues with error details: %s%n", 
                         error.getClass().getSimpleName());
    }
    
    public void reportWarning(String agentName, String phase, String warning, Object context) {
        logger.warn("⚠️ Agent Warning: {} in phase {} | Warning: {} | Context: {}", 
            agentName, phase, warning, 
            context != null ? context.getClass().getSimpleName() : "null");
        
        System.out.printf("⚠️  WARNING in %s (%s): %s%n", agentName, phase, warning);
        
        if (context != null && !context.toString().trim().isEmpty()) {
            System.out.printf("    Context: %s%n", truncateContext(context.toString()));
        }
    }
    
    private String truncateContext(String context) {
        if (context.length() <= 100) return context;
        return context.substring(0, 100) + "...";
    }
}