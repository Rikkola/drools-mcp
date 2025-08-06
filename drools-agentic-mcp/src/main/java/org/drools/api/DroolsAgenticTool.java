package org.drools.api;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import org.drools.exception.DefinitionNotFoundException;
import org.drools.exception.DRLExecutionException;
import org.drools.exception.DRLValidationException;
import org.drools.model.JsonResponseBuilder;
import org.drools.service.DefinitionManagementService;
import org.drools.service.DRLExecutionService;
import org.drools.service.DRLValidationService;
import org.drools.storage.DefinitionStorage;
import org.drools.agentic.example.workflows.MainWorkflow;
import org.drools.agentic.example.config.ChatModels;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.data.message.UserMessage;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DroolsAgenticTool {

    private final MainWorkflow mainWorkflow;
    private final DRLExecutionService drlExecutionService;
    private final DefinitionManagementService definitionManagementService;

    public DroolsAgenticTool() {
        this.mainWorkflow = new MainWorkflow();
        this.drlExecutionService = new DRLExecutionService();
        this.definitionManagementService = new DefinitionManagementService();
    }

    @Tool(description = "Provides the Domain model definitions currently stored in the knowledge base")
    public String getModel() {
        try {
            List<DefinitionStorage.DroolsDefinition> definitions = definitionManagementService.getAllDefinitions();
            if (definitions.isEmpty()) {
                return "No domain model definitions found in the knowledge base.";
            }
            
            return JsonResponseBuilder.create()
                .success()
                .definitions(definitions)
                .build();
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                .error("Error retrieving domain model: " + e.getMessage())
                .build();
        }
    }

    @Tool(description = "Execute rules against JSON input. The JSON format needs to match the existing domain model.")
    public String runKnowledgeBase(@ToolArg(description = "JSON input matching the domain model") String json) {
        try {
            List<Object> facts = drlExecutionService.executeDRLWithJsonFactsAgainstStoredDefinitions(json, 0, definitionManagementService);
            return JsonResponseBuilder.create()
                .success()
                .facts(facts)
                .build();
        } catch (DRLExecutionException e) {
            return JsonResponseBuilder.create()
                .error("Rule execution failed: " + e.getMessage())
                .build();
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                .error("Unexpected error: " + e.getMessage())
                .build();
        }
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
}
