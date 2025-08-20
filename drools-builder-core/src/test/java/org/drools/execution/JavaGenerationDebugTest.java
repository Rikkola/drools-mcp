package org.drools.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.drools.storage.DefinitionStorage;

/**
 * Test to debug the Java code generation from DRL declare statements
 */
public class JavaGenerationDebugTest {

    @Test
    @DisplayName("Debug Java class generation from DRL declare statements")
    public void debugJavaGeneration() {
        // Create DynamicJsonToJavaFactory with storage
        DefinitionStorage storage = new DefinitionStorage();
        DynamicJsonToJavaFactory factory = new DynamicJsonToJavaFactory(storage);

        // Test DRL declare statement
        String drlDeclare = """
            declare User
                age : int
                adult : boolean
            end
            """;

        // Register it in storage like DRLPopulatorRunner does
        storage.addDefinition("User", "declare", drlDeclare);

        System.out.println("=== ORIGINAL DRL DECLARE ===");
        System.out.println(drlDeclare);

        // Now let's see what Java class gets generated
        try {
            // Use reflection to access the private method
            java.lang.reflect.Method method = DynamicJsonToJavaFactory.class
                .getDeclaredMethod("convertDRLDeclareToJavaClass", String.class);
            method.setAccessible(true);
            
            String generatedJava = (String) method.invoke(factory, drlDeclare);
            
            System.out.println("\n=== GENERATED JAVA CLASS ===");
            System.out.println(generatedJava);

            // Now let's see what happens when we try to create an object
            System.out.println("\n=== TESTING OBJECT CREATION ===");
            
            String jsonFacts = """
                [{"_type": "User", "age": 25, "adult": false}]
                """;
            
            try {
                factory.createObjectsFromJson("{\"age\": 25, \"adult\": false}", "User");
                System.out.println("✅ Object creation succeeded!");
                
            } catch (Exception e) {
                System.out.println("❌ Object creation failed: " + e.getMessage());
                System.out.println("Full exception:");
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.out.println("Failed to debug: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test  
    @DisplayName("Test simple declare statement parsing")
    public void testSimpleDeclareStatement() {
        String simpleDRL = "declare TestUser name : String age : int end";
        
        System.out.println("=== SIMPLE DRL (one line) ===");
        System.out.println(simpleDRL);

        DefinitionStorage storage = new DefinitionStorage();
        DynamicJsonToJavaFactory factory = new DynamicJsonToJavaFactory(storage);
        
        try {
            java.lang.reflect.Method method = DynamicJsonToJavaFactory.class
                .getDeclaredMethod("convertDRLDeclareToJavaClass", String.class);
            method.setAccessible(true);
            
            String generatedJava = (String) method.invoke(factory, simpleDRL);
            
            System.out.println("\n=== GENERATED JAVA FROM SIMPLE DRL ===");
            System.out.println(generatedJava);
            
        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test multiline declare statement parsing")  
    public void testMultilineDeclareStatement() {
        String multilineDRL = """
            declare User
                age : int
                adult : boolean
            end
            """;
            
        System.out.println("=== MULTILINE DRL ===");
        System.out.println(multilineDRL);

        DefinitionStorage storage = new DefinitionStorage();
        DynamicJsonToJavaFactory factory = new DynamicJsonToJavaFactory(storage);
        
        try {
            java.lang.reflect.Method method = DynamicJsonToJavaFactory.class
                .getDeclaredMethod("convertDRLDeclareToJavaClass", String.class);
            method.setAccessible(true);
            
            String generatedJava = (String) method.invoke(factory, multilineDRL);
            
            System.out.println("\n=== GENERATED JAVA FROM MULTILINE DRL ===");
            System.out.println(generatedJava);
            
        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}