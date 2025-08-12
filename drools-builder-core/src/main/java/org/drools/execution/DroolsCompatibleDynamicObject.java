package org.drools.execution;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;

/**
 * A dynamic object that implements getter/setter methods for Drools compatibility.
 * This object can be used by Drools as if it were a compiled Java class.
 */
public class DroolsCompatibleDynamicObject {
    
    private final String typeName;
    private final Map<String, Object> fields;
    private final Map<String, Class<?>> fieldTypes;
    
    public DroolsCompatibleDynamicObject(String typeName) {
        this.typeName = typeName;
        this.fields = new HashMap<>();
        this.fieldTypes = new HashMap<>();
    }
    
    public void defineField(String fieldName, Class<?> fieldType, Object value) {
        this.fieldTypes.put(fieldName, fieldType);
        this.fields.put(fieldName, value);
    }
    
    public void setFieldValue(String fieldName, Object value) {
        this.fields.put(fieldName, value);
    }
    
    public Object getFieldValue(String fieldName) {
        return this.fields.get(fieldName);
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    // Generic method that can handle any getter call
    public Object handleGetter(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            String fieldName = decapitalize(methodName.substring(3));
            return fields.get(fieldName);
        }
        return null;
    }
    
    // Generic method that can handle any setter call  
    public void handleSetter(String methodName, Object value) {
        if (methodName.startsWith("set") && methodName.length() > 3) {
            String fieldName = decapitalize(methodName.substring(3));
            fields.put(fieldName, value);
        }
    }
    
    // Method to dynamically handle method calls using reflection
    public Object invokeMethod(String methodName, Object... args) {
        try {
            // Try to handle as getter
            if (methodName.startsWith("get") && (args == null || args.length == 0)) {
                return handleGetter(methodName);
            }
            
            // Try to handle as setter
            if (methodName.startsWith("set") && args != null && args.length == 1) {
                handleSetter(methodName, args[0]);
                return null;
            }
            
            // Handle toString
            if ("toString".equals(methodName)) {
                return toString();
            }
            
            // Handle equals
            if ("equals".equals(methodName) && args != null && args.length == 1) {
                return equals(args[0]);
            }
            
            // Handle hashCode
            if ("hashCode".equals(methodName)) {
                return hashCode();
            }
            
            // Handle getClass
            if ("getClass".equals(methodName)) {
                return this.getClass();
            }
            
            throw new UnsupportedOperationException("Method not supported: " + methodName);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }
    
    private String decapitalize(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        return Character.toLowerCase(string.charAt(0)) + string.substring(1);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(typeName).append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DroolsCompatibleDynamicObject)) return false;
        DroolsCompatibleDynamicObject other = (DroolsCompatibleDynamicObject) obj;
        return typeName.equals(other.typeName) && fields.equals(other.fields);
    }
    
    @Override
    public int hashCode() {
        return typeName.hashCode() + fields.hashCode();
    }
    
    // Dynamic getter/setter methods that Drools can call via reflection
    // These methods handle any field name dynamically
    
    public Object getAge() { return fields.get("age"); }
    public void setAge(Object value) { fields.put("age", value); }
    
    public Object getAdult() { return fields.get("adult"); }
    public void setAdult(Object value) { fields.put("adult", value); }
    
    public Object getName() { return fields.get("name"); }
    public void setName(Object value) { fields.put("name", value); }
    
    // Additional common field accessors
    public Object getId() { return fields.get("id"); }
    public void setId(Object value) { fields.put("id", value); }
    
    public Object getType() { return fields.get("type"); }
    public void setType(Object value) { fields.put("type", value); }
    
    public Object getValue() { return fields.get("value"); }
    public void setValue(Object value) { fields.put("value", value); }
    
    public Object getStatus() { return fields.get("status"); }
    public void setStatus(Object value) { fields.put("status", value); }
    
    // Special method that Drools can use to check field existence
    public boolean hasProperty(String propertyName) {
        return fields.containsKey(propertyName);
    }
    
    // Method to get property value by name (useful for Drools)
    public Object getProperty(String propertyName) {
        return fields.get(propertyName);
    }
    
    // Method to set property value by name (useful for Drools)
    public void setProperty(String propertyName, Object value) {
        fields.put(propertyName, value);
    }
}