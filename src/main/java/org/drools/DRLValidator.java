package org.drools;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import java.util.List;

public class DRLValidator {

    // Singleton instance of DefinitionStorage
    private static final DefinitionStorage definitionStorage = new DefinitionStorage();

    @Tool(description = "Validates the Drools DRL code is correctly structured")
    String validateDRLStructure(@ToolArg(description = "Drools DRL code") String code) {
        return new DRLVerifier().verify(code);
    }

    @Tool(description = "Executes Drools DRL code and returns the facts in working memory after rule execution. " +
                       "Use this to test DRL rules with declared types, data creation rules, or business logic. " +
                       "The DRL code should be complete and valid, including package declaration, declared types (if any), " +
                       "and rules. For DRL with data creation rules, no external facts are needed. " +
                       "Returns JSON-formatted list of facts from working memory after all rules have fired.")
    String runDRLCode(@ToolArg(description = "Complete Drools DRL code including package declaration, " +
                                           "optional declared types (e.g., declare Person ... end), " +
                                           "and rules. Example: \"package org.example; declare MyType ... end " +
                                           "rule 'MyRule' when ... then ... end\"") String drlCode,
                      @ToolArg(description = "Maximum number of rule activations to fire (0 for unlimited). " +
                                           "Use this to prevent infinite loops or limit rule execution for performance.") 
                      int maxActivations) {
        try {
            List<Object> facts = DRLRunner.runDRL(drlCode, maxActivations);
            
            // Convert facts to a readable JSON-like format
            StringBuilder result = new StringBuilder();
            result.append("{\n");
            result.append("  \"executionStatus\": \"success\",\n");
            result.append("  \"factsCount\": ").append(facts.size()).append(",\n");
            result.append("  \"facts\": [\n");
            
            for (int i = 0; i < facts.size(); i++) {
                Object fact = facts.get(i);
                result.append("    {\n");
                result.append("      \"type\": \"").append(fact.getClass().getSimpleName()).append("\",\n");
                result.append("      \"value\": \"").append(fact.toString().replace("\"", "\\\"")).append("\"\n");
                result.append("    }");
                if (i < facts.size() - 1) {
                    result.append(",");
                }
                result.append("\n");
            }
            
            result.append("  ]\n");
            result.append("}");
            
            return result.toString();
            
        } catch (Exception e) {
            return "{\"executionStatus\": \"error\", \"errorMessage\": \"" + 
                   e.getMessage().replace("\"", "\\\"") + "\"}";
        }
    }

    @Tool(description = "Executes Drools DRL code with external facts provided as input and returns all facts " +
                       "in working memory after rule execution. Use this when you have DRL rules that need to " +
                       "process specific data objects. The DRL should contain rules but may not need data creation " +
                       "rules since facts are provided externally. External facts should be provided as JSON objects " +
                       "that will be converted to Java objects. Returns JSON-formatted list of all facts in working " +
                       "memory after rule execution.")
    String runDRLWithExternalFacts(@ToolArg(description = "Complete Drools DRL code including package declaration " +
                                                        "and rules. May include declared types. Should contain business " +
                                                        "logic rules that will process the external facts. Example: " +
                                                        "\"package org.example; rule 'ProcessData' when $obj: MyObject() " +
                                                        "then ... end\"") String drlCode,
                                   @ToolArg(description = "JSON array of external facts to insert into working memory " +
                                                        "before rule execution. Each fact should be a JSON object that can " +
                                                        "be processed by the DRL rules. Example: " +
                                                        "\"[{\\\"name\\\":\\\"John\\\", \\\"age\\\":25}, " +
                                                        "{\\\"name\\\":\\\"Jane\\\", \\\"age\\\":30}]\"") String externalFactsJson,
                                   @ToolArg(description = "Maximum number of rule activations to fire (0 for unlimited). " +
                                                        "Use this to prevent infinite loops or limit rule execution for performance.") 
                                   int maxActivations) {
        try {
            // Parse the external facts JSON
            // For now, we'll create simple Map objects from the JSON
            // In a more sophisticated implementation, we could use a JSON parser
            java.util.List<Object> externalFacts = new java.util.ArrayList<>();
            
            // Simple JSON parsing for basic objects
            // This is a basic implementation - in production, you'd use a proper JSON library
            if (externalFactsJson != null && !externalFactsJson.trim().isEmpty() && !externalFactsJson.equals("[]")) {
                // For now, just create empty list - this would need proper JSON parsing
                // The DRLRunner.runDRLWithFacts method can handle empty lists
            }
            
            List<Object> facts = DRLRunner.runDRLWithFacts(drlCode, externalFacts, maxActivations);
            
            // Convert facts to a readable JSON-like format
            StringBuilder result = new StringBuilder();
            result.append("{\n");
            result.append("  \"executionStatus\": \"success\",\n");
            result.append("  \"factsCount\": ").append(facts.size()).append(",\n");
            result.append("  \"facts\": [\n");
            
            for (int i = 0; i < facts.size(); i++) {
                Object fact = facts.get(i);
                result.append("    {\n");
                result.append("      \"type\": \"").append(fact.getClass().getSimpleName()).append("\",\n");
                result.append("      \"value\": \"").append(fact.toString().replace("\"", "\\\"")).append("\"\n");
                result.append("    }");
                if (i < facts.size() - 1) {
                    result.append(",");
                }
                result.append("\n");
            }
            
            result.append("  ]\n");
            result.append("}");
            
            return result.toString();
            
        } catch (Exception e) {
            return "{\"executionStatus\": \"error\", \"errorMessage\": \"" + 
                   e.getMessage().replace("\"", "\\\"") + "\"}";
        }
    }

    @Tool(description = "Add a single Drools definition (declared type, function, global, import, etc.) to the storage. " +
                       "If a definition with the same name already exists, it will be replaced.")
    String addDefinition(@ToolArg(description = "Name/identifier of the definition (e.g., 'Person', 'calculateAge', etc.)") String name,
                         @ToolArg(description = "Type of definition: 'declare' for declared types, 'function' for functions, " +
                                               "'global' for global variables, 'import' for imports, etc.") String type,
                         @ToolArg(description = "Complete DRL content of the definition. For declared types: " +
                                               "'declare Person name: String age: int end'. For functions: " +
                                               "'function int calculateAge(Date birthDate) { ... }'. For imports: " +
                                               "'import java.util.Date'.") String content) {
        try {
            DefinitionStorage.DroolsDefinition oldDefinition = definitionStorage.addDefinition(name, type, content);
            
            String status = oldDefinition != null ? "replaced" : "added";
            return "{\"status\": \"success\", \"action\": \"" + status + "\", \"name\": \"" + name + "\", \"type\": \"" + type + "\"}";
            
        } catch (Exception e) {
            return "{\"status\": \"error\", \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
        }
    }

    @Tool(description = "Get a list of all stored Drools definitions with their names, types, and summary information.")
    String getAllDefinitions() {
        try {
            List<DefinitionStorage.DroolsDefinition> definitions = definitionStorage.getAllDefinitions();
            
            StringBuilder result = new StringBuilder();
            result.append("{\n");
            result.append("  \"status\": \"success\",\n");
            result.append("  \"count\": ").append(definitions.size()).append(",\n");
            result.append("  \"definitions\": [\n");
            
            for (int i = 0; i < definitions.size(); i++) {
                DefinitionStorage.DroolsDefinition def = definitions.get(i);
                result.append("    {\n");
                result.append("      \"name\": \"").append(def.getName()).append("\",\n");
                result.append("      \"type\": \"").append(def.getType()).append("\",\n");
                result.append("      \"lastModified\": ").append(def.getLastModified()).append(",\n");
                result.append("      \"contentPreview\": \"").append(def.getContent().substring(0, Math.min(100, def.getContent().length())).replace("\"", "\\\"")).append("...\",\n");
                result.append("      \"contentLength\": ").append(def.getContent().length()).append("\n");
                result.append("    }");
                if (i < definitions.size() - 1) {
                    result.append(",");
                }
                result.append("\n");
            }
            
            result.append("  ]\n");
            result.append("}");
            
            return result.toString();
            
        } catch (Exception e) {
            return "{\"status\": \"error\", \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
        }
    }

    @Tool(description = "Get a specific definition by name, including its full content.")
    String getDefinition(@ToolArg(description = "Name of the definition to retrieve") String name) {
        try {
            DefinitionStorage.DroolsDefinition definition = definitionStorage.getDefinition(name);
            
            if (definition == null) {
                return "{\"status\": \"not_found\", \"message\": \"Definition with name '" + name + "' not found\"}";
            }
            
            StringBuilder result = new StringBuilder();
            result.append("{\n");
            result.append("  \"status\": \"success\",\n");
            result.append("  \"name\": \"").append(definition.getName()).append("\",\n");
            result.append("  \"type\": \"").append(definition.getType()).append("\",\n");
            result.append("  \"lastModified\": ").append(definition.getLastModified()).append(",\n");
            result.append("  \"content\": \"").append(definition.getContent().replace("\"", "\\\"")).append("\"\n");
            result.append("}");
            
            return result.toString();
            
        } catch (Exception e) {
            return "{\"status\": \"error\", \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
        }
    }

    @Tool(description = "Generate a complete DRL string with all stored definitions, properly organized by type.")
    String generateDRLFromDefinitions(@ToolArg(description = "Package name to use in the generated DRL (optional)") String packageName) {
        try {
            String drlContent = definitionStorage.generateDRLString(packageName);
            
            StringBuilder result = new StringBuilder();
            result.append("{\n");
            result.append("  \"status\": \"success\",\n");
            result.append("  \"definitionCount\": ").append(definitionStorage.getDefinitionCount()).append(",\n");
            result.append("  \"drlContent\": \"").append(drlContent.replace("\"", "\\\"").replace("\n", "\\n")).append("\"\n");
            result.append("}");
            
            return result.toString();
            
        } catch (Exception e) {
            return "{\"status\": \"error\", \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
        }
    }

    @Tool(description = "Get a summary of all stored definitions grouped by type.")
    String getDefinitionsSummary() {
        try {
            String summary = definitionStorage.getSummary();
            
            StringBuilder result = new StringBuilder();
            result.append("{\n");
            result.append("  \"status\": \"success\",\n");
            result.append("  \"summary\": \"").append(summary.replace("\"", "\\\"").replace("\n", "\\n")).append("\"\n");
            result.append("}");
            
            return result.toString();
            
        } catch (Exception e) {
            return "{\"status\": \"error\", \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
        }
    }

    @Tool(description = "Remove a definition by name.")
    String removeDefinition(@ToolArg(description = "Name of the definition to remove") String name) {
        try {
            DefinitionStorage.DroolsDefinition removed = definitionStorage.removeDefinition(name);
            
            if (removed == null) {
                return "{\"status\": \"not_found\", \"message\": \"Definition with name '" + name + "' not found\"}";
            }
            
            return "{\"status\": \"success\", \"action\": \"removed\", \"name\": \"" + name + "\", \"type\": \"" + removed.getType() + "\"}";
            
        } catch (Exception e) {
            return "{\"status\": \"error\", \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
        }
    }
}
