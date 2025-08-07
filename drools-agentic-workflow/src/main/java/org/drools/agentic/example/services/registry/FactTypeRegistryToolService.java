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
 * Minimal tool service for fact type registry with only 5 essential tools.
 * Simplified for better LLM performance and reduced cognitive load.
 * 
 * WORKFLOW:
 * 1. STEP 1: getExistingFactTypes() - Check what already exists
 * 2. STEP 2: updateFactType() - Create new or modify existing types
 * 3. STEP 2b: addFieldToFactType() - Add single field to existing type
 * 4. STEP 3: generateAllDRLDeclarations() - Get all DRL declare blocks
 * 5. STEP 3b: generateDRLDeclarations(names) - Get specific DRL declare blocks
 */
public class FactTypeRegistryToolService {
    
    private final FactTypeRegistry registry;
    private final ObjectMapper objectMapper;
    
    public FactTypeRegistryToolService(FactTypeRegistry registry) {
        this.registry = registry;
        this.objectMapper = new ObjectMapper();
    }


    @Tool("STEP 1: Check what fact types already exist. Always call this FIRST before creating new types. Shows all existing fact types with their fields and details. NO PARAMETERS NEEDED. Example: If you see Person type with name/age fields, you can reuse it instead of creating a new one.")
    public String getExistingFactTypes() {
        if (registry.size() == 0) {
            return "No fact types are currently registered.";
        }
        
        return registry.getAllFactTypes().stream()
                .map(this::formatFactType)
                .collect(Collectors.joining("\n\n"));
    }



    @Tool("STEP 2: Create a new fact type OR modify an existing one. EXAMPLE: typeName='Person', packageName='com.example', fieldsJson='{\"name\":\"String\", \"age\":\"int\", \"active\":\"boolean\"}' creates Person with 3 fields. Use String, int, boolean, double as field types.")
    public String updateFactType(@P("typeName - The name of the fact type to create or update. Example: 'Person', 'Order', 'Customer'") String typeName, 
                                @P("packageName - The Java package name for the fact type. Example: 'com.example', 'org.drools.facts'") String packageName,
                                @P("fieldsJson - JSON object with field definitions. Format: {\"fieldName\":\"fieldType\"}. Example: '{\"name\":\"String\", \"age\":\"int\", \"active\":\"boolean\"}'. Use String, int, boolean, double, Date as types.") String fieldsJson) {
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

    @Tool("STEP 2b: Add ONE field to an existing fact type. EXAMPLE: typeName='Person', fieldName='email', fieldType='String', defaultValue='' adds email field to existing Person type. Only works if Person already exists. Use String, int, boolean, double as fieldType.")
    public String addFieldToFactType(@P("typeName - Name of existing fact type to extend. Example: 'Person' (must already exist in registry)") String typeName,
                                   @P("fieldName - Name of the new field to add. Example: 'email', 'phoneNumber', 'address'") String fieldName,
                                   @P("fieldType - Java type for the field. Use: String, int, boolean, double, Date. Example: 'String' for text, 'int' for numbers") String fieldType,
                                   @P("defaultValue - Default value for the field. Can be empty string. Example: '' for no default, 'false' for boolean, '0' for numbers") String defaultValue) {
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


    @Tool("STEP 3: Generate DRL 'declare' blocks for ALL fact types. Call this AFTER creating fact types. NO PARAMETERS NEEDED. Returns ready-to-use DRL code like 'declare Person name: String; age: int; end'. Use this to get declare blocks for ALL types.")
    public String generateAllDRLDeclarations() {
        if (registry.size() == 0) {
            return "No fact types registered to generate declarations for.";
        }
        
        return registry.generateDRLDeclarations();
    }

    @Tool("STEP 3b: Generate DRL 'declare' blocks for SPECIFIC fact types only. EXAMPLE: typeNames='Person,Order' generates declare blocks for only Person and Order types. Use comma-separated list. Use this when you want specific types, not all.")
    public String generateDRLDeclarations(@P("typeNames - Comma-separated list of fact type names. Example: 'Person,Order' or 'Customer,Product,Invoice'. No spaces around commas.") String typeNamesCommaDelimited) {
        Set<String> typeNames = Set.of(typeNamesCommaDelimited.split(","));
        typeNames = typeNames.stream().map(String::trim).collect(Collectors.toSet());
        
        String declarations = registry.generateDRLDeclarations(typeNames);
        
        if (declarations.isEmpty()) {
            return "No matching fact types found for: " + String.join(", ", typeNames);
        }
        
        return declarations;
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