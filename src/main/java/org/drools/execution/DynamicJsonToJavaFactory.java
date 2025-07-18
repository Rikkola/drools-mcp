package org.drools.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.*;
import org.drools.storage.DefinitionStorage;

/**
 * DynamicJsonToJavaFactory converts JSON data into dynamic Java objects using DynamicObjectCreator.
 * It can handle individual objects or arrays of objects, automatically determining the class type
 * from the JSON structure and creating appropriate dynamic objects.
 */
public class DynamicJsonToJavaFactory {
    
    private final ObjectMapper objectMapper;
    private final DynamicObjectCreator objectCreator;
    private final DefinitionStorage definitionStorage;
    
    public DynamicJsonToJavaFactory() {
        this.objectMapper = new ObjectMapper();
        this.objectCreator = new DynamicObjectCreator();
        this.definitionStorage = new DefinitionStorage();
    }
    
    public DynamicJsonToJavaFactory(DefinitionStorage definitionStorage) {
        this.objectMapper = new ObjectMapper();
        this.objectCreator = new DynamicObjectCreator();
        this.definitionStorage = definitionStorage;
    }
    
    /**
     * Creates Java objects from JSON string. Can handle both single objects and arrays.
     * 
     * @param json JSON string containing object(s) to create
     * @param classType The target class type (e.g., "Person", "Order", "Address")
     * @return List of created objects (even for single objects, returns list with one item)
     */
    public List<Object> createObjectsFromJson(String json, String classType) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            List<Object> results = new ArrayList<>();
            
            if (rootNode.isArray()) {
                // Handle array of objects
                ArrayNode arrayNode = (ArrayNode) rootNode;
                for (JsonNode node : arrayNode) {
                    Object obj = createSingleObjectFromJsonNode(node, classType);
                    if (obj != null) {
                        results.add(obj);
                    }
                }
            } else {
                // Handle single object
                Object obj = createSingleObjectFromJsonNode(rootNode, classType);
                if (obj != null) {
                    results.add(obj);
                }
            }
            
            return results;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates objects from JSON with automatic class type detection.
     * Attempts to determine the class type from the JSON structure.
     * 
     * @param json JSON string
     * @return List of created objects with automatically detected types
     */
    public List<Object> createObjectsFromJsonWithAutoDetect(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            List<Object> results = new ArrayList<>();
            
            if (rootNode.isArray()) {
                ArrayNode arrayNode = (ArrayNode) rootNode;
                for (JsonNode node : arrayNode) {
                    String detectedType = detectClassType(node);
                    Object obj = createSingleObjectFromJsonNode(node, detectedType);
                    if (obj != null) {
                        results.add(obj);
                    }
                }
            } else {
                String detectedType = detectClassType(rootNode);
                Object obj = createSingleObjectFromJsonNode(rootNode, detectedType);
                if (obj != null) {
                    results.add(obj);
                }
            }
            
            return results;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates objects from a mixed JSON array where each object might be of different types.
     * Each object in the array should have a "type" field indicating its class.
     * 
     * @param json JSON array string
     * @return List of objects of mixed types
     */
    public List<Object> createMixedObjectsFromJson(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            List<Object> results = new ArrayList<>();
            
            if (!rootNode.isArray()) {
                throw new IllegalArgumentException("Mixed objects require JSON array format");
            }
            
            ArrayNode arrayNode = (ArrayNode) rootNode;
            for (JsonNode node : arrayNode) {
                String classType = null;
                
                // Look for explicit type field
                if (node.has("type")) {
                    classType = node.get("type").asText();
                } else {
                    // Auto-detect if no type specified
                    classType = detectClassType(node);
                }
                
                Object obj = createSingleObjectFromJsonNode(node, classType);
                if (obj != null) {
                    results.add(obj);
                }
            }
            
            return results;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse mixed JSON: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a single object from a JsonNode.
     */
    private Object createSingleObjectFromJsonNode(JsonNode node, String classType) {
        if (!node.isObject()) {
            throw new IllegalArgumentException("JSON node must be an object");
        }
        
        ObjectNode objectNode = (ObjectNode) node;
        
        return createGenericObjectFromNode(objectNode, classType);
    }

    /**
     * Creates a generic object from JSON node using DRL definitions from DefinitionStorage.
     */
    private Object createGenericObjectFromNode(ObjectNode node, String classType) {
        // First try to get Java class definition from DynamicObjectCreator
        String classDefinition = DynamicObjectCreator.getClassDefinition(classType);
        
        // If not found, try to convert from DRL declare statement
        if (classDefinition == null) {
            DefinitionStorage.DroolsDefinition drlDef = definitionStorage.getDefinition(classType);
            if (drlDef != null && "declare".equals(drlDef.getType())) {
                classDefinition = convertDRLDeclareToJavaClass(drlDef.getContent());
            }
        }
        
        if (classDefinition == null) {
            throw new IllegalArgumentException("No class definition found for type: " + classType);
        }
        
        // Extract field values from JSON and create constructor arguments
        List<Object> constructorArgs = extractConstructorArgs(node, classDefinition);
        
        // Create the object using the Java class definition
        Map<String, String> classDefinitions = new HashMap<>();
        classDefinitions.put(classType, classDefinition);
        
        String constructorCall = buildConstructorCall(classType, constructorArgs);
        return objectCreator.createObjectFromString(constructorCall, classDefinitions);
    }
    
    /**
     * Converts a DRL declare statement to a Java class definition.
     */
    private String convertDRLDeclareToJavaClass(String drlDeclare) {
        // Parse the DRL declare statement and convert to Java class
        // Example DRL: "declare Person name : String age : int end"
        // Should become a Java class with constructor and getters/setters
        
        String[] lines = drlDeclare.split("\\n");
        String className = null;
        List<String> fields = new ArrayList<>();
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("declare ")) {
                className = line.substring(8).trim();
            } else if (line.contains(":") && !line.equals("end")) {
                // Parse field: "name : String"
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String fieldName = parts[0].trim();
                    String fieldType = parts[1].trim();
                    fields.add(fieldName + ":" + mapDRLTypeToJava(fieldType));
                }
            }
        }
        
        if (className == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Invalid DRL declare statement: " + drlDeclare);
        }
        
        return generateJavaClass(className, fields);
    }
    
    /**
     * Maps DRL types to Java types.
     */
    private String mapDRLTypeToJava(String drlType) {
        switch (drlType.toLowerCase()) {
            case "string": return "String";
            case "int": return "int";
            case "integer": return "Integer";
            case "double": return "double";
            case "float": return "float";
            case "boolean": return "boolean";
            case "long": return "long";
            case "date": return "java.util.Date";
            default: return drlType; // Assume it's already a valid Java type
        }
    }
    
    /**
     * Generates a Java class from field definitions.
     */
    private String generateJavaClass(String className, List<String> fields) {
        StringBuilder classBuilder = new StringBuilder();
        
        classBuilder.append("class ").append(className).append(" {\n");
        
        // Generate fields
        for (String field : fields) {
            String[] parts = field.split(":");
            String fieldName = parts[0];
            String fieldType = parts[1];
            classBuilder.append("    private ").append(fieldType).append(" ").append(fieldName).append(";\n");
        }
        
        classBuilder.append("\n");
        
        // Generate constructor
        classBuilder.append("    public ").append(className).append("(");
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) classBuilder.append(", ");
            String[] parts = fields.get(i).split(":");
            classBuilder.append(parts[1]).append(" ").append(parts[0]);
        }
        classBuilder.append(") {\n");
        
        for (String field : fields) {
            String fieldName = field.split(":")[0];
            classBuilder.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
        }
        classBuilder.append("    }\n\n");
        
        // Generate getters and setters
        for (String field : fields) {
            String[] parts = field.split(":");
            String fieldName = parts[0];
            String fieldType = parts[1];
            String capitalizedName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            
            // Getter
            classBuilder.append("    public ").append(fieldType).append(" get").append(capitalizedName).append("() { return ").append(fieldName).append("; }\n");
            
            // Setter
            classBuilder.append("    public void set").append(capitalizedName).append("(").append(fieldType).append(" ").append(fieldName).append(") { this.").append(fieldName).append(" = ").append(fieldName).append("; }\n\n");
        }
        
        // Generate toString
        classBuilder.append("    @Override\n");
        classBuilder.append("    public String toString() {\n");
        classBuilder.append("        return \"").append(className).append("{\"");
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) classBuilder.append(" + \", ");
            else classBuilder.append(" + \"");
            String fieldName = fields.get(i).split(":")[0];
            classBuilder.append(fieldName).append("=\" + ").append(fieldName);
        }
        classBuilder.append(" + \"}\";\n");
        classBuilder.append("    }\n");
        
        classBuilder.append("}\n");
        
        return classBuilder.toString();
    }
    
    /**
     * Extracts constructor arguments from JSON based on class definition.
     */
    private List<Object> extractConstructorArgs(ObjectNode node, String classDefinition) {
        List<Object> args = new ArrayList<>();
        
        // Extract values in the order they appear in JSON, excluding "type" field
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode value = field.getValue();
            
            // Skip the "type" field as it's metadata, not part of the object
            if ("type".equals(fieldName)) {
                continue;
            }
            
            if (value.isTextual()) {
                args.add(value.asText());
            } else if (value.isInt()) {
                args.add(value.asInt());
            } else if (value.isDouble()) {
                args.add(value.asDouble());
            } else if (value.isBoolean()) {
                args.add(value.asBoolean());
            } else {
                args.add(value.asText());
            }
        }
        
        return args;
    }
    
    /**
     * Builds a constructor call string for dynamic object creation.
     */
    private String buildConstructorCall(String className, List<Object> args) {
        StringBuilder constructor = new StringBuilder();
        constructor.append(className).append(" obj = new ").append(className).append("(");
        
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) constructor.append(", ");
            
            Object arg = args.get(i);
            if (arg instanceof String) {
                constructor.append("\"").append(escapeString((String) arg)).append("\"");
            } else {
                constructor.append(arg.toString());
            }
        }
        
        constructor.append(");");
        return constructor.toString();
    }
    
    /**
     * Attempts to detect the class type from JSON structure.
     */
    private String detectClassType(JsonNode node) {
        if (!node.isObject()) {
            return "Object";
        }
        
        Set<String> fieldNames = new HashSet<>();
        node.fieldNames().forEachRemaining(fieldNames::add);
        
        // Person detection
        if (fieldNames.contains("name") && fieldNames.contains("age")) {
            return "Person";
        }
        
        // Order detection
        if (fieldNames.contains("orderId") && fieldNames.contains("amount")) {
            return "Order";
        }
        
        // Address detection
        if (fieldNames.contains("street") && fieldNames.contains("city") && fieldNames.contains("zipCode")) {
            return "Address";
        }
        
        // Default fallback
        return "Object";
    }
    
    /**
     * Escapes special characters in strings for Java code generation.
     */
    private String escapeString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    /**
     * Utility method to convert objects back to JSON for verification/debugging.
     */
    public String objectsToJson(List<Object> objects) {
        try {
            return objectMapper.writeValueAsString(objects);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert objects to JSON: " + e.getMessage(), e);
        }
    }
    
}
