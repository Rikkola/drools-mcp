package org.drools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kie.api.builder.Message;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DRLRunner {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DynamicObjectFactory dynamicObjectFactory = new DynamicObjectFactory();

    /**
     * Executes a DRL file that may contain declared types and data creation rules
     * @param drlContent The DRL content as a string
     * @return List of facts in working memory after rule execution
     */
    public static List<Object> runDRL(String drlContent) {
        return runDRL(drlContent, 0); // 0 means unlimited rules
    }

    /**
     * Executes a DRL file that may contain declared types and data creation rules
     * @param drlContent The DRL content as a string
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return List of facts in working memory after rule execution
     */
    public static List<Object> runDRL(String drlContent, int maxRuns) {
        try {
            // Create KieSession from DRL string
            KieHelper kieHelper = new KieHelper();
            kieHelper.addContent(drlContent, ResourceType.DRL);
            
            // Build and check for errors
            KieContainer kieContainer = kieHelper.getKieContainer();
            if (kieHelper.verify().hasMessages(Message.Level.ERROR)) {
                throw new RuntimeException("DRL compilation errors: " + 
                    kieHelper.verify().getMessages(Message.Level.ERROR));
            }
            
            KieSession kieSession = kieContainer.newKieSession();

            // Fire all rules (including data creation rules)
            int firedRules;
            if (maxRuns > 0) {
                firedRules = kieSession.fireAllRules(maxRuns);
            } else {
                firedRules = kieSession.fireAllRules();
            }
            System.out.println("Fired " + firedRules + " rules");

            // Collect all facts from working memory
            Collection<?> facts = kieSession.getObjects();
            List<Object> factList = new ArrayList<>(facts);
            
            // Print facts for debugging
            System.out.println("Facts in working memory: " + factList.size());
            factList.forEach(fact -> System.out.println("  " + fact));
            
            // Dispose session
            kieSession.dispose();
            
            return factList;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute DRL: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a DRL file with external facts provided as JSON
     * @param drlContent The DRL content as a string
     * @param factsJson JSON string containing array of facts
     * @param objectDefinitions Map of object definitions for creating dynamic objects
     * @return List of facts in working memory after rule execution
     */
    public static List<Object> runDRLWithJsonFacts(String drlContent, String factsJson, Map<String, ObjectDefinition> objectDefinitions) {
        return runDRLWithJsonFacts(drlContent, factsJson, objectDefinitions, 0);
    }

    /**
     * Executes a DRL file with external facts provided as JSON
     * @param drlContent The DRL content as a string
     * @param factsJson JSON string containing array of facts
     * @param objectDefinitions Map of object definitions for creating dynamic objects
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return List of facts in working memory after rule execution
     */
    public static List<Object> runDRLWithJsonFacts(String drlContent, String factsJson, Map<String, ObjectDefinition> objectDefinitions, int maxRuns) {
        try {
            // Parse JSON facts
            List<Object> facts = parseJsonFacts(factsJson, objectDefinitions);
            
            // Execute DRL with the parsed facts
            return runDRLWithFacts(drlContent, facts, maxRuns);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute DRL with JSON facts: " + e.getMessage(), e);
        }
    }

    /**
     * Parse JSON facts and create dynamic objects based on provided object definitions
     * @param factsJson JSON string containing array of facts
     * @param objectDefinitions Map of object definitions for creating dynamic objects
     * @return List of parsed facts as objects
     */
    private static List<Object> parseJsonFacts(String factsJson, Map<String, ObjectDefinition> objectDefinitions) throws Exception {
        List<Object> facts = new ArrayList<>();
        
        if (factsJson == null || factsJson.trim().isEmpty() || "[]".equals(factsJson.trim())) {
            return facts;
        }
        
        // Parse JSON as array of maps
        List<Map<String, Object>> jsonFacts = objectMapper.readValue(factsJson, new TypeReference<List<Map<String, Object>>>() {});
        
        // Register object definitions with factory
        if (objectDefinitions != null) {
            for (ObjectDefinition definition : objectDefinitions.values()) {
                dynamicObjectFactory.registerObjectDefinition(definition);
            }
        }
        
        // Convert each JSON fact to an object
        for (Map<String, Object> jsonFact : jsonFacts) {
            Object fact = createObjectFromJson(jsonFact, objectDefinitions);
            facts.add(fact);
        }
        
        return facts;
    }

    /**
     * Create an object from JSON map using object definitions
     * @param jsonFact JSON map representing a fact
     * @param objectDefinitions Map of object definitions
     * @return Created object
     */
    private static Object createObjectFromJson(Map<String, Object> jsonFact, Map<String, ObjectDefinition> objectDefinitions) throws Exception {
        // Check if JSON contains type information
        String typeName = (String) jsonFact.get("_type");
        
        if (typeName != null && objectDefinitions != null && objectDefinitions.containsKey(typeName)) {
            // Create dynamic object using object definition
            ObjectDefinition definition = objectDefinitions.get(typeName);
            
            // Remove type information from data
            Map<String, Object> factData = new java.util.HashMap<>(jsonFact);
            factData.remove("_type");
            
            return dynamicObjectFactory.createFromMap(definition, factData);
        } else {
            // Return as a simple Map if no type definition is available
            return jsonFact;
        }
    }

    /**
     * Executes a DRL file with external facts
     * @param drlContent The DRL content as a string
     * @param facts External facts to insert into working memory
     * @return List of facts in working memory after rule execution
     */
    public static List<Object> runDRLWithFacts(String drlContent, List<Object> facts) {
        return runDRLWithFacts(drlContent, facts, 0); // 0 means unlimited rules
    }

    /**
     * Executes a DRL file with external facts
     * @param drlContent The DRL content as a string
     * @param facts External facts to insert into working memory
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return List of facts in working memory after rule execution
     */
    public static List<Object> runDRLWithFacts(String drlContent, List<Object> facts, int maxRuns) {
        try {
            // Create KieSession from DRL string
            KieHelper kieHelper = new KieHelper();
            kieHelper.addContent(drlContent, ResourceType.DRL);
            
            // Build and check for errors
            KieContainer kieContainer = kieHelper.getKieContainer();
            if (kieHelper.verify().hasMessages(Message.Level.ERROR)) {
                throw new RuntimeException("DRL compilation errors: " + 
                    kieHelper.verify().getMessages(Message.Level.ERROR));
            }
            
            KieSession kieSession = kieContainer.newKieSession();

            // Insert external facts
            for (Object fact : facts) {
                kieSession.insert(fact);
                System.out.println("Inserted external fact: " + fact);
            }

            // Fire all rules
            int firedRules;
            if (maxRuns > 0) {
                firedRules = kieSession.fireAllRules(maxRuns);
            } else {
                firedRules = kieSession.fireAllRules();
            }
            System.out.println("Fired " + firedRules + " rules");

            // Collect all facts from working memory
            Collection<?> workingMemoryFacts = kieSession.getObjects();
            List<Object> factList = new ArrayList<>(workingMemoryFacts);
            
            // Print facts for debugging
            System.out.println("Facts in working memory: " + factList.size());
            factList.forEach(fact -> System.out.println("  " + fact));
            
            // Dispose session
            kieSession.dispose();
            
            return factList;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute DRL with facts: " + e.getMessage(), e);
        }
    }

    /**
     * Filter facts by type name (useful for declared types)
     * @param facts List of facts
     * @param typeName Name of the type to filter by
     * @return List of facts matching the type name
     */
    public static List<Object> filterFactsByType(List<Object> facts, String typeName) {
        return facts.stream()
            .filter(fact -> {
                if (fact instanceof DynamicObjectFactory.DynamicObject) {
                    return typeName.equals(((DynamicObjectFactory.DynamicObject) fact).getObjectTypeName());
                }
                return fact.getClass().getSimpleName().equals(typeName);
            })
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get the dynamic object factory instance
     * @return DynamicObjectFactory instance
     */
    public static DynamicObjectFactory getDynamicObjectFactory() {
        return dynamicObjectFactory;
    }

    /**
     * Create object definitions from JSON schema
     * @param schemaJson JSON schema defining object structures
     * @return Map of object definitions
     */
    public static Map<String, ObjectDefinition> createObjectDefinitionsFromSchema(String schemaJson) throws Exception {
        Map<String, ObjectDefinition> definitions = new java.util.HashMap<>();
        
        if (schemaJson == null || schemaJson.trim().isEmpty()) {
            return definitions;
        }
        
        // Parse schema JSON - expecting array of object definitions
        List<Map<String, Object>> schemas = objectMapper.readValue(schemaJson, new TypeReference<List<Map<String, Object>>>() {});
        
        for (Map<String, Object> schema : schemas) {
            ObjectDefinition definition = parseObjectDefinitionFromSchema(schema);
            if (definition != null) {
                definitions.put(definition.getName(), definition);
            }
        }
        
        return definitions;
    }

    /**
     * Parse a single object definition from schema
     * @param schema Schema map
     * @return ObjectDefinition or null if parsing fails
     */
    private static ObjectDefinition parseObjectDefinitionFromSchema(Map<String, Object> schema) {
        try {
            String name = (String) schema.get("name");
            if (name == null || name.trim().isEmpty()) {
                return null;
            }
            
            ObjectDefinition definition = new ObjectDefinition(name);
            
            // Set optional properties
            if (schema.containsKey("packageName")) {
                definition.setPackageName((String) schema.get("packageName"));
            }
            if (schema.containsKey("description")) {
                definition.setDescription((String) schema.get("description"));
            }
            
            // Parse fields
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fieldsData = (List<Map<String, Object>>) schema.get("fields");
            
            if (fieldsData != null) {
                for (Map<String, Object> fieldData : fieldsData) {
                    FieldDefinition field = parseFieldDefinition(fieldData);
                    if (field != null) {
                        definition.addField(field);
                    }
                }
            }
            
            return definition;
            
        } catch (Exception e) {
            System.err.println("Failed to parse object definition from schema: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse a field definition from schema data
     * @param fieldData Field data map
     * @return FieldDefinition or null if parsing fails
     */
    private static FieldDefinition parseFieldDefinition(Map<String, Object> fieldData) {
        try {
            String name = (String) fieldData.get("name");
            String typeStr = (String) fieldData.get("type");
            
            if (name == null || typeStr == null) {
                return null;
            }
            
            FieldDefinition.FieldType type = FieldDefinition.FieldType.valueOf(typeStr.toUpperCase());
            boolean required = Boolean.TRUE.equals(fieldData.get("required"));
            
            FieldDefinition field = new FieldDefinition(name, type, required);
            
            // Set optional properties
            if (fieldData.containsKey("description")) {
                field.setDescription((String) fieldData.get("description"));
            }
            if (fieldData.containsKey("defaultValue")) {
                field.setDefaultValue(fieldData.get("defaultValue"));
            }
            if (fieldData.containsKey("objectTypeName")) {
                field.setObjectTypeName((String) fieldData.get("objectTypeName"));
            }
            
            return field;
            
        } catch (Exception e) {
            System.err.println("Failed to parse field definition: " + e.getMessage());
            return null;
        }
    }
}