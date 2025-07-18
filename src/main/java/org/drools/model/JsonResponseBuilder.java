package org.drools.model;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Builder class for creating consistent JSON responses across the application.
 * Centralizes JSON formatting logic and eliminates code duplication.
 */
public class JsonResponseBuilder {
    
    private final Map<String, Object> data;
    
    private JsonResponseBuilder() {
        this.data = new LinkedHashMap<>();
    }
    
    /**
     * Create a new JsonResponseBuilder instance.
     */
    public static JsonResponseBuilder create() {
        return new JsonResponseBuilder();
    }
    
    /**
     * Set success status.
     */
    public JsonResponseBuilder success() {
        data.put("status", "success");
        return this;
    }
    
    /**
     * Set error status with message.
     */
    public JsonResponseBuilder error(String message) {
        data.put("status", "error");
        data.put("message", escapeJsonString(message));
        return this;
    }
    
    /**
     * Set not found status with message.
     */
    public JsonResponseBuilder notFound(String message) {
        data.put("status", "not_found");
        data.put("message", escapeJsonString(message));
        return this;
    }
    
    /**
     * Add a field to the response.
     */
    public JsonResponseBuilder field(String key, Object value) {
        if (value instanceof String) {
            data.put(key, escapeJsonString((String) value));
        } else {
            data.put(key, value);
        }
        return this;
    }
    
    /**
     * Add an action field (for operations like add, remove, replace).
     */
    public JsonResponseBuilder action(String action) {
        data.put("action", action);
        return this;
    }
    
    /**
     * Add a count field.
     */
    public JsonResponseBuilder count(int count) {
        data.put("count", count);
        return this;
    }
    
    /**
     * Add facts array for DRL execution results.
     */
    public JsonResponseBuilder facts(List<Object> facts) {
        data.put("executionStatus", "success");
        data.put("factsCount", facts.size());
        
        StringBuilder factsArray = new StringBuilder();
        factsArray.append("[\n");
        
        for (int i = 0; i < facts.size(); i++) {
            Object fact = facts.get(i);
            factsArray.append("    {\n");
            factsArray.append("      \"type\": \"").append(fact.getClass().getSimpleName()).append("\",\n");
            factsArray.append("      \"isDynamicObject\": false,\n");
            factsArray.append("      \"value\": \"").append(escapeJsonString(fact.toString())).append("\"\n");
            factsArray.append("    }");
            if (i < facts.size() - 1) {
                factsArray.append(",");
            }
            factsArray.append("\n");
        }
        
        factsArray.append("  ]");
        data.put("facts", factsArray.toString());
        return this;
    }
    
    /**
     * Add definitions array for definition listing.
     */
    public JsonResponseBuilder definitions(List<org.drools.storage.DefinitionStorage.DroolsDefinition> definitions) {
        data.put("count", definitions.size());
        
        StringBuilder defsArray = new StringBuilder();
        defsArray.append("[\n");
        
        for (int i = 0; i < definitions.size(); i++) {
            org.drools.storage.DefinitionStorage.DroolsDefinition def = definitions.get(i);
            defsArray.append("    {\n");
            defsArray.append("      \"name\": \"").append(escapeJsonString(def.getName())).append("\",\n");
            defsArray.append("      \"type\": \"").append(escapeJsonString(def.getType())).append("\",\n");
            defsArray.append("      \"lastModified\": ").append(def.getLastModified()).append(",\n");
            defsArray.append("      \"contentPreview\": \"").append(escapeJsonString(def.getContent().substring(0, Math.min(100, def.getContent().length())))).append("...\",\n");
            defsArray.append("      \"contentLength\": ").append(def.getContent().length()).append("\n");
            defsArray.append("    }");
            if (i < definitions.size() - 1) {
                defsArray.append(",");
            }
            defsArray.append("\n");
        }
        
        defsArray.append("  ]");
        data.put("definitions", defsArray.toString());
        return this;
    }
    
    /**
     * Add DRL content field (for generateDRLFromDefinitions).
     */
    public JsonResponseBuilder drlContent(String content, int definitionCount) {
        data.put("definitionCount", definitionCount);
        data.put("drlContent", escapeJsonString(content).replace("\n", "\\n"));
        return this;
    }
    
    /**
     * Add summary field (for definition summary).
     */
    public JsonResponseBuilder summary(String summary) {
        data.put("summary", escapeJsonString(summary).replace("\n", "\\n"));
        return this;
    }
    
    /**
     * Build the final JSON string.
     */
    public String build() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(",\n");
            }
            first = false;
            
            json.append("  \"").append(entry.getKey()).append("\": ");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                // Check if it's already a JSON structure (like arrays)
                String strValue = (String) value;
                if (strValue.startsWith("[") || strValue.startsWith("{")) {
                    json.append(strValue);
                } else {
                    json.append("\"").append(strValue).append("\"");
                }
            } else {
                json.append(value.toString());
            }
        }
        
        json.append("\n}");
        return json.toString();
    }
    
    /**
     * Escape special characters in JSON strings.
     */
    private String escapeJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}