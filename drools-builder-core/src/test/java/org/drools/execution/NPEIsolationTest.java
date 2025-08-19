package org.drools.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.drools.storage.DefinitionStorage;
import java.util.List;

/**
 * Test to isolate the NPE issue by testing the exact scenario that causes it
 */
public class NPEIsolationTest {

    @Test
    @DisplayName("Isolate the NPE by testing step by step")
    public void isolateNPEStepByStep() {
        System.out.println("=== Isolating NPE Step by Step ===");
        
        try {
            // Step 1: Create storage and factory
            DefinitionStorage storage = new DefinitionStorage();
            DynamicJsonToJavaFactory factory = new DynamicJsonToJavaFactory(storage);
            
            // Step 2: Register a declared type exactly like DRLRunner does
            String drlDeclare = """
                declare User
                    age : Number
                end
                """;
            
            storage.addDefinition("User", "declare", drlDeclare);
            System.out.println("✅ Step 1: Registered type in storage");
            
            // Step 3: Create JSON objects like the workflow does
            String json = "{\"age\": 18}";
            System.out.println("✅ Step 2: Created JSON: " + json);
            
            // Step 4: Try to create objects - this should be where the NPE occurs
            System.out.println("✅ Step 3: Attempting object creation...");
            List<Object> objects = factory.createObjectsFromJson(json, "User");
            
            System.out.println("✅ Step 4: Objects created: " + objects);
            
            // Step 5: Examine what was actually created
            if (!objects.isEmpty()) {
                Object obj = objects.get(0);
                System.out.println("Object type: " + obj.getClass().getName());
                System.out.println("Object value: " + obj);
                System.out.println("Is String? " + (obj instanceof String));
                
                // If it's a String, that's the problem!
                if (obj instanceof String) {
                    System.out.println("❌ FOUND THE ISSUE: Object is a String, not an actual User object!");
                    System.out.println("String content: '" + obj + "'");
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Exception caught: " + e.getMessage());
            System.out.println("Exception type: " + e.getClass().getName());
            
            // Check if this is the NPE we're looking for
            if (e.getMessage().contains("intValue") || e.getMessage().contains("Number")) {
                System.out.println("🎯 This looks like the NPE we're trying to fix!");
            }
            
            e.printStackTrace();
        }
    }

    @Test  
    @DisplayName("Test DRL execution without JSON - use pure DRL object creation")
    public void testPureDRLExecution() {
        System.out.println("=== Testing Pure DRL Execution ===");
        
        try {
            // Test DRL that creates its own objects without JSON
            String drlContent = """
                declare User
                    age : int
                    adult : boolean
                end
                
                rule "Create Test User"
                when
                then
                    User user = new User();
                    user.setAge(25);
                    user.setAdult(false);
                    insert(user);
                    System.out.println("Created user: " + user);
                end
                
                rule "Check Adult"
                when
                    $user : User(age >= 18)
                then
                    modify($user) { setAdult(true) }
                    System.out.println("User is now adult: " + $user);
                end
                """;
            
            DRLRunnerResult result = DRLRunner.runDRL(drlContent);
            System.out.println("✅ Pure DRL execution succeeded: " + result.objects().size() + " facts");
            result.objects().forEach(fact -> {
                System.out.println("  Fact: " + fact + " (type: " + fact.getClass().getName() + ")");
            });
            
        } catch (Exception e) {
            System.out.println("❌ Pure DRL execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}