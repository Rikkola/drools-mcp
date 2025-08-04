package dev.langchain4j.agentic.example;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.drools.service.DRLExecutionService;
import org.drools.service.DefinitionManagementService;
import org.drools.storage.DefinitionStorage;

import java.util.List;

/**
 * Service class that provides Tool-annotated methods for executing stored DRL definitions with JSON facts.
 * This class focuses on executing rules from DefinitionStorage rather than accepting arbitrary DRL code.
 */
public class DRLExecutionToolService {
    private final DRLExecutionService executionService = new DRLExecutionService();
    private final DefinitionManagementService definitionService;

    public DRLExecutionToolService(DefinitionStorage storage) {
        this.definitionService = new DefinitionManagementService(storage);
    }

    @Tool("Execute JSON facts against all stored DRL definitions")
    public String executeWithJsonFacts(@P("JSON facts to insert") String jsonFacts, 
                                      @P("maximum rule activations (0 for unlimited)") int maxActivations) {
        try {
            if (definitionService.getDefinitionCount() == 0) {
                return "❌ No definitions stored. Please add some definitions first using the definition storage tools.";
            }

            // Show what definitions we're using
            StringBuilder response = new StringBuilder();
            response.append(String.format("🚀 Executing JSON facts against %d stored definitions:\n", definitionService.getDefinitionCount()));
            response.append("=" .repeat(50) + "\n\n");
            
            // Show stored definitions summary
            response.append("📋 Using stored definitions:\n");
            response.append(definitionService.getDefinitionsSummary()).append("\n");
            
            // Execute the facts
            List<Object> results = executionService.executeDRLWithJsonFactsAgainstStoredDefinitions(
                jsonFacts, maxActivations, definitionService);
            
            response.append("⚡ Execution Results:\n");
            response.append("-".repeat(20) + "\n");
            response.append(String.format("✅ Execution completed successfully!\n"));
            response.append(String.format("📊 Facts in working memory: %d\n", results.size()));
            
            if (!results.isEmpty()) {
                response.append("\n💾 Working memory contents:\n");
                for (Object fact : results) {
                    response.append(String.format("  • %s\n", fact.toString()));
                }
            } else {
                response.append("\n🗑️  No facts remaining in working memory.\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            return String.format("❌ Execution failed: %s\n\nPlease check your JSON facts format and stored definitions.", e.getMessage());
        }
    }

    @Tool("Execute empty facts against stored definitions (test rule firing without external data)")
    public String executeStoredDefinitionsOnly(@P("maximum rule activations (0 for unlimited)") int maxActivations) {
        try {
            if (definitionService.getDefinitionCount() == 0) {
                return "❌ No definitions stored. Please add some definitions first using the definition storage tools.";
            }

            StringBuilder response = new StringBuilder();
            response.append(String.format("🧪 Testing stored definitions without external facts:\n"));
            response.append("=" .repeat(45) + "\n\n");
            
            response.append("📋 Testing definitions:\n");
            response.append(definitionService.getDefinitionsSummary()).append("\n");
            
            // Execute with empty JSON facts to test rule creation and firing
            List<Object> results = executionService.executeDRLWithJsonFactsAgainstStoredDefinitions(
                "[]", maxActivations, definitionService);
            
            response.append("⚡ Test Results:\n");
            response.append("-".repeat(15) + "\n");
            response.append(String.format("✅ Test execution completed!\n"));
            response.append(String.format("📊 Facts created by rules: %d\n", results.size()));
            
            if (!results.isEmpty()) {
                response.append("\n💾 Facts created during execution:\n");
                for (Object fact : results) {
                    response.append(String.format("  • %s\n", fact.toString()));
                }
            } else {
                response.append("\n📝 No facts were created by the rules (this is normal for rules that only modify existing facts).\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            return String.format("❌ Test execution failed: %s", e.getMessage());
        }
    }

    @Tool("Get execution statistics and stored definitions info")
    public String getExecutionInfo() {
        try {
            StringBuilder response = new StringBuilder();
            response.append("📊 Execution Service Information:\n");
            response.append("=" .repeat(30) + "\n\n");
            
            int definitionCount = definitionService.getDefinitionCount();
            response.append(String.format("📋 Stored definitions: %d\n", definitionCount));
            
            if (definitionCount == 0) {
                response.append("⚠️  No definitions available for execution.\n");
                response.append("💡 Use the definition storage tools to add types, functions, and rules first.\n");
            } else {
                response.append("✅ Ready for execution!\n\n");
                response.append("📖 Definitions breakdown:\n");
                response.append(definitionService.getDefinitionsSummary());
                response.append("\n💡 You can now execute JSON facts against these definitions.\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            return String.format("❌ Failed to get execution info: %s", e.getMessage());
        }
    }
}
