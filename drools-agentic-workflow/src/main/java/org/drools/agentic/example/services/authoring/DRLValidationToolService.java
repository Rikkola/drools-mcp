package org.drools.agentic.example.services.authoring;

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

}
