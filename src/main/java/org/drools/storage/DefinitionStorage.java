package org.drools.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage for Drools rule engine definitions.
 * Manages declared types, functions, and other DRL definitions.
 */
public class DefinitionStorage {
    
    // Thread-safe storage for definitions
    private final Map<String, DroolsDefinition> definitions = new ConcurrentHashMap<>();
    
    /**
     * Represents a Drools definition (declared type, function, etc.)
     */
    public static class DroolsDefinition {
        private String name;
        private String type;  // "declare", "function", "global", "import", etc.
        private String content;
        private long lastModified;
        
        public DroolsDefinition(String name, String type, String content) {
            this.name = name;
            this.type = type;
            this.content = content;
            this.lastModified = System.currentTimeMillis();
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getContent() { return content; }
        public void setContent(String content) { 
            this.content = content;
            this.lastModified = System.currentTimeMillis();
        }
        
        public long getLastModified() { return lastModified; }
        
        @Override
        public String toString() {
            return String.format("DroolsDefinition{name='%s', type='%s', lastModified=%d}", 
                               name, type, lastModified);
        }
    }
    
    /**
     * Add a single definition. If a definition with the same name exists, it will be replaced.
     * @param name The name/identifier of the definition
     * @param type The type of definition (declare, function, global, import, etc.)
     * @param content The actual DRL content
     * @return The previous definition if it existed, null otherwise
     */
    public DroolsDefinition addDefinition(String name, String type, String content) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Definition name cannot be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Definition type cannot be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Definition content cannot be null or empty");
        }
        
        DroolsDefinition definition = new DroolsDefinition(name.trim(), type.trim(), content.trim());
        return definitions.put(name.trim(), definition);
    }
    
    /**
     * Add multiple definitions at once. Existing definitions with the same names will be replaced.
     * @param definitionList List of definitions to add
     * @return Map of replaced definitions (name -> old definition)
     */
    public Map<String, DroolsDefinition> addDefinitions(List<DroolsDefinition> definitionList) {
        if (definitionList == null) {
            throw new IllegalArgumentException("Definition list cannot be null");
        }
        
        Map<String, DroolsDefinition> replacedDefinitions = new HashMap<>();
        
        for (DroolsDefinition definition : definitionList) {
            if (definition == null) {
                continue; // Skip null definitions
            }
            
            DroolsDefinition replaced = addDefinition(definition.getName(), 
                                                    definition.getType(), 
                                                    definition.getContent());
            if (replaced != null) {
                replacedDefinitions.put(definition.getName(), replaced);
            }
        }
        
        return replacedDefinitions;
    }
    
    /**
     * Get a specific definition by name
     * @param name The name of the definition
     * @return The definition if found, null otherwise
     */
    public DroolsDefinition getDefinition(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return definitions.get(name.trim());
    }
    
    /**
     * Get all definitions
     * @return List of all stored definitions
     */
    public List<DroolsDefinition> getAllDefinitions() {
        return new ArrayList<>(definitions.values());
    }
    
    /**
     * Get definitions filtered by type
     * @param type The type to filter by (declare, function, global, etc.)
     * @return List of definitions matching the type
     */
    public List<DroolsDefinition> getDefinitionsByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return definitions.values().stream()
                .filter(def -> type.trim().equalsIgnoreCase(def.getType()))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get all definition names
     * @return List of all definition names
     */
    public List<String> getDefinitionNames() {
        return new ArrayList<>(definitions.keySet());
    }
    
    /**
     * Remove a definition by name
     * @param name The name of the definition to remove
     * @return The removed definition if it existed, null otherwise
     */
    public DroolsDefinition removeDefinition(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return definitions.remove(name.trim());
    }
    
    /**
     * Remove all definitions
     */
    public void clearAllDefinitions() {
        definitions.clear();
    }
    
    /**
     * Check if a definition exists
     * @param name The name of the definition
     * @return true if the definition exists, false otherwise
     */
    public boolean hasDefinition(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return definitions.containsKey(name.trim());
    }
    
    /**
     * Get the count of stored definitions
     * @return Number of definitions
     */
    public int getDefinitionCount() {
        return definitions.size();
    }
    
    /**
     * Generate a complete DRL string with all definitions
     * @param packageName The package name to use (optional)
     * @return Complete DRL string with all definitions
     */
    public String generateDRLString(String packageName) {
        StringBuilder drlBuilder = new StringBuilder();
        
        // Add package declaration if provided
        if (packageName != null && !packageName.trim().isEmpty()) {
            drlBuilder.append("package ").append(packageName.trim()).append(";\n\n");
        }
        
        // Group definitions by type for better organization
        Map<String, List<DroolsDefinition>> definitionsByType = definitions.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(DroolsDefinition::getType));
        
        // Add imports first
        if (definitionsByType.containsKey("import")) {
            drlBuilder.append("// Imports\n");
            for (DroolsDefinition def : definitionsByType.get("import")) {
                drlBuilder.append(def.getContent()).append("\n");
            }
            drlBuilder.append("\n");
        }
        
        // Add globals
        if (definitionsByType.containsKey("global")) {
            drlBuilder.append("// Globals\n");
            for (DroolsDefinition def : definitionsByType.get("global")) {
                drlBuilder.append(def.getContent()).append("\n");
            }
            drlBuilder.append("\n");
        }
        
        // Add declared types
        if (definitionsByType.containsKey("declare")) {
            drlBuilder.append("// Declared Types\n");
            for (DroolsDefinition def : definitionsByType.get("declare")) {
                drlBuilder.append(def.getContent()).append("\n\n");
            }
        }
        
        // Add functions
        if (definitionsByType.containsKey("function")) {
            drlBuilder.append("// Functions\n");
            for (DroolsDefinition def : definitionsByType.get("function")) {
                drlBuilder.append(def.getContent()).append("\n\n");
            }
        }
        
        // Add other types
        for (Map.Entry<String, List<DroolsDefinition>> entry : definitionsByType.entrySet()) {
            String type = entry.getKey();
            if (!"import".equals(type) && !"global".equals(type) && 
                !"declare".equals(type) && !"function".equals(type)) {
                drlBuilder.append("// ").append(type.toUpperCase()).append("\n");
                for (DroolsDefinition def : entry.getValue()) {
                    drlBuilder.append(def.getContent()).append("\n\n");
                }
            }
        }
        
        return drlBuilder.toString();
    }
    
    /**
     * Get a summary of all definitions
     * @return String summary of all definitions
     */
    public String getSummary() {
        if (definitions.isEmpty()) {
            return "No definitions stored.";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Definitions Summary:\n");
        summary.append("Total definitions: ").append(definitions.size()).append("\n\n");
        
        // Group by type
        Map<String, List<DroolsDefinition>> definitionsByType = definitions.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(DroolsDefinition::getType));
        
        for (Map.Entry<String, List<DroolsDefinition>> entry : definitionsByType.entrySet()) {
            String type = entry.getKey();
            List<DroolsDefinition> defs = entry.getValue();
            
            summary.append(type.toUpperCase()).append(" (").append(defs.size()).append("):\n");
            for (DroolsDefinition def : defs) {
                summary.append("  - ").append(def.getName()).append("\n");
            }
            summary.append("\n");
        }
        
        return summary.toString();
    }
}