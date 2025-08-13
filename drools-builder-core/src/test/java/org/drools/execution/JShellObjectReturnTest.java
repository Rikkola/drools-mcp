package org.drools.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Test to isolate the issue where JShell returns String instead of actual objects
 */
public class JShellObjectReturnTest {

    @Test
    @DisplayName("Debug JShell object return behavior")
    public void debugJShellObjectReturn() {
        System.out.println("=== Testing JShell Object Return ===");
        
        JShell jshell = null;
        try {
            jshell = JShell.create();
            
            // First, define a simple class
            String classDefinition = """
                class User {
                    private int age;
                    public User(int age) { this.age = age; }
                    public int getAge() { return age; }
                    public String toString() { return "User{age=" + age + "}"; }
                }
                """;
            
            System.out.println("1. Defining class:");
            System.out.println(classDefinition);
            
            List<SnippetEvent> classEvents = jshell.eval(classDefinition);
            for (SnippetEvent event : classEvents) {
                System.out.println("  Class definition event: " + event.status() + " -> " + event.value());
            }
            
            // Now create an object
            String objectCreation = "User obj = new User(25);";
            System.out.println("\n2. Creating object: " + objectCreation);
            
            List<SnippetEvent> objEvents = jshell.eval(objectCreation);
            for (SnippetEvent event : objEvents) {
                System.out.println("  Object creation event: " + event.status() + " -> " + event.value());
            }
            
            // Try to get the object
            System.out.println("\n3. Getting object by evaluating variable 'obj':");
            List<SnippetEvent> getEvents = jshell.eval("obj");
            for (SnippetEvent event : getEvents) {
                Object value = event.value();
                System.out.println("  Value: " + value);
                System.out.println("  Value type: " + (value != null ? value.getClass().getName() : "null"));
                System.out.println("  Event status: " + event.status());
                System.out.println("  Raw event: " + event);
            }
            
            // Try different approaches to get the actual object
            System.out.println("\n4. Trying different retrieval methods:");
            
            // Method 1: Try to access a field
            List<SnippetEvent> fieldEvents = jshell.eval("obj.getAge()");
            for (SnippetEvent event : fieldEvents) {
                System.out.println("  obj.getAge(): " + event.value() + " (type: " + (event.value() != null ? event.value().getClass().getName() : "null") + ")");
            }
            
            // Method 2: Try to get class info
            List<SnippetEvent> classEvents2 = jshell.eval("obj.getClass().getName()");
            for (SnippetEvent event : classEvents2) {
                System.out.println("  obj.getClass().getName(): " + event.value());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (jshell != null) {
                jshell.close();
            }
        }
    }
    
    @Test
    @DisplayName("Test DynamicObjectCreator directly")
    public void testDynamicObjectCreatorDirectly() {
        System.out.println("=== Testing DynamicObjectCreator Directly ===");
        
        try {
            DynamicObjectCreator creator = new DynamicObjectCreator();
            
            // Create class definition
            String classDefinition = """
                class TestUser {
                    private int age;
                    public TestUser(int age) { this.age = age; }
                    public int getAge() { return age; }
                    public String toString() { return "TestUser{age=" + age + "}"; }
                }
                """;
            
            Map<String, String> classDefinitions = new HashMap<>();
            classDefinitions.put("TestUser", classDefinition);
            
            // Try to create object
            String constructorCall = "TestUser obj = new TestUser(30);";
            System.out.println("Constructor call: " + constructorCall);
            System.out.println("Class definition: " + classDefinition);
            
            Object result = creator.createObjectFromString(constructorCall, classDefinitions);
            
            System.out.println("Result: " + result);
            System.out.println("Result type: " + (result != null ? result.getClass().getName() : "null"));
            
            if (result != null) {
                System.out.println("Is result a String? " + (result instanceof String));
                System.out.println("Is result a TestUser? " + result.getClass().getSimpleName().equals("TestUser"));
            }
            
        } catch (Exception e) {
            System.out.println("❌ Creator test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}