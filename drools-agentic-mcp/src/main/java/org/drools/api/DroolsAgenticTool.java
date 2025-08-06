package org.drools.api;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import org.drools.exception.DefinitionNotFoundException;
import org.drools.exception.DRLExecutionException;
import org.drools.exception.DRLValidationException;
import org.drools.model.JsonResponseBuilder;
import org.drools.agentic.example.workflows.MainWorkflow;
import org.drools.agentic.example.config.ChatModels;
import org.drools.agentic.example.services.execution.KnowledgeRunnerService;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.data.message.UserMessage;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DroolsAgenticTool {

    private final MainWorkflow mainWorkflow;
    private final KnowledgeRunnerService knowledgeRunnerService;

    public DroolsAgenticTool() {
        this.mainWorkflow = new MainWorkflow();
        this.knowledgeRunnerService = new KnowledgeRunnerService();
    }

    @Tool(description = "Improve the knowledge base by creating new rules or modifying existing ones based on requirements")
    public String improveKnowledgeBase(@ToolArg(description = "Requirements for improving the knowledge base") String requirements) {
        try {
            // Create agent workflow with default chat models
            UntypedAgent agentWorkflow = mainWorkflow.createAgentWorkflow(
                ChatModels.OLLAMA_GRANITE_MODEL, 
                ChatModels.OLLAMA_GRANITE3_MOE_MODEL
            );
            
            // Execute the workflow with the requirements
            Object result = agentWorkflow.invoke(Map.of(
                "request", "Improve the Drools knowledge base based on these requirements: " + requirements
            ));
            
            return JsonResponseBuilder.create()
                .success()
                .field("result", "Knowledge base improvement completed")
                .field("details", result)
                .build();
            
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                .error("Error improving knowledge base: " + e.getMessage())
                .build();
        }
    }

    @Tool(description = "Execute rules with JSON facts using the shared knowledge base")
    public String executeRules(@ToolArg(description = "JSON facts to insert and execute rules against") String jsonFacts,
                              @ToolArg(description = "Maximum rule activations (0 for unlimited)") Integer maxActivations) {
        try {
            String result = knowledgeRunnerService.executeRules(jsonFacts, maxActivations);
            
            return JsonResponseBuilder.create()
                .success()
                .field("executionResult", result)
                .build();
                
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                .error("Rule execution failed: " + e.getMessage())
                .build();
        }
    }

    @Tool(description = "Get status and information about the shared knowledge base")
    public String getKnowledgeBaseStatus() {
        try {
            String status = knowledgeRunnerService.getKnowledgeBaseStatus();
            
            return JsonResponseBuilder.create()
                .success()
                .field("knowledgeBaseStatus", status)
                .build();
                
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                .error("Failed to get knowledge base status: " + e.getMessage())
                .build();
        }
    }

    @Tool(description = "Clear all facts from the shared knowledge base session")
    public String clearFacts() {
        try {
            String result = knowledgeRunnerService.clearFacts();
            
            return JsonResponseBuilder.create()
                .success()
                .field("clearResult", result)
                .build();
                
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                .error("Failed to clear facts: " + e.getMessage())
                .build();
        }
    }

    @Tool(description = "Execute rules multiple times with different fact sets in batch mode")
    public String executeBatch(@ToolArg(description = "JSON array of fact batches to process") String jsonFactBatches,
                              @ToolArg(description = "Maximum rule activations per batch (0 for unlimited)") Integer maxActivations) {
        try {
            String result = knowledgeRunnerService.executeBatch(jsonFactBatches, maxActivations);
            
            return JsonResponseBuilder.create()
                .success()
                .field("batchExecutionResult", result)
                .build();
                
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                .error("Batch execution failed: " + e.getMessage())
                .build();
        }
    }
}
