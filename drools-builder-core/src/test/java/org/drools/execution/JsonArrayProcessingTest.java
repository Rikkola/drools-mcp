package org.drools.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.drools.storage.DefinitionStorage;

/**
 * Test to isolate the JSON array processing issue
 */
public class JsonArrayProcessingTest {

    @Test
    @DisplayName("Test JSON array processing that's failing")
    public void testJsonArrayProcessing() {
        System.out.println("=== Testing JSON Array Processing ===");
        
        try {
            // 1. Set up DefinitionStorage like DRLRunner does
            DefinitionStorage storage = new DefinitionStorage();
            
            String drlContent = """
                declare User
                    age : int
                    adult : boolean
                end
                """;
            
            storage.addDefinition("User", "declare", drlContent);
            System.out.println("✅ Registered User type in DefinitionStorage");
            
            // 2. Create factory exactly like DRLRunner does
            DynamicJsonToJavaFactory factory = new DynamicJsonToJavaFactory(storage);
            
            // 3. Test the exact JSON that fails in DeclaredTypeTest
            String jsonFacts = """
                [
                    {"_type": "User", "age": 25, "adult": false},
                    {"_type": "User", "age": 16, "adult": false}
                ]
                """;
            
            System.out.println("JSON Facts: " + jsonFacts);
            
            // 4. Parse exactly like DRLRunner.parseJsonFacts does
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> jsonFactsList = 
                objectMapper.readValue(jsonFacts, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {});
            
            System.out.println("Parsed " + jsonFactsList.size() + " JSON facts");
            
            // 5. Process each fact exactly like DRLRunner.createObjectFromJson does
            for (java.util.Map<String, Object> jsonFact : jsonFactsList) {
                System.out.println("\n=== Processing fact: " + jsonFact + " ===");
                
                String typeName = (String) jsonFact.get("_type");
                System.out.println("Type name: " + typeName);
                
                if (typeName != null) {
                    // Remove type information from data
                    java.util.Map<String, Object> factData = new java.util.HashMap<>(jsonFact);
                    factData.remove("_type");
                    System.out.println("Fact data (without _type): " + factData);
                    
                    // Convert to JSON string for DynamicJsonToJavaFactory
                    String jsonString = objectMapper.writeValueAsString(factData);
                    System.out.println("JSON string for factory: " + jsonString);
                    
                    // This is where it fails - call createObjectsFromJson
                    java.util.List<Object> objects = factory.createObjectsFromJson(jsonString, typeName);
                    System.out.println("✅ Created object: " + objects);
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test single JSON object processing that works")
    public void testSingleJsonProcessing() {
        System.out.println("=== Testing Single JSON Processing (Working) ===");
        
        try {
            // Same setup but with single object
            DefinitionStorage storage = new DefinitionStorage();
            
            String drlContent = """
                declare User
                    age : int
                    adult : boolean
                end
                """;
            
            storage.addDefinition("User", "declare", drlContent);
            DynamicJsonToJavaFactory factory = new DynamicJsonToJavaFactory(storage);
            
            // Single object (this works)
            String jsonString = "{\"age\": 25, \"adult\": false}";
            System.out.println("JSON: " + jsonString);
            
            java.util.List<Object> objects = factory.createObjectsFromJson(jsonString, "User");
            System.out.println("✅ Created object: " + objects);
            
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}