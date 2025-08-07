package org.drools.agentic.example.services.validation;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.drools.service.DRLValidationService;

/**
 * Tool-based DRL validation service that provides validation capabilities
 * to AI agents through @Tool annotations.
 * Similar to KnowledgeRunnerService but focused on DRL validation.
 */
public class DRLValidatorToolService {

    private final DRLValidationService validationService = new DRLValidationService();

    @Tool("STEP 1: Validate DRL code structure and syntax. Use this to check if DRL code is syntactically correct before execution. Returns detailed validation results with specific error messages if validation fails.")
    public String validateDRLStructure(@P("drlCode - The complete DRL code to validate including package declaration, imports, declare blocks, and rules") String drlCode) {
        try {
            if (drlCode == null || drlCode.trim().isEmpty()) {
                return "❌ VALIDATION FAILED: No DRL code provided for validation";
            }

            StringBuilder response = new StringBuilder();
            response.append("🔍 DRL Structure Validation\n");
            response.append("=".repeat(27) + "\n\n");

            response.append("📄 Validating DRL Code:\n");
            response.append("```\n");
            response.append(drlCode.length() > 200 ? 
                drlCode.substring(0, 200) + "..." : drlCode);
            response.append("\n```\n\n");

            // Perform validation using the underlying service
            String validationResult = validationService.validateDRLStructure(drlCode);
            boolean isValid = !validationResult.contains("FAILED");

            if (isValid) {
                response.append("✅ **VALIDATION PASSED**\n\n");
                response.append("📋 **Analysis Results:**\n");
                response.append("  • Syntax is correct\n");
                response.append("  • Structure is valid\n");
                response.append("  • Package declaration found\n");
                response.append("  • Rules are well-formed\n");
                response.append("  • Ready for execution\n\n");
                
                response.append("🎯 **Status:** VALID - Proceed to execution\n");
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
                
                response.append("🔧 **Status:** INVALID - Fix errors and retry\n");
            }

            return response.toString();

        } catch (Exception e) {
            return "❌ VALIDATION ERROR: " + e.getMessage() + 
                   "\n\nTroubleshooting:\n" +
                   "  • Check for malformed DRL code\n" +
                   "  • Ensure code is complete and not truncated\n" +
                   "  • Verify proper encoding/formatting";
        }
    }

    @Tool("STEP 2: Get detailed validation guidance and tips for writing correct DRL code. Use this when you need help understanding DRL syntax requirements or common validation issues.")
    public String getValidationGuidance() {
        StringBuilder response = new StringBuilder();
        response.append("📚 DRL Validation Guidance\n");
        response.append("=".repeat(26) + "\n\n");
        
        response.append("🎯 **Required DRL Structure:**\n");
        response.append("  1. Package declaration: package com.example;\n");
        response.append("  2. Import statements (if needed)\n");
        response.append("  3. Declare blocks for fact types\n");
        response.append("  4. Rules with when/then/end structure\n\n");
        
        response.append("📝 **Declare Block Format:**\n");
        response.append("  declare TypeName\n");
        response.append("      fieldName : FieldType\n");
        response.append("      anotherField : String\n");
        response.append("  end\n\n");
        
        response.append("📝 **Rule Format:**\n");
        response.append("  rule \"Rule Name\"\n");
        response.append("  when\n");
        response.append("      $obj : TypeName( condition )\n");
        response.append("  then\n");
        response.append("      // Java code here\n");
        response.append("      System.out.println(\"Fired!\");\n");
        response.append("  end\n\n");
        
        response.append("⚠️ **Common Validation Errors:**\n");
        response.append("  • Missing package declaration\n");
        response.append("  • Unclosed declare/rule blocks\n");
        response.append("  • Invalid field type names\n");
        response.append("  • Missing semicolons in then clause\n");
        response.append("  • Incorrect when clause syntax\n\n");
        
        response.append("💡 **Best Practices:**\n");
        response.append("  • Always start with package declaration\n");
        response.append("  • Use meaningful rule names\n");
        response.append("  • Keep declare blocks simple\n");
        response.append("  • Test with basic rules first\n");
        
        return response.toString();
    }
}