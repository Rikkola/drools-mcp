package org.drools.agentic.example.services.execution;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.drools.execution.DRLRunnerResult;
import org.drools.service.DRLExecutionService;

import java.util.List;

/**
 * Simple service class that provides Tool-annotated methods for executing DRL code with JSON facts.
 * This class accepts DRL code and JSON facts directly without requiring DefinitionStorage.
 */
public class SimpleDRLExecutionToolService {
    private final DRLExecutionService executionService = new DRLExecutionService();

    public SimpleDRLExecutionToolService() {
        // No storage needed - works with direct DRL and JSON input
    }

    @Tool("Execute DRL code with JSON facts")
    public String executeDRLWithFacts(@P("DRL code to execute") String drlCode, 
                                     @P("JSON facts to insert") String jsonFacts,
                                     @P("maximum rule activations (0 for unlimited)") int maxActivations) {
        try {
            StringBuilder response = new StringBuilder();
            response.append("🚀 Executing DRL code with JSON facts:\n");
            response.append("=" .repeat(40) + "\n\n");
            
            // Show the DRL being executed
            response.append("📜 DRL Code:\n");
            response.append("-".repeat(10) + "\n");
            response.append(drlCode).append("\n\n");
            
            // Show the facts being inserted
            response.append("📊 JSON Facts:\n");
            response.append("-".repeat(12) + "\n");
            response.append(jsonFacts).append("\n\n");
            
            // Execute the DRL with facts
            DRLRunnerResult result = executionService.executeDRLWithJsonFacts(
                drlCode, jsonFacts, maxActivations);
            
            response.append("⚡ Execution Results:\n");
            response.append("-".repeat(18) + "\n");
            response.append("✅ Execution completed successfully!\n");
            response.append(String.format("🔥 Rules fired: %d\n", result.firedRules()));
            response.append(String.format("📊 Facts in working memory: %d\n", result.objects().size()));
            
            if (!result.objects().isEmpty()) {
                response.append("\n💾 Working memory contents:\n");
                for (Object fact : result.objects()) {
                    response.append(String.format("  • %s\n", fact.toString()));
                }
            } else {
                response.append("\n🗑️  No facts remaining in working memory.\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            return String.format("❌ Execution failed: %s\n\nPlease check your DRL syntax and JSON facts format.", e.getMessage());
        }
    }

}
