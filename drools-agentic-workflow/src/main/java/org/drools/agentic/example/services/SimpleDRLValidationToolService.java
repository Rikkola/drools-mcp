package org.drools.agentic.example.services;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.drools.service.DRLValidationService;

/**
 * Simple service class that provides Tool-annotated methods for validating DRL code.
 * This class works with DRL code directly without requiring DefinitionStorage.
 */
public class SimpleDRLValidationToolService {
    private final DRLValidationService validationService = new DRLValidationService();

    public SimpleDRLValidationToolService() {
        // No storage needed - works with direct DRL input
    }

    @Tool("Validate DRL code and provide detailed guidance")
    public String validateWithGuidance(@P("DRL code to validate") String drlCode) {
        StringBuilder response = new StringBuilder();
        response.append("🔍 DRL Validation with Guidance:\n");
        response.append("=" .repeat(32) + "\n\n");
        
        try {
            String validationResult = validationService.validateDRLStructure(drlCode);
            boolean isValid = !validationResult.contains("FAILED");
            
            if (isValid) {
                response.append("✅ **VALIDATION PASSED**\n\n");
                response.append("📋 **Code Analysis:**\n");
                response.append("• Syntax is correct\n");
                response.append("• Structure is valid\n");
                response.append("• Ready for execution\n\n");
                
                response.append("💡 **Suggestions:**\n");
                response.append("• Code looks good to execute\n");
                response.append("• Consider testing with sample facts\n");
                response.append("• Verify rule logic meets your requirements\n");
            } else {
                response.append("❌ **VALIDATION FAILED**\n\n");
                response.append("📋 **Issues Detected:**\n");
                response.append("• DRL syntax or structure problems\n");
                response.append("• Code cannot be compiled\n\n");
                
                response.append("💡 **Common Issues to Check:**\n");
                response.append("• Missing 'package' declaration\n");
                response.append("• Incorrect rule syntax (when/then/end)\n");
                response.append("• Missing semicolons in rule consequences\n");
                response.append("• Incorrect declare block syntax\n");
                response.append("• Invalid field types or names\n\n");
                
                response.append("🔧 **Quick Fix Tips:**\n");
                response.append("• Ensure proper package declaration at the top\n");
                response.append("• Check that all rules have proper when/then/end structure\n");
                response.append("• Verify declared types use correct syntax\n");
                response.append("• Make sure rule consequences end with semicolons\n");
            }
            
        } catch (Exception e) {
            response.append("❌ **VALIDATION ERROR**\n\n");
            response.append(String.format("Error details: %s\n\n", e.getMessage()));
            
            response.append("🔧 **Troubleshooting:**\n");
            response.append("• Check for basic syntax errors\n");
            response.append("• Ensure the DRL code is complete\n");
            response.append("• Verify package and import statements\n");
            response.append("• Make sure all blocks are properly closed\n");
        }
        
        return response.toString();
    }

}
