package org.drools;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating dynamic objects based on ObjectDefinition.
 * Uses Java Proxy to create objects that can be used with Drools.
 */
public class DynamicObjectFactory {
    
    private final Map<String, ObjectDefinition> objectDefinitions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Register an object definition
     */
    public void registerObjectDefinition(ObjectDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Object definition cannot be null");
        }
        if (definition.getName() == null || definition.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Object definition name cannot be null or empty");
        }
        
        objectDefinitions.put(definition.getName(), definition);
    }
    
    /**
     * Register multiple object definitions
     */
    public void registerObjectDefinitions(Collection<ObjectDefinition> definitions) {
        if (definitions == null) {
            throw new IllegalArgumentException("Object definitions cannot be null");
        }
        
        for (ObjectDefinition definition : definitions) {
            registerObjectDefinition(definition);
        }
    }
    
    /**
     * Get a registered object definition
     */
    public ObjectDefinition getObjectDefinition(String name) {
        return objectDefinitions.get(name);
    }
    
    /**
     * Check if an object definition is registered
     */
    public boolean hasObjectDefinition(String name) {
        return objectDefinitions.containsKey(name);
    }
    
    /**
     * Get all registered object definition names
     */
    public Set<String> getRegisteredDefinitionNames() {
        return new HashSet<>(objectDefinitions.keySet());
    }
    
    /**
     * Create a dynamic object from JSON data using a specific object definition
     */
    public Object createFromJson(String definitionName, String jsonData) throws Exception {
        ObjectDefinition definition = objectDefinitions.get(definitionName);
        if (definition == null) {
            throw new IllegalArgumentException("Object definition not found: " + definitionName);
        }
        
        // Parse JSON to Map
        @SuppressWarnings("unchecked")
        Map<String, Object> jsonMap = objectMapper.readValue(jsonData, Map.class);
        
        return createFromMap(definition, jsonMap);
    }
    
    /**
     * Create a dynamic object from a Map using a specific object definition
     */
    public Object createFromMap(String definitionName, Map<String, Object> data) throws Exception {
        ObjectDefinition definition = objectDefinitions.get(definitionName);
        if (definition == null) {
            throw new IllegalArgumentException("Object definition not found: " + definitionName);
        }
        
        return createFromMap(definition, data);
    }
    
    /**
     * Create a dynamic object from a Map using an ObjectDefinition
     */
    public Object createFromMap(ObjectDefinition definition, Map<String, Object> data) throws Exception {
        // Validate required fields
        List<String> missingFields = definition.validateRequiredFields(data);
        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("Missing required fields: " + missingFields);
        }
        
        // Create the dynamic object using Proxy
        return createDynamicObject(definition, data);
    }
    
    /**
     * Create multiple objects from JSON array
     */
    public List<Object> createMultipleFromJson(String definitionName, String jsonArrayData) throws Exception {
        ObjectDefinition definition = objectDefinitions.get(definitionName);
        if (definition == null) {
            throw new IllegalArgumentException("Object definition not found: " + definitionName);
        }
        
        // Parse JSON array to List of Maps
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> jsonList = objectMapper.readValue(jsonArrayData, List.class);
        
        List<Object> results = new ArrayList<>();
        for (Map<String, Object> jsonMap : jsonList) {
            results.add(createFromMap(definition, jsonMap));
        }
        
        return results;
    }
    
    /**
     * Create a dynamic object using Java Proxy
     */
    private Object createDynamicObject(ObjectDefinition definition, Map<String, Object> data) {
        // Create a map to store the object's field values
        Map<String, Object> fieldValues = new HashMap<>();
        
        // Initialize field values from data and defaults
        for (FieldDefinition field : definition.getFields()) {
            Object value = data.get(field.getName());
            
            if (value == null && field.getDefaultValue() != null) {
                value = field.getDefaultValue();
            }
            
            // Convert and validate the value
            Object convertedValue = convertValue(field, value);
            fieldValues.put(field.getName(), convertedValue);
        }
        
        // Create the proxy object
        return Proxy.newProxyInstance(
            DynamicObjectFactory.class.getClassLoader(),
            new Class[]{DynamicObject.class},
            new DynamicObjectHandler(definition, fieldValues)
        );
    }
    
    /**
     * Convert a value to the appropriate type based on field definition
     */
    private Object convertValue(FieldDefinition field, Object value) {
        if (value == null) {
            return null;
        }
        
        try {
            switch (field.getType()) {
                case STRING:
                    return value.toString();
                    
                case INTEGER:
                    if (value instanceof Number) {
                        return ((Number) value).intValue();
                    }
                    return Integer.valueOf(value.toString());
                    
                case LONG:
                    if (value instanceof Number) {
                        return ((Number) value).longValue();
                    }
                    return Long.valueOf(value.toString());
                    
                case DOUBLE:
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                    return Double.valueOf(value.toString());
                    
                case BOOLEAN:
                    if (value instanceof Boolean) {
                        return value;
                    }
                    return Boolean.valueOf(value.toString());
                    
                case LIST:
                    if (value instanceof List) {
                        return new ArrayList<>((List<?>) value);
                    }
                    return Arrays.asList(value);
                    
                case MAP:
                    if (value instanceof Map) {
                        return new HashMap<>((Map<?, ?>) value);
                    }
                    return value;
                    
                case OBJECT:
                    // For nested objects, recursively create them if definition exists
                    if (field.getObjectTypeName() != null && value instanceof Map) {
                        ObjectDefinition nestedDefinition = objectDefinitions.get(field.getObjectTypeName());
                        if (nestedDefinition != null) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> nestedData = (Map<String, Object>) value;
                            return createDynamicObject(nestedDefinition, nestedData);
                        }
                    }
                    return value;
                    
                default:
                    return value;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert value for field " + field.getName() + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Interface that dynamic objects implement
     */
    public interface DynamicObject {
        Object getFieldValue(String fieldName);
        void setFieldValue(String fieldName, Object value);
        String getObjectTypeName();
        Map<String, Object> getAllFieldValues();
        ObjectDefinition getObjectDefinition();
    }
    
    /**
     * InvocationHandler for dynamic objects
     */
    private static class DynamicObjectHandler implements InvocationHandler {
        private final ObjectDefinition definition;
        private final Map<String, Object> fieldValues;
        
        public DynamicObjectHandler(ObjectDefinition definition, Map<String, Object> fieldValues) {
            this.definition = definition;
            this.fieldValues = new HashMap<>(fieldValues);
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            
            // Handle DynamicObject interface methods
            if ("getFieldValue".equals(methodName) && args.length == 1) {
                return fieldValues.get(args[0]);
            }
            
            if ("setFieldValue".equals(methodName) && args.length == 2) {
                fieldValues.put((String) args[0], args[1]);
                return null;
            }
            
            if ("getObjectTypeName".equals(methodName)) {
                return definition.getName();
            }
            
            if ("getAllFieldValues".equals(methodName)) {
                return new HashMap<>(fieldValues);
            }
            
            if ("getObjectDefinition".equals(methodName)) {
                return definition;
            }
            
            // Handle getter methods
            if (methodName.startsWith("get") && args == null) {
                String fieldName = decapitalize(methodName.substring(3));
                return fieldValues.get(fieldName);
            }
            
            // Handle is methods (for boolean fields)
            if (methodName.startsWith("is") && args == null) {
                String fieldName = decapitalize(methodName.substring(2));
                return fieldValues.get(fieldName);
            }
            
            // Handle setter methods
            if (methodName.startsWith("set") && args != null && args.length == 1) {
                String fieldName = decapitalize(methodName.substring(3));
                fieldValues.put(fieldName, args[0]);
                return null;
            }
            
            // Handle toString
            if ("toString".equals(methodName)) {
                return definition.getName() + fieldValues.toString();
            }
            
            // Handle equals
            if ("equals".equals(methodName) && args.length == 1) {
                if (args[0] == null) return false;
                if (proxy == args[0]) return true;
                if (args[0] instanceof DynamicObject) {
                    DynamicObject other = (DynamicObject) args[0];
                    return Objects.equals(definition.getName(), other.getObjectTypeName()) &&
                           Objects.equals(fieldValues, other.getAllFieldValues());
                }
                return false;
            }
            
            // Handle hashCode
            if ("hashCode".equals(methodName)) {
                return Objects.hash(definition.getName(), fieldValues);
            }
            
            throw new UnsupportedOperationException("Method not supported: " + methodName);
        }
        
        private String decapitalize(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toLowerCase() + str.substring(1);
        }
    }
    
    /**
     * Clear all registered object definitions
     */
    public void clearAll() {
        objectDefinitions.clear();
    }
    
    /**
     * Get count of registered object definitions
     */
    public int getRegisteredDefinitionCount() {
        return objectDefinitions.size();
    }
    
    /**
     * Get a summary of all registered object definitions
     */
    public String getSummary() {
        if (objectDefinitions.isEmpty()) {
            return "No object definitions registered.";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Registered Object Definitions (").append(objectDefinitions.size()).append("):\n");
        
        for (ObjectDefinition definition : objectDefinitions.values()) {
            summary.append("- ").append(definition.getName())
                   .append(" (").append(definition.getFields().size()).append(" fields)\n");
        }
        
        return summary.toString();
    }
}
