package org.drools;

import java.util.Objects;

/**
 * Represents a field definition for dynamic object creation.
 * Contains metadata about a field including name, type, and validation rules.
 */
public class FieldDefinition {
    
    public enum FieldType {
        STRING,
        INTEGER,
        LONG,
        DOUBLE,
        BOOLEAN,
        OBJECT,
        LIST,
        MAP
    }
    
    private String name;
    private FieldType type;
    private String javaType;  // Full Java type name (e.g., "java.lang.String", "java.util.List<String>")
    private boolean required;
    private Object defaultValue;
    private String description;
    private String objectTypeName;  // For OBJECT type, specifies the object definition name
    private String listElementType;  // For LIST type, specifies the element type
    
    public FieldDefinition() {
    }
    
    public FieldDefinition(String name, FieldType type) {
        this.name = name;
        this.type = type;
        this.javaType = getDefaultJavaType(type);
        this.required = false;
    }
    
    public FieldDefinition(String name, FieldType type, boolean required) {
        this.name = name;
        this.type = type;
        this.javaType = getDefaultJavaType(type);
        this.required = required;
    }
    
    public FieldDefinition(String name, FieldType type, String javaType, boolean required) {
        this.name = name;
        this.type = type;
        this.javaType = javaType;
        this.required = required;
    }
    
    /**
     * Get the default Java type for a field type
     */
    private String getDefaultJavaType(FieldType type) {
        switch (type) {
            case STRING:
                return "java.lang.String";
            case INTEGER:
                return "java.lang.Integer";
            case LONG:
                return "java.lang.Long";
            case DOUBLE:
                return "java.lang.Double";
            case BOOLEAN:
                return "java.lang.Boolean";
            case OBJECT:
                return "java.lang.Object";
            case LIST:
                return "java.util.List";
            case MAP:
                return "java.util.Map";
            default:
                return "java.lang.Object";
        }
    }
    
    /**
     * Check if this field represents a primitive type
     */
    public boolean isPrimitive() {
        return type == FieldType.STRING || 
               type == FieldType.INTEGER || 
               type == FieldType.LONG || 
               type == FieldType.DOUBLE || 
               type == FieldType.BOOLEAN;
    }
    
    /**
     * Check if this field represents a collection type
     */
    public boolean isCollection() {
        return type == FieldType.LIST || type == FieldType.MAP;
    }
    
    /**
     * Get the getter method name for this field
     */
    public String getGetterName() {
        if (type == FieldType.BOOLEAN) {
            return "is" + capitalize(name);
        }
        return "get" + capitalize(name);
    }
    
    /**
     * Get the setter method name for this field
     */
    public String getSetterName() {
        return "set" + capitalize(name);
    }
    
    /**
     * Capitalize the first letter of a string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public FieldType getType() {
        return type;
    }
    
    public void setType(FieldType type) {
        this.type = type;
    }
    
    public String getJavaType() {
        return javaType;
    }
    
    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getObjectTypeName() {
        return objectTypeName;
    }
    
    public void setObjectTypeName(String objectTypeName) {
        this.objectTypeName = objectTypeName;
    }
    
    public String getListElementType() {
        return listElementType;
    }
    
    public void setListElementType(String listElementType) {
        this.listElementType = listElementType;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldDefinition that = (FieldDefinition) o;
        return required == that.required &&
               Objects.equals(name, that.name) &&
               type == that.type &&
               Objects.equals(javaType, that.javaType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, type, javaType, required);
    }
    
    @Override
    public String toString() {
        return String.format("FieldDefinition{name='%s', type=%s, javaType='%s', required=%s}", 
                           name, type, javaType, required);
    }
}
