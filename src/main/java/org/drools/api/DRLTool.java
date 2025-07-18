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

/**
 * Main MCP tool provider class for DRL operations.
 * This class serves as the API layer, delegating business logic to service classes.
 */
public class DRLTool {

    private final DRLValidationService validationService;
    private final DRLExecutionService executionService;
    private final DefinitionManagementService definitionService;

    public DRLTool() {
        this.validationService = new DRLValidationService();
        this.executionService = new DRLExecutionService();
        this.definitionService = new DefinitionManagementService();
    }

    public DRLTool(DRLValidationService validationService, 
                   DRLExecutionService executionService,
                   DefinitionManagementService definitionService) {
        this.validationService = validationService;
        this.executionService = executionService;
        this.definitionService = definitionService;
    }

    @Tool(description = "Validates the Drools DRL code is correctly structured")
    public String validateDRLStructure(@ToolArg(description = "Drools DRL code") String code) {
        try {
            String validationResult = validationService.validateDRLStructure(code);
            return JsonResponseBuilder.create()
                    .success()
                    .field("validationResult", validationResult)
                    .build();
        } catch (DRLValidationException e) {
            return JsonResponseBuilder.create()
                    .error(e.getMessage())
                    .build();
        }
    }

    @Tool(description = "Executes Drools DRL code with external facts provided as JSON and returns all facts " +
                       "in working memory after rule execution. Use this when you have DRL rules that need to " +
                       "process specific data objects. The DRL should contain rules but may not need data creation " +
                       "rules since facts are provided externally. External facts should be provided as JSON objects " +
                       "with a '_type' field to specify the object type. Returns JSON-formatted list of all facts in working " +
                       "memory after rule execution.")
    public String runDRLWithExternalFacts(@ToolArg(description = "Complete Drools DRL code including package declaration " +
                                                        "and rules. May include declared types. Should contain business " +
                                                        "logic rules that will process the external facts. Example: " +
                                                        "\"package org.example; rule 'ProcessData' when $obj: MyObject() " +
                                                        "then ... end\"") String drlCode,
                                   @ToolArg(description = "JSON array of external facts to insert into working memory " +
                                                        "before rule execution. Each fact should be a JSON object with a '_type' field " +
                                                        "to specify the object type. Example: " +
                                                        "\"[{\\\"_type\\\":\\\"Person\\\", \\\"name\\\":\\\"John\\\", \\\"age\\\":25}, " +
                                                        "{\\\"_type\\\":\\\"Person\\\", \\\"name\\\":\\\"Jane\\\", \\\"age\\\":30}]\"") String externalFactsJson,
                                   @ToolArg(description = "Maximum number of rule activations to fire (0 for unlimited). " +
                                                        "Use this to prevent infinite loops or limit rule execution for performance.") 
                                   int maxActivations) {
        try {
            List<Object> facts = executionService.executeDRLWithJsonFacts(drlCode, externalFactsJson, maxActivations);
            return JsonResponseBuilder.create()
                    .facts(facts)
                    .build();
        } catch (DRLExecutionException e) {
            return JsonResponseBuilder.create()
                    .error(e.getMessage())
                    .build();
        }
    }

    @Tool(description = "Add a single Drools definition (declared type, function, global, import, etc.) to the storage. " +
                       "If a definition with the same name already exists, it will be replaced.")
    public String addDefinition(@ToolArg(description = "Name/identifier of the definition (e.g., 'Person', 'calculateAge', etc.)") String name,
                         @ToolArg(description = "Type of definition: 'declare' for declared types, 'function' for functions, " +
                                               "'global' for global variables, 'import' for imports, etc.") String type,
                         @ToolArg(description = "Complete DRL content of the definition. For declared types: " +
                                               "'declare Person name: String age: int end'. For functions: " +
                                               "'function int calculateAge(Date birthDate) { ... }'. For imports: " +
                                               "'import java.util.Date'.") String content) {
        try {
            DefinitionStorage.DroolsDefinition oldDefinition = definitionService.addDefinition(name, type, content);
            
            String action = oldDefinition != null ? "replaced" : "added";
            return JsonResponseBuilder.create()
                    .success()
                    .action(action)
                    .field("name", name)
                    .field("type", type)
                    .build();
        } catch (IllegalArgumentException e) {
            return JsonResponseBuilder.create()
                    .error(e.getMessage())
                    .build();
        }
    }

    @Tool(description = "Get a list of all stored Drools definitions with their names, types, and summary information.")
    public String getAllDefinitions() {
        try {
            List<DefinitionStorage.DroolsDefinition> definitions = definitionService.getAllDefinitions();
            return JsonResponseBuilder.create()
                    .success()
                    .definitions(definitions)
                    .build();
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                    .error(e.getMessage())
                    .build();
        }
    }

    @Tool(description = "Get a specific definition by name, including its full content.")
    public String getDefinition(@ToolArg(description = "Name of the definition to retrieve") String name) {
        try {
            DefinitionStorage.DroolsDefinition definition = definitionService.getDefinition(name);
            return JsonResponseBuilder.create()
                    .success()
                    .field("name", definition.getName())
                    .field("type", definition.getType())
                    .field("lastModified", definition.getLastModified())
                    .field("content", definition.getContent())
                    .build();
        } catch (DefinitionNotFoundException e) {
            return JsonResponseBuilder.create()
                    .notFound(e.getMessage())
                    .build();
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                    .error(e.getMessage())
                    .build();
        }
    }

    @Tool(description = "Generate a complete DRL string with all stored definitions, properly organized by type.")
    public String generateDRLFromDefinitions(@ToolArg(description = "Package name to use in the generated DRL (optional)") String packageName) {
        try {
            String drlContent = definitionService.generateDRLFromDefinitions(packageName);
            int definitionCount = definitionService.getDefinitionCount();
            
            return JsonResponseBuilder.create()
                    .success()
                    .drlContent(drlContent, definitionCount)
                    .build();
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                    .error(e.getMessage())
                    .build();
        }
    }

    @Tool(description = "Get a summary of all stored definitions grouped by type.")
    public String getDefinitionsSummary() {
        try {
            String summary = definitionService.getDefinitionsSummary();
            return JsonResponseBuilder.create()
                    .success()
                    .summary(summary)
                    .build();
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                    .error(e.getMessage())
                    .build();
        }
    }

    @Tool(description = "Remove a definition by name.")
    public String removeDefinition(@ToolArg(description = "Name of the definition to remove") String name) {
        try {
            DefinitionStorage.DroolsDefinition removed = definitionService.removeDefinition(name);
            return JsonResponseBuilder.create()
                    .success()
                    .action("removed")
                    .field("name", name)
                    .field("type", removed.getType())
                    .build();
        } catch (DefinitionNotFoundException e) {
            return JsonResponseBuilder.create()
                    .notFound(e.getMessage())
                    .build();
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                    .error(e.getMessage())
                    .build();
        }
    }
}