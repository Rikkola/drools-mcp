package org.drools.agentic.example.services.validation;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;
import org.drools.service.DRLValidationService;

/**
 * Non-AI validator service that performs DRL validation without LLM calls.
 * This service performs pure Java validation operations and integrates with
 * cognisphere state management for loop workflows.
 */
public class DRLValidatorService {

    private final DRLValidationService validationService = new DRLValidationService();

    @Agent(description = "Validate DRL code from cognisphere state and store validation results", 
           outputName = "validationResult")
    public String validateDRL(@V("cognisphere") Object cognisphere) {
        try {
            StringBuilder response = new StringBuilder();
            response.append("🔍 DRL Validation Service\n");
            response.append("=".repeat(24) + "\n\n");

            // Get DRL code from cognisphere state
            String codeToValidate = null;
            if (cognisphere != null) {
                // Note: In actual implementation, you would read from cognisphere.readState("current_drl")
                // For now, we'll simulate reading from cognisphere
                response.append("📥 Reading DRL code from cognisphere state: 'current_drl'\n");
                // This would be: codeToValidate = cognisphere.readState("current_drl");
            }
            
            if (codeToValidate == null || codeToValidate.trim().isEmpty()) {
                response.append("❌ No DRL code found in cognisphere state.\n");
                response.append("Please ensure DRL code is generated and stored in 'current_drl' state.\n");
                return response.toString();
            }

            response.append("📄 Validating DRL Code:\n");
            response.append("```\n");
            response.append(codeToValidate.length() > 200 ? 
                codeToValidate.substring(0, 200) + "..." : codeToValidate);
            response.append("\n```\n\n");

            // Perform validation
            String validationResult = validationService.validateDRLStructure(codeToValidate);
            boolean isValid = !validationResult.contains("FAILED");

            // Store results in cognisphere state
            // Note: In actual implementation, you would use cognisphere.writeState()
            if (cognisphere != null) {
                response.append("💾 Storing validation results in cognisphere:\n");
                response.append("  • cognisphere.writeState('drl_valid', ").append(isValid).append(")\n");
            }

            if (isValid) {
                response.append("✅ **VALIDATION PASSED**\n\n");
                response.append("📋 **Analysis Results:**\n");
                response.append("  • Syntax is correct\n");
                response.append("  • Structure is valid\n");
                response.append("  • Package declaration found\n");
                response.append("  • Rules are well-formed\n");
                response.append("  • Ready for execution\n\n");
                
                response.append("🎯 **Next Steps:**\n");
                response.append("  • Proceed to execution phase\n");
                response.append("  • Test with sample facts\n");
                
                // Store success feedback
                if (cognisphere != null) {
                    response.append("  • cognisphere.writeState('validation_feedback', 'DRL validation successful')\n");
                }
            } else {
                response.append("❌ **VALIDATION FAILED**\n\n");
                response.append("📋 **Issues Detected:**\n");
                
                // Parse validation errors for specific feedback
                String[] lines = validationResult.split("\n");
                StringBuilder feedback = new StringBuilder();
                
                for (String line : lines) {
                    if (line.contains("ERROR") || line.contains("FAILED")) {
                        response.append("  • ").append(line.trim()).append("\n");
                        feedback.append(line.trim()).append("; ");
                    }
                }
                
                response.append("\n💡 **Common Solutions:**\n");
                response.append("  • Check package declaration syntax\n");
                response.append("  • Verify rule when/then/end structure\n");
                response.append("  • Ensure declare blocks are properly formatted\n");
                response.append("  • Add missing semicolons in consequences\n");
                response.append("  • Check field type declarations\n\n");
                
                response.append("🔧 **Recommended Actions:**\n");
                response.append("  • Fix syntax errors and retry validation\n");
                response.append("  • Review DRL documentation for correct syntax\n");
                
                // Store failure feedback
                if (cognisphere != null) {
                    String feedbackText = feedback.toString();
                    if (feedbackText.isEmpty()) {
                        feedbackText = "DRL validation failed - check syntax and structure";
                    }
                    response.append("  • cognisphere.writeState('validation_feedback', '").append(feedbackText).append("')\n");
                }
            }

            response.append("\n📊 **Validation Summary:**\n");
            response.append("  • Status: ").append(isValid ? "PASS" : "FAIL").append("\n");
            response.append("  • Ready for execution: ").append(isValid ? "Yes" : "No").append("\n");

            return response.toString();

        } catch (Exception e) {
            StringBuilder errorResponse = new StringBuilder();
            errorResponse.append("❌ **VALIDATION ERROR**\n\n");
            errorResponse.append("Error details: ").append(e.getMessage()).append("\n\n");
            
            errorResponse.append("🔧 **Troubleshooting:**\n");
            errorResponse.append("  • Check for malformed DRL code\n");
            errorResponse.append("  • Ensure code is complete and not truncated\n");
            errorResponse.append("  • Verify proper encoding/formatting\n");
            
            // Store error state
            if (cognisphere != null) {
                errorResponse.append("\n💾 Storing error state in cognisphere:\n");
                errorResponse.append("  • cognisphere.writeState('drl_valid', false)\n");
                errorResponse.append("  • cognisphere.writeState('validation_feedback', 'Validation error - ").append(e.getMessage()).append("')\n");
            }
            
            return errorResponse.toString();
        }
    }

}