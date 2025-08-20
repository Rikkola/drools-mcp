package org.drools.model;

import org.drools.storage.DefinitionStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder utility for creating JSON responses in MCP tools
 */
public class JsonResponseBuilder {
    
    private Map<String, Object> response;
    
    private JsonResponseBuilder() {
        this.response = new HashMap<>();
    }
    
    /**
     * Creates a new JsonResponseBuilder instance
     */
    public static JsonResponseBuilder create() {
        return new JsonResponseBuilder();
    }
    
    /**
     * Marks response as successful
     */
    public JsonResponseBuilder success() {
        response.put("status", "success");
        return this;
    }
    
    /**
     * Marks response as error with message
     */
    public JsonResponseBuilder error(String message) {
        response.put("status", "error");
        response.put("message", message);
        return this;
    }
    
    /**
     * Marks response as not found with message
     */
    public JsonResponseBuilder notFound(String message) {
        response.put("status", "not_found");
        response.put("message", message);
        return this;
    }
    
    /**
     * Adds a generic field to the response
     */
    public JsonResponseBuilder field(String key, Object value) {
        response.put(key, value);
        return this;
    }
    
    /**
     * Adds action field (e.g., "added", "replaced", "removed")
     */
    public JsonResponseBuilder action(String action) {
        response.put("action", action);
        return this;
    }
    
    /**
     * Adds count field (e.g., number of fired rules)
     */
    public JsonResponseBuilder count(int count) {
        response.put("count", count);
        return this;
    }
    
    /**
     * Adds execution status (for execution operations)
     */
    public JsonResponseBuilder executionStatus(String status) {
        response.put("executionStatus", status);
        return this;
    }
    
    /**
     * Adds facts count (number of facts)
     */
    public JsonResponseBuilder factsCount(int count) {
        response.put("factsCount", count);
        return this;
    }
    
    /**
     * Adds facts list to response
     */
    public JsonResponseBuilder facts(List<Object> facts) {
        response.put("facts", facts);
        return this;
    }
    
    /**
     * Adds definitions list to response
     */
    public JsonResponseBuilder definitions(List<DefinitionStorage.DroolsDefinition> definitions) {
        List<Map<String, Object>> definitionMaps = new ArrayList<>();
        for (DefinitionStorage.DroolsDefinition def : definitions) {
            Map<String, Object> defMap = new HashMap<>();
            defMap.put("name", def.getName());
            defMap.put("type", def.getType());
            defMap.put("lastModified", def.getLastModified());
            defMap.put("content", def.getContent());
            definitionMaps.add(defMap);
        }
        response.put("definitions", definitionMaps);
        return this;
    }
    
    /**
     * Adds DRL content with definition count
     */
    public JsonResponseBuilder drlContent(String content, int definitionCount) {
        response.put("drlContent", content);
        response.put("definitionCount", definitionCount);
        return this;
    }
    
    /**
     * Adds summary field
     */
    public JsonResponseBuilder summary(String summary) {
        response.put("summary", summary);
        return this;
    }
    
    /**
     * Builds and returns the JSON string
     */
    public String build() {
        try {
            return convertToJson(response);
        } catch (Exception e) {
            // Fallback to simple error response
            return "{\"status\":\"error\",\"message\":\"Failed to build JSON response: " + 
                   escapeJsonString(e.getMessage()) + "\"}";
        }
    }
    
    /**
     * Simple JSON conversion without external dependencies
     */
    private String convertToJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        if (obj instanceof String) {
            return "\"" + escapeJsonString((String) obj) + "\"";
        }
        
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeJsonString(entry.getKey())).append("\":")
                  .append(convertToJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        
        if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                sb.append(convertToJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        
        // For other objects, convert to string
        return "\"" + escapeJsonString(obj.toString()) + "\"";
    }
    
    /**
     * Escapes special characters in JSON strings
     */
    private String escapeJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\b", "\\b")
                 .replace("\f", "\\f")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
}