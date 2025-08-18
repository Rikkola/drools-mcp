package org.drools.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import org.drools.storage.DefinitionStorage;

public class DynamicJsonToJavaFactory {
    
    private final ObjectMapper objectMapper;
    private final DefinitionStorage definitionStorage;
    private final Map<String, Class<?>> compiledClasses;
    
    public DynamicJsonToJavaFactory() {
        this.objectMapper = new ObjectMapper();
        this.definitionStorage = new DefinitionStorage();
        this.compiledClasses = new HashMap<>();
    }
    
    public DynamicJsonToJavaFactory(DefinitionStorage definitionStorage) {
        this.objectMapper = new ObjectMapper();
        this.definitionStorage = definitionStorage;
        this.compiledClasses = new HashMap<>();
    }
    
    public List<Object> createObjectsFromJson(String json, String classType) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            List<Object> results = new ArrayList<>();
            
            if (rootNode.isArray()) {
                ArrayNode arrayNode = (ArrayNode) rootNode;
                for (JsonNode node : arrayNode) {
                    Object obj = createSingleObjectFromJsonNode(node, classType);
                    if (obj != null) {
                        results.add(obj);
                    }
                }
            } else {
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
                
                if (node.has("type")) {
                    classType = node.get("type").asText();
                } else {
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
    
    private Object createSingleObjectFromJsonNode(JsonNode node, String classType) {
        if (!node.isObject()) {
            throw new IllegalArgumentException("JSON node must be an object");
        }
        
        ObjectNode objectNode = (ObjectNode) node;
        
        try {
            Class<?> clazz = getOrCreateClass(classType);
            return createObjectUsingReflection(clazz, objectNode);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create object of type " + classType + ": " + e.getMessage(), e);
        }
    }

    private Class<?> getOrCreateClass(String classType) throws Exception {
        if (compiledClasses.containsKey(classType)) {
            return compiledClasses.get(classType);
        }
        
        try {
            Class<?> existingClass = Class.forName(classType);
            compiledClasses.put(classType, existingClass);
            return existingClass;
        } catch (ClassNotFoundException e) {
            // Class doesn't exist, we need to create it dynamically
            Class<?> dynamicClass = createDynamicClass(classType);
            compiledClasses.put(classType, dynamicClass);
            return dynamicClass;
        }
    }
    
    private Class<?> createDynamicClass(String classType) throws Exception {
        String classDefinition = getClassDefinition(classType);
        if (classDefinition == null) {
            throw new IllegalArgumentException("No class definition found for type: " + classType);
        }
        
        return compileJavaClass(classType, classDefinition);
    }
    
    private String getClassDefinition(String classType) {
            DefinitionStorage.DroolsDefinition drlDef = definitionStorage.getDefinition(classType);
            if (drlDef != null && "declare".equals(drlDef.getType())) {
                return convertDRLDeclareToJavaClass(drlDef.getContent());
            }

        return null;
    }
    
    private Class<?> compileJavaClass(String className, String classDefinition) throws Exception {
        javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("No Java compiler available. Make sure you're running with a JDK, not just a JRE.");
        }
        
        String fullClassDefinition = "package org.drools.execution.dynamic;\n\n" + classDefinition;
        
        InMemoryJavaFileObject sourceFile = new InMemoryJavaFileObject(
            "org.drools.execution.dynamic." + className, 
            fullClassDefinition
        );
        
        InMemoryFileManager fileManager = new InMemoryFileManager(compiler.getStandardFileManager(null, null, null));
        
        javax.tools.JavaCompiler.CompilationTask task = compiler.getTask(
            null, 
            fileManager, 
            null, 
            null, 
            null, 
            Arrays.asList(sourceFile)
        );
        
        if (!task.call()) {
            throw new RuntimeException("Compilation failed for class: " + className);
        }
        
        InMemoryClassLoader classLoader = new InMemoryClassLoader(fileManager.getCompiledClasses());
        return classLoader.loadClass("org.drools.execution.dynamic." + className);
    }
    
    private Object createObjectUsingReflection(Class<?> clazz, ObjectNode jsonNode) throws Exception {
        Constructor<?>[] constructors = clazz.getConstructors();
        
        Constructor<?> constructor = findBestConstructor(constructors, jsonNode);
        if (constructor == null) {
            Constructor<?> defaultConstructor = clazz.getDeclaredConstructor();
            Object instance = defaultConstructor.newInstance();
            setFieldsUsingSetters(instance, jsonNode);
            return instance;
        }
        
        Object[] args = extractConstructorArguments(constructor, jsonNode);
        return constructor.newInstance(args);
    }
    
    private Constructor<?> findBestConstructor(Constructor<?>[] constructors, ObjectNode jsonNode) {
        Set<String> jsonFields = new HashSet<>();
        jsonNode.fieldNames().forEachRemaining(jsonFields::add);
        
        Constructor<?> bestMatch = null;
        int bestScore = -1;
        
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                continue;
            }
            
            // Get field order for this class to match parameters properly
            String className = constructor.getDeclaringClass().getSimpleName();
            String classDefinition = getClassDefinition(className);
            List<String> fieldOrder = parseFieldOrderFromClassDefinition(classDefinition);
            
            int score = 0;
            boolean canUse = true;
            
            Class<?>[] paramTypes = constructor.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                String fieldName;
                if (i < fieldOrder.size()) {
                    fieldName = fieldOrder.get(i);
                } else {
                    fieldName = inferFieldNameFromType(paramTypes[i]);
                }
                
                if (jsonFields.contains(fieldName)) {
                    score++;
                } else {
                    canUse = false;
                    break;
                }
            }
            
            if (canUse && score > bestScore) {
                bestMatch = constructor;
                bestScore = score;
            }
        }
        
        return bestMatch;
    }
    
    private String inferFieldNameFromType(Class<?> type) {
        String typeName = type.getSimpleName().toLowerCase();
        if (typeName.equals("string")) return "name";
        if (typeName.equals("int") || typeName.equals("integer")) return "age";
        if (typeName.equals("double")) return "amount";
        return typeName;
    }
    
    private Object[] extractConstructorArguments(Constructor<?> constructor, ObjectNode jsonNode) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        
        // Get field order from the class definition to match constructor parameter order
        String className = constructor.getDeclaringClass().getSimpleName();
        String classDefinition = getClassDefinition(className);
        List<String> fieldOrder = parseFieldOrderFromClassDefinition(classDefinition);
        
        for (int i = 0; i < paramTypes.length; i++) {
            String fieldName;
            if (i < fieldOrder.size()) {
                fieldName = fieldOrder.get(i);
            } else {
                fieldName = inferFieldNameFromType(paramTypes[i]);
            }
            
            JsonNode value = jsonNode.get(fieldName);
            
            if (value != null) {
                args[i] = convertJsonValueToType(value, paramTypes[i]);
            } else {
                args[i] = getDefaultValue(paramTypes[i]);
            }
        }
        
        return args;
    }
    
    private void setFieldsUsingSetters(Object instance, ObjectNode jsonNode) throws Exception {
        Class<?> clazz = instance.getClass();
        Method[] methods = clazz.getMethods();
        
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode value = field.getValue();
            
            String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            
            for (Method method : methods) {
                if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    Object convertedValue = convertJsonValueToType(value, paramType);
                    method.invoke(instance, convertedValue);
                    break;
                }
            }
        }
    }
    
    private Object convertJsonValueToType(JsonNode value, Class<?> targetType) {
        if (value.isNull()) {
            return null;
        }
        
        if (targetType == String.class) {
            return value.asText();
        } else if (targetType == int.class || targetType == Integer.class) {
            return value.asInt();
        } else if (targetType == double.class || targetType == Double.class) {
            return value.asDouble();
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return value.asBoolean();
        } else if (targetType == long.class || targetType == Long.class) {
            return value.asLong();
        } else if (targetType == float.class || targetType == Float.class) {
            return (float) value.asDouble();
        } else if (targetType == Number.class) {
            // For Number type, return appropriate subclass based on JSON value
            if (value.isInt()) {
                return value.asInt();
            } else if (value.isLong()) {
                return value.asLong();
            } else if (value.isDouble() || value.isFloat()) {
                return value.asDouble();
            } else {
                return value.asInt(); // Default to int for numeric strings
            }
        }
        
        return value.asText();
    }
    
    private Object getDefaultValue(Class<?> type) {
        if (type == int.class) return 0;
        if (type == double.class) return 0.0;
        if (type == boolean.class) return false;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == Number.class) return 0;
        return null;
    }
    
    private String convertDRLDeclareToJavaClass(String drlDeclare) {
        String[] lines = drlDeclare.split("\\n");
        String className = null;
        List<String> fields = new ArrayList<>();
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("declare ")) {
                // Handle both single-line and multi-line declarations
                String afterDeclare = line.substring(8).trim();
                
                // For single-line: "declare TestUser name : String age : int end"
                // Extract class name (first word after "declare")
                String[] parts = afterDeclare.split("\\s+");
                if (parts.length > 0) {
                    className = parts[0];
                    
                    // If it's a single-line declaration, parse fields from the same line
                    if (line.contains(":")) {
                        // Remove the class name and "end" to get field definitions
                        String fieldsStr = afterDeclare.substring(className.length()).replaceAll("\\s*end\\s*$", "").trim();
                        parseFieldsFromString(fieldsStr, fields);
                    }
                }
            } else if (line.contains(":") && !line.equals("end")) {
                // Multi-line field definitions
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String fieldName = parts[0].trim();
                    String fieldTypeWithDefault = parts[1].trim();
                    
                    // Extract just the type part, ignoring default values (e.g., "boolean = false" -> "boolean")
                    String fieldType = fieldTypeWithDefault.split("\\s*=\\s*")[0].trim();
                    
                    fields.add(fieldName + ":" + mapDRLTypeToJava(fieldType));
                }
            }
        }
        
        if (className == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Invalid DRL declare statement: " + drlDeclare);
        }
        
        return generateJavaClass(className, fields);
    }
    
    private void parseFieldsFromString(String fieldsStr, List<String> fields) {
        // Parse field definitions from a single line like "name : String age : int"
        // Split by field pattern, looking for "fieldName : Type" patterns
        String[] tokens = fieldsStr.split("\\s+");
        
        for (int i = 0; i < tokens.length - 2; i++) {
            if (":".equals(tokens[i + 1])) {
                String fieldName = tokens[i];
                String fieldTypeWithDefault = tokens[i + 2];
                
                // Extract just the type part, ignoring default values
                String fieldType = fieldTypeWithDefault.split("\\s*=\\s*")[0].trim();
                
                fields.add(fieldName + ":" + mapDRLTypeToJava(fieldType));
                i += 2; // Skip the next two tokens as we've processed them
            }
        }
    }
    
    private String mapDRLTypeToJava(String drlType) {
        switch (drlType.toLowerCase()) {
            case "string": return "String";
            case "int": return "int";
            case "integer": return "Integer";
            case "number": return "Number";
            case "double": return "double";
            case "float": return "float";
            case "boolean": return "boolean";
            case "long": return "long";
            case "date": return "java.util.Date";
            default: return drlType;
        }
    }
    
    private String generateJavaClass(String className, List<String> fields) {
        StringBuilder classBuilder = new StringBuilder();
        
        classBuilder.append("public class ").append(className).append(" {\n");
        
        for (String field : fields) {
            String[] parts = field.split(":");
            String fieldName = parts[0];
            String fieldType = parts[1];
            classBuilder.append("    private ").append(fieldType).append(" ").append(fieldName).append(";\n");
        }
        
        classBuilder.append("\n");
        classBuilder.append("    public ").append(className).append("() {}\n\n");
        
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
        
        for (String field : fields) {
            String[] parts = field.split(":");
            String fieldName = parts[0];
            String fieldType = parts[1];
            String capitalizedName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            
            classBuilder.append("    public ").append(fieldType).append(" get").append(capitalizedName).append("() { return ").append(fieldName).append("; }\n");
            classBuilder.append("    public void set").append(capitalizedName).append("(").append(fieldType).append(" ").append(fieldName).append(") { this.").append(fieldName).append(" = ").append(fieldName).append("; }\n\n");
        }
        
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
    
    private String detectClassType(JsonNode node) {
        if (!node.isObject()) {
            return "Object";
        }
        
        Set<String> fieldNames = new HashSet<>();
        node.fieldNames().forEachRemaining(fieldNames::add);
        
        if (fieldNames.contains("name") && fieldNames.contains("age")) {
            return "Person";
        }
        
        if (fieldNames.contains("orderId") && fieldNames.contains("amount")) {
            return "Order";
        }
        
        if (fieldNames.contains("street") && fieldNames.contains("city") && fieldNames.contains("zipCode")) {
            return "Address";
        }
        
        return "Object";
    }
    
    private List<String> parseFieldOrderFromClassDefinition(String classDefinition) {
        List<String> fieldOrder = new ArrayList<>();
        
        if (classDefinition == null) {
            return fieldOrder;
        }
        
        // Extract field declarations in the order they appear in the class definition
        String[] lines = classDefinition.split("\\n");
        for (String line : lines) {
            line = line.trim();
            // Look for private field declarations: "private type fieldName;"
            if (line.startsWith("private ") && line.endsWith(";")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    String fieldName = parts[2].replace(";", "");
                    fieldOrder.add(fieldName);
                }
            }
        }
        
        return fieldOrder;
    }
    
    public String objectsToJson(List<Object> objects) {
        try {
            return objectMapper.writeValueAsString(objects);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert objects to JSON: " + e.getMessage(), e);
        }
    }
    
    private static class InMemoryJavaFileObject extends javax.tools.SimpleJavaFileObject {
        private final String code;
        
        protected InMemoryJavaFileObject(String className, String code) {
            super(java.net.URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }
        
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
    
    private static class InMemoryFileManager extends javax.tools.ForwardingJavaFileManager<javax.tools.StandardJavaFileManager> {
        private final Map<String, ByteArrayOutputStream> compiledClasses = new HashMap<>();
        
        protected InMemoryFileManager(javax.tools.StandardJavaFileManager fileManager) {
            super(fileManager);
        }
        
        @Override
        public javax.tools.JavaFileObject getJavaFileForOutput(Location location, String className, javax.tools.JavaFileObject.Kind kind, javax.tools.FileObject sibling) {
            return new InMemoryClassFileObject(className, compiledClasses);
        }
        
        public Map<String, byte[]> getCompiledClasses() {
            Map<String, byte[]> result = new HashMap<>();
            for (Map.Entry<String, ByteArrayOutputStream> entry : compiledClasses.entrySet()) {
                result.put(entry.getKey(), entry.getValue().toByteArray());
            }
            return result;
        }
    }
    
    private static class InMemoryClassFileObject extends javax.tools.SimpleJavaFileObject {
        private final String className;
        private final Map<String, ByteArrayOutputStream> compiledClasses;
        
        protected InMemoryClassFileObject(String className, Map<String, ByteArrayOutputStream> compiledClasses) {
            super(java.net.URI.create("string:///" + className.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
            this.className = className;
            this.compiledClasses = compiledClasses;
        }
        
        @Override
        public java.io.OutputStream openOutputStream() {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            compiledClasses.put(className, stream);
            return stream;
        }
    }
    
    private static class InMemoryClassLoader extends ClassLoader {
        private final Map<String, byte[]> compiledClasses;
        
        public InMemoryClassLoader(Map<String, byte[]> compiledClasses) {
            this.compiledClasses = compiledClasses;
        }
        
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] classBytes = compiledClasses.get(name);
            if (classBytes == null) {
                throw new ClassNotFoundException(name);
            }
            return defineClass(name, classBytes, 0, classBytes.length);
        }
    }
}