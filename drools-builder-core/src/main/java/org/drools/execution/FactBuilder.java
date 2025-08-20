package org.drools.execution;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles conversion of JSON data to Drools fact objects
 */
public class FactBuilder {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Builds facts from a JSON array containing multiple facts with _type fields
     * @param factsJson JSON array string containing facts with _type fields
     * @param kieContainer KieContainer to get FactTypes from
     * @param packageName Package name for declared types
     * @return List of created fact objects
     * @throws RuntimeException if JSON parsing or fact creation fails
     */
    public List<Object> buildFromJsonArray(String factsJson, KieContainer kieContainer, String packageName) {
        try {
            List<Map<String, Object>> jsonFacts = objectMapper.readValue(factsJson, 
                new TypeReference<List<Map<String, Object>>>() {});
            
            List<Object> facts = new ArrayList<>();
            for (Map<String, Object> jsonFact : jsonFacts) {
                Object fact = buildFromJsonMap(jsonFact, kieContainer, packageName);
                if (fact != null) {
                    facts.add(fact);
                }
            }
            
            return facts;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON facts array: " + e.getMessage(), e);
        }
    }

    /**
     * Builds a single fact from JSON string with _type field
     * @param jsonString JSON string containing a single fact with _type field
     * @param kieContainer KieContainer to get FactTypes from
     * @param packageName Package name for declared types
     * @return Created fact object
     * @throws RuntimeException if JSON parsing or fact creation fails
     */
    public Object buildFromJsonSingle(String jsonString, KieContainer kieContainer, String packageName) {
        try {
            Map<String, Object> jsonData = objectMapper.readValue(jsonString, Map.class);
            
            String typeName = (String) jsonData.get("_type");
            if (typeName == null) {
                throw new RuntimeException("JSON data must contain '_type' field to specify the declared type");
            }
            
            return buildFromJsonMap(jsonData, kieContainer, packageName);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON fact: " + e.getMessage(), e);
        }
    }

    /**
     * Builds a fact from JSON data using explicit type information
     * @param jsonString JSON string containing fact data (without _type field)
     * @param kieContainer KieContainer to get FactTypes from
     * @param packageName Package name for the declared type
     * @param typeName Type name for the declared type
     * @return Created fact object
     * @throws RuntimeException if JSON parsing or fact creation fails
     */
    public Object buildFromJsonWithExplicitType(String jsonString, KieContainer kieContainer, 
                                               String packageName, String typeName) {
        try {
            Map<String, Object> jsonData = objectMapper.readValue(jsonString, Map.class);
            
            FactType factType = kieContainer.getKieBase().getFactType(packageName, typeName);
            if (factType == null) {
                throw new RuntimeException("Could not find declared type: " + typeName + " in package: " + packageName);
            }
            
            return createFactFromJson(jsonData, factType);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create fact with explicit type: " + e.getMessage(), e);
        }
    }

    /**
     * Builds a fact from a JSON map that may contain _type field
     * @param jsonData JSON data as a map
     * @param kieContainer KieContainer to get FactTypes from
     * @param packageName Package name for declared types
     * @return Created fact object or null if _type field is missing
     */
    private Object buildFromJsonMap(Map<String, Object> jsonData, KieContainer kieContainer, String packageName) {
        String typeName = (String) jsonData.get("_type");
        
        if (typeName == null) {
            System.out.println("Warning: JSON fact missing '_type' field: " + jsonData);
            return null;
        }

        // Remove type information from data
        Map<String, Object> factData = new HashMap<>(jsonData);
        factData.remove("_type");

        // Get the FactType
        FactType factType = kieContainer.getKieBase().getFactType(packageName, typeName);
        if (factType == null) {
            System.out.println("Warning: Could not find declared type: " + typeName + " in package: " + packageName);
            return null;
        }

        return createFactFromJson(factData, factType);
    }

    /**
     * Creates a fact instance from JSON data using a FactType
     * @param factData JSON data as a map (without _type field)
     * @param factType FactType to create instance of
     * @return Created fact object
     * @throws RuntimeException if fact creation fails
     */
    private Object createFactFromJson(Map<String, Object> factData, FactType factType) {
        try {
            Object fact = factType.newInstance();

            // Set fields from JSON
            for (Map.Entry<String, Object> entry : factData.entrySet()) {
                try {
                    factType.set(fact, entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    System.out.println("Warning: Could not set field " + entry.getKey() + ": " + e.getMessage());
                }
            }

            return fact;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create fact instance: " + e.getMessage(), e);
        }
    }
}