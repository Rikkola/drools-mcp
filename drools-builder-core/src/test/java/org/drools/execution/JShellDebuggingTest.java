package org.drools.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Test to debug JShell behavior and understand why class definitions are being rejected
 */
public class JShellDebuggingTest {

    @Test
    @DisplayName("Debug JShell class definition rejection")
    public void debugJShellClassRejection() {
        // Generate the exact same Java class that DynamicJsonToJavaFactory creates
        String javaClass = """
            class User {
                private int age;
                private boolean adult;

                public User(int age, boolean adult) {
                    this.age = age;
                    this.adult = adult;
                }

                public int getAge() { return age; }
                public void setAge(int age) { this.age = age; }

                public boolean getAdult() { return adult; }
                public void setAdult(boolean adult) { this.adult = adult; }

                @Override
                public String toString() {
                    return "User{" + "age=" + age + ", adult=" + adult + "}";
                }
            }
            """;

        System.out.println("=== Testing JShell Class Definition ===");
        System.out.println("Java Class:");
        System.out.println(javaClass);

        JShell jshell = null;
        try {
            jshell = JShell.create();
            
            System.out.println("\n=== JShell Evaluation ===");
            List<SnippetEvent> events = jshell.eval(javaClass);
            
            System.out.println("JShell Events:");
            for (SnippetEvent event : events) {
                System.out.println("  Event: " + event);
                System.out.println("  Status: " + event.status());
                System.out.println("  Value: " + event.value());
                if (event.exception() != null) {
                    System.out.println("  Exception: " + event.exception().getMessage());
                    event.exception().printStackTrace();
                }
            }
            
            // Check for errors using the same method as DynamicObjectCreator
            try {
                checkForErrors(events, "Class definition for User");
                System.out.println("✅ Class definition accepted by JShell");
                
                // Try to create an instance
                List<SnippetEvent> constructorEvents = jshell.eval("User user = new User(25, false);");
                checkForErrors(constructorEvents, "Object creation");
                System.out.println("✅ Object creation succeeded");
                
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
            }
            
        } finally {
            if (jshell != null) {
                jshell.close();
            }
        }
    }

    @Test
    @DisplayName("Test JShell in DRLRunner context")
    public void testJShellInDRLRunnerContext() {
        System.out.println("=== Testing JShell in DRLRunner Context ===");
        
        // Simulate the exact flow that DRLRunner uses
        try {
            // 1. Extract and register declared types (like DRLRunner does)
            String drlContent = """
                declare User
                    age : int
                    adult : boolean
                end
                """;
            
            // 2. Create the DynamicJsonToJavaFactory with DefinitionStorage
            org.drools.storage.DefinitionStorage storage = new org.drools.storage.DefinitionStorage();
            storage.addDefinition("User", "declare", drlContent);
            
            DynamicJsonToJavaFactory factory = new DynamicJsonToJavaFactory(storage);
            
            // 3. Try to create an object exactly like DRLRunner does
            String jsonString = "{\"age\": 25, \"adult\": false}";
            
            System.out.println("JSON: " + jsonString);
            System.out.println("Type: User");
            
            List<Object> objects = factory.createObjectsFromJson(jsonString, "User");
            System.out.println("✅ Success! Created objects: " + objects);
            
        } catch (Exception e) {
            System.out.println("❌ Failed in DRLRunner context: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Copy of DynamicObjectCreator.checkForErrors method
     */
    private void checkForErrors(List<SnippetEvent> events, String context) {
        for (SnippetEvent event : events) {
            if (event.exception() != null) {
                throw new RuntimeException(context + " failed: " + event.exception().getMessage(), 
                                         event.exception());
            }
            if (event.status() == jdk.jshell.Snippet.Status.REJECTED) {
                throw new RuntimeException(context + " was rejected by JShell");
            }
        }
    }
}