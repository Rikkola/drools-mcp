package dev.langchain4j.agentic.example;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.drools.service.DRLExecutionService;
import org.drools.service.DefinitionManagementService;
import org.drools.storage.DefinitionStorage;

import java.util.List;

/**
 * Service class that provides Tool-annotated methods for executing DRL code.
 * This class wraps the DRLExecutionService functionality to make it usable by AI agents.
 */
public class DRLExecutionToolService {
    private final DRLExecutionService executionService = new DRLExecutionService();
    private final DefinitionManagementService definitionService;

    public DRLExecutionToolService(DefinitionStorage storage) {
        this.definitionService = new DefinitionManagementService(storage);
    }

    @Tool("Execute DRL code with JSON facts")
    public String executeDRLWithJsonFacts(@P("DRL code to execute") String drlCode, 
                                         @P("JSON facts to insert") String jsonFacts, 
                                         @P("maximum rule activations (0 for unlimited)") int maxActivations) {
        try {
            List<Object> results = executionService.executeDRLWithJsonFacts(drlCode, jsonFacts, maxActivations);
            
            StringBuilder response = new StringBuilder();
            response.append(String.format("DRL execution completed successfully!\n"));
            response.append(String.format("Facts in working memory: %d\n", results.size()));
            
            if (!results.isEmpty()) {
                response.append("Working memory contents:\n");
                for (Object fact : results) {
                    response.append(String.format("  - %s\n", fact.toString()));
                }
            } else {
                response.append("No facts remaining in working memory.\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            return String.format("DRL execution failed: %s", e.getMessage());
        }
    }

    @Tool("Execute JSON facts against all stored DRL definitions")
    public String executeJsonFactsAgainstStoredDefinitions(@P("JSON facts to insert") String jsonFacts, 
                                                          @P("maximum rule activations (0 for unlimited)") int maxActivations) {
        try {
            List<Object> results = executionService.executeDRLWithJsonFactsAgainstStoredDefinitions(
                jsonFacts, maxActivations, definitionService);
            
            StringBuilder response = new StringBuilder();
            response.append(String.format("DRL execution against stored definitions completed successfully!\n"));
            response.append(String.format("Used %d stored definitions\n", definitionService.getDefinitionCount()));
            response.append(String.format("Facts in working memory: %d\n", results.size()));
            
            if (!results.isEmpty()) {
                response.append("Working memory contents:\n");
                for (Object fact : results) {
                    response.append(String.format("  - %s\n", fact.toString()));
                }
            } else {
                response.append("No facts remaining in working memory.\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            return String.format("DRL execution failed: %s", e.getMessage());
        }
    }

    @Tool("Execute DRL code without external facts (using only rules that create facts)")
    public String executeDRLOnly(@P("DRL code to execute") String drlCode, 
                                @P("maximum rule activations (0 for unlimited)") int maxActivations) {
        try {
            // Execute with empty JSON facts
            List<Object> results = executionService.executeDRLWithJsonFacts(drlCode, "[]", maxActivations);
            
            StringBuilder response = new StringBuilder();
            response.append(String.format("DRL execution completed successfully!\n"));
            response.append(String.format("Facts in working memory: %d\n", results.size()));
            
            if (!results.isEmpty()) {
                response.append("Working memory contents:\n");
                for (Object fact : results) {
                    response.append(String.format("  - %s\n", fact.toString()));
                }
            } else {
                response.append("No facts remaining in working memory.\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            return String.format("DRL execution failed: %s", e.getMessage());
        }
    }

    @Tool("Get generated DRL from all stored definitions")
    public String getGeneratedDRLFromDefinitions(@P("package name (optional)") String packageName) {
        try {
            if (definitionService.getDefinitionCount() == 0) {
                return "No definitions stored. Please add some definitions first.";
            }
            
            String drl = definitionService.generateDRLFromDefinitions(packageName != null ? packageName : "org.drools.generated");
            return String.format("Generated DRL from %d definitions:\n\n%s", 
                               definitionService.getDefinitionCount(), drl);
        } catch (Exception e) {
            return String.format("Failed to generate DRL: %s", e.getMessage());
        }
    }

    @Tool("Execute stored definitions with JSON facts (combined operation)")
    public String executeStoredDefinitionsWithJsonFacts(@P("JSON facts to insert") String jsonFacts, 
                                                       @P("maximum rule activations (0 for unlimited)") int maxActivations) {
        try {
            if (definitionService.getDefinitionCount() == 0) {
                return "No definitions stored. Please add some definitions first using the definition storage tools.";
            }

            // Show what definitions we're using
            StringBuilder response = new StringBuilder();
            response.append(String.format("Executing with %d stored definitions:\n", definitionService.getDefinitionCount()));
            response.append(definitionService.getDefinitionsSummary()).append("\n");
            
            // Execute the facts
            List<Object> results = executionService.executeDRLWithJsonFactsAgainstStoredDefinitions(
                jsonFacts, maxActivations, definitionService);
            
            response.append(String.format("Execution completed successfully!\n"));
            response.append(String.format("Facts in working memory: %d\n", results.size()));
            
            if (!results.isEmpty()) {
                response.append("Working memory contents:\n");
                for (Object fact : results) {
                    response.append(String.format("  - %s\n", fact.toString()));
                }
            } else {
                response.append("No facts remaining in working memory.\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            return String.format("Execution failed: %s", e.getMessage());
        }
    }
}