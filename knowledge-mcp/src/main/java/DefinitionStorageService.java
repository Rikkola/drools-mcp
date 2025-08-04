package dev.langchain4j.agentic.example;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Service class that provides Tool-annotated methods for managing Drools definitions.
 * This class wraps the DefinitionStorage functionality to make it usable by AI agents.
 */
public class DefinitionStorageService {
    private final org.drools.storage.DefinitionStorage storage;

    public DefinitionStorageService() {
        this.storage = new org.drools.storage.DefinitionStorage();
    }

    public DefinitionStorageService(org.drools.storage.DefinitionStorage storage) {
        this.storage = storage;
    }

    @Tool("Add a new Drools definition")
    public String addDefinition(@P("definition name") String name, 
                               @P("definition type") String type, 
                               @P("definition content") String content) {
        try {
            org.drools.storage.DefinitionStorage.DroolsDefinition oldDef = storage.addDefinition(name, type, content);
            if (oldDef != null) {
                return String.format("Updated definition '%s' of type '%s'. Previous definition was replaced.", name, type);
            } else {
                return String.format("Added new definition '%s' of type '%s' successfully.", name, type);
            }
        } catch (Exception e) {
            return String.format("Failed to add definition '%s': %s", name, e.getMessage());
        }
    }

    @Tool("Get a Drools definition by name")
    public String getDefinition(@P("definition name") String name) {
        org.drools.storage.DefinitionStorage.DroolsDefinition def = storage.getDefinition(name);
        if (def != null) {
            return String.format("Definition '%s' (type: %s):\n%s", def.getName(), def.getType(), def.getContent());
        } else {
            return String.format("Definition '%s' not found.", name);
        }
    }

    @Tool("Get all stored Drools definitions")
    public String getAllDefinitions() {
        var definitions = storage.getAllDefinitions();
        if (definitions.isEmpty()) {
            return "No definitions stored.";
        }
        
        StringBuilder result = new StringBuilder();
        result.append(String.format("Found %d definitions:\n", definitions.size()));
        for (var def : definitions) {
            result.append(String.format("- %s (%s)\n", def.getName(), def.getType()));
        }
        return result.toString();
    }

    @Tool("Get definitions by type")
    public String getDefinitionsByType(@P("definition type") String type) {
        var definitions = storage.getDefinitionsByType(type);
        if (definitions.isEmpty()) {
            return String.format("No definitions of type '%s' found.", type);
        }
        
        StringBuilder result = new StringBuilder();
        result.append(String.format("Found %d definitions of type '%s':\n", definitions.size(), type));
        for (var def : definitions) {
            result.append(String.format("- %s: %s\n", def.getName(), def.getContent().substring(0, Math.min(50, def.getContent().length())) + "..."));
        }
        return result.toString();
    }

    @Tool("Remove a Drools definition")
    public String removeDefinition(@P("definition name") String name) {
        org.drools.storage.DefinitionStorage.DroolsDefinition removed = storage.removeDefinition(name);
        if (removed != null) {
            return String.format("Removed definition '%s' of type '%s'.", removed.getName(), removed.getType());
        } else {
            return String.format("Definition '%s' not found, nothing to remove.", name);
        }
    }

    @Tool("Get summary of all definitions")
    public String getDefinitionsSummary() {
        return storage.getSummary();
    }

    @Tool("Generate complete DRL from all definitions")
    public String generateDRL(@P("package name (optional)") String packageName) {
        String drl = storage.generateDRLString(packageName);
        if (drl.trim().isEmpty() || drl.trim().equals("package " + packageName + ";\n\n")) {
            return "No definitions available to generate DRL.";
        }
        return "Generated DRL:\n" + drl;
    }

    @Tool("Check if definition exists")
    public String hasDefinition(@P("definition name") String name) {
        boolean exists = storage.hasDefinition(name);
        return String.format("Definition '%s' %s.", name, exists ? "exists" : "does not exist");
    }
}