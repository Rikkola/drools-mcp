package org.drools.agentic.example.services.registry;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.drools.agentic.example.registry.FactTypeDefinition;
import org.drools.agentic.example.registry.FactTypeRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tool service that provides fact type registry capabilities to AI agents.
 * Enables agents to query, create, and modify fact type declarations.
 */
public class FactTypeRegistryToolService {
    
    private final FactTypeRegistry registry;
    private final ObjectMapper objectMapper;
    
    public FactTypeRegistryToolService(FactTypeRegistry registry) {
        this.registry = registry;
        this.objectMapper = new ObjectMapper();
    }

    @Tool("Get all existing fact type names in the registry")
    public String getExistingFactTypeNames() {
        Set<String> typeNames = registry.getAllFactTypeNames();
        if (typeNames.isEmpty()) {
            return "No fact types are currently registered.";
        }
        return "Registered fact types: " + String.join(", ", typeNames);
    }

    @Tool("Get detailed information about all existing fact types")
    public String getExistingFactTypes() {
        if (registry.size() == 0) {
            return "No fact types are currently registered.";
        }
        
        return registry.getAllFactTypes().stream()
                .map(this::formatFactType)
                .collect(Collectors.joining("\n\n"));
    }

    @Tool("Get details of a specific fact type by name")
    public String getFactType(@P("typeName") String typeName) {
        Optional<FactTypeDefinition> factType = registry.getFactType(typeName);
        
        if (factType.isEmpty()) {
            return "Fact type '" + typeName + "' not found in registry.";
        }
        
        return formatFactType(factType.get());
    }

    @Tool("Check if a fact type exists in the registry")
    public String hasFactType(@P("typeName") String typeName) {
        boolean exists = registry.hasFactType(typeName);
        return "Fact type '" + typeName + "' " + (exists ? "exists" : "does not exist") + " in registry.";
    }

    @Tool("Create or update a fact type with field definitions")
    public String updateFactType(@P("typeName") String typeName, 
                                @P("packageName") String packageName,
                                @P("fieldsJson") String fieldsJson) {
        try {
            FactTypeDefinition definition = new FactTypeDefinition(typeName, packageName);
            
            // Parse fields JSON
            JsonNode fieldsNode = objectMapper.readTree(fieldsJson);
            
            if (fieldsNode.isArray()) {
                for (JsonNode fieldNode : fieldsNode) {
                    String fieldName = fieldNode.get("name").asText();
                    String fieldType = fieldNode.get("type").asText();
                    String defaultValue = fieldNode.has("defaultValue") ? 
                        fieldNode.get("defaultValue").asText() : null;
                    boolean required = fieldNode.has("required") && 
                        fieldNode.get("required").asBoolean();
                    
                    definition.addField(fieldName, fieldType, defaultValue, required);
                }
            } else {
                // Handle object format
                fieldsNode.fields().forEachRemaining(entry -> {
                    String fieldName = entry.getKey();
                    JsonNode fieldInfo = entry.getValue();
                    
                    if (fieldInfo.isTextual()) {
                        // Simple format: "fieldName": "fieldType"
                        definition.addField(fieldName, fieldInfo.asText());
                    } else {
                        // Complex format with type, defaultValue, etc.
                        String fieldType = fieldInfo.get("type").asText();
                        String defaultValue = fieldInfo.has("defaultValue") ? 
                            fieldInfo.get("defaultValue").asText() : null;
                        boolean required = fieldInfo.has("required") && 
                            fieldInfo.get("required").asBoolean();
                        
                        definition.addField(fieldName, fieldType, defaultValue, required);
                    }
                });
            }
            
            boolean existed = registry.hasFactType(typeName);
            registry.updateFactType(typeName, definition);
            
            return (existed ? "Updated" : "Created") + " fact type '" + typeName + 
                   "' with " + definition.getFields().size() + " fields.";
                   
        } catch (Exception e) {
            return "Error creating/updating fact type: " + e.getMessage();
        }
    }

    @Tool("Add a single field to an existing fact type")
    public String addFieldToFactType(@P("typeName") String typeName,
                                   @P("fieldName") String fieldName,
                                   @P("fieldType") String fieldType,
                                   @P("defaultValue") String defaultValue) {
        Optional<FactTypeDefinition> existing = registry.getFactType(typeName);
        
        if (existing.isEmpty()) {
            return "Fact type '" + typeName + "' not found. Create it first using updateFactType.";
        }
        
        FactTypeDefinition definition = existing.get();
        definition.addField(fieldName, fieldType, 
                          defaultValue != null && !defaultValue.trim().isEmpty() ? defaultValue : null);
        
        registry.updateFactType(typeName, definition);
        
        return "Added field '" + fieldName + " : " + fieldType + "' to fact type '" + typeName + "'.";
    }

    @Tool("Remove a fact type from the registry")
    public String removeFactType(@P("typeName") String typeName) {
        boolean removed = registry.removeFactType(typeName);
        
        if (removed) {
            return "Successfully removed fact type '" + typeName + "' from registry.";
        } else {
            return "Fact type '" + typeName + "' not found in registry.";
        }
    }

    @Tool("Generate DRL declare blocks for all registered fact types")
    public String generateAllDRLDeclarations() {
        if (registry.size() == 0) {
            return "No fact types registered to generate declarations for.";
        }
        
        return registry.generateDRLDeclarations();
    }

    @Tool("Generate DRL declare blocks for specific fact types")
    public String generateDRLDeclarations(@P("typeNames") String typeNamesCommaDelimited) {
        Set<String> typeNames = Set.of(typeNamesCommaDelimited.split(","));
        typeNames = typeNames.stream().map(String::trim).collect(Collectors.toSet());
        
        String declarations = registry.generateDRLDeclarations(typeNames);
        
        if (declarations.isEmpty()) {
            return "No matching fact types found for: " + String.join(", ", typeNames);
        }
        
        return declarations;
    }

    @Tool("Load fact types from existing DRL content")
    public String loadFromDRL(@P("drlContent") String drlContent, 
                             @P("packageName") String packageName) {
        try {
            int loadedCount = registry.loadFromDRL(drlContent, packageName);
            
            if (loadedCount == 0) {
                return "No fact type declarations found in the provided DRL content.";
            }
            
            return "Successfully loaded " + loadedCount + " fact type(s) from DRL content.";
            
        } catch (Exception e) {
            return "Error loading from DRL: " + e.getMessage();
        }
    }

    @Tool("Get registry statistics and summary")
    public String getRegistryInfo() {
        int count = registry.size();
        
        if (count == 0) {
            return "Registry is empty - no fact types registered.";
        }
        
        Set<String> typeNames = registry.getAllFactTypeNames();
        int totalFields = registry.getAllFactTypes().stream()
                .mapToInt(def -> def.getFields().size())
                .sum();
        
        return String.format("Registry contains %d fact type(s) with %d total fields:\n%s", 
                           count, totalFields, String.join(", ", typeNames));
    }

    @Tool("Clear all fact types from the registry")
    public String clearRegistry() {
        int previousCount = registry.size();
        registry.clear();
        
        return "Cleared registry. Removed " + previousCount + " fact type(s).";
    }

    /**
     * Formats a fact type definition for human-readable output.
     */
    private String formatFactType(FactTypeDefinition definition) {
        StringBuilder sb = new StringBuilder();
        sb.append("Fact Type: ").append(definition.getTypeName());
        
        if (definition.getPackageName() != null) {
            sb.append(" (package: ").append(definition.getPackageName()).append(")");
        }
        
        sb.append("\nFields (").append(definition.getFields().size()).append("):");
        
        if (definition.getFields().isEmpty()) {
            sb.append(" none");
        } else {
            definition.getFields().values().forEach(field -> {
                sb.append("\n  - ").append(field.toString());
            });
        }
        
        sb.append("\nLast Modified: ").append(definition.getLastModified());
        
        return sb.toString();
    }
}