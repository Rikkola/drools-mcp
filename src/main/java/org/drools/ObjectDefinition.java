package org.drools;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * Represents an object definition that can be used to create dynamic objects from JSON.
 * Contains metadata about the object structure including fields and their types.
 */
public class ObjectDefinition {
    
    private String name;
    private String packageName;
    private String description;
    private List<FieldDefinition> fields;
    private Map<String, Object> metadata;
    private boolean generateToString;
    private boolean generateEquals;
    private boolean generateHashCode;
    
    public ObjectDefinition() {
        this.fields = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.generateToString = true;
        this.generateEquals = true;
        this.generateHashCode = true;
    }
    
    public ObjectDefinition(String name) {
        this();
        this.name = name;
    }
    
    public ObjectDefinition(String name, String packageName) {
        this(name);
        this.packageName = packageName;
    }
    
    /**
     * Add a field to this object definition
     */
    public ObjectDefinition addField(FieldDefinition field) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }
        
        // Check for duplicate field names
        if (hasField(field.getName())) {
            throw new IllegalArgumentException("Field with name '" + field.getName() + "' already exists");
        }
        
        this.fields.add(field);
        return this;
    }
    
    /**
     * Add a simple field with name and type
     */
    public ObjectDefinition addField(String name, FieldDefinition.FieldType type) {
        return addField(new FieldDefinition(name, type));
    }
    
    /**
     * Add a required field with name and type
     */
    public ObjectDefinition addField(String name, FieldDefinition.FieldType type, boolean required) {
        return addField(new FieldDefinition(name, type, required));
    }
    
    /**
     * Add a field with custom Java type
     */
    public ObjectDefinition addField(String name, FieldDefinition.FieldType type, String javaType, boolean required) {
        return addField(new FieldDefinition(name, type, javaType, required));
    }
    
    /**
     * Remove a field by name
     */
    public boolean removeField(String fieldName) {
        return fields.removeIf(field -> Objects.equals(field.getName(), fieldName));
    }
    
    /**
     * Get a field by name
     */
    public FieldDefinition getField(String fieldName) {
        return fields.stream()
                .filter(field -> Objects.equals(field.getName(), fieldName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Check if a field exists
     */
    public boolean hasField(String fieldName) {
        return getField(fieldName) != null;
    }
    
    /**
     * Get all required fields
     */
    public List<FieldDefinition> getRequiredFields() {
        return fields.stream()
                .filter(FieldDefinition::isRequired)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get all field names
     */
    public List<String> getFieldNames() {
        return fields.stream()
                .map(FieldDefinition::getName)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get the fully qualified class name
     */
    public String getFullyQualifiedName() {
        if (packageName == null || packageName.trim().isEmpty()) {
            return name;
        }
        return packageName + "." + name;
    }
    
    /**
     * Create ObjectDefinition from JSON string
     */
    public static ObjectDefinition fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, ObjectDefinition.class);
    }
    
    /**
     * Convert ObjectDefinition to JSON string
     */
    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
    
    /**
     * Convert ObjectDefinition to pretty JSON string
     */
    public String toPrettyJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }
    
    /**
     * Generate a DRL declare statement for this object definition
     */
    public String toDrlDeclare() {
        StringBuilder drl = new StringBuilder();
        
        drl.append("declare ").append(name).append("\n");
        
        for (FieldDefinition field : fields) {
            drl.append("    ").append(field.getName()).append(" : ");
            
            // Map field types to DRL types
            switch (field.getType()) {
                case STRING:
                    drl.append("String");
                    break;
                case INTEGER:
                    drl.append("Integer");
                    break;
                case LONG:
                    drl.append("Long");
                    break;
                case DOUBLE:
                    drl.append("Double");
                    break;
                case BOOLEAN:
                    drl.append("Boolean");
                    break;
                case LIST:
                    drl.append("java.util.List");
                    break;
                case MAP:
                    drl.append("java.util.Map");
                    break;
                case OBJECT:
                    if (field.getObjectTypeName() != null) {
                        drl.append(field.getObjectTypeName());
                    } else {
                        drl.append("Object");
                    }
                    break;
                default:
                    drl.append("Object");
            }
            
            drl.append("\n");
        }
        
        drl.append("end");
        
        return drl.toString();
    }
    
    /**
     * Validate that all required fields are present in the given JSON data
     */
    public List<String> validateRequiredFields(Map<String, Object> jsonData) {
        List<String> missingFields = new ArrayList<>();
        
        for (FieldDefinition field : getRequiredFields()) {
            if (!jsonData.containsKey(field.getName()) || jsonData.get(field.getName()) == null) {
                missingFields.add(field.getName());
            }
        }
        
        return missingFields;
    }
    
    /**
     * Get a summary of this object definition
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("ObjectDefinition: ").append(name).append("\n");
        
        if (packageName != null) {
            summary.append("Package: ").append(packageName).append("\n");
        }
        
        if (description != null) {
            summary.append("Description: ").append(description).append("\n");
        }
        
        summary.append("Fields (").append(fields.size()).append("):\n");
        
        for (FieldDefinition field : fields) {
            summary.append("  - ").append(field.getName())
                   .append(" (").append(field.getType()).append(")");
            
            if (field.isRequired()) {
                summary.append(" [REQUIRED]");
            }
            
            summary.append("\n");
        }
        
        return summary.toString();
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<FieldDefinition> getFields() {
        return fields;
    }
    
    public void setFields(List<FieldDefinition> fields) {
        this.fields = fields != null ? fields : new ArrayList<>();
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
    
    public boolean isGenerateToString() {
        return generateToString;
    }
    
    public void setGenerateToString(boolean generateToString) {
        this.generateToString = generateToString;
    }
    
    public boolean isGenerateEquals() {
        return generateEquals;
    }
    
    public void setGenerateEquals(boolean generateEquals) {
        this.generateEquals = generateEquals;
    }
    
    public boolean isGenerateHashCode() {
        return generateHashCode;
    }
    
    public void setGenerateHashCode(boolean generateHashCode) {
        this.generateHashCode = generateHashCode;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectDefinition that = (ObjectDefinition) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(packageName, that.packageName) &&
               Objects.equals(fields, that.fields);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, packageName, fields);
    }
    
    @Override
    public String toString() {
        return String.format("ObjectDefinition{name='%s', packageName='%s', fields=%d}", 
                           name, packageName, fields.size());
    }
}
