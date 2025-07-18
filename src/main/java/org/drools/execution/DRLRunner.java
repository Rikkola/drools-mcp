package org.drools.execution;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.drools.execution.DynamicJsonToJavaFactory;
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
    private static final DynamicJsonToJavaFactory dynamicJsonToJavaFactory = new DynamicJsonToJavaFactory();

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
     * @param factsJson JSON string containing array of facts with type fields
     * @return List of facts in working memory after rule execution
     */
    public static List<Object> runDRLWithJsonFacts(String drlContent, String factsJson) {
        return runDRLWithJsonFacts(drlContent, factsJson, 0);
    }

    /**
     * Executes a DRL file with external facts provided as JSON
     * @param drlContent The DRL content as a string
     * @param factsJson JSON string containing array of facts with type fields
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return List of facts in working memory after rule execution
     */
    public static List<Object> runDRLWithJsonFacts(String drlContent, String factsJson, int maxRuns) {
        try {
            // Parse JSON facts using DynamicJsonToJavaFactory
            List<Object> facts = parseJsonFacts(factsJson);
            
            // Execute DRL with the parsed facts
            return runDRLWithFacts(drlContent, facts, maxRuns);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute DRL with JSON facts: " + e.getMessage(), e);
        }
    }

    /**
     * Parse JSON facts and create dynamic objects using DynamicJsonToJavaFactory
     * @param factsJson JSON string containing array of facts with type fields
     * @return List of parsed facts as objects
     */
    private static List<Object> parseJsonFacts(String factsJson) throws Exception {
        List<Object> facts = new ArrayList<>();
        
        if (factsJson == null || factsJson.trim().isEmpty() || "[]".equals(factsJson.trim())) {
            return facts;
        }
        
        // Parse JSON as array of maps
        List<Map<String, Object>> jsonFacts = objectMapper.readValue(factsJson, new TypeReference<List<Map<String, Object>>>() {});
        
        // Convert each JSON fact to an object using DynamicJsonToJavaFactory
        for (Map<String, Object> jsonFact : jsonFacts) {
            Object fact = createObjectFromJson(jsonFact);
            facts.add(fact);
        }
        
        return facts;
    }

    /**
     * Create an object from JSON map using DynamicJsonToJavaFactory
     * @param jsonFact JSON map representing a fact
     * @return Created object
     */
    private static Object createObjectFromJson(Map<String, Object> jsonFact) throws Exception {
        // Check if JSON contains type information
        String typeName = (String) jsonFact.get("_type");
        
        if (typeName != null) {
            // Remove type information from data
            Map<String, Object> factData = new java.util.HashMap<>(jsonFact);
            factData.remove("_type");
            
            // Convert to JSON string for DynamicJsonToJavaFactory
            String jsonString = objectMapper.writeValueAsString(factData);
            
            // Create objects using DynamicJsonToJavaFactory
            java.util.List<Object> objects = dynamicJsonToJavaFactory.createObjectsFromJson(jsonString, typeName);
            
            return objects.isEmpty() ? jsonFact : objects.get(0);
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
            .filter(fact -> fact.getClass().getSimpleName().equals(typeName))
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get the dynamic JSON to Java factory instance
     * @return DynamicJsonToJavaFactory instance
     */
    public static DynamicJsonToJavaFactory getDynamicJsonToJavaFactory() {
        return dynamicJsonToJavaFactory;
    }

}