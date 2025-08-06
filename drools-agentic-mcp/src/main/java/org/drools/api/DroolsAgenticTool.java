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

import java.util.List;

public class DroolsAgenticTool {


    @Tool(description = "Provides the Domain model")
    public String getModel() {
      return "TODO get the model. Maybe the DRL author should also generate DRL declarations for the models used and those can be served here.";
    }

    @Tool(description = "Test JSON input. Format needs to match the existing model.")
    public String runKnowledgeBase(@ToolArg(description = "JSON input matching the model") String json) {
      return "TODO run rules using KnowledgeRunnerService";
    }

    @Tool(description = "When knowledge base does not know what to do. This tool can be used to improve it.")
    public String improveKnowledgeBase(@ToolArg(description = "Requirements") String requirements) {
      return "TODO put MainWorkflowRunner to work";
    }
}
