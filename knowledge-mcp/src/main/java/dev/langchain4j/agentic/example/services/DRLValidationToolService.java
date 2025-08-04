package dev.langchain4j.agentic.example.services;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.drools.service.DRLValidationService;
import org.drools.service.DefinitionManagementService;
import org.drools.storage.DefinitionStorage;

/**
 * Service class that provides Tool-annotated methods for validating DRL code.
 * This class wraps the DRLValidationService functionality to make it usable by AI agents.
 */
public class DRLValidationToolService {
    private final DRLValidationService validationService = new DRLValidationService();
    private final DefinitionManagementService definitionService;

    public DRLValidationToolService(DefinitionStorage storage) {
        this.definitionService = new DefinitionManagementService(storage);
    }

    @Tool("Validate DRL code structure and syntax")
    public String validateDRLCode(@P("DRL code to validate") String drlCode) {
        try {
            String result = validationService.validateDRLStructure(drlCode);
            
            StringBuilder response = new StringBuilder();
            response.append("DRL Validation Results:\n");
            response.append("======================\n");
            
            if ("Code looks good".equals(result)) {
                response.append("✅ Validation PASSED: The DRL code structure is valid.\n");
                response.append("No syntax errors or structural issues detected.\n");
            } else {
                response.append("⚠️  Validation NOTES/WARNINGS:\n");
                response.append(result).append("\n");
                response.append("\nNote: These are validation notes, not necessarily errors.\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            return String.format("❌ Validation FAILED: %s\n\nPlease check your DRL syntax and structure.", e.getMessage());
        }
    }

    @Tool("Validate stored DRL definitions")
    public String validateStoredDefinitions() {
        try {
            if (definitionService.getDefinitionCount() == 0) {
                return "No definitions stored to validate. Please add some definitions first.";
            }

            StringBuilder response = new StringBuilder();
            response.append(String.format("Validating %d stored definitions:\n", definitionService.getDefinitionCount()));
            response.append("=====================================\n\n");

            // Generate complete DRL from stored definitions
            String completeDRL = definitionService.generateDRLFromDefinitions("org.drools.validation");
            
            // Validate the complete DRL
            String validationResult = validationService.validateDRLStructure(completeDRL);
            
            response.append("Generated DRL:\n");
            response.append("-------------\n");
            response.append(completeDRL).append("\n\n");
            
            response.append("Validation Results:\n");
            response.append("------------------\n");
            
            if ("Code looks good".equals(validationResult)) {
                response.append("✅ All stored definitions are valid!\n");
                response.append("The combined DRL structure is correct and ready for execution.\n");
            } else {
                response.append("⚠️  Validation notes for stored definitions:\n");
                response.append(validationResult).append("\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            return String.format("❌ Validation of stored definitions failed: %s", e.getMessage());
        }
    }

    @Tool("Validate DRL code and provide syntax guidance")
    public String validateWithGuidance(@P("DRL code to validate") String drlCode) {
        StringBuilder response = new StringBuilder();
        
        try {
            String validationResult = validationService.validateDRLStructure(drlCode);
            
            response.append("DRL Validation with Guidance:\n");
            response.append("============================\n\n");
            
            response.append("Code being validated:\n");
            response.append("```drl\n");
            response.append(drlCode);
            response.append("\n```\n\n");
            
            if ("Code looks good".equals(validationResult)) {
                response.append("✅ VALIDATION PASSED\n");
                response.append("Your DRL code structure is valid and ready for execution!\n\n");
                response.append("💡 Tips:\n");
                response.append("- Your syntax and structure look correct\n");
                response.append("- You can now execute this DRL with facts\n");
                response.append("- Consider adding more specific conditions if needed\n");
            } else {
                response.append("⚠️  VALIDATION NOTES\n");
                response.append("The validator found the following notes:\n");
                response.append(validationResult).append("\n\n");
                
                response.append("💡 Common DRL Guidelines:\n");
                response.append("- Make sure rule names are unique and in quotes\n");
                response.append("- Check that 'when' and 'then' clauses are properly structured\n");
                response.append("- Ensure all variables are properly declared with '$'\n");
                response.append("- Verify that fact types match your declared types\n");
                response.append("- End statements in 'then' clause with semicolons\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            response = new StringBuilder();
            response.append("❌ VALIDATION FAILED\n");
            response.append("Error: ").append(e.getMessage()).append("\n\n");
            
            response.append("💡 Common DRL Syntax Issues:\n");
            response.append("- Missing 'package' declaration at the top\n");
            response.append("- Incorrect rule structure: rule \"name\" when ... then ... end\n");
            response.append("- Missing 'end' keyword to close rules\n");
            response.append("- Syntax errors in conditions or actions\n");
            response.append("- Undefined fact types or fields\n");
            
            return response.toString();
        }
    }

    @Tool("Quick syntax check for DRL code snippet")
    public String quickSyntaxCheck(@P("DRL code snippet to check") String drlCode) {
        try {
            // Add basic package declaration if missing for validation
            String codeToValidate = drlCode;
            if (!drlCode.trim().startsWith("package")) {
                codeToValidate = "package org.drools.quickcheck;\n\n" + drlCode;
            }
            
            String result = validationService.validateDRLStructure(codeToValidate);
            
            if ("Code looks good".equals(result)) {
                return "✅ Quick Check PASSED - DRL syntax looks correct";
            } else {
                return String.format("⚠️  Quick Check NOTES: %s", result);
            }
        } catch (Exception e) {
            return String.format("❌ Quick Check FAILED: %s", e.getMessage());
        }
    }
}